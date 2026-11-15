package com.campusoverflow.reputation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 领域元数据的小用例：把账本/枚举里不经过业务路径的代码也纳入覆盖率门禁。 */
class ReputationDomainMetaTest {

    @Test
    @DisplayName("账本流水必须带 eventId 与 reason")
    void ledgerEntryRejectsNullKeys() {
        Instant now = Instant.parse("2026-10-10T10:00:00Z");

        assertThatThrownBy(() -> new LedgerEntry(null, 1L, null, ReputationReason.ANSWER_VOTE,
                1, "ANSWER", 1L, now)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LedgerEntry(null, 1L, "evt-1", null,
                1, "ANSWER", 1L, now)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("声誉原因带中文标签，并能区分投票类")
    void reasonsExposeLabelsAndVoteFlag() {
        assertThat(ReputationReason.values()).hasSize(4);
        assertThat(ReputationReason.ANSWER_ACCEPTED.label()).isEqualTo("回答被采纳");
        assertThat(ReputationReason.BOUNTY_AWARDED.label()).isEqualTo("赢得悬赏");
        assertThat(ReputationReason.QUESTION_VOTE.isVote()).isTrue();
        assertThat(ReputationReason.ANSWER_VOTE.isVote()).isTrue();
        assertThat(ReputationReason.BOUNTY_AWARDED.isVote()).isFalse();
    }

    @Test
    @DisplayName("徽章类型带展示名与说明")
    void badgeTypesExposeText() {
        assertThat(BadgeType.values()).hasSize(6);
        assertThat(BadgeType.FIRST_ANSWER.displayName()).isEqualTo("初来乍到");
        assertThat(BadgeType.FIRST_ANSWER.description()).contains("第一个回答");
        assertThat(BadgeType.REPUTATION_100.description()).contains("100");
        assertThat(BadgeType.HELPER_10.displayName()).isEqualTo("解惑达人");
    }
}
