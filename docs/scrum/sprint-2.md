# Sprint 2 · 执行记录（Reputation / 悬赏）

> Scrum Master：青岑（C）　PO：魏志国
> 时间盒：2026-11-09（周一）～ 2026-11-20（周五），共 10 个工作日
> 范围：PB-20～PB-30（Reputation / 悬赏）
> 范围来源：[`docs/backlog/product-backlog.md`](../backlog/product-backlog.md)
> 燃尽图：待补（数据截至 2026-11-14）

---

## 一、Sprint 2 计划会（2026-11-09）

| 议题 | 结论 | 落地物 |
| --- | --- | --- |
| Sprint 目标 | 打通 Reputation / 悬赏链路：发起悬赏 → 冻结积分 → 采纳结算 / 到期退款 | 本文件 |
| 迭代范围 | PB-20～PB-30；P0 优先 PB-20、PB-21、PB-22、PB-24、PB-25、PB-27、PB-30 | `docs/backlog/product-backlog.md` |
| C 组主责 | T2-03 Bounty 聚合、T2-07 BountyService | 角色卡 C |
| 分工 | A 刘俊琒：ReputationAccount / BadgePolicy；B 魏志国：ReputationRulebook / BadgeService / 账本；C 青岑：Bounty；E 陈明哲：CI / 对账；D 房国栋：前端 / 环境 | 角色卡 |
| 本次不做 | 超出 PB-20～PB-30 的需求；Q&A 核心功能继续由 S1 交付 | — |

## 二、每日站会记录

> 三问：昨天完成什么 / 今天做什么 / 有什么阻塞。时间为站会当日 10:00，时长 ≤15 分钟。

### 2026-11-10（周二）

| 成员 | 昨日完成 | 今日计划 | 阻塞 |
| --- | --- | --- | --- |
| 魏志国 | 确认 PB-20～PB-30 验收口径 | Reputation 规则与账本联调 | 无 |
| 陈明哲 | S2 门禁与 CI 检查 | 修复 CI 配置，核对必需检查 | GitHub Actions 额度报错 |
| 刘俊琒 | ReputationAccount / BadgePolicy 实现 | 合并 reputation-core，补测试 | 无 |
| 青岑 | Bounty 聚合与 BountyService 实现，领域 3/3、服务 7/7 通过 | 提交 Bounty PR #92 | CI 被根目录 `ci.yaml` 阻塞 |
| 房国栋 | 悬赏对话框接口字段梳理 | 等待后端 DTO 后联调 | 字段未最终确定 |

**站会后动作**：青岑提交 PR #92；刘俊琒的 Reputation 核心经 PR #91 合入 develop；陈明哲确认 CI 失败根因是 develop 基线的 `.github/workflows/ci.yaml` 在根目录执行 `mvn clean test`。

### 2026-11-12（周四）

| 成员 | 昨日完成 | 今日计划 | 阻塞 |
| --- | --- | --- | --- |
| 魏志国 | Reputation 规则验收 | 核对悬赏结算与冻结积分 | 无 |
| 陈明哲 | 删除重复 CI 配置，仓库改为 Public | 重跑 develop CI | 无 |
| 刘俊琒 | Reputation 事件处理器与账本 | 补充跨模块联调测试 | 无 |
| 青岑 | PR #92、#94 已合并；整理 E3 环境证据 | 更新 env-matrix 并提交 PR #98 | 无 |
| 房国栋 | 前端课程页与悬赏入口 | 联调悬赏接口 | 无 |

**站会后动作**：仓库改为 Public，GitHub Actions 额度问题解除；青岑把环境截图归档到 `docs/lab1-submission/env/`，提交 E3 后续 PR #98。

### 2026-11-14（周六）

| 成员 | 昨日完成 | 今日计划 | 阻塞 |
| --- | --- | --- | --- |
| 魏志国 | 核对 Bounty 验收清单 | 补端到端验收 | 无 |
| 陈明哲 | 确认 Public 后 CI 可启动 | 观察 develop CI #50 | 无 |
| 刘俊琒 | Reputation 核心联调 | 补充边界测试 | 无 |
| 青岑 | PR #98 合并，E3 环境证据进入 develop | 收尾 Sprint 2 文档与 Bounty 验收 | 无 |
| 房国栋 | 前端悬赏入口联调 | 补前端构建验证 | 无 |

**站会后动作**：PR #98 合并（`edbf5c8`），develop CI #50 全部成功：后端构建与验证、前端构建、容器镜像构建均通过。

### 2026-11-15（S2 收尾补测）

| 事项 | 结果 |
| --- | --- |
| Bounty 边界测试 | points 9/10/500/501、days 0/1/14/15 已覆盖 |
| 到期与结算顺序 | 新增 `settledBountyIsNotReturnedByExpireDue`、`expiredBountyCannotBeSettledAgain` |
| 悬赏脚本 | 新增 `http/bounty.http`（学生发问题 → 教师 50 分 / 7 天 → 查 950/50） |
| 悬赏执行证据 | `scripts/bounty-smoke.ps1` 实测：教师 1000/0 → 950/50，悬赏 OPEN / 50 分，日志见 `docs/scrum/sprint-2-bounty.log` |
| 测试结果 | `co-reputation-domain` 20 条、`co-reputation-application` 23 条，全部通过 |

