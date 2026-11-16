package com.campusoverflow.reputation.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * 徽章授予规则（纯函数，易于测试与扩展）。
 *
 * <p>阈值是「累计达到即授予」的单调规则：达到高阈值时低阈值徽章一并具备，
 * 因此用 {@link EnumSet} 一次装齐，重复调用结果一致（幂等），
 * 上层即便重复投递事件也不会产生重复徽章。</p>
 */
public final class BadgePolicy {

    /** 被采纳 1 次 → 初露锋芒 */
    private static final long FIRST_ACCEPTED_THRESHOLD = 1;
    /** 被采纳 10 次 → 解惑达人 */
    private static final long HELPER_THRESHOLD = 10;
    /** 声誉 100 → 小有名气 */
    private static final int REPUTATION_100_THRESHOLD = 100;
    /** 声誉 1000 → 德高望重 */
    private static final int REPUTATION_1000_THRESHOLD = 1000;

    private BadgePolicy() {
    }

    /**
     * 按「回答被采纳次数」应得的徽章。
     *
     * <p>入参为负数（异常数据）时按 0 处理，返回空集而不是抛异常——
     * 这是查询类纯函数，宁可直接给出「没有徽章」也不要打断调用方。</p>
     */
    public static Set<BadgeType> forAcceptedCount(long acceptedCount) {
        EnumSet<BadgeType> badges = EnumSet.noneOf(BadgeType.class);
        if (acceptedCount < FIRST_ACCEPTED_THRESHOLD) {
            return badges;
        }
        badges.add(BadgeType.FIRST_ACCEPTED);
        if (acceptedCount >= HELPER_THRESHOLD) {
            badges.add(BadgeType.HELPER_10);
        }
        return badges;
    }

    /** 按「声誉值」应得的徽章；同样单调累加，达到 1000 时两个都返回。 */
    public static Set<BadgeType> forReputation(int reputation) {
        EnumSet<BadgeType> badges = EnumSet.noneOf(BadgeType.class);
        if (reputation >= REPUTATION_100_THRESHOLD) {
            badges.add(BadgeType.REPUTATION_100);
        }
        if (reputation >= REPUTATION_1000_THRESHOLD) {
            badges.add(BadgeType.REPUTATION_1000);
        }
        return badges;
    }
}
