# Lab 4 总实验报告（团队汇总）

> 课程：《软件工程》CampusOverflow 贯穿项目  
> 实验：Lab 4 · 后端工程骨架与领域代码（V3 指导书 Sprint 1）  
> 团队：cs2412-team04  
> 快照日期：2026-11-15  
> 汇总人：青岑（C · 投票与评论，Sprint 2 SM）

---

## 1. 实验目标

在 V3 起始仓库已有的 27 个 Maven 模块、Flyway、统一异常与 CI 基础上，补完 Q&A 上下文的领域层与应用层，使“提问 → 回答 → 采纳 → 投票/评论”主链路可以在 MySQL/Redis 上真实落库并返回。

本实验的验收重点不是“能不能启动”，而是：

- 六个限界上下文按 domain / application / infrastructure / interfaces 分层；
- 聚合通过工厂方法和聚合方法维护不变式，不暴露 Setter；
- 领域异常使用统一错误码，而不是 `IllegalArgumentException`；
- 至少一条端到端接口链路在数据库中留下真实数据；
- 测试、CI 与 PR 评审记录可追溯。

---

## 2. 实验环境

| 组件 | 版本 |
| --- | --- |
| JDK | 21.0.10 LTS |
| Maven | 3.9.2 |
| Node / npm | 26.13.0 / 11.20.0 |
| Docker Desktop | 29.9.2（WSL2） |
| MySQL | 8.0.39（Docker `co-mysql`） |
| Redis | 7-alpine（Docker `co-redis`） |
| IDE | IntelliJ IDEA Community 2023.2.2 |

中间件启动：

```bash
docker compose -f deploy/docker-compose.yml up -d mysql redis
```

后端启动：

```bash
cd backend
mvn spring-boot:run -pl co-bootstrap
```

环境验证证据：[`docs/lab1-submission/env/env-qingcen.png`](../lab1-submission/env/env-qingcen.png)。

---

## 3. 分工与交付

对应 V3 任务 T1-01～T1-15：

| 任务 | 内容 | 负责人 | 交付状态 |
| --- | --- | --- | --- |
| T1-01～02 | MarkdownBody、Tags、Question 聚合 | A 刘俊琒 | 已合入 `develop` |
| T1-03～04 | Question.accept、Answer 聚合 | B 魏志国 | 已合入 `develop` |
| T1-05 | Vote、Comment 聚合 | C 青岑 | 已合入 `develop` |
| T1-06 | 领域边界测试（12 条） | E 陈明哲 | 已合入 `develop` |
| T1-07～08 | QuestionCommandService、AnswerCommandService | A、B | 已合入 `develop` |
| T1-09 | VoteService、CommentService | C 青岑 | 已合入 `develop` |
| T1-10～11 | QuestionQueryService、QaQueryApiImpl | A 刘俊琒 | 已合入 `develop` |
| T1-12 | 行走骨架与 tech-debt Issue | E 陈明哲 | 已完成 |
| T1-13 | 前端权限按钮与错误码提示 | D 房国栋 | 已实现 |
| T1-14 | CI 纳入 Q&A、覆盖率 0.70 | E 陈明哲 | 已开启 |
| T1-15 | `http/qa.http` 问题侧/投票侧、演示与冒烟 | A、C、D、E | 投票/评论侧已完成，问题侧与冒烟串联待补齐 |
| Step 1 | 骨架核对 | E 陈明哲 | 已完成 |
| Step 3 | JPA/Flyway/异常/日志核对 | B、E | 已完成 |
| Step 4 | POST /api/v1/questions 端到端 | B、D、E | 已完成，截图见 A 的 Lab 4 报告 |

各角色的个人交付索引：

- A：[`docs/lab4-submission/README.md`](README.md)
- C：[`docs/lab4-submission/screenshots/qingcen/README.md`](screenshots/qingcen/README.md)

---

## 4. 架构与关键实现

### 4.1 分层

```text
co-bootstrap            启动装配、安全链、全局异常
co-identity             身份、会话、课程（参考实现）
co-qa                   问题、回答、投票、评论（本实验主战场）
co-reputation           声誉、徽章、悬赏
co-discovery            搜索、通知、相关推荐
co-shared-kernel        领域异常、Actor、事件契约、分页
```

约束：领域层不依赖 Spring/JPA；跨上下文只依赖对方的 `*-api`；控制器只做参数校验和状态码映射。

### 4.2 Q&A 主链路

```text
POST /api/v1/questions
  → QuestionCommandService.post
  → Question.post（标题/正文/标签不变式）
  → QuestionRepository.save（JPA）
  → QuestionPostedEvent（事务性发件箱）
  → 列表缓存失效 + 审计

POST /api/v1/questions/{id}/answers
  → AnswerCommandService.submit
  → Answer.submit（问题存在且未删除）
  → AnswerPostedEvent

PUT /api/v1/questions/{id}/accepted-answer
  → Question.accept（只能采纳一个、终态）
  → AnswerAcceptedEvent
  → Reputation 异步结算
```

