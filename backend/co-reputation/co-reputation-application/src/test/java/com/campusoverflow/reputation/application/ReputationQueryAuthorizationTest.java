package com.campusoverflow.reputation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.reputation.domain.BadgeType;
import com.campusoverflow.reputation.domain.ReputationReason;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 角色 A（徽章与声誉查询）的授权口径——角色卡第四节与验收表里的「查询越权」「他人看不到积分」。
 *
 * <p>开发期这几条只能靠人工点页面确认，退化成回归也就没人管了，所以落成用例：</p>
 * <ul>
 *   <li>流水：仅本人与管理员可看，其余（含未登录）一律 {@code REP-3002} → 403；</li>
 *   <li>档案：声誉值与徽章公开，{@code availablePoints} / {@code frozenPoints} 只有本人与管理员拿得到；</li>
 *   <li>用户不存在：{@code ID-4001} → 404，而不是返回一份 0 分档案。</li>
 * </ul>
 */
class ReputationQueryAuthorizationTest {

    private final ReputationTestFixtures fx = new ReputationTestFixtures();
    private final ReputationQueryService queries =
            new ReputationQueryService(fx.accounts, fx.ledger, fx.badges, fx.identity);

    private final Actor author = fx.student(2, "刘俊琒");
    private final Actor classmate = fx.student(3, "同班同学");
    private final Actor admin = fx.actor(99, "系统管理员", Role.ADMIN);
    private final Actor teacher = fx.teacher(7);

    @Test
    @DisplayName("本人可以看自己的流水，条目的原因、增量、引用都对得上")
    void ownerCanReadOwnLedger() {
        fx.seedAccount(2, 15, 0, 0);
        fx.ledgerService.record(2, "evt-accepted-1", ReputationReason.ANSWER_ACCEPTED, 15, "ANSWER", 42L);

        var page = queries.ledger(2, author, new PageRequest(1, 20));

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.items()).singleElement().satisfies(entry -> {
            assertThat(entry.delta()).isEqualTo(15);
            assertThat(entry.reason()).isEqualTo(ReputationReason.ANSWER_ACCEPTED.name());
            assertThat(entry.reasonLabel()).isEqualTo(ReputationReason.ANSWER_ACCEPTED.label());
            assertThat(entry.refType()).isEqualTo("ANSWER");
            assertThat(entry.refId()).isEqualTo(42L);
        });
    }

    @Test
    @DisplayName("同学看别人的流水 → 403 REP-3002")
    void classmateCannotReadOthersLedger() {
        fx.seedAccount(2, 15, 0, 0);
        fx.ledgerService.record(2, "evt-accepted-1", ReputationReason.ANSWER_ACCEPTED, 15, "ANSWER", 42L);

        assertThatThrownBy(() -> queries.ledger(2, classmate, new PageRequest(1, 20)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("只能查看自己的声誉流水")
                .satisfies(e -> {
                    assertThat(((ForbiddenException) e).code()).isEqualTo("REP-3002");
                    assertThat(((ForbiddenException) e).suggestedStatus()).isEqualTo(403);
                });
    }

    @Test
    @DisplayName("未登录（viewer 为 null）也不能看，不能靠「没有身份」绕过")
    void anonymousCannotReadLedger() {
        fx.seedAccount(2, 15, 0, 0);
        fx.ledgerService.record(2, "evt-accepted-1", ReputationReason.ANSWER_ACCEPTED, 15, "ANSWER", 42L);

        assertThatThrownBy(() -> queries.ledger(2, null, new PageRequest(1, 20)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("只能查看自己的声誉流水");
    }

    @Test
    @DisplayName("管理员可以看任何人的流水（申诉、对账要查）")
    void adminCanReadAnyLedger() {
        fx.seedAccount(2, 15, 0, 0);
        fx.ledgerService.record(2, "evt-accepted-1", ReputationReason.ANSWER_ACCEPTED, 15, "ANSWER", 42L);

        var page = queries.ledger(2, admin, new PageRequest(1, 20));

        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).delta()).isEqualTo(15);
    }

    @Test
    @DisplayName("教师不是管理员，看别人流水同样 403")
    void teacherIsNotAdminForLedger() {
        fx.seedAccount(2, 15, 0, 0);

        assertThatThrownBy(() -> queries.ledger(2, teacher, new PageRequest(1, 20)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("只能查看自己的声誉流水");
    }

    @Test
    @DisplayName("他人档案：声誉与徽章公开，但两个积分字段为 null（前端显示「—」而不是 0）")
    void otherProfileHidesPointsButShowsReputationAndBadges() {
        fx.seedAccount(2, 130, 85, 15);
        fx.badgeService.grant(2, BadgeType.REPUTATION_100);

        var profile = queries.profile(2, classmate);

        assertThat(profile.reputation()).isEqualTo(130);
        assertThat(profile.availablePoints()).isNull();
        assertThat(profile.frozenPoints()).isNull();
        assertThat(profile.displayName()).isEqualTo("刘俊琒");
        assertThat(profile.badges()).extracting(ReputationProfileView.BadgeView::code)
                .containsExactly("REPUTATION_100");
        assertThat(profile.badges().get(0).name()).isEqualTo(BadgeType.REPUTATION_100.displayName());
    }

    @Test
    @DisplayName("本人档案：两个积分字段都在")
    void ownerProfileShowsPoints() {
        fx.seedAccount(2, 130, 85, 15);

        var profile = queries.profile(2, author);

        assertThat(profile.availablePoints()).isEqualTo(85);
        assertThat(profile.frozenPoints()).isEqualTo(15);
    }

    @Test
    @DisplayName("管理员看他人档案也能拿到积分（客服排障要能对账）")
    void adminProfileShowsOthersPoints() {
        fx.seedAccount(2, 130, 85, 15);

        var profile = queries.profile(2, admin);

        assertThat(profile.availablePoints()).isEqualTo(85);
        assertThat(profile.frozenPoints()).isEqualTo(15);
    }

    @Test
    @DisplayName("用户不存在 → 404 ID-4001，不要返回一份 0 分档案误导前端")
    void unknownUserIsNotFound() {
        assertThatThrownBy(() -> queries.profile(404404, classmate))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("用户不存在")
                .satisfies(e -> assertThat(((NotFoundException) e).code()).isEqualTo("ID-4001"));
    }
}
