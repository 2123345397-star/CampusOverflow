# 1. 架构

> 答辩口径：2 分钟内讲清「为什么是模块化单体 + 六边形分层」「4 个上下文怎么协作」「事件如何保证最终一致」。

## 1.1 一句话架构

CampusOverflow 是 **模块化单体（Modular Monolith）+ 六边形分层**：按 4 个限界上下文物理分 Maven 模块，每个上下文内部再分 `domain / application / infrastructure / interfaces` 四层；跨上下文只通过 `api` 契约或领域事件集成，为后续拆分服务保留边界。

## 1.2 模块结构

```text
backend/pom.xml（父 POM：依赖版本、JaCoCo、覆盖率门槛 0.70）
├── co-shared-kernel      Actor / 领域异常 / 分页 / 事件契约（共享内核）
├── co-bootstrap          启动入口、安全链、Flyway、Outbox 中继、可观测性装配
├── co-identity           Identity：用户、课程、角色、会话（参考实现）
├── co-qa                 Q&A：问题、回答、投票、评论、采纳
├── co-reputation         Reputation：账户、账本、徽章、悬赏、排行榜
└── co-discovery          Discovery：搜索投影、相关问题、通知、排行榜读模型
```

每个上下文内部固定四层：

| 层 | 职责 | 约束 |
| --- | --- | --- |
| `domain` | 聚合、值对象、领域服务、仓储接口 | 零 Spring 依赖，纯 Java + JUnit |
| `application` | 用例编排、事务边界、事件发布 | 只依赖 domain + api，测试用 fake 仓储 |
| `infrastructure` | JPA/JDBC/Redis 适配器、仓储实现、定时任务 | 实现 domain 端口 |
| `interfaces` | REST 控制器、请求/响应 DTO、OpenAPI | 不写业务规则，权限来自 `permissions` |

`ArchitectureTest`（ArchUnit，FF-4）强制分层：domain 不得依赖 application/infrastructure/interfaces，跨上下文不得直接引用对方 domain。

## 1.3 四个限界上下文

| 上下文 | 模块 | 职责 | 对外契约 |
| --- | --- | --- | --- |
| Identity | `co-identity` | 用户、课程、成员角色、会话、CSRF | `IdentityApi`（同步）、`UserRegisteredEvent`（异步） |
| Q&A | `co-qa` | 问题/回答/投票/评论/采纳、软删除 | `QaQueryApi`（同步）、`QuestionPosted`/`AnswerSubmitted`/`VoteCast`/`AnswerAccepted` 等事件 |
| Reputation | `co-reputation` | 声誉账户与账本、徽章、悬赏、排行榜 | `ReputationApi`；消费 Q&A 事件，发 `ReputationChanged`/`BadgeAwarded`/`Bounty*` |
| Discovery | `co-discovery` | 搜索索引、相关问题、通知、排行榜读模型 | `SearchQuery`/`SearchPort`；消费 Q&A/Reputation 事件 |

上下文映射与事件风暴见：

- [`docs/ddd/context-map.md`](../ddd/context-map.md) / [`context-map.png`](../ddd/context-map.png)
- [`docs/ddd/event-storming.png`](../ddd/event-storming.png)（4 上下文 / 30 事件）
- [`docs/ddd/glossary.md`](../ddd/glossary.md)（统一语言）

## 1.4 关键流程

**R-2 采纳最佳答案（同步 + 异步）**

```text
PUT /api/v1/questions/{id}/accepted-answer
  → AnswerCommandService.accept（Q&A 事务：改 Question.acceptedAnswerId）
  → 写 outbox_event(AnswerAcceptedEvent)
  → OutboxRelay 异步投递
      → ReputationEventHandlers.on(AnswerAccepted)：+15 声誉、徽章、悬赏结算
      → Discovery 投影：搜索文档/通知
```

**最终一致与幂等**

- 事务性发件箱（ADR-002）：业务数据与事件同事务写入 `outbox_event`；
- 消费端按 `(event_id, user_id, reason)`（账本）/ `processed_event`（投影）去重，至少一次投递只生效一次；
- 对账 FF-8：`GET /api/v1/admin/reputation/reconciliation` 校验「账户余额 = 流水之和」。

**搜索**

- MySQL ngram 全文索引（ADR-005）；`SearchIndexProjector` 消费 Question/Answer 事件维护 `dis_search_doc`；
- `DiscoveryQueryService` 通过 `SearchPort` 查询并批量取作者，避免 N+1；单字关键词返回 422 `DIS-1001`。

## 1.5 可观测性

- Actuator 暴露 `/actuator/prometheus`（`health,info,metrics,prometheus`）；
- 自定义指标：`co_outbox_backlog`、`co_outbox_dead_letters`、`co_reputation_mismatches`；
- Grafana 四面板见 [`deploy/grafana/`](../../deploy/grafana/)；Prometheus 抓取配置 [`deploy/prometheus.yml`](../../deploy/prometheus.yml)。

## 1.6 答辩可能被问到的三点

1. **为什么不用微服务？** 课程规模与团队容量下，模块化单体降低分布式事务/运维成本；边界用 Maven 模块 + ArchUnit + 契约守住，后续可按上下文拆分。
2. **跨上下文一致性怎么保证？** 本地事务 + outbox + 消费端幂等 + 对账（FF-8），不追求强一致。
3. **领域层为什么零框架依赖？** 单元测试毫秒级、规则可读、可替换持久化；JPA 实体留在 infrastructure 并与领域对象分离（ADR-008）。
