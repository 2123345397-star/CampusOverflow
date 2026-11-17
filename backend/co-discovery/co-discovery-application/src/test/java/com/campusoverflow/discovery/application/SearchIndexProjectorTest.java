package com.campusoverflow.discovery.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusoverflow.discovery.domain.search.SearchDocument;
import com.campusoverflow.discovery.domain.search.SearchIndex;
import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.AnswerSubmittedEvent;
import com.campusoverflow.qa.api.event.ContentDeletedEvent;
import com.campusoverflow.qa.api.event.QuestionEditedEvent;
import com.campusoverflow.qa.api.event.QuestionPostedEvent;
import com.campusoverflow.qa.api.event.VoteCastEvent;
import com.campusoverflow.reputation.api.event.BountyAwardedEvent;
import com.campusoverflow.reputation.api.event.BountyClosedEvent;
import com.campusoverflow.reputation.api.event.BountyOpenedEvent;
import com.campusoverflow.shared.event.ProcessedEventStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SearchIndexProjectorTest {

    private static final Instant NOW = Instant.parse("2026-10-08T02:00:00Z");

    private final FakeIndex index = new FakeIndex();
    private final Set<String> seen = new HashSet<>();
    private final ProcessedEventStore processed = (eventId, consumer) -> seen.add(eventId + ":" + consumer);
    private final SearchIndexProjector projector = new SearchIndexProjector(index, processed);

    @Test
    void questionPostedTwiceInsertsOnlyOnce() {
        QuestionPostedEvent e = new QuestionPostedEvent("e1", NOW, 7L, 3L, 100L, "红黑树为什么必须旋转？",
                "黑高不变量的解释", List.of("数据结构", "红黑树"));

        projector.on(e);
        projector.on(e);

        assertThat(index.inserted).hasSize(1);
        SearchDocument doc = index.inserted.get(0);
        assertThat(doc.questionId()).isEqualTo(7L);
        assertThat(doc.courseId()).isEqualTo(100L);
        assertThat(doc.authorId()).isEqualTo(3L);
        assertThat(doc.tags()).containsExactly("数据结构", "红黑树");
    }

    @Test
    void answerCountAcceptedAndDeletionAreIdempotent() {
        AnswerSubmittedEvent submitted = new AnswerSubmittedEvent("e2", NOW, 50L, 7L, 3L, 1L, "title");
        AnswerAcceptedEvent accepted = new AnswerAcceptedEvent("e3", NOW, 50L, 7L, 3L, 1L, 1L, "title");
        ContentDeletedEvent answerDeleted = new ContentDeletedEvent("e4", NOW, "ANSWER", 50L, 7L);
        ContentDeletedEvent questionDeleted = new ContentDeletedEvent("e5", NOW, "QUESTION", 7L, 7L);

        projector.on(submitted);
        projector.on(submitted);
        projector.on(accepted);
        projector.on(accepted);
        projector.on(answerDeleted);
        projector.on(answerDeleted);
        projector.on(questionDeleted);
        projector.on(questionDeleted);

        assertThat(index.answerCountDeltas).containsExactly(1, -1);
        assertThat(index.accepted).containsExactly(7L);
        assertThat(index.deleted).containsExactly(7L);
    }

    @Test
    void questionVoteAndBountyUpdatesAreIdempotent() {
        VoteCastEvent questionVote = new VoteCastEvent("e6", NOW, "QUESTION", 7L, 3L, 4L, 7L, 0, 1);
        VoteCastEvent answerVote = new VoteCastEvent("e7", NOW, "ANSWER", 50L, 3L, 4L, 7L, 0, 1);
        BountyOpenedEvent opened = new BountyOpenedEvent("e8", NOW, 1L, 7L, 9L, 50, NOW.plusSeconds(3600));
        BountyAwardedEvent awarded = new BountyAwardedEvent("e9", NOW, 1L, 7L, 9L, 3L, 50);
        BountyClosedEvent closed = new BountyClosedEvent("e10", NOW, 1L, 7L, 9L, 50);

        projector.on(questionVote);
        projector.on(questionVote);
        projector.on(answerVote);
        projector.on(opened);
        projector.on(opened);
        projector.on(awarded);
        projector.on(closed);

        assertThat(index.scoreDeltas).containsExactly(1);
        assertThat(index.bountyStates).containsExactly(true, false, false);
    }

    private static final class FakeIndex implements SearchIndex {
        final List<SearchDocument> inserted = new ArrayList<>();
        final List<Integer> answerCountDeltas = new ArrayList<>();
        final List<Long> accepted = new ArrayList<>();
        final List<Long> deleted = new ArrayList<>();
        final List<Integer> scoreDeltas = new ArrayList<>();
        final List<Boolean> bountyStates = new ArrayList<>();

        @Override public void insert(SearchDocument document) { inserted.add(document); }
        @Override public void updateText(long questionId, String title, String bodyText, List<String> tags,
                                         Instant at) { }
        @Override public void adjustAnswerCount(long questionId, int delta, Instant at) {
            answerCountDeltas.add(delta);
        }
        @Override public void adjustScore(long questionId, int delta) { scoreDeltas.add(delta); }
        @Override public void markAccepted(long questionId, Instant at) { accepted.add(questionId); }
        @Override public void setBountyOpen(long questionId, boolean open) { bountyStates.add(open); }
        @Override public void markDeleted(long questionId) { deleted.add(questionId); }
        @Override public List<String> findTags(long questionId) { return List.of(); }
        @Override public List<RelatedCandidate> findCandidatesSharingTags(long questionId, List<String> tags,
                                                                          int limit) { return List.of(); }
    }
}
