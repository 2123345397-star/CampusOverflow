# Lab 4 · 回答与采纳端到端证据（B 魏志国）

> 日期：2026-11-12　分支：`develop`（AnswerCommandService 已由 #89 合入）
> 范围：POST 问题 → POST 回答 → 采纳 → GET 详情 → MySQL 落库确认

## 一、运行环境

- Docker Desktop 29.9.2（Linux 引擎）
- MySQL 8.0.39（容器 `co-mysql`，映射 **3307**；本机 3306 已被本地 mysqld 占用）
- Redis 7-alpine（容器 `co-redis`，6379）
- 后端：`co-bootstrap/campus-overflow.jar`，`dev` profile，8080

> 注意：起始模板的 JDBC URL 使用 `characterEncoding=utf8mb4`，与 MySQL Connector/J 9.1 不兼容，
> 会报 `Unsupported character encoding 'utf8mb4'`。本 PR 已改为 `characterEncoding=UTF-8`。
> 下列证据是在修复前采集的，当时用 `SPRING_DATASOURCE_URL` 临时覆盖为 UTF-8 启动；
> 合并后可直接按默认配置启动，无需覆盖。

## 二、接口结果

| 步骤 | 请求 | 结果 |
| --- | --- | --- |
| 1 | `POST /api/v1/questions`（用户 A） | `201`，`{"id":1}` |
| 2 | `POST /api/v1/questions/1/answers`（用户 B） | `201`，`{"id":1}` |
| 3 | `PUT /api/v1/questions/1/accepted-answer`（用户 A） | `204` |
| 4 | `GET /api/v1/questions/1` | `200`，`answerCount=1`，`acceptedAnswerId=1`，`answers.length=1` |

说明：本项目用服务端 SESSION + CSRF Cookie，写请求需带 `X-XSRF-TOKEN`，值取 `XSRF-TOKEN` Cookie 的原始值；
`/api/v1/auth/csrf` 返回的 JSON token 是 XOR 掩码值，不能直接用于请求头。

## 三、数据库确认

```sql
select id, answer_count, accepted_answer_id from campus_overflow.qa_question;
-- 1 | 1 | 1

select id, question_id, author_id, score from campus_overflow.qa_answer;
-- 1 | 1 | 2 | 0
```

## 四、截图

- [POST 问题](screenshots/lab4-01-post-question.png)
- [POST 回答](screenshots/lab4-02-post-answer.png)
- [GET 详情（answerCount=1、acceptedAnswerId=1）](screenshots/lab4-03-get-detail.png)
- [MySQL 落库确认](screenshots/lab4-04-mysql-select.png)

## 五、accept 五个前置校验顺序

1. 问题未删除（QA-1020）
2. 当前用户是提问者或课程管理者（否则 403 QA-3001）
3. 问题尚未采纳过（否则 409 QA-2002）
4. 答案属于本题（否则 422 QA-1010）
5. 答案未删除（否则 422 QA-1011）
6. 通过后 `acceptedAnswerId = answer.id()`

对应测试：`QuestionTest.Accepting` 5 条、`AnswerAndVoteServiceTest` 4/4 全绿。
