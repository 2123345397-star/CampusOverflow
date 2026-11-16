package com.campusoverflow.reputation.application;

import com.campusoverflow.identity.api.CourseSummary;
import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.qa.api.QaQueryApi;
import com.campusoverflow.qa.api.QuestionRef;
import com.campusoverflow.reputation.domain.BadgeAward;
import com.campusoverflow.reputation.domain.BadgeRepository;
import com.campusoverflow.reputation.domain.BadgeType;
import com.campusoverflow.reputation.domain.Bounty;
import com.campusoverflow.reputation.domain.BountyRepository;
import com.campusoverflow.reputation.domain.BountyStatus;
import com.campusoverflow.reputation.domain.LedgerEntry;
import com.campusoverflow.reputation.domain.LedgerRepository;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import com.campusoverflow.reputation.domain.ReputationReason;
import com.campusoverflow.shared.audit.AuditTrail;
import com.campusoverflow.shared.event.IntegrationEvent;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicLong;

/**
 * S2 声誉上下文的测试夹具。
 *
 * <p>五个替身（账户、账本、徽章、悬赏、身份）+ 事件/审计记录器 + 可推进的固定时钟，
 * 并通过 {@link #handlers}、{@link #ledgerService}、{@link #bountyService} 暴露组装好的真实服务。
 * 其他成员的测试可以直接 {@code new ReputationTestFixtures()} 后取用，无需再写内存仓储。</p>
 */
public class ReputationTestFixtures {

    /** 可推进的固定时钟：跨日上限、悬赏到期等用例用它推进时间。 */
    public static final class MutableClock extends Clock {

        private Instant now;
        private final ZoneId zone;

        public MutableClock(Instant now) {
            this(now, ZoneOffset.UTC);
        }

        private MutableClock(Instant now, ZoneId zone) {
            this.now = now;
            this.zone = zone;
        }

