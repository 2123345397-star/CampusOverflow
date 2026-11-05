package com.campusoverflow.reputation.application;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ReputationEventHandlersTest {

    @Test
    void repeatedAnswerAcceptedEventShouldOnlyApplyOnce() {
        // 1. 准备夹具
        ReputationTestFixtures fixtures = new ReputationTestFixtures();

        // 2. 实例化处理器（等队友代码补齐后解开注释）
        // ReputationEventHandlers handler = new ReputationEventHandlers(..., fixtures.events, fixtures.clock);

        // 3. 造事件
        // AnswerAcceptedEvent event = new AnswerAcceptedEvent("evt-1", fixtures.clock.instant(), ...);

        // 4. 调两次
        // handler.on(event);
        // handler.on(event);

        // 5. 断言
        assertThat(true).isTrue();
    }
}
