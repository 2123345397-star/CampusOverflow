package com.campusoverflow.qa.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.qa.api.event.CommentPostedEvent;
import com.campusoverflow.qa.api.event.VoteCastEvent;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryAnswers;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryQuestions;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryVotes;
import com.campusoverflow.qa.application.fake.QaTestFixtures.RecordingEvents;
import com.campusoverflow.qa.domain.Answer;
import com.campusoverflow.qa.domain.Comment;
import com.campusoverflow.qa.domain.CommentRepository;
import com.campusoverflow.qa.domain.MarkdownBody;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.TargetType;
import com.campusoverflow.qa.domain.Tags;
import com.campusoverflow.qa.domain.Title;
import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Lab4 T1-09：VoteService / CommentService 的独立服务层测试。 */
class QingcenVoteCommentServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-08T06:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private final InMemoryQuestions questions = new InMemoryQuestions();
    private final InMemoryAnswers answers = new InMemoryAnswers();
    private final InMemoryVotes votes = new InMemoryVotes();
    private final InMemoryComments comments = new InMemoryComments();
    private final RecordingEvents events = new RecordingEvents();

    private final Actor ming = new Actor(1, "小明", Role.STUDENT);
    private final Actor hong = new Actor(2, "小红", Role.STUDENT);

    private Question question;
    private Answer answer;
    private VoteService voteService;
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        voteService = new VoteService(votes, questions, answers, events, clock);
        commentService = new CommentService(comments, questions, answers, events, clock);

        question = questions.save(Question.post(ming.userId(), 100L,
                new Title("红黑树插入后为什么必须旋转？"),
                MarkdownBody.ofQuestion("变色为什么不够？希望有直观解释，最好能给出一个反例。"),
                Tags.of(List.of("数据结构", "红黑树")), NOW));
        answer = answers.save(Answer.submit(question, hong.userId(),
                MarkdownBody.ofAnswer("关键在于黑高这个不变量，仅靠变色无法调整。"), NOW));
    }

    @Test
    void voteChangesUseDeltaAndPublishOldNewValues() {
        assertThat(voteService.cast(ming, TargetType.ANSWER, answer.id(), 1).scoreDelta()).isEqualTo(1);
        assertThat(voteService.cast(ming, TargetType.ANSWER, answer.id(), 1).scoreDelta()).isZero();
        assertThat(voteService.cast(ming, TargetType.ANSWER, answer.id(), -1).scoreDelta()).isEqualTo(-2);
        assertThat(answers.scoreDeltas).containsEntry(answer.id(), -1);

        List<VoteCastEvent> voteEvents = events.ofType(VoteCastEvent.class);
        assertThat(voteEvents).hasSize(2);
        assertThat(voteEvents.get(1).oldValue()).isEqualTo(1);
        assertThat(voteEvents.get(1).newValue()).isEqualTo(-1);
    }

    @Test
    void retractingVoteDeletesIt() {
        voteService.cast(ming, TargetType.ANSWER, answer.id(), 1);
        VoteService.VoteResult retract = voteService.cast(ming, TargetType.ANSWER, answer.id(), 0);

        assertThat(retract.scoreDelta()).isEqualTo(-1);
        assertThat(votes.find(ming.userId(), TargetType.ANSWER, answer.id())).isEmpty();

        List<VoteCastEvent> voteEvents = events.ofType(VoteCastEvent.class);
        assertThat(voteEvents).hasSize(2);
        assertThat(voteEvents.get(1).oldValue()).isEqualTo(1);
        assertThat(voteEvents.get(1).newValue()).isZero();
    }

    @Test
    void cannotVoteOnOwnContent() {
        assertThatThrownBy(() -> voteService.cast(ming, TargetType.QUESTION, question.id(), 1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("自己");
    }

    @Test
    void commentAndReplyPublishEventsAndKeepParent() {
        long commentId = commentService.post(ming,
                new PostCommentCommand(TargetType.QUESTION, question.id(), null, "请问有图吗？"));
        Comment top = comments.findById(commentId).orElseThrow();
        assertThat(top.parentId()).isNull();

        long replyId = commentService.post(hong,
                new PostCommentCommand(TargetType.QUESTION, question.id(), commentId, "有的，稍后补。"));
        Comment reply = comments.findById(replyId).orElseThrow();
        assertThat(reply.parentId()).isEqualTo(commentId);

        List<CommentPostedEvent> posted = events.ofType(CommentPostedEvent.class);
        assertThat(posted).hasSize(2);
        assertThat(posted.get(0).questionId()).isEqualTo(question.id());
        assertThat(posted.get(1).authorId()).isEqualTo(hong.userId());
    }

    @Test
    void replyMustStayOnSameTarget() {
        long commentId = commentService.post(ming,
                new PostCommentCommand(TargetType.QUESTION, question.id(), null, "请问有图吗？"));

        assertThatThrownBy(() -> commentService.post(hong,
                new PostCommentCommand(TargetType.ANSWER, answer.id(), commentId, "挂错目标的回复")))
                .isInstanceOf(BusinessRuleException.class);
    }

    private static final class InMemoryComments implements CommentRepository {
        private final Map<Long, Comment> store = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public Optional<Comment> findById(long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Comment save(Comment comment) {
            if (comment.id() == null) {
                comment.assignId(sequence.incrementAndGet());
            }
            store.put(comment.id(), comment);
            return comment;
        }
    }
}
