# 端到端冒烟脚本（T1-15）

一条命令跑通主链路，作为 Sprint 评审的演示脚本，也是 `http/auth.http` + `http/qa.http` 的可执行版本：

```
CSRF → 注册提问者 → 登录 → 发问题 → 注册回答者 → 登录 → 回答 → 投票 → 切回提问者 → 采纳 → 详情校验
```

## 前置条件

- 后端已启动（`dev` 或 `demo` profile），默认地址 `http://localhost:8080`；
- MySQL、Redis 可用（登录限流与 Session 依赖 Redis）；
- 运行机器有 `curl`（bash 版）或 Windows PowerShell 5.1+（PowerShell 版）。

## 用法

Windows PowerShell（推荐在仓库根目录执行）：

```powershell
powershell -ExecutionPolicy Bypass -File scripts/smoke/smoke.ps1
# 指定地址
powershell -ExecutionPolicy Bypass -File scripts/smoke/smoke.ps1 -BaseUrl http://localhost:18080
```

Linux / macOS / Git Bash：

```bash
bash scripts/smoke/smoke.sh
# 指定地址
bash scripts/smoke/smoke.sh http://localhost:18080
```

成功输出示例：

```text
SMOKE PASS
  用户 A=smokeA1115164825187 (id=18)  用户 B=smokeB1115164825187 (id=19)
  问题 id=8  回答 id=6  score=1  acceptedAnswerId=6
  耗时 5s  base=http://localhost:8080
```

失败时返回非 0 退出码并打印失败步骤与响应体。

## 说明

- 每次运行会注册两个带时间戳后缀的临时账号，并留下一条「已采纳」的问题数据，可重复运行；
- 两个账号的 `displayName`、`username` 都带后缀，避免唯一键冲突；
- 脚本按 `XSRF-TOKEN` Cookie 回填 `X-XSRF-TOKEN` 头（后端使用 SPA 的 CSRF 模式），登录后重新获取；
- 本地验证：2026-11-15 在 `demo` profile（MySQL 8.4 + Redis 5.0）上，两个脚本均 `SMOKE PASS`。
