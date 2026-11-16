package com.campusoverflow.qa.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.qa.application.fake.QaTestFixtures.CountingViewCounter;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryListCache;
import com.campusoverflow.qa.application.fake.QaTestFixtures.InMemoryReadModel;
import com.campusoverflow.qa.application.fake.QaTestFixtures.StubIdentity;
import com.campusoverflow.qa.application.fake.QaTestFixtures.StubRenderer;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * T1-10 验收：list 命中缓存不再回源、detail 的“我的投票”按 QUESTION:id / ANSWER:id 取值、
 * popularTags 的 limit 夹在 1–50。
 */
class QuestionQueryServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-08T06:00:00Z");
    private static final Long COURSE_ID = 100L;
    private static final PageRequest PAGE = new PageRequest(1, 20);

    private final InMemoryReadModel readModel = new InMemoryReadModel();
    private final InMemoryListCache cache = new InMemoryListCache();
    private final CountingViewCounter viewCounter = new CountingViewCounter();
    private final StubIdentity identity = new StubIdentity()
            .withUser(1, "小明", Role.STUDENT)
            .withUser(2, "小红", Role.STUDENT)
            .withUser(9, "王老师", Role.TEACHER);

    private final Actor ming = new Actor(1, "小明", Role.STUDENT);

    private QuestionQueryService service;

    @BeforeEach
    void setUp() {
        service = new QuestionQueryService(readModel, identity, new StubRenderer(), viewCounter, cache);
    }

    private static QuestionRow questionRow(long id, long authorId, String title, String body, Long acceptedAnswerId) {
        return new QuestionRow(id, authorId, COURSE_ID, title, body, List.of("java", "红黑树"), acceptedAnswerId, 5, 2,
                30, NOW, NOW);
    }

    @Test
    @DisplayName("列表：第一次回源并回填缓存，第二次直接命中缓存")
    void listUsesCacheAside() {
        readModel.questions.add(questionRow(12L, 1L, "红黑树插入后为什么必须旋转？", "变色为什么不够？希望有直观解释。", null));

        PageResult<QuestionSummaryView> first = service.list(COURSE_ID, PAGE);
        PageResult<QuestionSummaryView> second = service.list(COURSE_ID, PAGE);

        assertThat(first.items()).hasSize(1);
        assertThat(second.items()).hasSize(1);
        assertThat(readModel.listCalls).hasSize(1);   // 第二次没有再查库
        assertThat(cache.misses).isEqualTo(1);
        assertThat(cache.hits).isEqualTo(1);
    }

    @Test
    @DisplayName("列表：作者信息批量回填，摘要截断到 160 字，有采纳回答时 accepted 为真")
    void listMapsAuthorExcerptAndAcceptedFlag() {
        String longBody = "变色为什么不够？".repeat(60);
        readModel.questions.add(questionRow(12L, 1L, "红黑树插入后为什么必须旋转？", longBody, 50L));
        readModel.questions.add(questionRow(13L, 404L, "作者已经注销的老问题标题", "这条问题的作者在用户表里查不到。",
                null));

        PageResult<QuestionSummaryView> page = service.list(COURSE_ID, PAGE);

        QuestionSummaryView first = page.items().get(0);
        assertThat(first.author().displayName()).isEqualTo("小明");
        assertThat(first.excerpt()).hasSize(160);
        assertThat(first.accepted()).isTrue();
        assertThat(first.tags()).containsExactly("java", "红黑树");

        QuestionSummaryView orphan = page.items().get(1);
        assertThat(orphan.author().displayName()).isEqualTo("已注销用户");
        assertThat(orphan.accepted()).isFalse();
    }

    @Test
    @DisplayName("详情：我的投票按 QUESTION:id 与 ANSWER:id 取值，评论挂到各自宿主，浏览数 +1")
    void detailAssemblesVotesCommentsAndPermissions() {
        readModel.questions.add(questionRow(12L, 1L, "红黑树插入后为什么必须旋转？", "变色为什么不够？", 50L));
        readModel.answers.add(new AnswerRow(50L, 2L, "关键看叔叔节点的颜色。", 8, 9L, NOW, NOW));
        readModel.answers.add(new AnswerRow(51L, 2L, "补充一个反例更直观。", 3, null, NOW.plusSeconds(60), NOW));
        readModel.comments.add(new CommentRow(70L, "QUESTION", 12L, 2L, null, "同问", NOW));
        readModel.comments.add(new CommentRow(71L, "ANSWER", 50L, 1L, null, "懂了，谢谢", NOW));
        readModel.myVotes.put("QUESTION:12", 1);
        readModel.myVotes.put("ANSWER:50", -1);

        QuestionDetailView detail = service.detail(12L, ming);

        assertThat(detail.myVote()).isEqualTo(1);
        assertThat(detail.permissions().canEdit()).isTrue();
        assertThat(detail.permissions().canAccept()).isFalse();     // 已经采纳过，不能再采纳
        assertThat(detail.bodyHtml()).isEqualTo("<p>变色为什么不够？</p>");
        assertThat(detail.course().code()).isEqualTo("CS2001");

        // 已采纳的回答排在最前，且 vote 取自 ANSWER:50
        AnswerView accepted = detail.answers().get(0);
        assertThat(accepted.id()).isEqualTo(50L);
        assertThat(accepted.accepted()).isTrue();
        assertThat(accepted.myVote()).isEqualTo(-1);
        assertThat(accepted.endorsedBy().displayName()).isEqualTo("王老师");
        assertThat(accepted.comments()).extracting(CommentView::body).containsExactly("懂了，谢谢");
        assertThat(accepted.canEdit()).isFalse();                  // 小红写的，小明不能改

        assertThat(detail.answers().get(1).myVote()).isZero();     // 没有投票记录
        assertThat(detail.comments()).extracting(CommentView::body).containsExactly("同问");

        assertThat(viewCounter.incremented).containsExactly(12L);
        assertThat(readModel.voteLookups).hasSize(1);
    }

    @Test
    @DisplayName("详情：未登录时不查投票、权限全为 false")
    void detailForAnonymous() {
        readModel.questions.add(questionRow(12L, 1L, "红黑树插入后为什么必须旋转？", "变色为什么不够？", null));

        QuestionDetailView detail = service.detail(12L, null);

        assertThat(detail.myVote()).isZero();
        assertThat(detail.permissions()).isEqualTo(PermissionsView.NONE);
        assertThat(readModel.voteLookups).isEmpty();
    }

    @Test
    @DisplayName("详情：问题不存在或已删除时返回 404")
    void detailOfMissingQuestionIsNotFound() {
        assertThatThrownBy(() -> service.detail(999L, ming)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("热门标签：limit 被夹在 1–50 之间")
    void popularTagsClampsLimit() {
        readModel.tags.addAll(List.of(new TagView("java", 30), new TagView("红黑树", 20), new TagView("mysql", 10)));

        assertThat(service.popularTags(0)).hasSize(1);      // 0 → 1
        assertThat(service.popularTags(-5)).hasSize(1);     // 负数 → 1
        assertThat(service.popularTags(100)).hasSize(3);    // 100 → 50，但只有 3 条
    }
}
