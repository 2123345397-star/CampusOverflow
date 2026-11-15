package com.campusoverflow.reputation.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.IsoFields;

/**
 * 排行榜周期键。学期划分（可按学校校历调整）：2–7 月为春季学期，8–12 月与次年 1 月为秋季学期。
 */
public final class AcademicTerm {

    public static final ZoneId CAMPUS_ZONE = ZoneId.of("Asia/Shanghai");

    private AcademicTerm() {
    }

    /**
     * 学期键：{@code YYYY-SPRING} / {@code YYYY-FALL}。
     *
     * <p>要点：<b>1 月属于上一年的秋季学期</b>——寒假是上学期的一部分，
     * 所以 2027-01 要算成 {@code 2026-FALL}，而不是 {@code 2027-SPRING}。</p>
     */
    public static String termKey(Instant at) {
        LocalDate date = LocalDate.ofInstant(at, CAMPUS_ZONE);
        int year = date.getYear();
        int month = date.getMonthValue();
        if (month == 1) {
            return (year - 1) + "-FALL";
        }
        if (month <= 7) {
            return year + "-SPRING";
        }
        return year + "-FALL";
    }

    /**
     * 周键：{@code YYYY-Www}，如 {@code 2026-W41}。
     *
     * <p>要点：年份必须取 <b>ISO 周年（week-based-year）</b>而不是日历年。
     * 跨年的那一周（如 12 月 31 日恰好属于次年第 1 周）两者会差一年，
     * 所以用 {@link IsoFields#WEEK_BASED_YEAR}，周序两位补零。</p>
     */
    public static String weekKey(Instant at) {
        LocalDate date = LocalDate.ofInstant(at, CAMPUS_ZONE);
        int weekBasedYear = date.get(IsoFields.WEEK_BASED_YEAR);
        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return String.format("%d-W%02d", weekBasedYear, week);
    }

    /** 月键：{@code YYYY-MM}，如 {@code 2026-10}。 */
    public static String monthKey(Instant at) {
        LocalDate date = LocalDate.ofInstant(at, CAMPUS_ZONE);
        return String.format("%d-%02d", date.getYear(), date.getMonthValue());
    }
}
