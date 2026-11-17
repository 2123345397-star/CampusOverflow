# 4. 过程证据

> 答辩口径：用「每个 Sprint 都有可运行增量 + Scrum 工件 + CI 记录」证明过程真实，而不是最后补文档。

## 4.1 Sprint 总览

| Sprint | 周期 | Sprint Goal | 记录 | 版本 |
| --- | --- | --- | --- | --- |
| S0 启动 | 2026-10-22 ～ 11-02 | 仓库、环境、CI、Backlog 与团队文档就绪 | [`sprint-0.md`](../scrum/sprint-0.md) | `v0.1.0`（补打） |
| S1 问答核心 | 2026-11-03 ～ 11-13 | 提问 → 回答 → 投票 → 评论 → 采纳闭环 | [`sprint-1.md`](../scrum/sprint-1.md)、[`sprint-1-demo.md`](../scrum/sprint-1-demo.md) | `v0.2.0`（补打） |
| S2 声誉激励 | 2026-11-09 ～ 11-20 | 声誉/徽章/悬赏/排行榜，幂等与对账 | [`sprint-2.md`](../scrum/sprint-2.md) | `v0.3.0` |
| S3 内容发现 | 2026-11-23 ～ 12-04（计划） | 搜索、相关问题、通知；移除骨架，`mvn verify` 全绿 | [`sprint-3.md`](../scrum/sprint-3.md)、[`sprint-3-search.md`](../scrum/sprint-3-search.md) | `v0.9.0`（RC，待发布） |
| S4 发布 | 2026-12-07 ～ 12-11（计划） | 稳定化、可观测、v1.0.0 与答辩 | 本目录 + `sprint-4.md`（S4 补） | `v1.0.0` |

## 4.2 Scrum 工件

| 工件 | 位置 | 说明 |
| --- | --- | --- |
| 团队章程 / 角色承诺 | [`docs/team/`](../team/) | 5 人签署（2026-10-26） |
| 工作约定 / DoR / DoD | [`docs/team-agreement.md`](../team-agreement.md)、[`dor-dod.md`](../team/dor-dod.md) | 含 CI 红灯 30 分钟认领、事件重放命令 |
| Product Backlog | [`docs/backlog/product-backlog.md`](../backlog/product-backlog.md) | PB-01～48，含 PBI/Issue 映射 |
| Sprint 计划与记录 | [`docs/scrum/`](../scrum/) | S0～S4（S4 为发布草案）；每个 Sprint 含目标、范围、站会、验收 |
| 燃尽图 | `sprint-1-burndown.png`、`sprint-2-burndown.png` | S1/S2 已归档 |
| 评审与回顾 | 各 `sprint-N.md` 的 Review / 回顾章节 | 含未达标项与行动项 |
| CI 运行记录 | GitHub Actions | 每次 PR 的后端/前端检查；v0.3.0 三阶段绿 |

## 4.3 协作与贡献

- 分支策略：`feature/*` / `docs/*` / `test/*` → PR → 1～2 Approve → squash/merge，合并后删分支；
- PR 模板包含「关联 Issue / 变更内容 / 自测 / 影响范围」；Issue 模板区分 story/task/bug/tech-debt；
- 代码评审：PR #100、#102、#103、#104、#118、#121 等均有非作者 Approve；
- 个人贡献以合并 PR、评审、Issue 关闭记录为准，不以代码行数论。

## 4.4 过程改进（回顾落地）

| 问题 | 改进 | 证据 |
| --- | --- | --- |
| S0/S1 发布没有打 tag | S4 前补齐 v0.1.0～v0.3.0，并固化「发布 PR + tag + Release Notes」 | [`docs/reports/release-v0.2.0-v0.3.0.md`](../reports/release-v0.2.0-v0.3.0.md) |
| 环境证据只有 2/5 | 采集脚本 + 统一归档，S3 前补到 4/5（刘俊琒待补） | [`docs/lab1-submission/env-matrix.md`](../lab1-submission/env-matrix.md) |
| CI 被重复 workflow 与额度拖累 | 删除重复 `ci.yaml`、仓库转 Public、固定 `SPRINT_MODULES` | `sprint-2.md` §五 |
| 缺陷台账状态过期 | 每次评审前复核并关闭已修复项 | [`docs/qa/defects.md`](../qa/defects.md) |
