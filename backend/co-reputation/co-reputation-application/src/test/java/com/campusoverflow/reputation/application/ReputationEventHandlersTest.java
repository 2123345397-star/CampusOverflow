package com.campusoverflow.reputation.application;

import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.reputation.domain.*;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReputationEventHandlersTest {

    // 1. 把 Handler 需要的 6 个依赖全部 Mock 出来
    @Mock
    private ReputationAccountRepository accounts;

    @Mock
    private LedgerRepository ledger;

    @Mock
    private ReputationLedgerService ledgerService;

    @Mock
    private BadgeService badges;

    @Mock
    private BountyService bounties;

    @Mock
    private Clock clock;

    @Mock
    private IntegrationEventPublisher events;

    // 2. 将这些假依赖注入到我们要测试的 Handler 中
    @InjectMocks
    private ReputationEventHandlers handler;

    @Test
    void repeatedAnswerAcceptedEventShouldOnlyApplyOnce() {
        // 1. 构造一个事件 (注意：下面的参数需要根据你项目中 AnswerAcceptedEvent 的真实定义来填)
        // 假设它需要 8 个参数：事件ID, 发生时间, 问题ID, 提问者ID, 回答ID, 回答者ID, 采纳者ID, 问题标题
        AnswerAcceptedEvent event = new AnswerAcceptedEvent(
                "evt-123", Instant.now(), 1L, 2L, 3L, 4L, 5L, "测试题目"
        );

        // 2. 模拟时钟
        when(clock.instant()).thenReturn(Instant.now());

        // 3. 第一次调用（模拟正常处理）：假设账本里没有这个事件
        // 注意：这里的 exists 参数要和 Handler 里实际调用的参数类型匹配
        when(ledger.exists(eq("evt-123"), anyLong(), any())).thenReturn(false);

        handler.on(event);

        // 4. 第二次调用（模拟事件重发）：此时账本里已经有这个事件了！
        when(ledger.exists(eq("evt-123"), anyLong(), any())).thenReturn(true);

        handler.on(event);

        // 5. 核心断言：不管调几次，记账方法 append 绝对只能被调用一次！
        // 如果队友的代码没做幂等拦截，这里会验证出 2 次，测试就会失败。
        verify(ledger, times(1)).append(any());
    }
}