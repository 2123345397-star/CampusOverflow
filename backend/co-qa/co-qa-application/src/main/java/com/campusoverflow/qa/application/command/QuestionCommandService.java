package com.campusoverflow.qa.application.command;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.qa.api.event.ContentDeletedEvent;
import com.campusoverflow.qa.api.event.QuestionEditedEvent;
import com.campusoverflow.qa.api.event.QuestionPostedEvent;
import com.campusoverflow.qa.application.port.ContentRenderer;
import com.campusoverflow.qa.application.port.QuestionListCache;
import com.campusoverflow.qa.domain.MarkdownBody;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.QuestionRepository;
import com.campusoverflow.qa.domain.TagRepository;
import com.campusoverflow.qa.domain.Tags;
import com.campusoverflow.qa.domain.Title;
import com.campusoverflow.shared.audit.AuditTrail;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.event.EventIds;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.security.Actor;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用例：发布 / 编辑 / 删除问题。
 *
 * <p>编排顺序固定为：校验外部引用（课程是否存在）→ 构造值对象（长度、标签格式在这里拦下）
 * → 委托聚合方法改状态 → 落库 → 同步副作用（标签用量、集成事件、缓存失效、审计）。
 * 副作用都在同一事务内完成，事件走事务性发件箱，不会出现“数据没写完事件先发”的情况。</p>
 */
@Service
public class QuestionCommandService {

    static final int PLAIN_TEXT_FOR_INDEX = 2000;

    private final QuestionRepository questions;
    private final TagRepository tags;
    private final IdentityApi identity;
    private final IntegrationEventPublisher events;
    private final ContentRenderer renderer;
    private final QuestionListCache listCache;
    private final AuditTrail audit;
    private final Clock clock;

    public QuestionCommandService(QuestionRepository questions, TagRepository tags, IdentityApi identity,
                                  IntegrationEventPublisher events, ContentRenderer renderer,
                                  QuestionListCache listCache, AuditTrail audit, Clock clock) {
        this.questions = questions;
        this.tags = tags;
        this.identity = identity;
        this.events = events;
        this.renderer = renderer;
        this.listCache = listCache;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public long post(Actor actor, PostQuestionCommand cmd) {
        // 1) 关联课程必须真实存在；不传课程表示“不挂课程的公开提问”，允许为空
        if (cmd.courseId() != null) {
            identity.findCourse(cmd.courseId())
                    .orElseThrow(() -> new NotFoundException("QA-1006", "课程不存在：" + cmd.courseId()));
        }

        // 2) 值对象负责格式校验：标题 10–150、正文 ≥20、标签 1–5 且小写去重
        Title title = new Title(cmd.title());
        MarkdownBody body = MarkdownBody.ofQuestion(cmd.body());
        Tags postTags = Tags.of(cmd.tags());
        Instant now = clock.instant();

        // 3) 聚合方法创建，仓储落库后再拿到自增 id
        Question question = questions.save(Question.post(actor.userId(), cmd.courseId(), title, body, postTags, now));
        long questionId = question.id();
        List<String> tagNames = question.tags().names();

        // 4) 标签用量 +1（热门标签读的是 qa_tag.question_count）
        tags.adjustUsage(tagNames, 1);

        // 5) 发集成事件：搜索索引只需要纯文本，别把整篇 Markdown 塞进去
        events.publish(new QuestionPostedEvent(EventIds.next(), now, questionId, actor.userId(), cmd.courseId(),
                question.title().value(), renderer.toPlainText(body.value(), PLAIN_TEXT_FOR_INDEX), tagNames));

        // 6) 列表缓存整体失效；审计留痕
        listCache.invalidateAll();
        audit.record("QUESTION_POSTED", actor.userId(), "QUESTION", String.valueOf(questionId), null);

        return questionId;
    }

    @Transactional
    public void edit(Actor actor, long questionId, EditQuestionCommand cmd) {
        Question question = load(questionId);

        // 权限判定入口在应用层（要不要查课程成员关系），真正的拒绝由聚合方法抛出
        boolean managesCourse = identity.canManageCourse(actor.userId(), question.courseId());
        Tags newTags = Tags.of(cmd.tags());
        List<String> before = question.tags().names();
        List<String> after = newTags.names();

        question.edit(actor, managesCourse, new Title(cmd.title()), MarkdownBody.ofQuestion(cmd.body()), newTags,
                clock.instant());
        questions.save(question);

        // 标签用量按差集调整：只对新增的 +1、被去掉的 -1，没变的标签不动
        List<String> added = after.stream().filter(tag -> !before.contains(tag)).toList();
        List<String> removed = before.stream().filter(tag -> !after.contains(tag)).toList();
        if (!added.isEmpty()) {
            tags.adjustUsage(added, 1);
        }
        if (!removed.isEmpty()) {
            tags.adjustUsage(removed, -1);
        }

        events.publish(new QuestionEditedEvent(EventIds.next(), clock.instant(), questionId,
                question.title().value(),
                renderer.toPlainText(question.body().value(), PLAIN_TEXT_FOR_INDEX), after));
        listCache.invalidateAll();
        audit.record("QUESTION_EDITED", actor.userId(), "QUESTION", String.valueOf(questionId), null);
    }

    @Transactional
    public void delete(Actor actor, long questionId) {
        Question question = load(questionId);
        boolean managesCourse = identity.canManageCourse(actor.userId(), question.courseId());

        question.softDelete(actor, managesCourse, clock.instant());
        questions.save(question);

        // 读模型靠这个事件把行标记为已删除，否则列表里还能看到它
        events.publish(new ContentDeletedEvent(EventIds.next(), clock.instant(), "QUESTION", questionId, questionId));
        listCache.invalidateAll();
        audit.record("QUESTION_DELETED", actor.userId(), "QUESTION", String.valueOf(questionId), null);
    }

    /** 统一 404：不存在与已软删除对外表现一致，避免泄露“这条内容曾经存在”。 */
    private Question load(long questionId) {
        return questions.findById(questionId)
                .filter(question -> !question.isDeleted())
                .orElseThrow(() -> new NotFoundException("QA-1000", "问题不存在或已被删除：" + questionId));
    }
}
