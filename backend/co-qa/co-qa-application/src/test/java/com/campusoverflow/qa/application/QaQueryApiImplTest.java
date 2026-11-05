package com.campusoverflow.qa.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusoverflow.qa.api.QuestionRef;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryQuestions;
import com.campusoverflow.qa.domain.MarkdownBody;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.Tags;
import com.campusoverflow.qa.domain.Title;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * T1-11 验收：QaQueryApi.findQuestion 把聚合映射成发布语言 QuestionRef，
 * 供 B 的悬赏结算与 D 的通知投影使用；不存在返回 empty，已删除如实标记 deleted。
 */
class QaQueryApiImplTest {

    private static final Instant NOW = Instant.parse("2026-10-08T06:00:00Z");
    private static final long ASKER_ID = 1L;
    private static final Actor TEACHER = new Actor(9L, "王老师", Role.TEACHER);

    private final InMemoryQuestions questions = new InMemoryQuestions();

    private QaQueryApiImpl api;

    @BeforeEach
    void setUp() {
        api = new QaQueryApiImpl(questions);
    }

    private Question seed(long id, Long courseId, Long acceptedAnswerId) {
        Question question = Question.reconstitute(id, ASKER_ID, courseId,
                new Title("红黑树插入后如何判断旋转方向？"),
                MarkdownBody.ofQuestion("插入节点后出现连续红色节点，什么时候左旋、什么时候右旋？"),
                Tags.of(List.of("数据结构", "红黑树")), acceptedAnswerId, 0, acceptedAnswerId == null ? 0 : 1, 0,
                NOW, NOW, null, 0L);
        questions.save(question);
        return question;
    }

    @Test
    @DisplayName("映射：id / 作者 / 课程 / 标题 / 采纳回答逐字段对齐")
    void mapsAllPublishedFields() {
        seed(12L, 100L, 50L);

        QuestionRef ref = api.findQuestion(12L).orElseThrow();

        assertThat(ref.id()).isEqualTo(12L);
        assertThat(ref.authorId()).isEqualTo(ASKER_ID);
        assertThat(ref.courseId()).isEqualTo(100L);
        assertThat(ref.title()).isEqualTo("红黑树插入后如何判断旋转方向？");
        assertThat(ref.acceptedAnswerId()).isEqualTo(50L);
        assertThat(ref.deleted()).isFalse();
    }

    @Test
    @DisplayName("映射：未采纳的公开提问 acceptedAnswerId 与 courseId 均为 null")
    void mapsUnacceptedPublicQuestion() {
        seed(13L, null, null);

        QuestionRef ref = api.findQuestion(13L).orElseThrow();

        assertThat(ref.courseId()).isNull();
        assertThat(ref.acceptedAnswerId()).isNull();
        assertThat(ref.deleted()).isFalse();
    }

    @Test
    @DisplayName("不存在的问题返回 empty，而不是抛异常——调用方按“查不到”处理")
    void returnsEmptyWhenQuestionMissing() {
        Optional<QuestionRef> ref = api.findQuestion(404L);

        assertThat(ref).isEmpty();
    }

    @Test
    @DisplayName("软删除的问题仍返回引用，但 deleted=true，下游据此跳过结算与通知")
    void flagsSoftDeletedQuestion() {
        Question question = seed(12L, 100L, 50L);
        // 已有回答的问题作者删不掉，这里由课程管理者删除——正是 QA-1013 之外的那条路径
        question.softDelete(TEACHER, true, NOW.plusSeconds(60));

        QuestionRef ref = api.findQuestion(12L).orElseThrow();

        assertThat(ref.deleted()).isTrue();
        assertThat(ref.id()).isEqualTo(12L);
        assertThat(ref.acceptedAnswerId()).isEqualTo(50L);   // 删除不清空采纳关系，结算方可自行判断
    }
}
