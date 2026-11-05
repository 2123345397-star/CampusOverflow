# Lab 4 提交物 · 角色卡 A（Q&A 问题侧）

> 姓名：刘俊琒　学号：2024121117　角色：A · Q&A 问题侧（Sprint 1 的 SM）
> 分支：`feat/qa-question`　PR：[#86](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/86)

本页是 Lab 4（后端工程骨架与领域代码）中**我负责部分**的交付索引：改了哪些代码、怎么验的、还剩什么。

---

## 1. 交付清单（T1-01 / T1-02 / T1-07 / T1-10 / T1-11）

| 任务 | 文件 | 内容 |
| --- | --- | --- |
| T1-01 值对象 | `co-qa-domain/.../MarkdownBody.java`、`Tags.java` | 正文按码点校验（问题 20–30000、回答 10–30000）；标签去重、保序、转小写，归一化后再判 1–5 个 |
| T1-02 Question 聚合 | `co-qa-domain/.../Question.java` | `post` / `edit` / `accept` / `softDelete` / `ensureOpenForAnswers` / `ensureNotDeleted`；删回答保护 QA-1013、重复采纳 QA-2002、已删统一 QA-1020 |
| T1-07 命令服务 | `co-qa-application/.../command/QuestionCommandService.java` | `post` 校验课程 → 构造值对象 → `save` → 标签用量 +1 → `QuestionPostedEvent`（正文走 `toPlainText`）→ 列表缓存失效 → 审计；`edit` 按标签差集 ±1；`delete` 软删 + `ContentDeletedEvent`；`load` 不存在或已删统一 404 |
| T1-10 查询服务 | `co-qa-application/.../query/QuestionQueryService.java` | `list` 缓存优先 + `findUsers` 批量补作者 + 摘要 160 字；`detail` 渲染安全 HTML、回答按「已采纳 → 得分降序 → 时间升序」、投票 key `QUESTION:{id}`/`ANSWER:{id}`、权限视图、浏览数只写 Redis；`popularTags` limit 夹在 1–50 |
| T1-11 发布语言 | `co-qa-application/.../QaQueryApiImpl.java` | `findQuestion` 把聚合映射成 `QuestionRef`（id / 作者 / 课程 / 标题 / 采纳回答 / 已删除标记）；查不到返回 `empty`，不抛异常 |

## 2. 测试

| 测试类 | 条数 | 结果 | 覆盖点 |
| --- | --- | --- | --- |
| `co-qa-domain` 全模块 | 42 | ✅ 全绿 | 本次交付时 33 条（含新增 12 条边界用例），合并后随队友补充增至 42 |
| `QuestionTest`（含 `Boundaries`） | 24 | ✅ | 标题 9/10/150/151、正文 19/20/30000/30001、标签去重后 5/6、重复采纳、已删问题编辑 |
| `QuestionCommandServiceTest` | 9 | ✅ | 事件恰好一次、标签差集、缓存失效、404/403/422 分支 |
| `QuestionQueryServiceTest` | 6 | ✅ | 缓存旁路、作者回填、投票键、答案排序、limit 夹取 |
| `QaQueryApiImplTest` | 4 | ✅ | 字段映射、公开提问的空值、不存在返回 empty、已删标记 |

运行：

```bash
mvn -f backend/pom.xml -pl co-qa/co-qa-domain -am test
mvn -f backend/pom.xml -pl co-qa/co-qa-application -am test \
    -Dtest='QaQueryApiImplTest,QuestionCommandServiceTest,QuestionQueryServiceTest'
```

> 交付时 `co-qa-application` 全量 28 条中有 2 条红（`AnswerAndVoteServiceTest` 的 `submitAnswerPublishesEventAndBumpsCounter`、`onlyAskerOrCourseManagerCanAccept`）。
> 这两条属 B（魏志国）的 T1-03，在**干净的 `develop` 工作树**上同样失败（断言期望 1L/2L 与实际 2L/1L 对调），不是本 PR 引入；已在 PR 里留给 B 处理，**后由 B 的 PR #89 修复，合并后 28 条全绿**。

## 3. 端到端验证（POST → GET，数据库确认落行）

环境：MySQL 8.0.44 + Redis 5.0.14（Windows 移植版，本机实例），`dev` profile，Flyway 建表；账号 `liujf`（教师）建课 `CS2001 数据结构` 后发布问题。

![POST 201 与 GET 详情 200](screenshots/qa-e2e-post-get.png)

![列表查询、Redis 缓存键与 qa_question 落库行](screenshots/qa-e2e-list-and-db.png)

关键结果：

- `POST /api/v1/questions` → `201`，返回 `{"id":1}`；
- `GET /api/v1/questions/1` → `200`，`bodyHtml` 已渲染、`permissions.canEdit/canManage` 为真；
- `GET /api/v1/questions?courseId=1` → `total=1`，Redis 出现 `qa:list:1:1:1:20`（列表缓存）与 `qa:views:pending`（浏览数缓冲）；
- `SELECT ... FROM qa_question` → 恰好一行，`answer_count=0`、`deleted_at=NULL`。

## 4. 流程与 SM 产出

作为 Sprint 1 的 SM，评审前需要的文档已补齐：

- Sprint 1 执行记录（计划会结论、两次站会、风险与对策）：[`docs/scrum/sprint-1.md`](../scrum/sprint-1.md)
- 燃尽图（迭代范围 40 SP，11-05 收盘剩余 25）：[`docs/scrum/sprint-1-burndown.png`](../scrum/sprint-1-burndown.png)
- Sprint 1 看板（T2-01 / T2-03 / T3-01 / T4-01 已推进到 Done）：[`docs/sprint-1-backlog.png`](../sprint-1-backlog.png)
- PR 已挂 `Closes #32, #33, #34`（PB-06 发布问题 / PB-07 列表 / PB-08 详情）。

## 5. 合并与验证结果

- **PR #86 已于 2026-11-05 11:00 合并进 `develop`**（合并提交 `3f96258`，8 个提交、14 个文件、+1093/−22）；评审记录：@qing963666、@2123345397-star 均为 `APPROVED`。
- 合并后切到 `develop` 重跑：`co-qa-domain` **42 条**、`co-qa-application` **28 条**，全部通过。此前 2 条红（`AnswerAndVoteServiceTest`）已由 B 的 `fix/lab4-answer-events`（PR #89）修掉。
- Issue 关闭：PB-06 #32 随合并自动关闭；**#33 / #34 由我补关**（PR 里一条 `Closes #32, #33, #34` 只自动关了首个编号，已纠正写法，今后一条 `Closes` 只带一个编号）。

## 6. 后续（Sprint 1 剩余）

- 燃尽图按每次站会续绘，D3（11-06）起数据待补。
- Sprint 1 范围 PB-06～PB-19 中，PB-09～PB-13 问题侧已随 #86 落地，回答/投票/评论由 B、C 推进，通知与前端由 C、D 推进。
