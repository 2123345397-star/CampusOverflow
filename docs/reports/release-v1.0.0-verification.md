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

1. 发布 PR [#135](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/135)：魏志国、青岑 Approve，后端/前端/镜像三检查绿，已合并 `10925c7`；
2. annotated tag `v1.0.0` 打在 main `10925c7`；
3. GitHub Release 已发布：https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/releases/tag/v1.0.0；
4. [`docs/final/README.md`](../final/README.md) 已更新 9.4 勾选。

## 三、Tag 与历史版本

```text
v0.1.0 -> 704cd60   Sprint 0 基线（补打）
v0.2.0 -> a381bb8   Sprint 1 基线（补打）
v0.3.0 -> a4114dc   Sprint 2 发布（main）
v0.9.0 -> 579ea59   Sprint 3 RC（develop）
v1.0.0 -> 10925c7   v1.0.0 发布（main，PR #135）
```

## 四、遗留风险

| 风险 | 影响 | 处置 |
| --- | --- | --- |
| 发布 PR 需要 2 个 Approve | 发布可能延迟 | 已请求 PO/SM 审批 |
| 干净环境 compose 部署未在本机验证（无 Docker） | 首次部署风险 | 彩排机器执行 `docker compose ... up -d --build` 并走 README |
| `docs/final` 视频与 3 份个人总结未交 | 9.4 交付不完整 | 责任人 S4 结束前补 |
| 两次彩排未执行 | T4-08 未闭环 | 按 [`rehearsal-log.md`](../final/rehearsal-log.md) 排期 |

## 五、干净环境部署验证

| 项 | 结果 | 说明 |
| --- | --- | --- |
| 镜像构建 | ✅ backend / frontend Dockerfile 均 build success | CI `容器镜像构建` job（main `10925c7`） |
| compose 配置 | ✅ 已入库并接入 observability profile | `deploy/docker-compose.yml`、`deploy/prometheus.yml`、`deploy/grafana/` |
| 运行时 `docker compose up -d --build` | ⬜ 待有 Docker 的机器执行 | 负责人：房国栋 / 彩排机；执行后补 `docker compose ps` + health + Grafana 截图 |
| 本机限制 | 无 Docker（WSL2/Hyper-V 未启用） | 不影响 v1.0.0 tag/Release，已记录为部署验证遗留 |

## 六、签署

| 角色 | 成员 | 结论 | 日期 |
| --- | --- | --- | --- |
| SM / E | 陈明哲 | 发布完成：PR #135、tag v1.0.0、GitHub Release | 2026-11-17 |
| PO | 魏志国 | 已批准发布 PR #135 | 2026-11-17 |
