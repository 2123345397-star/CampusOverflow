package com.campusoverflow.qa.application.query;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.qa.application.port.ContentRenderer;
import com.campusoverflow.qa.application.port.QaReadModel;
import com.campusoverflow.qa.application.port.QuestionListCache;
import com.campusoverflow.qa.application.port.ViewCounter;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 问答查询用例（读路径）。
 *
 * <p>直接读投影表，不装配聚合：列表页只需要摘要，没必要把标签、正文、计数都从聚合里加载一遍。
 * 作者信息一律批量查 Identity，避免 N+1；列表结果走 Cache-Aside，写操作会把整代缓存作废。</p>
 */
@Service
@Transactional(readOnly = true)
public class QuestionQueryService {

    private static final int EXCERPT_LENGTH = 160;
    private static final int MAX_TAG_LIMIT = 50;
    private static final int MIN_TAG_LIMIT = 1;

    private final QaReadModel readModel;
    private final IdentityApi identity;
    private final ContentRenderer renderer;
    private final ViewCounter viewCounter;
    private final QuestionListCache cache;

    public QuestionQueryService(QaReadModel readModel, IdentityApi identity, ContentRenderer renderer,
                                ViewCounter viewCounter, QuestionListCache cache) {
        this.readModel = readModel;
        this.identity = identity;
        this.renderer = renderer;
        this.viewCounter = viewCounter;
        this.cache = cache;
    }

    /** 列表：先看缓存，未命中再查读模型；作者信息一次批量补齐后回填缓存。 */
    public PageResult<QuestionSummaryView> list(Long courseId, PageRequest page) {
        Optional<PageResult<QuestionSummaryView>> cached = cache.get(courseId, page);
        if (cached.isPresent()) {
            return cached.get();
        }

        PageResult<QuestionRow> rows = readModel.listQuestions(courseId, page);
        List<Long> authorIds = rows.items().stream().map(QuestionRow::authorId).distinct().toList();
        Map<Long, UserSummary> authors = authorIds.isEmpty() ? Map.of() : identity.findUsers(authorIds);

        PageResult<QuestionSummaryView> result = rows.map(row -> toSummary(row, authors.get(row.authorId())));
        cache.put(courseId, page, result);
        return result;
    }

    /** 详情：正文渲染成安全 HTML，回答按“已采纳 → 得分 → 时间”排序，评论挂到各自的宿主上。 */
    public QuestionDetailView detail(long questionId, Actor actorOrNull) {
        QuestionRow row = readModel.findQuestion(questionId)
                .orElseThrow(() -> new NotFoundException("QA-1000", "问题不存在或已被删除：" + questionId));

        List<AnswerRow> answerRows = readModel.findAnswers(questionId);
        List<CommentRow> commentRows = readModel.findComments(questionId);

        Map<Long, UserSummary> users = loadUsers(row, answerRows, commentRows);
        boolean managesCourse = actorOrNull != null && identity.canManageCourse(actorOrNull.userId(), row.courseId());

        List<Long> answerIds = answerRows.stream().map(AnswerRow::id).toList();
        Map<String, Integer> myVotes = actorOrNull == null
                ? Map.of()
                : readModel.findMyVotes(actorOrNull.userId(), questionId, answerIds);

        List<CommentView> questionComments = new ArrayList<>();
        Map<Long, List<CommentView>> answerComments = new HashMap<>();
        for (CommentRow comment : commentRows) {
            CommentView view = toCommentView(comment, users);
            if ("ANSWER".equals(comment.targetType())) {
                answerComments.computeIfAbsent(comment.targetId(), key -> new ArrayList<>()).add(view);
            } else {
                questionComments.add(view);
            }
        }

        List<AnswerView> answers = answerRows.stream()
                .map(answer -> toAnswerView(answer, row, users, myVotes, answerComments, actorOrNull, managesCourse))
                .sorted(Comparator.comparing(AnswerView::accepted).reversed()
                        .thenComparing(Comparator.comparingInt(AnswerView::score).reversed())
                        .thenComparing(AnswerView::createdAt))
                .toList();

        // 浏览数只写 Redis，由定时任务批量落库，所以放在最后、且不影响读取结果
        viewCounter.increment(questionId);

        return new QuestionDetailView(row.id(), row.title(), row.body(), renderer.toSafeHtml(row.body()),
                row.tags(), courseRefOf(row), AuthorView.of(row.authorId(), users.get(row.authorId())),
                row.score(), row.answerCount(), row.viewCount(), row.acceptedAnswerId(),
                myVotes.getOrDefault("QUESTION:" + questionId, 0),
                row.createdAt(), row.updatedAt(), permissionsOf(row, actorOrNull, managesCourse),
                questionComments, answers);
    }

