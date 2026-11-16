package com.campusoverflow.discovery.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusoverflow.discovery.domain.search.SearchDocument;
import com.campusoverflow.discovery.domain.search.SearchHit;
import com.campusoverflow.discovery.domain.search.SearchIndex;
import com.campusoverflow.discovery.domain.search.SearchIndex.RelatedCandidate;
import com.campusoverflow.discovery.domain.search.SearchPort;
import com.campusoverflow.discovery.domain.search.SearchQuery;
import com.campusoverflow.discovery.domain.search.SearchSort;
import com.campusoverflow.identity.api.CourseSummary;
import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Role;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DiscoveryQueryServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-08T02:00:00Z");

    @Test
    void searchMapsAuthorAndFilters() {
        SearchHit hit = new SearchHit(7, "红黑树插入后为什么必须旋转？", "黑高不变量的解释", List.of("数据结构"),
                10L, 3L, 5, 2, true, true, NOW);
        DiscoveryQueryService service = new DiscoveryQueryService(
                query -> new PageResult<>(List.of(hit), 1, 1, 20),
                new EmptyIndex(),
                new StubIdentity().withUser(3, "小红", Role.STUDENT));

        PageResult<SearchResultView> result = service.search(new SearchQuery("红黑树", null, null,
                SearchSort.RELEVANCE, false, false, new PageRequest(1, 20)));

        assertThat(result.total()).isEqualTo(1);
        SearchResultView view = result.items().get(0);
        assertThat(view.id()).isEqualTo(7);
        assertThat(view.title()).isEqualTo("红黑树插入后为什么必须旋转？");
        assertThat(view.author().id()).isEqualTo(3);
        assertThat(view.author().displayName()).isEqualTo("小红");
        assertThat(view.author().role()).isEqualTo("STUDENT");
        assertThat(view.accepted()).isTrue();
        assertThat(view.bountyOpen()).isTrue();
    }

    @Test
    void relatedRanksByJaccardAndMapsSimilarity() {
        DiscoveryQueryService service = new DiscoveryQueryService(
                query -> PageResult.empty(1, 20),
                new EmptyIndex(
                        List.of("java", "spring"),
                        List.of(
                                new SearchIndex.RelatedCandidate(1, "A", 1, 4, 10, 1, false),
                                new SearchIndex.RelatedCandidate(2, "B", 2, 3, 1, 4, true),
                                new SearchIndex.RelatedCandidate(3, "C", 2, 2, 5, 0, false))),
                new StubIdentity());

        List<RelatedQuestionView> result = service.related(99, 2);

        assertThat(result).extracting(RelatedQuestionView::id).containsExactly(3L, 2L);
        assertThat(result.get(0).similarity()).isEqualTo(1.0);
        assertThat(result.get(1).similarity()).isCloseTo(2.0 / 3.0, within(0.0001));
        assertThat(result.get(1).accepted()).isTrue();
    }

    @Test
    void relatedReturnsEmptyWithoutTags() {
        DiscoveryQueryService service = new DiscoveryQueryService(
                query -> PageResult.empty(1, 20),
                new EmptyIndex(List.of(), List.of()),
                new StubIdentity());

        assertThat(service.related(99, 5)).isEmpty();
    }

    private static org.assertj.core.data.Offset<Double> within(double value) {
        return org.assertj.core.data.Offset.offset(value);
    }

    private static final class EmptyIndex implements SearchIndex {
        private final List<String> tags;
        private final List<RelatedCandidate> candidates;

        private EmptyIndex() {
            this(List.of(), List.of());
        }

        private EmptyIndex(List<String> tags, List<RelatedCandidate> candidates) {
            this.tags = tags;
            this.candidates = candidates;
        }

        @Override
        public List<String> findTags(long questionId) {
            return tags;
        }

        @Override
        public List<RelatedCandidate> findCandidatesSharingTags(long questionId, List<String> tags, int limit) {
            return candidates;
        }

        @Override public void insert(SearchDocument document) { }
        @Override public void updateText(long questionId, String title, String bodyText, List<String> tags, Instant at) { }
        @Override public void adjustAnswerCount(long questionId, int delta, Instant at) { }
        @Override public void adjustScore(long questionId, int delta) { }
        @Override public void markAccepted(long questionId, Instant at) { }
        @Override public void setBountyOpen(long questionId, boolean open) { }
        @Override public void markDeleted(long questionId) { }
    }

    private static final class StubIdentity implements IdentityApi {
        private final Map<Long, UserSummary> users = new java.util.HashMap<>();

        private StubIdentity withUser(long id, String name, Role role) {
            users.put(id, new UserSummary(id, name, role, true, "计算机工程学院"));
            return this;
        }

        @Override public Optional<UserSummary> findUser(long userId) { return Optional.ofNullable(users.get(userId)); }
        @Override public Map<Long, UserSummary> findUsers(Collection<Long> userIds) {
            return userIds.stream().filter(users::containsKey)
                    .collect(java.util.stream.Collectors.toMap(id -> id, users::get));
        }
        @Override public Optional<UserSummary> findByDisplayName(String displayName) { return Optional.empty(); }
        @Override public Optional<CourseSummary> findCourse(long courseId) { return Optional.empty(); }
        @Override public boolean canManageCourse(long userId, Long courseId) { return false; }
    }
}
