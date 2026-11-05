package com.campusoverflow.qa.application.command;

import com.campusoverflow.qa.api.event.CommentPostedEvent;
import com.campusoverflow.qa.domain.Answer;
import com.campusoverflow.qa.domain.AnswerRepository;
import com.campusoverflow.qa.domain.Comment;
import com.campusoverflow.qa.domain.CommentRepository;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.QuestionRepository;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.event.EventIds;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.security.Actor;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用例：评论与二级回复。评论中的 @昵称 由 Discovery 解析并发送提醒。 */
@Service
public class CommentService {

    private final CommentRepository comments;
    private final QuestionRepository questions;
    private final AnswerRepository answers;
    private final IntegrationEventPublisher events;
    private final Clock clock;

    public CommentService(CommentRepository comments, QuestionRepository questions, AnswerRepository answers,
                          IntegrationEventPublisher events, Clock clock) {
        this.comments = comments;
        this.questions = questions;
        this.answers = answers;
        this.events = events;
        this.clock = clock;
    }

    @Transactional
    public long post(Actor actor, PostCommentCommand cmd) {
        long questionId = resolveQuestionId(cmd.targetType(), cmd.targetId());

        Comment parent = null;
        if (cmd.parentId() != null) {
            parent = comments.findById(cmd.parentId())
                    .orElseThrow(() -> new NotFoundException("QA-4003", "父评论不存在"));
        }

        Instant now = clock.instant();
        Comment saved = comments.save(Comment.post(cmd.targetType(), cmd.targetId(), questionId, actor.userId(),
                parent, cmd.body(), now));
        events.publish(new CommentPostedEvent(EventIds.next(), now, saved.id(), questionId, actor.userId(),
                saved.body()));
        return saved.id();
    }

    private long resolveQuestionId(TargetType type, long targetId) {
        if (type == TargetType.QUESTION) {
            Question question = questions.findById(targetId)
                    .filter(q -> !q.isDeleted())
                    .orElseThrow(() -> new NotFoundException("QA-4001", "问题不存在"));
            return question.id();
        }
        if (type == TargetType.ANSWER) {
            Answer answer = answers.findById(targetId)
                    .filter(a -> !a.isDeleted())
                    .orElseThrow(() -> new NotFoundException("QA-4002", "回答不存在"));
            return answer.questionId();
        }
        throw new BusinessRuleException("QA-4003", "不支持的评论目标");
    }
}
