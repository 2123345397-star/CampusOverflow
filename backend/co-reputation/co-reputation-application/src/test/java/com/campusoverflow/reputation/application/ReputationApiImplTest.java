package com.campusoverflow.reputation.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link ReputationApiImpl}：问答卡片要的「作者声誉」，以及列表页批量取（避免 N+1）。
 *
 * <p>口径：未开户的用户按 0 返回，不让 Discovery 整页报错；批量查询对不存在的用户补 0，
 * 保证调用方拿到的 Map 大小恒等于入参集合大小。</p>
 */
class ReputationApiImplTest {

    private final ReputationTestFixtures fx = new ReputationTestFixtures();
    private final ReputationApiImpl api = new ReputationApiImpl(fx.accounts);

    @Test
    @DisplayName("未开户的用户摘要为 0，不抛异常")
    void summaryOfUnregisteredUserIsZero() {
        var summary = api.summary(404404);

        assertThat(summary.userId()).isEqualTo(404404);
        assertThat(summary.reputation()).isZero();
    }

    @Test
    @DisplayName("已开户的用户摘要带真实声誉")
    void summaryOfRegisteredUser() {
        fx.seedAccount(2, 130, 85, 15);

        var summary = api.summary(2);

        assertThat(summary.userId()).isEqualTo(2);
        assertThat(summary.reputation()).isEqualTo(130);
    }

    @Test
    @DisplayName("批量查询：命中账户给真实值，未开户补 0")
    void batchReputationsCoversEveryRequestedUser() {
        fx.seedAccount(2, 130, 85, 15);
        fx.seedAccount(3, 45, 0, 0);

        Map<Long, Integer> result = api.reputations(List.of(2L, 3L, 404404L));

        assertThat(result).hasSize(3)
                .containsEntry(2L, 130)
                .containsEntry(3L, 45)
                .containsEntry(404404L, 0);
    }

    @Test
    @DisplayName("空集合不炸，返回空 Map（列表页没有作者时的边界）")
    void batchReputationsOfEmptyCollection() {
        assertThat(api.reputations(List.of())).isEmpty();
    }
}