        public void advance(Duration duration) {
            this.now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId newZone) {
            return new MutableClock(now, newZone);
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    public final MutableClock clock = new MutableClock(Instant.parse("2026-10-10T10:00:00Z"));
    public final InMemoryAccounts accounts = new InMemoryAccounts();
    public final InMemoryLedger ledger = new InMemoryLedger();
    public final InMemoryBadges badges = new InMemoryBadges();
    public final InMemoryBounties bounties = new InMemoryBounties();
    public final RecordingEvents events = new RecordingEvents();
    public final RecordingAudit audit = new RecordingAudit();
    public final StubIdentity identity = new StubIdentity();
    public final StubQa qa = new StubQa();

    public final BadgeService badgeService;
    public final ReputationLedgerService ledgerService;
    public final BountyService bountyService;
    public final ReputationEventHandlers handlers;

    public ReputationTestFixtures() {
        this.badgeService = new BadgeService(badges, events, clock);
        this.ledgerService = new ReputationLedgerService(accounts, ledger, badgeService, events, clock);
        this.bountyService = new BountyService(bounties, accounts, ledgerService, qa, identity, events, audit, clock);
        this.handlers = new ReputationEventHandlers(accounts, ledger, ledgerService, badgeService, bountyService, clock);
    }

    // ---------- 常用构造 ----------

    /** 注册一个用户并返回对应的操作主体（同时登记到 StubIdentity）。 */
    public Actor actor(long userId, String displayName, Role role) {
        identity.users.put(userId, new UserSummary(userId, displayName, role, true, "计算机工程学院"));
        return new Actor(userId, displayName, role);
    }

    /** 造一个教师账户（可管理课程、发起悬赏）。 */
    public Actor teacher(long userId) {
        return actor(userId, "王老师", Role.TEACHER);
    }

    /** 造一个学生操作主体。 */
    public Actor student(long userId, String displayName) {
        return actor(userId, displayName, Role.STUDENT);
    }

    /** 直接落一个已知余额的账户（用于每日上限等“已有历史”的场景）。 */
    public ReputationAccount seedAccount(long userId, int reputation, int availablePoints, int frozenPoints) {
        ReputationAccount account = ReputationAccount.reconstitute(userId, reputation, availablePoints, frozenPoints, 0L);
        accounts.save(account);
        return account;
    }

    /** 预置“今天已经因投票获得 points 点”的流水，配合每日上限用例。 */
    public LedgerEntry seedVoteGain(long userId, int points) {
        LedgerEntry entry = LedgerEntry.of(userId, "seed-vote-" + ledger.size(), ReputationReason.ANSWER_VOTE,
                points, "ANSWER", 0L, clock.instant());
        ledger.append(entry);
        return entry;
    }

    /** 造一个提问者与问题引用（悬赏用例需要课程 ID 判定管理权限）。 */
    public QuestionRef seedQuestion(long questionId, long authorId, Long courseId) {
        QuestionRef ref = new QuestionRef(questionId, authorId, courseId, "红黑树插入后为什么必须旋转？", null, false);
        qa.questions.put(questionId, ref);
        return ref;
    }

    /** 用真实 BountyService 发起悬赏并返回 ID（顺带验证冻结积分）。 */
    public long openBounty(Actor sponsor, long questionId, int points, int days) {
        return bountyService.open(sponsor, questionId, points, days);
    }

    // ---------- 替身实现 ----------

    /** 内存账户仓储。 */
    public static final class InMemoryAccounts implements ReputationAccountRepository {

        private final Map<Long, ReputationAccount> store = new LinkedHashMap<>();

        @Override
        public Optional<ReputationAccount> findByUserId(long userId) {
            return Optional.ofNullable(store.get(userId));
        }

        @Override
        public List<ReputationAccount> findAllByUserId(Collection<Long> userIds) {
            return userIds.stream().map(store::get).filter(Objects::nonNull).toList();
        }

        @Override
        public List<ReputationAccount> topByReputation(int limit) {
            return store.values().stream()
                    .sorted(Comparator.comparingInt(ReputationAccount::reputation).reversed())
                    .limit(limit)
                    .toList();
        }

        @Override
        public List<ReputationAccount> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public ReputationAccount save(ReputationAccount account) {
            store.put(account.userId(), account);
            return account;
        }
    }

    /** 内存账本：按 (eventId, userId, reason) 查重，行为与唯一键一致。 */
    public static final class InMemoryLedger implements LedgerRepository {

        private final List<LedgerEntry> entries = new ArrayList<>();

        @Override
        public boolean exists(String eventId, long userId, ReputationReason reason) {
            return entries.stream().anyMatch(e -> e.eventId().equals(eventId) && e.userId() == userId
                    && e.reason() == reason);
        }

        @Override
        public void append(LedgerEntry entry) {
            entries.add(entry);
        }

        @Override
        public PageResult<LedgerEntry> findByUser(long userId, PageRequest page) {
            List<LedgerEntry> mine = entries.stream()
                    .filter(e -> e.userId() == userId)
                    .sorted(Comparator.comparing(LedgerEntry::createdAt).reversed())
                    .toList();
            int from = Math.min(page.offset(), mine.size());
            int to = Math.min(from + page.size(), mine.size());
            return new PageResult<>(mine.subList(from, to), mine.size(), page.page(), page.size());
        }

        @Override
        public long countByUserAndReason(long userId, ReputationReason reason) {
            return entries.stream().filter(e -> e.userId() == userId && e.reason() == reason).count();
        }

        @Override
        public int sumPositiveVoteDeltaSince(long userId, Instant since) {
            return entries.stream()
                    .filter(e -> e.userId() == userId && e.reason().isVote() && e.delta() > 0
                            && !e.createdAt().isBefore(since))
                    .mapToInt(LedgerEntry::delta)
                    .sum();
        }

        @Override
        public Map<Long, Long> sumByUser() {
            Map<Long, Long> sums = new LinkedHashMap<>();
            for (LedgerEntry entry : entries) {
                sums.merge(entry.userId(), (long) entry.delta(), Long::sum);
            }
            return sums;
        }

        public int size() {
            return entries.size();
        }

        public long sumFor(long userId) {
            return entries.stream().filter(e -> e.userId() == userId).mapToLong(LedgerEntry::delta).sum();
        }

        public List<LedgerEntry> all() {
            return List.copyOf(entries);
        }
    }

    /** 内存徽章仓储：同一 (userId, type) 只保留一枚，天然幂等。 */
    public static final class InMemoryBadges implements BadgeRepository {

        private final List<BadgeAward> awards = new ArrayList<>();

        @Override
        public boolean has(long userId, BadgeType type) {
            return awards.stream().anyMatch(a -> a.userId() == userId && a.type() == type);
        }

        @Override
        public void award(BadgeAward award) {
            if (!has(award.userId(), award.type())) {
                awards.add(award);
            }
        }

        @Override
        public List<BadgeAward> findByUser(long userId) {
            return awards.stream().filter(a -> a.userId() == userId).toList();
        }

        public int size() {
            return awards.size();
        }

        public Set<BadgeType> typesOf(long userId) {
            Set<BadgeType> types = new LinkedHashSet<>();
            findByUser(userId).forEach(a -> types.add(a.type()));
            return types;
        }
    }

    /** 内存悬赏仓储。 */
    public static final class InMemoryBounties implements BountyRepository {

        private final Map<Long, Bounty> store = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public Optional<Bounty> findOpenByQuestion(long questionId) {
            return store.values().stream()
                    .filter(b -> b.questionId() == questionId && b.status() == BountyStatus.OPEN)
                    .findFirst();
        }

        @Override
        public Optional<Bounty> findLatestByQuestion(long questionId) {
            return store.values().stream()
                    .filter(b -> b.questionId() == questionId)
                    .max(Comparator.comparing(Bounty::id));
        }

        @Override
        public List<Bounty> findDue(Instant now, int limit) {
            return store.values().stream().filter(b -> b.isDue(now)).limit(limit).toList();
        }

        @Override
        public Bounty save(Bounty bounty) {
            if (bounty.id() == null) {
                bounty.assignId(sequence.incrementAndGet());
            }
            store.put(bounty.id(), bounty);
            return bounty;
        }

        public Bounty get(long id) {
            return store.get(id);
        }

        public int size() {
            return store.size();
        }
    }

    /** 记录所有发布出去的集成事件。 */
    public static final class RecordingEvents implements IntegrationEventPublisher {

        private final List<IntegrationEvent> published = new ArrayList<>();

        @Override
        public void publish(IntegrationEvent event) {
            published.add(event);
        }

        public <T extends IntegrationEvent> List<T> ofType(Class<T> type) {
            return published.stream().filter(type::isInstance).map(type::cast).toList();
        }

        public List<IntegrationEvent> all() {
            return List.copyOf(published);
        }

        public int size() {
            return published.size();
        }
    }

    /** 记录审计动作。 */
    public static final class RecordingAudit implements AuditTrail {

        public final List<String> actions = new ArrayList<>();
        public final List<String> details = new ArrayList<>();

        @Override
        public void record(String action, Long actorId, String targetType, String targetId, String detail) {
            actions.add(action);
            details.add(detail);
        }
    }

    /** 身份上下文替身：用户目录 + 课程管理权限。 */
    public static final class StubIdentity implements IdentityApi {

        private final Map<Long, UserSummary> users = new LinkedHashMap<>();
        private final Set<Long> courseManagers = new LinkedHashSet<>();

        @Override
        public Optional<UserSummary> findUser(long userId) {
            return Optional.ofNullable(users.get(userId));
        }

        @Override
        public Map<Long, UserSummary> findUsers(Collection<Long> userIds) {
            Map<Long, UserSummary> result = new LinkedHashMap<>();
            userIds.forEach(id -> {
                if (users.containsKey(id)) {
                    result.put(id, users.get(id));
                }
            });
            return result;
        }

        @Override
        public Optional<UserSummary> findByDisplayName(String displayName) {
            return users.values().stream().filter(u -> u.displayName().equals(displayName)).findFirst();
        }

        @Override
        public Optional<CourseSummary> findCourse(long courseId) {
            return Optional.empty();
        }

        @Override
        public boolean canManageCourse(long userId, Long courseId) {
            UserSummary user = users.get(userId);
            if (user == null) {
                return false;
            }
            return user.role() == Role.TEACHER || user.role() == Role.ADMIN || courseManagers.contains(userId);
        }

        public void grantCourseManager(long userId) {
            courseManagers.add(userId);
        }
    }

    /** Q&A 查询替身。 */
    public static final class StubQa implements QaQueryApi {

        private final Map<Long, QuestionRef> questions = new LinkedHashMap<>();

        @Override
        public Optional<QuestionRef> findQuestion(long questionId) {
            return Optional.ofNullable(questions.get(questionId));
        }
    }
}
