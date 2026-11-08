package com.campusoverflow.reputation.application;

import com.campusoverflow.shared.event.IntegrationEvent;
import com.campusoverflow.shared.event.IntegrationEventPublisher;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class ReputationTestFixtures {
    // 记录发出的事件
    public final List<IntegrationEvent> publishedEvents = new ArrayList<>();

    // 固定时间，方便断言
    public final Clock clock = Clock.fixed(Instant.parse("2026-10-10T10:00:00Z"), ZoneOffset.UTC);

    // 模拟事件发布器（空实现，只把事件存进列表）
    public final IntegrationEventPublisher events = event -> publishedEvents.add(event);
}
