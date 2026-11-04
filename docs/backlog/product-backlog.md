# Product Backlog 产品待办列表

> 项目：CampusOverflow 校园问答平台　团队：TeamX　维护：魏志国（PO）
> 版本：v1.1（2026-10-28，补 Story Point、去重 US005、追加课程域故事）
> 关联文档：故事地图 [`story-map.png`](story-map.png)　课程故事卡 [`course-stories.md`](course-stories.md)

**估算口径**：1 SP ≈ 2 人时；团队容量 6 人 × 10 天 × 4 h × 0.7 ≈ 168 h ≈ 42 SP，Sprint 承诺不超过 70%（约 28 SP）。

## 一、故事清单

| ID | 用户故事（作为 / 我想要 / 以便） | 优先级 | SP | 迭代 | 验收标准（Gherkin） | 状态 |
| --- | --- | --- | --- | --- | --- | --- |
| US001 | 作为在校学生，我想要注册并登录账号，以便在平台上提问和回答 | P0 | 8 | Sprint 1 | [`US-01`](stories/US-01.feature) · [`US-02`](stories/US-02.feature) | 进行中 |
| US002 | 作为在校学生，我想要浏览问题列表，以便查看别人提出的问题 | P0 | 5 | Sprint 1 | [`US-03`](stories/US-03.feature) | 进行中 |
| US003 | 作为在校学生，我想要发布问题，以便就课程内容寻求解答 | P0 | 8 | Sprint 1 | [`US-04`](stories/US-04.feature) | 进行中 |
| US004 | 作为在校学生，我想要提交回答，以便回复别人的提问 | P0 | 5 | Sprint 1 | `US-04.feature`（回答场景） | 进行中 |
| US005 | 作为在校学生，我想要给问题/回答投票，以便标记有价值的内容 | P1 | 3 | Sprint 1 | [`US05`](stories/US05‑vote‑question.feature) | 待开始 |
| US006 | 作为在校学生，我想要收藏问题，以便快速找回感兴趣的内容 | P1 | 3 | Sprint 1 | [`US06`](stories/US06‑collect‑question.feature) | 待开始 |
| US007 | 作为在校学生，我想要按标签筛选问题，以便找到对应主题的内容 | P1 | 5 | Sprint 1 | [`US07`](stories/US07‑question‑tag‑filter.feature) | 待开始 |
| US008 | 作为提问学生，我想要采纳满意的回答，以便标记问题已解决 | P1 | 5 | Sprint 1 | [`US08`](stories/US08‑accept‑answer.feature) | 待开始 |
| US009 | 作为在校学生，我想要浏览课程目录，以便找到我关心的课程并在其中提问 | P1 | 5 | Sprint 2 | [`US-09`](stories/US-09-course-catalog.feature) | 待开始 |
| US010 | 作为已加入课程的学生，我想要查看"我的课程"，以便快速回到课程继续提问 | P1 | 3 | Sprint 2 | [`US-10`](stories/US-10-my-courses.feature) | 待开始 |
| US011 | 作为在校学生，我想要加入一门课程，以便在该课程下提问并收到通知 | P1 | 5 | Sprint 2 | [`US-11`](stories/US-11-join-course.feature) | 待开始 |
| US012 | 作为在校学生，我想要提问时选择课程，以便问题出现在正确的课程下 | P1 | 3 | Sprint 2 | [`US-12`](stories/US-12-ask-with-course.feature) | 待开始 |

图例：状态取值 = 待开始 / 进行中 / 待评审 / 已完成。

## 二、容量与承诺

| 迭代 | 承诺故事 | 合计 SP | 团队容量 70% | 结论 |
| --- | --- | --- | --- | --- |
| Sprint 1 | US001–US004（P0 主链路） | 26 | 约 28 | ✅ 未超容量，留 2 点余量 |
| Sprint 2 | US009–US012（课程域）+ 问答增量 | 16 + 增量 | 约 28 | ⚠️ 需在 Planning 时与问答增量一起复核 |

## 三、优先级依据（价值 / 成本）

| 象限 | 故事 | 决策 |
| --- | --- | --- |
| 高价值 · 低成本 | US002、US005、US006 | 优先做，构成"能看、能互动"的最小体验 |
| 高价值 · 高成本 | US001、US003 | Sprint 1 必须做，是 Walking Skeleton 的骨架 |
| 中价值 · 低成本 | US010、US012 | Sprint 2 补齐课程维度，成本低但让问答有归属 |
| 中价值 · 高成本 | US009、US011 | 需要新增课程上下文（见 `docs/domain/course-context.md`） |

## 四、变更记录

| 日期 | 版本 | 变更 | 变更人 |
| --- | --- | --- | --- |
| 2026-10-26 | v1.0 | 初版 8 条故事（问答主链路） | 魏志国 |
| 2026-10-28 | v1.1 | ①修正 Markdown 转义与行间空行；②合并重复的 US005；③补齐全部 Story Point；④追加课程域 US009–US012；⑤增加 Gherkin 链接与状态列 | 房国栋 |

## 五、待 PO 确认

1. US009–US012 是否纳入 Sprint 2 承诺范围（当前按"候选"放在 Sprint 2 列）；
2. US004 与 US-04.feature 是否合并为独立故事（现与 US003 共用一份 Gherkin）；
3. 课程是否允许跨学期复用（影响 `CourseCode` 唯一性范围，见 `course-context.md` §6）。
