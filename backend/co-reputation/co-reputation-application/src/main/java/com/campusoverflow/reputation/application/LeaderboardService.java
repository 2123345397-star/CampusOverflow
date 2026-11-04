package com.campusoverflow.reputation.application;

import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.reputation.api.event.ReputationChangedEvent;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.shared.event.ProcessedEventStore;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 排行榜：周/月/学期榜来自 Redis ZSET，总榜直接读账户表。 */
@Service
public class LeaderboardService {

    private final LeaderboardStore store;
    private final ReputationAccountRepository accounts;
    private final IdentityApi identity;
    private final ProcessedEventStore processed;
    private final Clock clock;

    public LeaderboardService(LeaderboardStore store, ReputationAccountRepository accounts, IdentityApi identity,
                              ProcessedEventStore processed, Clock clock) {
        this.store = store;
        this.accounts = accounts;
        this.identity = identity;
        this.processed = processed;
        this.clock = clock;
    }

    /**
     * 声誉变动 → 写入周/月/学期三个 ZSET。
     *
     * <p>幂等：outbox 可能重复投递同一事件，先用 ProcessedEventStore 占位，
     * 只有首次处理返回 true 时才累加；消费者的名字 "reputation.leaderboard" 是幂等键的一部分，
     * 与同模块其他监听器互不干扰。</p>
     */
    @EventListener
    @Transactional
    public void on(ReputationChangedEvent e) {
        if (!processed.markProcessed(e.eventId(), "reputation.leaderboard")) {
            return;
        }
        store.increment(e.userId(), e.delta(), e.occurredAt());
    }

    /**
     * 排行榜 Top N。总榜（ALL）读账户表，其余周期读 Redis ZSET；
     * 昵称与认证标记通过 IdentityApi 批量取，避免 N+1。
     */
    @Transactional(readOnly = true)
    public List<LeaderboardEntryView> top(LeaderboardPeriod period, int limit) {
        int size = Math.max(1, Math.min(limit, 100));
        if (period == LeaderboardPeriod.ALL) {
            return viewsOf(accounts.topByReputation(size));
        }
        List<LeaderboardStore.Entry> entries = store.top(period, clock.instant(), size);
        if (entries.isEmpty()) {
            return List.of();
        }
        Map<Long, UserSummary> users = identity.findUsers(
                entries.stream().map(LeaderboardStore.Entry::userId).toList());
        List<LeaderboardEntryView> views = new ArrayList<>(entries.size());
        int rank = 1;
        for (LeaderboardStore.Entry entry : entries) {
            UserSummary user = users.get(entry.userId());
            views.add(new LeaderboardEntryView(rank++, entry.userId(), displayNameOf(user),
                    user != null && user.verified(), entry.score()));
        }
        return views;
    }

    /** 总榜：分数取账户的累计声誉值。 */
    private List<LeaderboardEntryView> viewsOf(List<ReputationAccount> topAccounts) {
        if (topAccounts.isEmpty()) {
            return List.of();
        }
        Map<Long, UserSummary> users = identity.findUsers(
                topAccounts.stream().map(ReputationAccount::userId).toList());
        List<LeaderboardEntryView> views = new ArrayList<>(topAccounts.size());
        int rank = 1;
        for (ReputationAccount account : topAccounts) {
            UserSummary user = users.get(account.userId());
            views.add(new LeaderboardEntryView(rank++, account.userId(), displayNameOf(user),
                    user != null && user.verified(), account.reputation()));
        }
        return views;
    }

    /** 账号被注销时仍然占位显示，不让榜单出现空洞。 */
    private static String displayNameOf(UserSummary user) {
        return user == null ? "已注销用户" : user.displayName();
    }
}
