package com.campusoverflow.qa.application;

import com.campusoverflow.qa.api.QaQueryApi;
import com.campusoverflow.qa.api.QuestionRef;
import com.campusoverflow.qa.domain.Question;
import com.campusoverflow.qa.domain.QuestionRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 发布语言（published language）的只读出口：把 Q&A 的聚合翻译成其他上下文需要的最小视图。
 *
 * <p>这里刻意不做权限判断、不抛 404：调用方（悬赏结算、通知投影）只关心“这条问题还在不在”，
 * 不存在就拿到 {@link Optional#empty()}，已软删除则通过 {@code deleted=true} 自行决定是否跳过。
 * 这样 Q&A 的删除策略变化时，不会把异常语义硬编码进下游上下文。
 */
@Service
@Transactional(readOnly = true)
public class QaQueryApiImpl implements QaQueryApi {

    private final QuestionRepository questions;

    public QaQueryApiImpl(QuestionRepository questions) {
        this.questions = questions;
    }

    @Override
    public Optional<QuestionRef> findQuestion(long questionId) {
        return questions.findById(questionId).map(QaQueryApiImpl::toRef);
    }

    private static QuestionRef toRef(Question question) {
        return new QuestionRef(question.id(), question.authorId(), question.courseId(),
                question.title().value(), question.acceptedAnswerId(), question.isDeleted());
    }
}
