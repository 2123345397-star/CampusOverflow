package com.campusoverflow.qa.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Lab4 Dev-1 角色卡要求的三个领域不变量测试。 */
class QingcenLab4Test {

    private static final Instant NOW = Instant.parse("2026-10-01T08:00:00Z");

    @Test
    void emptyTitleIsRejected() {
        assertThatThrownBy(() -> new Title(""))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("标题");
    }

    @Test
    void deletedQuestionCannotBeAnswered() {
        Actor asker = new Actor(1, "提问者", Role.STUDENT);
        Actor answerer = new Actor(2, "回答者", Role.STUDENT);
        Question question = Question.post(asker.userId(), null,
                new Title("红黑树插入后如何判断旋转方向？"),
                MarkdownBody.ofQuestion("插入节点后出现连续红色节点，什么时候左旋、什么时候右旋？"),
                Tags.of(List.of("数据结构", "红黑树")), NOW);
        question.assignId(10);
        question.softDelete(asker, false, NOW);

        assertThatThrownBy(() -> Answer.submit(question, answerer.userId(),
                MarkdownBody.ofAnswer("这是一个迟到了很久的回答内容"), NOW))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void duplicateVoteOverwritesPreviousVote() {
        Vote vote = Vote.cast(1, 3, TargetType.ANSWER, 50, 1, NOW);

        assertThat(vote.change(-1, NOW.plusSeconds(1))).isEqualTo(1);
        assertThat(vote.value()).isEqualTo(-1);

        assertThat(vote.change(1, NOW.plusSeconds(2))).isEqualTo(-1);
        assertThat(vote.value()).isEqualTo(1);
    }
}
