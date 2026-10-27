# 统一语言术语表 v1.0
> 统一语言：中文在前，英文对照，标注定义与所属限界上下文

| 中文 | 英文 | 业务定义 | 所属限界上下文 |
| ---- | ---- | ---- | ---- |
| 用户 | User | 平台注册使用者 | 身份上下文(co-identity) |
| 问题 | Question | 用户发布、等待他人解答的提问 | 问答上下文(co-qa) |
| 回答 | Answer | 用户针对问题给出的回复内容 | 问答上下文(co-qa) |
| 评论 | Comment | 依附于问题或回答的简短留言 | 问答上下文(co-qa) |
| 点赞 | Upvote | 用户对问题/回答表达认可 | 问答上下文(co-qa) |
| 点踩 | Downvote | 用户对问题/回答表达不认可 | 问答上下文(co-qa) |
| 采纳回答 | AcceptAnswer | 提问者选定一个回答作为正确答案 | 问答上下文(co-qa) |
| 发布问题 | PublishQuestion | 用户提交一条新问题 | 问答上下文(co-qa) |
| 提交回答 | SubmitAnswer | 用户针对问题提交回答 | 问答上下文(co-qa) |
| 编辑问题 | EditQuestion | 修改已发布问题内容 | 问答上下文(co-qa) |
| 编辑回答 | EditAnswer | 修改已提交回答内容 | 问答上下文(co-qa) |
| 删除问题 | DeleteQuestion | 用户移除自己发布的问题 | 问答上下文(co-qa) |
| 删除回答 | DeleteAnswer | 用户移除自己提交的回答 | 问答上下文(co-qa) |
| 声誉值 | Reputation | 用户在平台的积分，由互动行为增减 | 声誉上下文(co-reputation) |
| 声誉增加 | ReputationIncreased | 用户获得正向互动，声誉上涨 | 声誉上下文(co-reputation) |
| 声誉扣减 | ReputationDecreased | 用户违规或删除优质内容，声誉下降 | 声誉上下文(co-reputation) |
| 徽章 | Badge | 用户达成条件后获得的荣誉标识 | 声誉上下文(co-reputation) |
| 授予徽章 | GrantBadge | 系统自动给用户发放徽章 | 声誉上下文(co-reputation) |
| 收回徽章 | RevokeBadge | 条件不满足时回收徽章 | 声誉上下文(co-reputation) |
| 通知 | Notification | 系统推送给用户的消息 | 通知上下文 |
| 发送通知 | SendNotification | 系统生成并推送消息 | 通知上下文 |
| 通知已读 | MarkNotificationRead | 用户查看消息标记为已读 | 通知上下文 |
| 角色 | Role | 用户权限分组，普通用户、管理员等 | 身份上下文(co-identity) |
| 账号 | Account | 用户登录凭证，绑定手机号 | 身份上下文(co-identity) |
| 注册 | Register | 新用户创建账号 | 身份上下文(co-identity) |
| 登录 | Login | 用户校验凭证进入系统 | 身份上下文(co-identity) |
| 权限 | Permission | 用户可执行操作的范围 | 身份上下文(co-identity) |
| 聚合根 | AggregateRoot | DDD聚合的对外访问入口 | 领域通用 |
| 限界上下文 | BoundedContext | 独立业务子域边界 | 领域通用 |
| 领域事件 | DomainEvent | 领域内发生、可被其他模块感知的事情 | 领域通用 |
| 命令 | Command | 用户发起，要求系统执行动作的请求 | 领域通用 |
