# Lab 3 提交物清单 · TeamX

> 团队：TeamX　项目：CampusOverflow 校园问答平台
> 仓库：<https://github.com/2123345397-star/CampusOverflow>　对应实验：Lab 3 领域驱动设计事件风暴（Sprint 1）
> 维护人：房国栋（Dev-2 前端与验证）　最近更新：2026-10-28

本页是 Lab 3 的**提交索引**。Lab 3 要求"事件风暴 → 上下文划分 → 聚合与不变式 → 统一语言"四件套，缺一件都会被评委追问。

---

## 1. 提交物对照

| # | 实验要求的提交物 | 仓库位置 | 状态 | 负责人 |
| --- | --- | --- | --- | --- |
| 1 | 事件风暴现场全景照片 | [`docs/domain/event-storming.png`](../domain/event-storming.png) | ✅ 已提交 | 青岑 |
| 2 | 上下文映射图 PNG | 文本版 [`docs/domain/context-map.md`](../domain/context-map.md)；Mermaid 源见 [`course-context.md` §3](../domain/course-context.md#5-上下文映射真实依赖) | ⚠️ **缺 PNG**，且总图缺"课程与选课"节点 | 青岑 / 房国栋 |
| 3 | 聚合识别清单 Markdown | [`docs/domain/aggregates.md`](../domain/aggregates.md)（问答）+ [`course-context.md` §2](../domain/course-context.md#4-聚合与不变式对齐真实字段)（课程） | ✅ 两个上下文齐备 | 青岑 / 房国栋 |
| 4 | 统一语言术语表 v1.0（≥30 术语，中英对照） | [`docs/domain/glossary.md`](../domain/glossary.md)（PR [#35](https://github.com/2123345397-star/CampusOverflow/pull/35)） | ⚠️ 已合并，待并入课程侧 12 条形成 v1.1 | 陈明哲 / 房国栋 |

图例：✅ 完成　⚠️ 部分完成　⬜ 待完成

---

## 2. 上下文全景（6 个限界上下文 + 1 个课程子域）

| # | 限界上下文 | 代码模块 | 核心聚合 | 材料 |
| --- | --- | --- | --- | --- |
| 1 | 身份与权限 | `co-identity` | User、Session | `aggregates.md` / ADR-003 |
| 2 | 问答核心 | `co-qa` | Question、Answer、Vote、Comment | `aggregates.md`（10 个事件、4 个聚合） |
| 3 | **课程与选课** | `co-identity`（课程子域，已实现） | **Course、CourseMember** | **`course-context.md`（本次补充）** |
| 4 | 内容治理 | `co-qa`（+ 管理端） | 举报、审核 | 待补 |
| 5 | 声誉 | `co-reputation` | ReputationAccount、Badge、Bounty | ADR-004 |
| 6 | 通知 | `co-discovery` | Notification | 待补 |
| 7 | 检索 | `co-discovery` | SearchIndex | ADR-005 |

> 第 3 项的重点：课程在代码里**已经实现在 `co-identity` 内**，但从未写进领域文档，导致"问题挂课程"这条关系在上下文映射里说不清。

---

## 3. 上下文关系一览

| 上游 | 下游 | 关系类型 | 说明 |
| --- | --- | --- | --- |
| 身份与权限（含课程子域） | 问答核心 | ACL / 发布语言 | 问答只依赖 `CourseSummary`，不引用 `identity-domain` |
| 问答核心 | 通知 / 检索 | 事件（发布-订阅） | 通过事务性发件箱投递领域事件（ADR-002） |
| 问答核心 | 声誉 | 事件（发布-订阅） | 投票/采纳事件驱动声誉账本（ADR-004） |
| 全部 | 全部 | 共享内核 | `co-shared-kernel`：UserId、CourseId、DomainEvent、Clock |

完整 Mermaid 图见 [`course-context.md` §3](../domain/course-context.md#5-上下文映射真实依赖)，可直接粘贴到 [mermaid.live](https://mermaid.live) 导出 PNG。

---

## 4. 还差什么（Lab 3 关闭条件）

1. **导出上下文映射图 PNG**：把 `course-context.md` §5 的 Mermaid 贴到 mermaid.live → Actions → PNG，存为 `docs/domain/context-map.png`，并在本页第 1 节改成 ✅；
2. **更新总图文本**：`context-map.md` 标注"身份与权限（含课程子域）→ 问答核心"的发布语言关系（现在只有 6 行列表，看不出关系类型）；
3. **术语表 v1.1**：把课程侧 12 条并入 `glossary.md`，标题改为 v1.1 并注明贡献者；
4. **聚合清单合订**：在 `aggregates.md` 末尾加一行指路"课程侧聚合见 course-context.md"，避免老师只看到问答部分。

---

## 5. 评分要点对照

| 评分项（占比） | 对应材料 | 状态 |
| --- | --- | --- |
| 事件识别完整性（25%） | `aggregates.md`（问答 10 事件）+ `course-context.md` §1（课程 5 事件） | ✅ |
| 上下文划分合理性（30%） | 本页第 2、3 节 + 总图 | ⚠️ 待补 PNG 与课程节点 |
| 聚合边界与不变式清晰度（25%） | 两个聚合清单文件 + 课程侧"聚合拆分取舍" | ✅ |
| 统一语言一致性与认同度（20%） | `glossary.md` + 课程 12 条增补 | ⚠️ 待合并为 v1.1 |