    /** 热门标签：limit 一律夹在 1–50，防止被传 100000 拖垮查询。 */
    public List<TagView> popularTags(int limit) {
        int safeLimit = Math.min(Math.max(limit, MIN_TAG_LIMIT), MAX_TAG_LIMIT);
        return readModel.popularTags(safeLimit);
    }

    private QuestionSummaryView toSummary(QuestionRow row, UserSummary author) {
        return new QuestionSummaryView(row.id(), row.title(), renderer.toPlainText(row.body(), EXCERPT_LENGTH),
                row.tags(), row.courseId(), AuthorView.of(row.authorId(), author), row.score(), row.answerCount(),
                row.viewCount(), row.acceptedAnswerId() != null, row.createdAt());
    }

    private AnswerView toAnswerView(AnswerRow answer, QuestionRow question, Map<Long, UserSummary> users,
                                    Map<String, Integer> myVotes, Map<Long, List<CommentView>> answerComments,
                                    Actor actorOrNull, boolean managesCourse) {
        boolean accepted = question.acceptedAnswerId() != null && question.acceptedAnswerId() == answer.id();
        boolean canEdit = actorOrNull != null && (actorOrNull.is(answer.authorId()) || managesCourse);
        return new AnswerView(answer.id(), answer.body(), renderer.toSafeHtml(answer.body()),
                AuthorView.of(answer.authorId(), users.get(answer.authorId())), answer.score(), accepted,
                answer.endorsedBy() == null ? null : AuthorView.of(answer.endorsedBy(), users.get(answer.endorsedBy())),
                myVotes.getOrDefault("ANSWER:" + answer.id(), 0), canEdit, answer.createdAt(), answer.updatedAt(),
                answerComments.getOrDefault(answer.id(), List.of()));
    }

    private static CommentView toCommentView(CommentRow comment, Map<Long, UserSummary> users) {
        return new CommentView(comment.id(), AuthorView.of(comment.authorId(), users.get(comment.authorId())),
                comment.parentId(), comment.body(), comment.createdAt());
    }

    private CourseRefView courseRefOf(QuestionRow row) {
        // 读模型只有 course_id，课程编码与名称属于 Identity 上下文，走 OHS 取一次
        return row.courseId() == null ? null
                : identity.findCourse(row.courseId())
                        .map(course -> new CourseRefView(course.id(), course.code(), course.name()))
                        .orElseGet(() -> new CourseRefView(row.courseId(), null, null));
    }

    private static PermissionsView permissionsOf(QuestionRow row, Actor actorOrNull, boolean managesCourse) {
        if (actorOrNull == null) {
            return PermissionsView.NONE;
        }
        boolean author = actorOrNull.is(row.authorId());
        boolean canEdit = author || managesCourse;
        boolean canDelete = managesCourse || (author && row.answerCount() == 0);
        boolean canAccept = (author || managesCourse) && row.acceptedAnswerId() == null;
        return new PermissionsView(canEdit, canDelete, canAccept, managesCourse);
    }

    /** 一次收集问题、回答、回答认证人、评论作者的全部 ID，只调一次 Identity。 */
    private Map<Long, UserSummary> loadUsers(QuestionRow row, List<AnswerRow> answers, List<CommentRow> comments) {
        Set<Long> ids = new HashSet<>();
        ids.add(row.authorId());
        answers.forEach(answer -> {
            ids.add(answer.authorId());
            if (answer.endorsedBy() != null) {
                ids.add(answer.endorsedBy());
            }
        });
        comments.forEach(comment -> ids.add(comment.authorId()));
        return ids.isEmpty() ? Map.of() : identity.findUsers(ids);
    }
}
