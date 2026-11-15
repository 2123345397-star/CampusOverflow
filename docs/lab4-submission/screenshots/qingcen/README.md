# 青岑 · Lab 4 证据归档

本目录集中存放 C 角色（投票与评论）在 Lab 4 的执行证据。

## 证据清单

| 文件 | 内容 | 状态 |
| --- | --- | --- |
| `qa-vote-comment.log` | 顺序执行登录、投票 +1、改投 -1、撤票、评论的完整执行日志 | ✅ 完整 |
| `qa-vote-comment-terminal.png` | 终端完整截图：5 个请求 + Summary | ✅ 完整 |
| `env-qingcen.png` | Java 21 / Maven 3.9.2 / Node / Docker 环境验证截图（从 Lab 1 E3 证据复制归档） | ✅ |
| `env-qingcen.txt` | 环境版本文本记录 | ✅ |
| `mvn-test-qingcen.png` | `co-qa-domain` 42 条 + `co-qa-application` 32 条全绿、`BUILD SUCCESS` | ✅ |

完整执行结果（见日志末尾 Summary）：

```text
POST /api/v1/auth/login       -> 200
PUT  /api/v1/votes            -> 200  {"myVote":1,"scoreDelta":1}
PUT  /api/v1/votes            -> 200  {"myVote":-1,"scoreDelta":-2}
PUT  /api/v1/votes            -> 200  {"myVote":0,"scoreDelta":1}
POST /api/v1/comments         -> 201  {"id":10}
```

> 截图与日志是两次独立运行，评论 id 会逐次递增；状态码、`myVote` 与 `scoreDelta` 一致。

## 课后补充任务

| 任务 | 实现 / 测试 | 结果 |
| --- | --- | --- |
| 同一用户并发投两次 | `QingcenVoteCommentServiceTest.concurrentVotesKeepSingleRecord`：用屏障让两个线程都先读到“无票”，再同时插入；仓储模拟数据库唯一键 `uk_vote` | 只保留 1 条记录，1 次成功、1 次唯一键冲突，分数只加 1 |
| 父评论删除级联 | `CommentService.delete` + `DELETE /api/v1/comments/{id}`；`deletingTopLevelCommentCascadesReplies`、`deletingReplyLeavesParentIntact`、`onlyAuthorOrAdminCanDeleteComment` | 删除顶层评论时连带删除回复；只删回复不误删父级；非作者/非管理员返回 403 |

测试命令与结果：

```text
mvn -pl co-qa/co-qa-domain,co-qa/co-qa-application test
co-qa-domain:      42 tests, 0 failures
co-qa-application: 32 tests, 0 failures（QingcenVoteCommentServiceTest 9 条）
```

## 复现方式

前提：MySQL/Redis 容器 healthy，后端以 dev profile 运行在 `http://localhost:8080`。

```powershell
powershell -ExecutionPolicy Bypass -File scripts\qa-smoke.ps1
```

脚本等价于 `http/qa.http` 的第 0～4 步，使用 SESSION + CSRF：
CSRF 原始 token 取自 `XSRF-TOKEN` Cookie（响应体里的是 XOR 掩码值）。

## 相关文件

- `http/qa.http`：投票 +1、改投 -1、撤票、评论四个请求
- `http/auth.http`：CSRF 获取与登录
- 环境验证截图原始位置：[`docs/lab1-submission/env/env-qingcen.png`](../../../lab1-submission/env/env-qingcen.png)（本目录已复制一份）
