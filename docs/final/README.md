# 9.4 最终交付物索引（草案）

> 负责人：魏志国（PO / 发布经理）　日期：2026-11-16
> 用途：对照指导书 9.4 的七项交付物逐项给链接；缺项标明责任人与阻塞。

| # | 交付物 | 链接 / 命令 | 状态 |
| --- | --- | --- | --- |
| 1 | `main` 上 v1.0.0 与完整提交历史 | <https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/commits/main> · tag `v1.0.0` | ⬜ 待 S4 发布 PR 合入并打 tag（当前 main 为 v0.3.0） |
| 2 | GitHub Release | <https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/releases> · [v0.9.0 RC](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/releases/tag/v0.9.0) | 🟨 v0.9.0 RC 已发布；v1.0.0 Release 待正式发布 |
| 3 | `docs/ddd/` | [`context-map.md`](../ddd/context-map.md)、[`events.md`](../ddd/events.md)、[`glossary.md`](../ddd/glossary.md)、[`aggregates.md`](../ddd/aggregates.md)、[`event-storming.png`](../ddd/event-storming.png) | ✅ |
| 4 | `docs/adr/` | [`docs/adr/`](../adr/)（001–009，ADR-009 已就位） | ✅ |
| 5 | `docs/scrum/sprint-0~4.md` | [`sprint-0.md`](../scrum/sprint-0.md) · [`sprint-1.md`](../scrum/sprint-1.md) · [`sprint-2.md`](../scrum/sprint-2.md) · [`sprint-3.md`](../scrum/sprint-3.md) · [`sprint-4.md`](../scrum/sprint-4.md) | 🟨 0–3 有；4 为本次新增草案 |
| 6 | 覆盖率报告 | CI `覆盖率门禁` job（`.github/workflows/ci.yml` 阶段 4，JDK 21 / JaCoCo 0.70） | 🟨 CI 结果为准；最终报告链接待补 |
| 7 | `docs/final/` 答辩材料 + ≤5 分钟视频 + 每人个人总结 | 本文件；视频与个人总结见下方 | ⬜ 视频、5 份个人总结待各成员提交 |

## 个人总结 / 视频（待补）

| 成员 | 个人总结 | 视频片段 |
| --- | --- | --- |
| 魏志国 | ⬜ | ⬜ |
| 刘俊琒 | ⬜ | ⬜ |
| 陈明哲 | ⬜ | ⬜ |
| 青岑 | ⬜ | ⬜ |
| 房国栋 | ⬜ | ⬜ |

## 发布阻塞

1. S3/S4 的 PR 需要至少 1 名非作者 Approve（v1.0.0 走 develop→main，需 ≥2 Approve 含 SM/PO）；
2. 陈明哲的最终彩排/CI 全绿确认；
3. 个人总结与 5 分钟视频由全员提交；
4. 覆盖率报告链接需在最终 CI run 后回填。
