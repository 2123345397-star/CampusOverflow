# 个人总结 · 陈明哲

| 项 | 内容 |
| --- | --- |
| 姓名 | 陈明哲 |
| 角色 | SM / E（测试与质量门禁、可观测性） |
| 负责 | CI 与覆盖率门禁、行走骨架、声誉幂等测试、冒烟脚本、可观测性、发布验证 |
| 参与 Sprint | S0 / S1 / S2 / S3（S4 进行中） |

## 一、我负责的交付物（带证据）

| 交付物 | 证据 | 结果 |
| --- | --- | --- |
| 渐进门禁与 CI 范围扩容（Q&A → Reputation → Discovery） | `.github/workflows/ci.yml`、PR #96 / #103 / 本轮 #126 | 五后端阶段 + 前端全绿 |
| 域覆盖率门槛 0.70 | `backend/pom.xml`、JaCoCo 报告；qa 83.12% / identity 75.74% / reputation 80.72% / discovery 78.26% | 通过 |
| 声誉幂等与结算测试 | `ReputationTestFixtures`、`ReputationEventHandlersTest`（6 条）、`ReputationSettlementTest`（6 条），PR #103 | 50+ 测试全绿 |
| T1-15 端到端冒烟 | `scripts/smoke/smoke.ps1` / `.sh`、PR #104 | `SMOKE PASS`，一条命令跑通主链路 |
| 可观测性 | `co_reputation_mismatches` 定时刷新、Grafana 四面板 + provisioning、`deploy/prometheus.yml` | 四指标可复现 |
| 发布验证 | `v0.1.0`～`v0.3.0` tag、`docs/reports/release-v0.2.0-v0.3.0.md`、PR #118 / #121 | 三版本 tag 与 CI 证据归档 |
| 团队文档 | `docs/scrum/sprint-0.md`、`team-agreement.md` 重放命令、`docs/final` 骨架 | 入库 |

## 二、技术收获

1. **幂等要按“事件被消费两次”设计，而不是只在单测里 mock。** 声誉账本用 `(event_id, user_id, reason)` 唯一键 + 处理前 `exists` 判断，`AnswerAccepted` 连调两次只产生一条流水、一枚徽章。
2. **CI 门禁要渐进，但“范围”必须显式写死。** 用 `SPRINT_MODULES` 从 Q&A 扩到 Reputation、Discovery，既保证早期 CI 有意义，又避免未实现模块把红灯拖成噪音。
3. **可观测性指标要能被演示。** 对账原来只在凌晨跑，面板永远看不到变化；改成 60s 刷新后，可以现场“造 1 → 归零”，指标才真正可用。

## 三、协作与流程

- 发起/参与 PR：#96（S1 门禁）、#103（声誉幂等测试）、#104（冒烟）、#118（S0 收口）、#121（发布验证）等；均经队友 Approve 后合并。
- 主持/参与：S0 计划会与收尾、S2 站会记录、S4 彩排排期（`docs/final/rehearsal-log.md`）。
- 流程改进：把「发布 PR + tag + Release Notes」固化为 Sprint 末动作；把环境采集脚本与冒烟脚本纳入仓库，减少口头交接。

## 四、不足与改进

| 不足 | 影响 | 下次怎么做 |
| --- | --- | --- |
| v0.1.0/v0.2.0 发布时漏打 tag，后来补打 | 版本追溯不连续 | Sprint 末把「打 tag」写进 DoD 检查项，发布 PR 合并后立即打 |
| 环境截图与 T0-05 签署拖到收尾 | 评审前集中补材料 | S0 第一周就按脚本采集并归档，签署随文档合并 |
| 分支保护 `required_status_checks` 仍未启用（BUG-003） | CI 失败不一定阻断合并 | S4 前与 PO 确认发布流程触发条件后开启 |

## 五、自评

- 对团队目标的贡献：把“能合并”变成“合并后还是绿的”，并用测试与报告证明；S3/S4 的可观测性与发布材料由我串起来。
- 需要支持：S4 彩排需要全员按脚本各讲 2 分钟；发布验证需要 PO 确认 main 发布窗口。
- 签字 / 日期：陈明哲 / 2026-11-16
