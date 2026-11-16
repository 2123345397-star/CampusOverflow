# CampusOverflow 架构理解笔记

> 负责人：青岑（Dev-1）　主讲：房国栋（§8 · ADR-003）
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

## 8. ADR 主讲：ADR-003 服务端会话（房国栋）

> 本节为 T0-05 交付内容，由本人讲解 ADR-003 并对照一层代码。
> 对应 ADR：[`docs/adr/003-服务端会话.md`](../adr/003-服务端会话.md)

### 8.1 这个决策要解决什么问题

校园场景有两个硬约束：

1. **能立即封禁**。教师/管理员发现违规账号，要求立刻踢下线；如果令牌自包含（JWT），签出去就收不回，只能等它自然过期；
2. **浏览器 SPA 面临 XSS 与 CSRF**。JWT 通常放 `localStorage`，一旦页面被注入脚本，令牌可直接被读走。

ADR-003 的选择是：**不做无状态令牌，改用 Redis 服务端会话**。

### 8.2 决策要点与代码落点

| 决策点 | 代码位置 | 说明 |
| --- | --- | --- |
| 会话存储用 Spring Session + Redis | `co-bootstrap/pom.xml`（`spring-session-data-redis`） | 会话数据落在 Redis，多实例共享；扩容不需要粘性会话 |
| CSRF 双提交 Cookie | `SecurityConfig`（`CookieCsrfTokenRepository.withHttpOnlyFalse()`）+ `CsrfCookieFilter` | 服务端下发 `XSRF-TOKEN` Cookie（必须 JS 可读，所以 `HttpOnly=false`），前端把它放进 `X-XSRF-TOKEN` 请求头；攻击者跨站拿不到该头 |
| 登录后轮换会话 ID | `AuthController` → `request.changeSessionId()` | 防**会话固定攻击**：攻击者先埋一个已知 SID，等受害者登录后复用 |
| 登录失败限流 | `LoginThrottle` 接口 + `RedisLoginThrottle` | Redis 固定窗口计数，同一用户名 1 分钟内失败 5 次即锁定到窗口结束，返回 `AUTH-429` |

### 8.3 `RedisLoginThrottle` 逐行理解（我讲的那一层代码）

```java
private static final Duration WINDOW = Duration.ofMinutes(1);
private final StringRedisTemplate redis;
private final int maxFailures;      // 来自配置 co.security.login-max-failures，默认 5

public boolean isBlocked(String username) {
    String v = redis.opsForValue().get(key(username));
    return v != null && Integer.parseInt(v) >= maxFailures;   // 达到阈值即锁
}

public void recordFailure(String username) {
    Long count = redis.opsForValue().increment(key(username));
    if (count != null && count == 1L) {
        redis.expire(key(username), WINDOW);                  // 只在第一次失败时设过期
    }
}

public void reset(String username) {
    redis.delete(key(username));                              // 登录成功后清零
}

private static String key(String username) {
    return "id:login-fail:" + username;                       // 按用户名计数
}
```

三个值得注意的设计点：

1. **`expire` 只设在计数为 1 时**。如果每次失败都刷新过期时间，攻击者持续尝试就会让窗口永远不过期，等于把"1 分钟内 5 次"变成"永久锁定"——锁死账号反而成了新的攻击面；
2. **登录成功后 `reset` 清零**。否则用户白天输错几次、成功登录后计数还留着，晚上再错一次就被锁；
3. **按用户名计数而不是按 IP**。按 IP 会误伤同一出口 IP 的整个机房/校园网用户；按用户名则精确落在被攻击的账号上（代价是可被用来恶意锁定他人账号，这是本方案已知的取舍）。

### 8.4 与 JWT 方案的对比（为什么否掉）

| 维度 | 服务端会话（本方案） | JWT（localStorage） |
| --- | --- | --- |
| 立即失效 | ✅ 删除 Redis 中的会话即可，封禁即时生效 | ❌ 令牌自包含，签发后无法收回 |
| XSS 窃取风险 | ✅ 会话 ID 在 `HttpOnly` Cookie，JS 读不到 | ❌ `localStorage` 可被任意脚本读取 |
| CSRF 防护 | 需要双提交 Cookie（已实现） | 天然免疫（无 Cookie 自动携带） |
| 水平扩容 | 依赖 Redis 共享会话 | ✅ 完全无状态 |
| Redis 故障影响 | ❌ Redis 不可用则无法登录 | ✅ 不受影响 |

ADR 里的结论是：**「JWT + 黑名单」是最差组合**——黑名单本身就要 Redis，既付出了状态成本，又没得到无状态的好处。

### 8.5 我的理解与疑问

- **理解**：这个决策的本质是**用一点可用性依赖换安全可运维性**。校园场景里"能立刻封人"的价值高于"Redis 挂了还能登录"，所以选会话。
- **疑问 1**：`RedisLoginThrottle` 按用户名锁定，恶意者可以故意输错来锁死别人的账号。是否需要叠加 IP 维度或验证码？这属于 QS-08 的延伸讨论。
- **疑问 2**：ADR 说"Redis 不可用则无法登录"，但未说明降级策略。是否需要在 Redis 故障时允许只读访问并给出明确提示？
- 以上两点已在组内提出，作为后续 ADR 修订的输入。
