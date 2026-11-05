package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.ConflictException;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.security.Actor;
import java.time.Instant;
import java.util.Objects;

/**
 * 问题聚合根。状态变更只能通过本类的领域方法，字段对外只读；
 * 回答数、得分、浏览数三个计数器由仓储的原子 UPDATE 维护，聚合内只保留快照值用于判断业务规则。
 */
public class Question {

    private Long id;
    private final long authorId;
    private final Long courseId;
    private Title title;
    private MarkdownBody body;
    private Tags tags;
    private Long acceptedAnswerId;
    private final int score;
    private final int answerCount;
    private final int viewCount;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private final long version;

    private Question(Long id, long authorId, Long courseId, Title title, MarkdownBody body, Tags tags,
                     Long acceptedAnswerId, int score, int answerCount, int viewCount,
                     Instant createdAt, Instant updatedAt, Instant deletedAt, long version) {
        this.id = id;
        this.authorId = authorId;
        this.courseId = courseId;
        this.title = Objects.requireNonNull(title);
        this.body = Objects.requireNonNull(body);
        this.tags = Objects.requireNonNull(tags);
        this.acceptedAnswerId = acceptedAnswerId;
        this.score = score;
        this.answerCount = answerCount;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    /** 发布新问题：未落库（id 为空），计数归零，未采纳、未删除。 */
    public static Question post(long authorId, Long courseId, Title title, MarkdownBody body, Tags tags, Instant now) {
        return new Question(null, authorId, courseId, title, body, tags, null, 0, 0, 0, now, now, null, 0L);
    }

    /** 从数据库重构（不做业务校验，参数顺序与 JPA 实体一一对应）。 */
    public static Question reconstitute(Long id, long authorId, Long courseId, Title title, MarkdownBody body,
                                        Tags tags, Long acceptedAnswerId, int score, int answerCount, int viewCount,
                                        Instant createdAt, Instant updatedAt, Instant deletedAt, long version) {
        return new Question(id, authorId, courseId, title, body, tags, acceptedAnswerId, score, answerCount,
                viewCount, createdAt, updatedAt, deletedAt, version);
    }

    /** 编辑标题 / 正文 / 标签。只有作者或课程管理者可以编辑，已删除的问题不可编辑。 */
    public void edit(Actor operator, boolean actorManagesCourse, Title newTitle, MarkdownBody newBody, Tags newTags,
                     Instant now) {
        ensureNotDeleted();
        if (!operator.is(authorId) && !actorManagesCourse) {
            throw new ForbiddenException("QA-1010", "只有作者或课程管理者可以编辑该问题");
        }
        this.title = Objects.requireNonNull(newTitle);
        this.body = Objects.requireNonNull(newBody);
        this.tags = Objects.requireNonNull(newTags);
        this.updatedAt = now;
    }

    /** 采纳最佳答案：只能采纳一次，且回答必须属于本问题、未被删除。 */
    public void accept(Answer answer, Actor operator, boolean actorManagesCourse) {
        ensureNotDeleted();
        if (acceptedAnswerId != null) {
            throw new ConflictException("QA-1011", "该问题已采纳过回答，不能重复采纳");
        }
        if (!operator.is(authorId) && !actorManagesCourse) {
            throw new ForbiddenException("QA-1012", "只有提问者或课程管理者可以采纳回答");
        }
        if (answer == null) {
            throw new BusinessRuleException("QA-1013", "回答不存在");
        }
        if (answer.questionId() != id) {
            throw new BusinessRuleException("QA-1013", "该回答不属于本问题");
        }
        if (answer.isDeleted()) {
            throw new BusinessRuleException("QA-1014", "已删除的回答不能被采纳");
        }
        this.acceptedAnswerId = answer.id();
    }

    /**
     * 软删除：课程管理者可删任意问题；作者只能删还没有回答的问题（避免“问完就删、留下无主回答”）。
     */
    public void softDelete(Actor operator, boolean actorManagesCourse, Instant now) {
        ensureNotDeleted();
        if (!actorManagesCourse) {
            if (!operator.is(authorId)) {
                throw new ForbiddenException("QA-1015", "只有作者或课程管理者可以删除该问题");
            }
            if (answerCount > 0 || acceptedAnswerId != null) {
                throw new BusinessRuleException("QA-1015", "该问题已有回答，作者不能删除，请联系课程管理者");
            }
        }
        this.deletedAt = now;
        this.updatedAt = now;
    }

    /** 回答前的守卫：已删除的问题不接受新回答。 */
    public void ensureOpenForAnswers() {
        ensureNotDeleted();
    }

    public void ensureNotDeleted() {
        if (deletedAt != null) {
            throw new BusinessRuleException("QA-1020", "该问题已被删除");
        }
    }

    public boolean isAccepted(long answerId) {
        return acceptedAnswerId != null && acceptedAnswerId == answerId;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void assignId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("ID 已分配，不能重复赋值");
        }
        this.id = id;
    }

    public Long id() {
        return id;
    }

    public long authorId() {
        return authorId;
    }

    public Long courseId() {
        return courseId;
    }

    public Title title() {
        return title;
    }

    public MarkdownBody body() {
        return body;
    }

    public Tags tags() {
        return tags;
    }

    public Long acceptedAnswerId() {
        return acceptedAnswerId;
    }

    public int score() {
        return score;
    }

    public int answerCount() {
        return answerCount;
    }

    public int viewCount() {
        return viewCount;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public Instant deletedAt() {
        return deletedAt;
    }

    public long version() {
        return version;
    }
}