### 4.3 Vote / Comment

- 投票值只允许 `+1/-1`，`0` 表示撤销；同一用户对同一目标最多一条有效票。
- 改投不是覆盖：先读旧值，计算 `delta = new - old`，再原子更新目标分数。
- 重复投同一值直接返回 `scoreDelta = 0`，不重复发事件。
- 评论正文 2–600 字；回复最多一层，父评论必须属于同一目标；删除顶层评论时级联删除其回复。
- 并发重复投票由数据库唯一键 `uk_vote(voter_id, target_type, target_id)` 兜底，冲突映射为 `409 SYS-409-DUPLICATE`。

---

## 5. 测试与验证

### 5.1 单元测试

```text
co-qa-domain:      42 tests, 0 failures
co-qa-application: 32 tests, 0 failures
QingcenVoteCommentServiceTest: 9 tests, 0 failures
```

重点覆盖：

- 标题/正文/标签边界；
- 采纳唯一性与终态；
- 投票自投、改投 delta、撤票、幂等；
- 评论父级校验、删除级联、权限；
- 并发投票只保留一条记录；
- Bounty 开放/结算/退款。

### 5.2 CI

`develop` 分支 CI #50 已通过：后端构建与验证、前端构建、容器镜像构建。仓库已改为 Public，Actions 额度问题解除。

### 5.3 端到端证据

- POST 201 与 GET 详情 200：[`screenshots/qa-e2e-post-get.png`](screenshots/qa-e2e-post-get.png)
- 列表查询、Redis 缓存键与 `qa_question` 落库：[`screenshots/qa-e2e-list-and-db.png`](screenshots/qa-e2e-list-and-db.png)
- 投票/评论四步顺序执行：[`screenshots/qingcen/qa-vote-comment-terminal.png`](screenshots/qingcen/qa-vote-comment-terminal.png)
- 完整执行日志：[`screenshots/qingcen/qa-vote-comment.log`](screenshots/qingcen/qa-vote-comment.log)

实测结果：

```text
POST /api/v1/auth/login   -> 200
PUT  /api/v1/votes        -> 200  {"myVote":1,"scoreDelta":1}
PUT  /api/v1/votes        -> 200  {"myVote":-1,"scoreDelta":-2}
PUT  /api/v1/votes        -> 200  {"myVote":0,"scoreDelta":1}
POST /api/v1/comments     -> 201  {"id":9}
```

---

## 6. 问题与解决

| 问题 | 现象 | 解决 |
| --- | --- | --- |
| JDBC 字符集 | 启动时报 `Unsupported character encoding 'utf8mb4'` | `characterEncoding` 改为 Java 字符集 `UTF-8`，数据库仍用 utf8mb4 |
| CSRF 校验 | `.http` 脚本用响应体 token 请求返回 403 | 服务端按原始 Cookie 校验；脚本改为从 `Set-Cookie` 取 `XSRF-TOKEN` 原值 |
| Docker 拉镜像超时 | `registry-1.docker.io` 连接超时 | 配置 `registry-mirrors`（daocloud / 1ms.run），无需代理 |
| Actions 额度 | CI job 不启动 | 仓库改为 Public，标准 runner 免费 |
| 重复投票 | 并发写可能产生重复记录 | `uk_vote` 唯一键兜底，冲突返回 409；同一事务回滚分数调整 |
| 评论孤儿 | 删除父评论后回复仍指向不存在的父级 | 删除顶层评论时级联删除回复 |

---

## 7. 交付物索引

| 交付物 | 位置 |
| --- | --- |
| 团队代码 | GitHub `hbuas-se-2026fall/cs2412-team04-campus-overflow` |
| A 角色报告 | `docs/lab4-submission/README.md` |
| C 角色证据 | `docs/lab4-submission/screenshots/qingcen/` |
| HTTP 请求 | `http/auth.http`、`http/qa.http` |
| 环境验证 | `docs/lab1-submission/env/env-qingcen.png` |
| Sprint 记录 | `docs/scrum/sprint-1.md`、`docs/scrum/sprint-2.md` |
| 错误码 | `docs/错误码.md` |
| 聚合与不变式 | `docs/ddd/aggregates.md` |

---

## 8. 结论

Lab 4 的 Q&A 主链路（提问 → 回答 → 采纳 → 投票/评论）已完成并合入 `develop`；领域层、应用层、接口层与数据库迁移均有测试或端到端证据支撑。

Sprint 2 的 Reputation/悬赏由各成员继续推进；S3 Discovery 属于后续 Sprint。当前已知的团队待办与缺陷统一记录在 `docs/qa/defects.md`，按迭代收尾处理，不在本报告重复展开。
