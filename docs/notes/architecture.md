# CampusOverflow 架构理解笔记

> 负责人：青岑（Dev-1）
> 依据：arc42 第 2/3/5/6 章、ADR-001～008、《实验指导书 V3》第 2～3 章

## 1. 一句话架构

模块化单体 + 六边形分层：按 4 个限界上下文物理分模块，每个上下文内部再分 domain / application / infrastructure / interfaces 四层；跨上下文只通过 api 或领域事件集成。

## 2. 模块结构
backend/pom.xml（父 POM）
├── co-shared-kernel：Actor、DomainEvent、领域异常、分页
├── co-bootstrap：启动入口、装配、安全链、Flyway
├── co-identity：用户、课程、角色、会话（完整参考实现）
├── co-qa：问题、回答、投票、评论、采纳
├── co-reputation：声誉账本、徽章、悬赏
└── co-discovery：搜索投影、通知、排行榜

每个上下文内部：

- `co-xxx-api`：发布语言（DTO、事件契约）
- `co-xxx-domain`：聚合、值对象、领域服务、仓储接口
- `co-xxx-application`：用例编排、事务边界、事件发布
- `co-xxx-infrastructure`：JPA / Redis / JDBC 适配器、仓储实现
- `co-xxx-interfaces`：REST 控制器、请求/响应 DTO

## 3. 分层规则

| 层 | 允许依赖 | 禁止 |
|---|---|---|
| domain | JDK、shared-kernel | 框架、Spring、JPA、其他上下文 |
| application | domain、api、shared-kernel | 直接依赖 infrastructure 实现 |
| infrastructure | domain 端口、JPA/Redis | 被 domain 反向依赖 |
| interfaces | application、api | 在控制器里写业务规则 |

依赖方向：`interfaces → application → domain`；infrastructure 实现 domain 端口，由 Spring 注入。

## 4. 上下文边界

- Identity → Q&A / Reputation / Discovery：OHS/PL，同步 IdentityApi + UserRegisteredEvent；下游只拿 UserSummary。
- Q&A → Reputation：客户/供应商，Answer* / VoteCast 事件 + QaQueryApi.findQuestion。
- Q&A → Discovery：遵奉者，Question* / Answer* / Vote / Comment / ContentDeleted 事件。
- Reputation → Discovery：遵奉者，Bounty* / BadgeAwarded 事件。
- 全部依赖 `co-shared-kernel`；修改共享内核需全组评审。

## 5. 关键机制

- **事务性发件箱**：业务数据与 `outbox_event` 同事务；中继异步投递；消费端按 `(event_id, user_id, reason)` 或 `processed_event` 幂等。
- **服务端会话**：Spring Session + Redis；SESSION Cookie 设置 HttpOnly、SameSite=Lax；CSRF 双提交；登录后轮换会话 ID；不用 JWT（ADR-003）。
- **软删除**：ContentDeletedEvent；读模型过滤 `deleted_at`。
- **原子计数**：score / answerCount / viewCount 由仓储原子 SQL 更新，不整行回写（ADR-008）。
- **Flyway**：每个上下文独立迁移目录；`ddl-auto: none`。
- **读模型**：Discovery 的 SearchDocument / Notification / Leaderboard 由事件投影维护。

## 6. 为什么这样分

- 业务边界清晰：4 个上下文对应 4 个业务能力，避免大泥球。
- 可测试：domain 零框架依赖，单元测试毫秒级；ArchUnit 强制约束。
- 可演进：跨上下文只经 api / 事件，后续可拆服务。
- 并发安全：聚合只引用 ID，不直接持有其他聚合对象。

## 7. 新同学阅读顺序

1. `docs/adr/` 8 条 ADR
2. `backend/co-identity`（完整参考实现）
3. `co-qa-domain` 的领域测试（需求说明书）
4. `docs/ddd/` 的事件、上下文映射、聚合与不变式
5. `ArchitectureTest`（架构适应度函数）