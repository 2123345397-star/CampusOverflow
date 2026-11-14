/*
 * Sprint 2 已实现 on(AnswerAcceptedEvent) / on(VoteCastEvent)。
 * on(UserRegisteredEvent)、on(AnswerSubmittedEvent)、on(AnswerEndorsedEvent) 仍为行走骨架，
 * 待 A/后续任务补齐；tech-debt Issue #44：https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/issues/44
 */
package com.campusoverflow.reputation.application;

import com.campusoverflow.identity.api.event.UserRegisteredEvent;
import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.AnswerEndorsedEvent;
import com.campusoverflow.qa.api.event.AnswerSubmittedEvent;
import com.campusoverflow.qa.api.event.VoteCastEvent;
import com.campusoverflow.reputation.domain.BadgePolicy;
import com.campusoverflow.reputation.domain.BadgeType;
import com.campusoverflow.reputation.domain.LedgerRepository;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.reputation.domain.ReputationReason;
import com.campusoverflow.reputation.domain.ReputationRulebook;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订阅上游上下文的集成事件（由 OutboxRelay 在事务提交后投递，至少一次语义）。
 * 每个处理器运行在自己的事务中，并通过账本唯一键 / 徽章存在性检查实现幂等。
 */

@Component
public class ReputationEventHandlers {

    private final ReputationAccountRepository accounts;
    private final LedgerRepository ledger;
    private final ReputationLedgerService ledgerService;
    private final BadgeService badges;
    private final BountyService bounties;
    private final Clock clock;


    public ReputationEventHandlers(ReputationAccountRepository accounts, LedgerRepository ledger,
                                   ReputationLedgerService ledgerService, BadgeService badges,
                                   BountyService bounties, Clock clock) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.ledgerService = ledgerService;
        this.badges = badges;
        this.bounties = bounties;
        this.clock = clock;
    }

    @EventListener
    @Transactional
    public void on(UserRegisteredEvent e) {
        // TODO(S3)：实现 ReputationEventHandlers.on——参考领域单元测试与 arc42 文档中的业务规则
        System.out.println("[walking-skeleton] Reputation 暂未实现，忽略事件 " + e.eventId());
    }

    /** 场景 R-2 的异步后半段：采纳 → 回答者 +15 → 徽章 → 悬赏结算。 */
    @EventListener
    @Transactional
    public void on(AnswerAcceptedEvent e) {
        int delta = ReputationRulebook.acceptedDelta(e.answerAuthorId(), e.questionAuthorId());
        ledgerService.record(e.answerAuthorId(), e.eventId(), ReputationReason.ANSWER_ACCEPTED, delta,
                "ANSWER", e.answerId());
        long acceptedCount = ledger.countByUserAndReason(e.answerAuthorId(), ReputationReason.ANSWER_ACCEPTED);
        BadgePolicy.forAcceptedCount(acceptedCount)
                .forEach(type -> badges.grant(e.answerAuthorId(), type));
        bounties.settleOnAccept(e.questionId(), e.answerId(), e.answerAuthorId(), e.eventId());
    }

    @EventListener
    @Transactional
    public void on(VoteCastEvent e) {
        int delta = ReputationRulebook.voteDelta(e.oldValue(), e.newValue());
        if (delta == 0) {
            return;
        }
        ReputationReason reason = "QUESTION".equalsIgnoreCase(e.targetType())
                ? ReputationReason.QUESTION_VOTE : ReputationReason.ANSWER_VOTE;
        LocalDate today = LocalDate.now(clock.withZone(ZoneOffset.UTC));
        int gainedToday = ledger.sumPositiveVoteDeltaSince(e.targetAuthorId(),
                today.atStartOfDay(ZoneOffset.UTC).toInstant());
        int capped = ReputationRulebook.applyDailyCap(delta, gainedToday);
        ledgerService.record(e.targetAuthorId(), e.eventId(), reason, capped, e.targetType(), e.targetId());
    }

    @EventListener
    @Transactional
    public void on(AnswerSubmittedEvent e) {
        // TODO(S3)：实现 ReputationEventHandlers.on——参考领域单元测试与 arc42 文档中的业务规则
        System.out.println("[walking-skeleton] Reputation 暂未实现，忽略事件 " + e.eventId());
    }

    @EventListener
    @Transactional
    public void on(AnswerEndorsedEvent e) {
        // TODO(S3)：实现 ReputationEventHandlers.on——参考领域单元测试与 arc42 文档中的业务规则
        System.out.println("[walking-skeleton] Reputation 暂未实现，忽略事件 " + e.eventId());
    }
}
