package com.campusoverflow.qa.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.qa.api.event.ContentDeletedEvent;
import com.campusoverflow.qa.api.event.QuestionEditedEvent;
import com.campusoverflow.qa.api.event.QuestionPostedEvent;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryListCache;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryQuestions;
import com.campusoverflow.qa.application.fake.QaTestFixtures.RecordingAudit;
import com.campusoverflow.qa.application.fake.QaTestFixtures.RecordingEvents;
import com.campusoverflow.qa.application.fake.QaTestFixtures.RecordingTags;
import com.campusoverflow.qa.application.fake.QaTestFixtures.StubIdentity;
import com.campusoverflow.qa.application.fake.QaTestFixtures.StubRenderer;
import com.campusoverflow.qa.domain.MarkdownBody;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.Tags;
import com.campusoverflow.qa.domain.Title;
import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * T1-07 验收：post 后标签用量 +1、集成事件恰好一次、列表缓存失效一次；load 找不到统一 404。
 * 只用端口替身，不启 Spring、不连数据库。
 */
class QuestionCommandServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-08T06:00:00Z");
    private static final Long COURSE_ID = 100L;

    private final InMemoryQuestions questions = new InMemoryQuestions();
    private final RecordingTags tags = new RecordingTags();
    private final RecordingEvents events = new RecordingEvents();
    private final InMemoryListCache cache = new InMemoryListCache();
    private final RecordingAudit audit = new RecordingAudit();
    private final StubIdentity identity = new StubIdentity()
            .withUser(1, "小明", Role.STUDENT)
            .withUser(2, "小红", Role.STUDENT)
            .withUser(9, "王老师", Role.TEACHER)
            .withCourseManager(9);

    private final Actor ming = new Actor(1, "小明", Role.STUDENT);
    private final Actor hong = new Actor(2, "小红", Role.STUDENT);
    private final Actor teacher = new Actor(9, "王老师", Role.TEACHER);

    private QuestionCommandService service;

    @BeforeEach
    void setUp() {
        service = serviceAt(NOW);
    }

    /** 用固定时钟建服务，便于让“编辑”晚于“发布”，从而断言 updatedAt 真的被推进。 */
    private QuestionCommandService serviceAt(Instant instant) {
        return new QuestionCommandService(questions, tags, identity, events, new StubRenderer(), cache, audit,
                Clock.fixed(instant, ZoneOffset.UTC));
    }

    private PostQuestionCommand command(String title, String body, List<String> tagNames) {
        return new PostQuestionCommand(COURSE_ID, title, body, tagNames);
    }

    @Test
    @DisplayName("发布：落库拿 id、标签用量 +1、事件一次、缓存失效一次、审计留痕")
    void postCreatesQuestionAndPublishesSideEffects() {
        long id = service.post(ming,
                command("红黑树插入后为什么必须旋转？", "变色为什么不够？希望有直观解释，最好能给出一个反例。",
                        List.of("#Java", "java", " Spring-Boot ")));

        Question saved = questions.findById(id).orElseThrow();
        assertThat(saved.authorId()).isEqualTo(ming.userId());
        assertThat(saved.courseId()).isEqualTo(COURSE_ID);
        assertThat(saved.answerCount()).isZero();
        assertThat(saved.acceptedAnswerId()).isNull();
        assertThat(saved.isDeleted()).isFalse();
        assertThat(saved.createdAt()).isEqualTo(NOW);

        // 标签去重后是两个，且各只 +1 一次
        assertThat(tags.usage).containsEntry("java", 1).containsEntry("spring-boot", 1).hasSize(2);

        List<QuestionPostedEvent> published = events.ofType(QuestionPostedEvent.class);
        assertThat(published).hasSize(1);
        QuestionPostedEvent event = published.get(0);
        assertThat(event.questionId()).isEqualTo(id);
        assertThat(event.authorId()).isEqualTo(ming.userId());
        assertThat(event.courseId()).isEqualTo(COURSE_ID);
        assertThat(event.tags()).containsExactly("java", "spring-boot");
        assertThat(event.plainText()).startsWith("变色为什么不够").doesNotContain("#").doesNotContain("*");
        assertThat(event.eventId()).isNotBlank();
        assertThat(event.occurredAt()).isEqualTo(NOW);

        assertThat(cache.invalidations).isEqualTo(1);
        assertThat(audit.actions).containsExactly("QUESTION_POSTED");
    }

    @Test
    @DisplayName("发布：关联课程不存在时直接 404，不落库、不发事件")
    void postRejectsUnknownCourse() {
        identity.withoutCourse(404L);

        assertThatThrownBy(() -> service.post(ming,
                new PostQuestionCommand(404L, "一门并不存在的课程下的提问", "这里的问题正文长度肯定超过二十个字符了。",
                        List.of("java"))))
                .isInstanceOf(NotFoundException.class);

        assertThat(questions.saves).isZero();
        assertThat(events.published).isEmpty();
        assertThat(cache.invalidations).isZero();
    }

    @Test
    @DisplayName("发布：标题过短由值对象拦下，返回 422 而不是 500")
    void postRejectsTooShortTitle() {
        assertThatThrownBy(() -> service.post(ming,
                command("太短了", "这里的问题正文长度肯定超过二十个字符，问题出在标题上。", List.of("java"))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("标题");

        assertThat(questions.saves).isZero();
        assertThat(events.published).isEmpty();
    }

    @Test
    @DisplayName("发布：不挂课程（courseId 为 null）的公开提问是允许的")
    void postWithoutCourseIsAllowed() {
        long id = service.post(ming, new PostQuestionCommand(null, "没有关联课程的公开提问标题",
                "这类问题不在任何课程下，所以不需要校验课程是否存在。", List.of("java")));

        QuestionPostedEvent event = events.ofType(QuestionPostedEvent.class).get(0);
        assertThat(event.courseId()).isNull();
        assertThat(questions.findById(id)).isPresent();
    }

    @Test
    @DisplayName("编辑：标签按差集调整用量，只对新增 +1、被删 -1，没变的标签不动")
    void editAdjustsTagUsageByDifference() {
        long id = service.post(ming, command("红黑树插入后为什么必须旋转？",
                "变色为什么不够？希望有直观解释，最好能给出一个反例。", List.of("java", "红黑树")));
        tags.usage.clear();
        tags.adjustments.clear();

        service.edit(ming, id, new EditQuestionCommand("红黑树插入后如何判断旋转方向？",
                "补充说明：希望在回答里给出一个必须双旋的反例，谢谢。", List.of("java", "数据结构")));
        assertThat(tags.adjustments).containsExactly("+1:数据结构", "-1:红黑树");
        assertThat(tags.usage).containsEntry("数据结构", 1).containsEntry("红黑树", -1).doesNotContainKey("java");

        Question edited = questions.findById(id).orElseThrow();
        assertThat(edited.title().value()).contains("旋转方向");
        // 固定时钟下用“晚 10 分钟”的服务再改一次，验证 updatedAt 确实被推进
        serviceAt(NOW.plusSeconds(600)).edit(ming, id, new EditQuestionCommand("红黑树插入后如何判断旋转方向？",
                "第二次编辑：再补充一句，答案里请说明左旋与右旋的判定条件。", List.of("java", "数据结构")));
        assertThat(questions.findById(id).orElseThrow().updatedAt()).isEqualTo(NOW.plusSeconds(600));
        assertThat(events.ofType(QuestionEditedEvent.class)).hasSize(2);
        assertThat(cache.invalidations).isEqualTo(3);

        QuestionEditedEvent event = events.ofType(QuestionEditedEvent.class).get(0);
        assertThat(event.questionId()).isEqualTo(id);
        assertThat(event.tags()).containsExactly("java", "数据结构");
        assertThat(audit.actions).containsExactly("QUESTION_POSTED", "QUESTION_EDITED", "QUESTION_EDITED");
    }

    @Test
    @DisplayName("编辑：非作者且非课程管理者不能改别人的问题")
    void editRejectedForStranger() {
        long id = service.post(ming, command("红黑树插入后为什么必须旋转？",
                "变色为什么不够？希望有直观解释，最好能给出一个反例。", List.of("java")));

        assertThatThrownBy(() -> service.edit(hong, id, new EditQuestionCommand("路人甲试图改别人的问题标题",
                "这段正文是路人甲提交的，长度同样超过二十个字符。", List.of("java"))))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("编辑 / 删除：已软删除的问题统一 404，而不是 403 或 409")
    void deletedQuestionIsNotFoundForFurtherCommands() {
        long id = service.post(ming, command("红黑树插入后为什么必须旋转？",
                "变色为什么不够？希望有直观解释，最好能给出一个反例。", List.of("java")));
        service.delete(ming, id);

        assertThatThrownBy(() -> service.edit(ming, id, new EditQuestionCommand("删除之后再改标题是什么结果",
                "这段正文用来验证已删除的问题继续编辑会返回什么。", List.of("java"))))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.delete(ming, id)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.edit(ming, 999L, new EditQuestionCommand("这个问题压根就不存在啊",
                "这段正文用来验证不存在的问题会返回什么异常。", List.of("java"))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("删除：作者可以删掉还没有回答的问题，并发 ContentDeleted 让读模型同步")
    void authorCanDeleteUnansweredQuestion() {
        long id = service.post(ming, command("红黑树插入后为什么必须旋转？",
                "变色为什么不够？希望有直观解释，最好能给出一个反例。", List.of("java")));

        service.delete(ming, id);

        assertThat(questions.findById(id).orElseThrow().isDeleted()).isTrue();
        ContentDeletedEvent event = events.ofType(ContentDeletedEvent.class).get(0);
        assertThat(event.targetType()).isEqualTo("QUESTION");
        assertThat(event.targetId()).isEqualTo(id);
        assertThat(event.questionId()).isEqualTo(id);
        assertThat(cache.invalidations).isEqualTo(2);
        assertThat(audit.actions).contains("QUESTION_DELETED");
    }

    @Test
    @DisplayName("删除：别人的问题删不掉；已有回答时作者也删不掉，但课程管理者可以")
    void deleteHonoursAuthorAndAnswerCountRules() {
        long id = service.post(ming, command("红黑树插入后为什么必须旋转？",
                "变色为什么不够？希望有直观解释，最好能给出一个反例。", List.of("java")));

        assertThatThrownBy(() -> service.delete(hong, id)).isInstanceOf(ForbiddenException.class);

        Question answered = Question.reconstitute(77L, ming.userId(), COURSE_ID,
                new Title("这个问题下面已经有人回答过了"),
                MarkdownBody.ofQuestion("这个问题的回答数不为零，所以作者不能直接删除它。"),
                Tags.of(List.of("java")), null, 0, 3, 0, NOW, NOW, null, 0L);
        questions.save(answered);

        assertThatThrownBy(() -> service.delete(ming, 77L)).isInstanceOf(BusinessRuleException.class);

        service.delete(teacher, 77L);
        assertThat(questions.findById(77L).orElseThrow().isDeleted()).isTrue();
    }
}
