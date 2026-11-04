# Lab 2 提交物清单 · TeamX

> 团队：TeamX　项目：CampusOverflow 校园问答平台
> 仓库：<https://github.com/2123345397-star/CampusOverflow>　对应实验：Lab 2 需求建模工作坊（Sprint 1）
> 维护人：房国栋（Dev-2 前端与验证）　最近更新：2026-10-28

本页是 Lab 2 的**提交索引**：老师按本页即可找到全部材料。

---

## 1. 提交物对照

| # | 实验要求的提交物 | 仓库位置 | 状态 | 负责人 |
| --- | --- | --- | --- | --- |
| 1 | 用户故事地图 PNG / PDF | [`docs/backlog/story-map.png`](../backlog/story-map.png) | ✅ 已提交 | 魏志国 |
| 2 | Product Backlog Markdown（含 ID、描述、优先级、Story Point） | [`docs/backlog/product-backlog.md`](../backlog/product-backlog.md) | ✅ 已补 SP 与课程故事 | 魏志国 / 房国栋 |
| 3 | 至少 8 条 Gherkin 验收标准 | [`docs/backlog/stories/`](../backlog/stories/)（现有 12 条） | ✅ 超出下限 4 条 | 全员 |
| 4 | Sprint 1 Backlog（任务已分配到人）+ 截图 | [`docs/sprint/sprint-1-plan.md`](../sprint/sprint-1-plan.md) | ⚠️ 有容量与承诺，**缺任务拆分到人与看板截图** | 刘俊琒 / 陈明哲 |

图例：✅ 完成　⚠️ 部分完成　⬜ 待完成

---

## 2. Gherkin 清单（12 条）

| 故事 | 文件 | 场景数 | 覆盖路径 |
| --- | --- | --- | --- |
| US-01 用户注册 | [`US-01.feature`](../backlog/stories/US-01.feature) | 4 | 快乐 / 边界 / 异常 |
| US-02 用户登录 | `US-02.feature` | ≥3 | — |
| US-03 问题列表 | `US-03.feature` | ≥3 | — |
| US-04 问题详情 | `US-04.feature` | ≥3 | — |
| US05 问题投票 | `US05‑vote‑question.feature` | ≥3 | — |
| US06 收藏问题 | `US06‑collect‑question.feature` | ≥3 | — |
| US07 标签筛选 | `US07‑question‑tag‑filter.feature` | ≥3 | — |
| US08 采纳回答 | `US08‑accept‑answer.feature` | ≥3 | — |
| **US009 课程目录** | [`US-09-course-catalog.feature`](../backlog/stories/US-09-course-catalog.feature) | 4 | 空态 / 接口异常 / 未登录 |
| **US010 我的课程** | [`US-10-my-courses.feature`](../backlog/stories/US-10-my-courses.feature) | 4 | 空态 / 未登录 / 归档过滤 |
| **US011 加入课程** | [`US-11-join-course.feature`](../backlog/stories/US-11-join-course.feature) | 5 | 重复加入 / 未登录 / **失败提示** / 归档 |
| **US012 提问选课** | [`US-12-ask-with-course.feature`](../backlog/stories/US-12-ask-with-course.feature) | 4 | 未选择 / 无课程 / 提交前归档 |

> 加粗 4 条为本次补充（课程域），故事卡与 INVEST 自评见 [`docs/backlog/course-stories.md`](../backlog/course-stories.md)。
> 补充动因：原 8 条故事全部落在问答主链路，课程维度缺失，导致角色卡 E1/E2 的三个页面在 Backlog 里没有对应故事、验收无据。

---

## 3. Product Backlog 本次修正内容

| 问题 | 修正 |
| --- | --- |
| 表格首尾带 `\#`、行间空行导致 Markdown 不渲染 | 重写为规范表格 |
| `US005` 重复两行、字段错位 | 合并为一行 |
| 全部故事 `Story Point` 为空 | 按 Planning 估算补齐（见下） |
| 缺少课程域故事 | 追加 US009–US012，归属 Sprint 2 |
| 缺少"验收标准"列 | 增加指向 `.feature` 的链接，做到一故事一验收 |

**SP 估算**（1 SP ≈ 2 人时，团队容量 168 h ≈ 42 SP）：

| Sprint 1（承诺 26 SP） | SP | Sprint 2（课程域，本次新增） | SP |
| --- | --- | --- | --- |
| US001 注册与登录 | 8 | US009 课程目录 | 5 |
| US002 浏览问题列表 | 5 | US010 我的课程 | 3 |
| US003 发布问题 | 8 | US011 加入课程 | 5 |
| US004 回答问题 | 5 | US012 提问选课 | 3 |
| 合计 | **26** | 合计 | **16** |

---

## 4. 还差什么（Lab 2 关闭条件）

1. **Sprint 1 Backlog 任务拆分到人**：在 `sprint-1-plan.md` 中为每个故事列出原子任务（≤8 h）与责任人；
2. **看板截图**：把上述任务卡移入 `To Do`，四列截图存 `screenshots/lab2-board.png`；
3. 故事地图若仍是旧版，补一版包含"课程 → 提问"分支的 PNG。

---

## 5. 评分要点对照

| 评分项（占比） | 对应材料 | 状态 |
| --- | --- | --- |
| 故事地图完整性与粒度（25%） | `story-map.png` | ✅ |
| INVEST 达标率（25%） | `course-stories.md` 第 3 节 INVEST 自评表 | ✅ |
| Gherkin 可测试性（25%） | 12 个 `.feature`，含失败与边界场景 | ✅ |
| Sprint Backlog 合理性（25%） | `sprint-1-plan.md` + 看板截图 | ⚠️ 待补任务分配与截图 |
