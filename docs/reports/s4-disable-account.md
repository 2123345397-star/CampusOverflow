# Sprint 4 · 禁用账号与会话撤销验证

> 日期：2026-11-16　负责人：魏志国
> 环境：Docker Desktop 29.9.2、MySQL 8.0.39（3307）、Redis 7、Spring Boot dev、JDK 25（本地）
> 截图：[`../final/screenshots/disable-account.png`](../final/screenshots/disable-account.png)

## 一、实现

| 层 | 内容 |
| --- | --- |
| 领域 | `User.disable(Actor)`：仅 ADMIN；禁自己抛 ID-1007；状态置 DISABLED，`canLogin=false` |
| 应用 | `AdminUserService.disable`：load → `user.disable` → save → `UserSessionRevoker.revokeAll(username)` → 审计 `DISABLE_USER` |
| 端口/适配器 | `UserSessionRevoker`（identity-application）+ `SpringSessionUserSessionRevoker`（bootstrap，Spring Session） |
| 接口 | `PUT /api/v1/admin/users/{userId}/disable` → 204 |
| 会话索引 | `spring.session.redis.repository-type=indexed`；登录成功写入 principal-name 索引 |

## 二、真实链路验证

```text
管理员 s4adm116 禁用 s4vic116：
  PUT /api/v1/admin/users/4/disable            -> 204

被禁用者旧 Cookie 访问 /api/v1/auth/me         -> 401 AUTH-401 "请先登录"
被禁用者重新登录 POST /api/v1/auth/login       -> 401 AUTH-401 "账号或密码错误"
管理员本人 GET /api/v1/auth/me                 -> 200（未被误踢）
```

说明：禁用后旧会话已从 Redis 删除，因此旧 Cookie 直接 401；禁用账号 `canLogin=false`，登录被 `DisabledException` 拒绝。

## 三、自动化测试

`AdminUserServiceTest` **3/3**：

1. 非 ADMIN 禁用 → `ForbiddenException`；
2. 管理员禁自己 → `BusinessRuleException` ID-1007；
3. 禁用他人 → 状态 DISABLED、会话被撤销、审计 `DISABLE_USER`、`canLogin=false`。

运行：

```bash
mvn -f backend/pom.xml -pl co-identity/co-identity-application -am test -Djacoco.skip=true
```

## 四、备注

- 会话撤销依赖 Spring Session 的 `repository-type=indexed`；该配置已加入 `application.yml`。
- 本地 Mockito 在 JDK 25 需要 `-Dnet.bytebuddy.experimental=true`，本次禁用测试不依赖 Mockito。
