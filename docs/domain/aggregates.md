# Lab 3 领域建模：聚合识别与不变式

> 负责人：青岑（Dev-1 后端视角）
> 范围：Q&A 限界上下文
> 依据：`backend/co-qa/co-qa-domain` 现有类与领域测试

## 1. 领域事件（橙色卡，10 个）

| # | 事件（过去时） | 中文 | 所属聚合 |
|---|---|---|---|
| 1 | QuestionPosted | 问题已发布 | Question |
| 2 | QuestionEdited | 问题已编辑 | Question |
| 3 | QuestionDeleted | 问题已删除 | Question |
| 4 | AnswerPosted | 回答已发布 | Answer |
| 5 | AnswerEdited | 回答已编辑 | Answer |
| 6 | AnswerDeleted | 回答已删除 | Answer |
| 7 | AnswerAccepted | 回答已采纳 | Question |
| 8 | AnswerEndorsed | 回答已加精 | Answer |
| 9 | VoteCast | 投票已投出/变更 | Vote |
| 10 | CommentPosted | 评论已发布 | Comment |

时间线：QuestionPosted → AnswerPosted → (VoteCast / CommentPosted) → AnswerAccepted / AnswerEndorsed。

## 2. 命令-事件对照表

| 命令 | 触发角色 | 领域事件 | 聚合 | 关键规则 |
|---|---|---|---|---|
| 发布问题 | 学生/教师 | QuestionPosted | Question | 标题 10–150 字，标签 1–5 个 |
| 编辑问题 | 作者/课程管理者 | QuestionEdited | Question | 仅作者或管理者 |
| 删除问题 | 提问者（无回答时）/管理者 | QuestionDeleted | Question | 有回答时提问者不可删 |
| 发布回答 | 登录用户 | AnswerPosted | Answer | 问题必须存在且未删除 |
| 编辑回答 | 回答作者/管理者 | AnswerEdited | Answer | 仅作者或管理者 |
| 删除回答 | 回答作者/管理者 | AnswerDeleted | Answer | 被采纳的回答作者不可删 |
| 采纳回答 | 提问者/课程教师 | AnswerAccepted | Question | 只能采纳一个，终态 |
| 加精回答 | 课程教师 | AnswerEndorsed | Answer | 只能加精一次 |
| 投出/修改投票 | 登录用户 | VoteCast | Vote | 不能给自己投票，值 ±1 |
| 发表评论 | 登录用户 | CommentPosted | Comment | 同一目标下一层回复 |

## 3. 聚合与不变式

### Question（根：Question）

- 内部值对象：Title、MarkdownBody、Tags/TagName
- 不变式：
    1. 标题必须是 10–150 字，标签去重后必须是 1–5 个。
    2. 一个问题最多只能采纳一个回答；被采纳的回答必须属于该问题且未被删除。
    3. 已有回答的问题不能被提问者删除；已删除的问题不能再被回答。

### Answer（根：Answer）

- 不变式：
    1. 每个回答必须属于一个存在且未删除的问题。
    2. 每个回答最多只能被课程教师加精一次。
    3. 被采纳的回答不能被作者删除；只有作者或课程管理者可以编辑或删除回答。

### Vote（根：Vote）

- 不变式：
    1. 用户不能给自己的问题或回答投票。
    2. 票值只能是 +1 或 -1。
    3. 同一用户对同一目标最多只有一个有效票；重复投票是变更，不是新增。

### Comment（根：Comment）

- 不变式：
    1. 评论必须挂在与父评论相同的目标（问题或回答）下。
    2. 回复最多一层：不能回复一条已经是回复的评论。
    3. 评论正文不能为空，长度不超过 600 个字符。

## 4. 聚合与上下文归属

| 聚合 | 根实体 | 限界上下文 | Maven 模块 |
|---|---|---|---|
| Question / Answer / Vote / Comment | Question 等 | Q&A | co-qa |
| User / Course | User | Identity | co-identity |
| ReputationAccount / Badge / Bounty | ReputationAccount | Reputation | co-reputation |
| Notification / SearchIndex | Notification | Discovery | co-discovery |
| Actor / 事件契约 / 异常 / 分页 | — | Shared Kernel | co-shared-kernel |

上下文关系：

- Q&A → Identity：客户-供应商（通过 identity-api 取用户/角色，ACL 防腐）
- Q&A → Reputation：事件驱动（AnswerAccepted / VoteCast → 声誉账本）
- Q&A → Discovery：事件驱动（问题/回答变更 → 搜索索引；回答/采纳 → 通知）
- Identity → Reputation：客户-供应商
- 全体共享 co-shared-kernel

## 5. 聚合之间的引用方式

- Question 只保存 acceptedAnswerId，不持有 Answer 对象。
- Answer 只保存 questionId / authorId，不持有 Question 或 User 对象。
- Vote 只保存 voterId / targetId / targetType。
- Comment 只保存 targetId / parentId / authorId。
- 所有跨聚合引用均通过 ID，不直接持有对象。

## 6. 待 PO 确认的问题

| 问题 | 现有代码/测试的答案 | 建议写入不变式 |
|---|---|---|
| 已采纳的回答能否取消采纳？ | 测试只允许采纳一次，代码没有取消采纳方法 | 采纳是终态，不可取消 |
| 标签是否必选？ | `requiresOneToFiveTags` 测试要求 1–5 个 | 标签必选，1–5 个 |