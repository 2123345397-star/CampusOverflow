# Product Backlog 产品待办列表

> 项目：CampusOverflow 校园问答平台　迭代：Sprint 1
> 编号规则：统一使用 `US-<两位数字>`，与 `docs/backlog/stories/` 下的 `.feature` 文件名一一对应。

| ID | 用户故事（As a / I want / So that） | 优先级 | SP | 迭代 | 验收标准 |
| --- | --- | --- | --- | --- | --- |
| US-01 | As a 未注册用户, I want 注册账号, So that 我可以提问和浏览内容 | P0 | 3 | Sprint 1 | `US-01.feature` |
| US-02 | As a 在校学生, I want 发布问答问题, So that 我可以在平台提问寻求解答 | P0 | 5 | Sprint 1 | `US-02.feature` |
| US-03 | As a 在校学生, I want 浏览问题列表, So that 我可以查看别人提出的问题 | P0 | 3 | Sprint 1 | `US-03.feature` |
| US-04 | As a 在校学生, I want 查看问题详情, So that 我可以阅读问题描述和已有回答 | P0 | 2 | Sprint 1 | `US-04.feature` |
| US-05 | As a 在校学生, I want 给回答点赞, So that 我可以标记优质回答 | P1 | 2 | Sprint 2 | `US-05.feature` |
| US-06 | As a 在校学生, I want 收藏问题, So that 我可以快速找到感兴趣的问题 | P1 | 2 | Sprint 2 | `US-06.feature` |
| US-07 | As a 在校学生, I want 通过标签筛选问题, So that 我可以找到对应主题内容 | P1 | 3 | Sprint 2 | `US-07.feature` |
| US-08 | As a 提问学生, I want 采纳满意回答, So that 标记问题已经解决 | P1 | 3 | Sprint 2 | `US-08.feature` |
| US-09 | As a 在校学生, I want 提交回答, So that 我可以回复别人的提问 | P1 | 5 | Sprint 2 | 待补 `US-09.feature` |

**Sprint 1 承诺**：US-01 ~ US-04（P0），合计 **13 点**，与 `docs/sprint/sprint-1-plan.md` 一致。

## P0 选取依据

Sprint 1 交付「用户从未注册到成为活跃贡献者」的最小闭环（Walking Skeleton），见 `docs/backlog/story-map.png`：

```
注册（US-01） → 发布问题（US-02） → 问题列表（US-03） → 问题详情（US-04）
```

- 不包含「提交回答」「点赞」「收藏」「标签筛选」「采纳」——这些属于 Sprint 2。
- 判断标准：缺任何一环，闭环就跑不通；多任何一环，Sprint 1 就做不完。

## 编号变更说明（2026-10-28 统一）

原文件编号与 `.feature` 文件名不一致，且存在重复行，本次以 `.feature` 文件名为准统一重编号：

| 原编号 | 新编号 | 说明 |
| --- | --- | --- |
| （无） | US-01 | 注册（原 backlog 漏列，但已有 `US-01.feature`） |
| US001 | US-02 | 发布问题 |
| US002 | US-03 | 问题列表 |
| US003 | US-04 | 问题详情 |
| US004 | US-09 | 提交回答（原编号与 US-04.feature 撞车，顺延为 US-09） |
| US005（重复两行） | US-05 | 点赞（删除重复行） |
| US006 | US-06 | 收藏 |
| US007 | US-07 | 标签筛选 |
| US008 | US-08 | 采纳 |

## 待补项

- **US-09 提交回答**：暂无 `US-09.feature`，Sprint 2 开始前补齐（≥3 个场景：提交成功 / 内容为空 / 问题已关闭不可回答）。
- 所有 `.feature` 文件须含 `# language: zh-CN` 头、一个文件对应一条故事、每条 ≥3 个 Given-When-Then 场景。