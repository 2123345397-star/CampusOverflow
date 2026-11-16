package com.campusoverflow.reputation.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusoverflow.identity.api.event.UserRegisteredEvent;
import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.AnswerEndorsedEvent;
import com.campusoverflow.qa.api.event.AnswerSubmittedEvent;
import com.campusoverflow.qa.api.event.VoteCastEvent;
import com.campusoverflow.reputation.api.event.ReputationChangedEvent;
import com.campusoverflow.reputation.domain.BadgeAward;
import com.campusoverflow.reputation.domain.BadgeType;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationReason;
import com.campusoverflow.shared.security.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * S2 验收重点：同一事件重复投递（至少一次语义 / outbox 重放）时，
 * 账户余额、账本流水、徽章都只能生效一次。
 *
 * <p>每个用例都调用 handler 两次，然后断言“只发生一次”。</p>
 */
class ReputationEventHandlersTest {

    private final ReputationTestFixtures fx = new ReputationTestFixtures();

    @Test
    @DisplayName("UserRegistered 重复投递：只开一个账户")
    void userRegisteredCalledTwiceOnlyOnce() {
        UserRegisteredEvent event = new UserRegisteredEvent("evt-user-1", fx.clock.instant(), 7, "小明", Role.STUDENT);

        fx.handlers.on(event);
        fx.handlers.on(event);

        assertThat(fx.accounts.findAll()).hasSize(1);
        ReputationAccount account = fx.accounts.findByUserId(7).orElseThrow();
        assertThat(account.reputation()).isZero();
        assertThat(account.availablePoints()).isZero();
    }

    @Test
    @DisplayName("教师注册：开户即带 1000 悬赏积分，重放不会翻倍")
    void teacherRegistrationGrantsInitialPointsOnce() {
        UserRegisteredEvent event = new UserRegisteredEvent("evt-user-2", fx.clock.instant(), 9, "王老师", Role.TEACHER);

        fx.handlers.on(event);
        fx.handlers.on(event);

        ReputationAccount account = fx.accounts.findByUserId(9).orElseThrow();
        assertThat(account.availablePoints()).isEqualTo(1000);
        assertThat(fx.accounts.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("AnswerSubmitted 重复投递：只发一枚「初来乍到」")
    void answerSubmittedCalledTwiceOnlyOnce() {
        AnswerSubmittedEvent event = new AnswerSubmittedEvent("evt-ans-1", fx.clock.instant(),
                50, 10, 2, 1, "红黑树插入后为什么必须旋转？");

        fx.handlers.on(event);
        fx.handlers.on(event);

        assertThat(fx.badges.findByUser(2)).extracting(BadgeAward::type)
                .containsExactly(BadgeType.FIRST_ANSWER);
    }

    @Test
    @DisplayName("AnswerEndorsed 重复投递：只发一枚「名师认可」")
    void answerEndorsedCalledTwiceOnlyOnce() {
        AnswerEndorsedEvent event = new AnswerEndorsedEvent("evt-end-1", fx.clock.instant(),
                50, 10, 2, 9, "红黑树插入后为什么必须旋转？");

        fx.handlers.on(event);
        fx.handlers.on(event);

        assertThat(fx.badges.findByUser(2)).extracting(BadgeAward::type)
                .containsExactly(BadgeType.TEACHER_ENDORSED);
    }

    @Test
    @DisplayName("AnswerAccepted 重复投递：账本一条、账户 +15 一次、徽章一枚")
    void answerAcceptedCalledTwiceOnlyOnce() {
        fx.seedAccount(2, 0, 0, 0);
        AnswerAcceptedEvent event = new AnswerAcceptedEvent("evt-acc-1", fx.clock.instant(),
                50, 10, 2, 1, 1, "红黑树插入后为什么必须旋转？");

        fx.handlers.on(event);
        fx.handlers.on(event);

        assertThat(fx.ledger.countByUserAndReason(2, ReputationReason.ANSWER_ACCEPTED)).isEqualTo(1);
        assertThat(fx.ledger.size()).isEqualTo(1);
        assertThat(fx.accounts.findByUserId(2).orElseThrow().reputation()).isEqualTo(15);
        assertThat(fx.badges.findByUser(2)).extracting(BadgeAward::type)
                .containsExactly(BadgeType.FIRST_ACCEPTED);
        assertThat(fx.events.ofType(ReputationChangedEvent.class)).hasSize(1);
    }

    @Test
    @DisplayName("VoteCast 重复投递：只记一次 +10")
    void voteCastCalledTwiceOnlyOnce() {
        fx.seedAccount(2, 0, 0, 0);
        VoteCastEvent event = new VoteCastEvent("evt-vote-1", fx.clock.instant(),
                "ANSWER", 50, 2, 1, 10, 0, 1);

        fx.handlers.on(event);
        fx.handlers.on(event);

        assertThat(fx.ledger.countByUserAndReason(2, ReputationReason.ANSWER_VOTE)).isEqualTo(1);
        assertThat(fx.ledger.size()).isEqualTo(1);
        assertThat(fx.accounts.findByUserId(2).orElseThrow().reputation()).isEqualTo(10);
    }
}
