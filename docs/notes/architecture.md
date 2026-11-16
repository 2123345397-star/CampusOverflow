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

## 8. 各成员讲解分工与署名（T0-05）

> 验收口径（角色卡全员任务 T0-05）：每人认领 1~2 条 ADR + 一层代码，讲透关键点后在本表署名。

| 成员 | 认领内容 | 关键点（讲什么） | 状态 |
| --- | --- | --- | --- |
| 青岑 | ADR-001 模块化单体 + 全文整理 | 4 上下文物理分模块；domain 零框架依赖；跨上下文只走 api/事件（即第 1~7 节） | 已署名（第 1~7 节） |
| 魏志国 | ADR-002 事务性发件箱 + application 层 | 业务数据与 `outbox_event` 同事务写入；中继异步投递；消费端按 `(event_id, user_id, reason)` / `processed_event` 幂等；`AnswerCommandService` 为典型编排：save → 原子计数 → 发事件 → 失效缓存 | 已整理（2026-11-16） |
| 刘俊琒 | ADR-008 领域与持久化分离 + co-qa-domain | 聚合只引用 ID、不直接持有其他聚合对象；score/answerCount 用原子 SQL 而非整行回写；`Question` 五方法的不变式即领域测试 | 内容已整理（魏志国代整理，待本人复核后讲评署名） |
| 房国栋 | ADR-003 服务端会话 + interfaces 层 | Spring Session + Redis；HttpOnly / SameSite=Lax / CSRF 双提交；控制器不写业务规则，前端按 `permissions` 渲染按钮 | 待整理 |
| 陈明哲 | ArchUnit 架构适应度函数 + ADR-009 渐进门禁 | `ArchitectureTest` 分层断言；`SPRINT_MODULES` 随 Sprint 扩容；覆盖率门槛 0.70 起 | 待整理 |

> 讲评排期：Sprint 3 计划会上每人约 5 分钟讲完各自认领项，讲完即更新本表状态。