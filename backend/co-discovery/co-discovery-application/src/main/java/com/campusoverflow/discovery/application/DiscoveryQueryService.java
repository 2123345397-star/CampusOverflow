package com.campusoverflow.discovery.application;

import com.campusoverflow.discovery.domain.search.RelatedQuestionPolicy;
import com.campusoverflow.discovery.domain.search.SearchHit;
import com.campusoverflow.discovery.domain.search.SearchIndex;
import com.campusoverflow.discovery.domain.search.SearchPort;
import com.campusoverflow.discovery.domain.search.SearchQuery;
import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.shared.page.PageResult;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 用例：搜索与筛选、相关问题推荐。 */
@Service
@Transactional(readOnly = true)
public class DiscoveryQueryService {

    private final SearchPort search;
    private final SearchIndex index;
    private final IdentityApi identity;

    public DiscoveryQueryService(SearchPort search, SearchIndex index, IdentityApi identity) {
        this.search = search;
        this.index = index;
        this.identity = identity;
    }

    private static final int CANDIDATE_LIMIT = 50;

    public PageResult<SearchResultView> search(SearchQuery query) {
        PageResult<SearchHit> hits = search.search(query);
        Map<Long, UserSummary> authors = identity.findUsers(hits.items().stream()
                .map(SearchHit::authorId)
                .collect(java.util.stream.Collectors.toSet()));
        return hits.map(hit -> toView(hit, authors.get(hit.authorId())));
    }

    public List<RelatedQuestionView> related(long questionId, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        List<String> ownTags = index.findTags(questionId);
        if (ownTags == null || ownTags.isEmpty()) {
            return List.of();
        }
        int ownTagCount = ownTags.size();
        return RelatedQuestionPolicy.rank(
                        index.findCandidatesSharingTags(questionId, ownTags, CANDIDATE_LIMIT),
                        ownTagCount, limit)
                .stream()
                .map(candidate -> new RelatedQuestionView(candidate.questionId(), candidate.title(),
                        candidate.answerCount(), candidate.accepted(),
                        RelatedQuestionPolicy.jaccard(candidate.sharedTags(), ownTagCount, candidate.tagCount())))
                .toList();
    }

    private SearchResultView toView(SearchHit hit, UserSummary user) {
        SearchResultView.Author author = user == null
                ? new SearchResultView.Author(0, "未知用户", "STUDENT", false)
                : new SearchResultView.Author(user.id(), user.displayName(), user.role().name(), user.verified());
        return new SearchResultView(hit.questionId(), hit.title(), hit.snippet(), hit.tags(), hit.courseId(),
                author, hit.score(), hit.answerCount(), hit.accepted(), hit.bountyOpen(), hit.createdAt());
    }
}