### 2026-11-16（悬赏 950/50 演示）

demo profile 启动后，把 T2001 账户重置为 1000/0，再执行 `scripts/bounty-smoke.ps1`：

```text
POST /api/v1/questions                    -> 201  {"id":5}
POST /api/v1/questions/5/bounty           -> 201  {"id":2}
Teacher before: available=1000, frozen=0
Bounty: status=OPEN, points=50
Teacher after : available=950, frozen=50
=== All checks passed ===
```

完整日志：[`sprint-2-bounty.log`](sprint-2-bounty.log)。

### 2026-11-16（悬赏结算与到期退款）

结算链路（教师发起悬赏 → 学生回答 → 提问者采纳）：

```text
answerer reputation before: 35
answerer reputation after : 100   (+65 = 采纳 +15 + 悬赏 +50)
teacher before: available=1000, frozen=0
teacher after : available=950,  frozen=0
bounty        : status=AWARDED, points=50
=== All checks passed ===
```

到期退款（把 expires_at 改成过去，定时任务每 2 秒扫描）：

```text
到期前：available=950,  frozen=50, bounty=OPEN
到期后：available=1000, frozen=0,  bounty=EXPIRED
```

日志：[`sprint-2-bounty-settle.log`](sprint-2-bounty-settle.log)、[`sprint-2-bounty-expiry.log`](sprint-2-bounty-expiry.log)。

## 三、燃尽图与数据

口径：纵轴为 Sprint 2 迭代范围内尚未完成的 Story Point；完成指代码合入 `develop` 且对应验收通过。S2 范围 PB-20～PB-30 合计 42 SP，数据截至 2026-11-14，后续站会续绘。

| 日期 | 工作日 | 理想剩余 | 实际剩余 | 当日完成 |
| --- | --- | --- | --- | --- |
| 2026-11-09 | D0 | 42 | 42 | 计划会，未开工 |
| 2026-11-10 | D1 | 38 | 37 | PB-24 / PB-25（Bounty，5 SP）随 PR #92 合入 |
| 2026-11-12 | D2 | 34 | 32 | Reputation 核心与 E3 环境证据合入 |
| 2026-11-14 | D3 | 30 | 29 | PR #98 合并，CI #50 全绿；后续待续绘 |
| 2026-11-15 | D4 | 26 | 24 | 悬赏边界/顺序测试、`http/bounty.http`、S2 收尾补测 |

燃尽图：待补（S2 评审前归档）。

## 四、看板状态

- Done：
  - T2-03 Bounty 聚合（PR #92）
  - T2-07 BountyService（PR #92）
  - Bounty 边界与到期/结算顺序测试（PR #110）
  - 悬赏 950/50 与结算/退款演示（PR #111）
  - E3 环境证据（PR #98，属 Lab 1 交付）
  - 悬赏对话框与个人声誉主页（PR #100）
- In Progress：
  - Sprint 2 评审彩排与燃尽图归档
- In Review：无
- To Do：
  - PB-23 徽章、PB-26 排行榜、PB-28 对账查看
  - PB-30 Reputation 应用层测试补全

## 五、风险与对策

| 风险 | 影响 | 对策 | 状态 |
| --- | --- | --- | --- |
| GitHub Actions 额度/账单 | CI job 无法启动，PR 卡住 | 仓库改为 Public，Actions 免费；对失败 run 重新运行 | 已解除 |
| 根目录 `ci.yaml` 重复且路径错误 | 所有 PR 的 Java CI 10 秒必红 | 删除 `ci.yaml`，只保留 `ci.yml` | 已解除 |
| Reputation 依赖未合入 | BountyService 无法端到端联调 | 先做领域层与服务层 fake 测试，依赖合入后补联调 | 已解除 |
| 评审等待 | PR 无法及时合并 | 指定非作者评审，24 小时内响应 | 已解除 |

## 六、验收

- [x] Bounty 领域测试 3/3 通过
- [x] Bounty 服务测试 7/7 通过
- [x] PR #92 合并进 `develop`
- [x] PR #94 / #98（环境证据）合并进 `develop`
- [x] develop CI #50 通过（后端、前端、容器镜像）
- [x] 悬赏边界：points 9/10/500/501、days 0/1/14/15、重复 open、自答退款
- [x] 到期与结算顺序单元测试（先结算不再到期、先到期不再结算）
- [x] `http/bounty.http` 已加入（demo 账号，脚本可一键跑）
- [x] 教师 50 分 / 7 天悬赏：可用 950、冻结 50（`sprint-2-bounty.log`）
- [x] 结算端到端：回答者声誉 35 → 100（采纳 +15 + 悬赏 +50），发起人冻结归零（`sprint-2-bounty-settle.log`）
- [x] 到期退款端到端：950/50 → 1000/0，悬赏 EXPIRED（`sprint-2-bounty-expiry.log`）
- [ ] Sprint 2 燃尽图归档

## 七、关联链接

- Bounty PR：[#92](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/92)
- 环境证据 PR：[#94](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/94)、[#98](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/98)
- 合并提交：`edbf5c8`（PR #98）
- 团队仓库：<https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow>
