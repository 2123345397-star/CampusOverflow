# v1.0.0 发布验证记录

> 验证人：陈明哲（SM / E）　验证时间：2026-11-17
> 候选提交：`develop` 最新 `0073294`（lint：本轮发布 PR 的 head）
> 对应任务：指导书 T4-01；Sprint 4 记录：[`docs/scrum/sprint-4.md`](../scrum/sprint-4.md)

## 一、发布前检查

| # | 检查项 | 结果 | 证据 |
| --- | --- | --- | --- |
| 1 | `develop` 最新 CI 三阶段 | 后端 ✅ / 前端 ✅ / 容器镜像 ✅ | GitHub Actions（`0073294`） |
| 2 | 全仓 `mvn verify`（27 模块，含 ArchUnit） | **BUILD SUCCESS** | 2026-11-17 本地 JDK 21 实跑 |
| 3 | domain 覆盖率门槛 0.70 | qa 83.12% / identity 75.74% / reputation 80.72% / discovery 78.26% | JaCoCo `jacoco.csv` |
| 4 | T1-15 主链路冒烟 | `SMOKE PASS` | `scripts/smoke/README.md`，2026-11-15 demo 环境 |
| 5 | demo profile 启动 + 演示数据 | 启动成功；6 账号 / 1 课程 / 3 问题，重启幂等 | [`t2-14-demo-profile.md`](t2-14-demo-profile.md) |
| 6 | 可观测性四指标 | 三项自定义指标 + HTTP P95 直方图；对账 1→0 演练通过 | [`t4-04-observability.md`](t4-04-observability.md) |
| 7 | 禁用账号（T4-02） | 接口 + 3 条测试 + 会话撤销；真实 Redis 会话验证列入彩排 | [`s4-disable-account.md`](s4-disable-account.md) |
| 8 | Release Notes | [`docs/release/v1.0.0.md`](../release/v1.0.0.md)（新功能/已知问题/部署步骤） | 本仓库 |

## 二、发布步骤与结果

1. `develop → main` 发布 PR：**待创建/审批**（main 保护：≥2 Approve + 后端/前端/镜像三项必需检查）；
2. 合并后在 main 合并提交上打 annotated tag `v1.0.0`；
3. 用 `docs/release/v1.0.0.md` 创建 GitHub Release（附件：无；部署说明见 Release Notes）；
4. 记录 main 合并提交与 Release URL 到 [`docs/final/README.md`](../final/README.md)。

## 三、Tag 与历史版本

```text
v0.1.0 -> 704cd60   Sprint 0 基线（补打）
v0.2.0 -> a381bb8   Sprint 1 基线（补打）
v0.3.0 -> a4114dc   Sprint 2 发布（main）
v0.9.0 -> 579ea59   Sprint 3 RC（develop）
v1.0.0 -> 发布 PR 合并提交（待打）
```

## 四、遗留风险

| 风险 | 影响 | 处置 |
| --- | --- | --- |
| 发布 PR 需要 2 个 Approve | 发布可能延迟 | 已请求 PO/SM 审批 |
| 干净环境 compose 部署未在本机验证（无 Docker） | 首次部署风险 | 彩排机器执行 `docker compose ... up -d --build` 并走 README |
| `docs/final` 视频与 3 份个人总结未交 | 9.4 交付不完整 | 责任人 S4 结束前补 |
| 两次彩排未执行 | T4-08 未闭环 | 按 [`rehearsal-log.md`](../final/rehearsal-log.md) 排期 |

## 五、签署

| 角色 | 成员 | 结论 | 日期 |
| --- | --- | --- | --- |
| SM / E | 陈明哲 | 发布前技术检查通过，待审批与 tag | 2026-11-17 |
| PO | 魏志国 | 待签 | |
