# 领域事件目录 v1.0

> 负责人：青岑（Dev-1）
> 范围：CampusOverflow · Sprint 0
> 规则：事件名用过去时；中英对照；按时间线排列

## 一、时间线

Identity → Course → Q&A → Reputation → Discovery

## 二、事件清单

| # | 事件（英文，过去时） | 中文 | 上下文 | 聚合 | 触发命令 | 订阅方 / 处理 |
|---|---|---|---|---|---|---|
| 1 | UserRegisteredEvent | 用户已注册 | Identity | User | 注册 | Reputation 开立账户、发初始积分 |
| 2 | UserVerifiedEvent | 用户已认证 | Identity | User | 管理员认证 | Discovery 发认证通知 |
| 3 | UserRoleChangedEvent | 用户角色已变更 | Identity | User | 调整角色 | 刷新权限/会话 |
| 4 | UserDisabledEvent | 用户已禁用 | Identity | User | 禁用账号 | 删除会话、停止通知 |
| 5 | CourseCreatedEvent | 课程已创建 | Identity | Course | 创建课程 | Discovery 更新课程投影 |
| 6 | CourseJoinedEvent | 用户已加入课程 | Identity | Course | 加入课程 | Discovery 成员数 +1 |
| 7 | CourseMemberRoleChangedEvent | 课程成员角色已变更 | Identity | Course | 任命助教 | 刷新课程权限 |
| 8 | QuestionPostedEvent | 问题已发布 | Q&A | Question | 发布问题 | Discovery 插入搜索投影 |
| 9 | QuestionEditedEvent | 问题已编辑 | Q&A | Question | 编辑问题 | Discovery 更新投影文本/标签 |
| 10 | AnswerSubmittedEvent | 回答已提交 | Q&A | Answer | 提交回答 | Reputation 检查徽章；Discovery 回答数 +1、通知提问者 |
| 11 | AnswerEditedEvent | 回答已编辑 | Q&A | Answer | 编辑回答 | Discovery 更新投影 |
| 12 | AnswerAcceptedEvent | 回答已采纳 | Q&A | Question | 采纳回答 | Reputation +15、徽章、悬赏结算；Discovery 标记已采纳、通知回答者 |
| 13 | AnswerEndorsedEvent | 回答已加精 | Q&A | Answer | 教师认证 | Reputation 名师认可；Discovery 通知回答者 |
| 14 | VoteCastEvent | 投票已投出 | Q&A | Vote | 投票/改投/撤销 | Reputation 按差值计分（日上限）；Discovery 得分同步 |
| 15 | CommentPostedEvent | 评论已发布 | Q&A | Comment | 发表评论 | Discovery 解析 @提及并通知 |
| 16 | ContentDeletedEvent | 内容已删除 | Q&A | Question/Answer | 软删除 | Discovery 投影删除、回答数 −1 |
| 17 | ReputationChangedEvent | 声誉已变更 | Reputation | ReputationAccount | 记账 | 排行榜更新周/月/学期 ZSET |
| 18 | BadgeAwardedEvent | 徽章已授予 | Reputation | Badge | 授予徽章 | Discovery 通知获得者 |
| 19 | BountyOpenedEvent | 悬赏已发起 | Reputation | Bounty | 发起悬赏 | Discovery 维护“悬赏中”标记 |
| 20 | BountyAwardedEvent | 悬赏已结算 | Reputation | Bounty | 采纳结算 | Discovery 通知获奖者 |
| 21 | BountyClosedEvent | 悬赏已关闭 | Reputation | Bounty | 关闭/退款 | Discovery 移除“悬赏中” |
| 22 | BountyExpiredEvent | 悬赏已过期 | Reputation | Bounty | 定时任务 | Reputation 退款；Discovery 移除“悬赏中” |
| 23 | ReputationReconciledEvent | 声誉对账已完成 | Reputation | ReputationAccount | 定时对账 | 输出对账结果 |
| 24 | SearchDocumentIndexedEvent | 搜索文档已索引 | Discovery | SearchDocument | 事件投影 | 搜索读模型 |
| 25 | SearchDocumentUpdatedEvent | 搜索文档已更新 | Discovery | SearchDocument | 事件投影 | 搜索读模型 |
| 26 | SearchDocumentRemovedEvent | 搜索文档已移除 | Discovery | SearchDocument | 事件投影 | 搜索读模型 |
| 27 | NotificationCreatedEvent | 通知已创建 | Discovery | Notification | 事件投影 | 通知读模型 |
| 28 | NotificationReadEvent | 通知已读 | Discovery | Notification | 用户操作 | 未读数 −1 |
| 29 | MentionNotifiedEvent | 提及已通知 | Discovery | Notification | 评论 @提及 | 通知读模型 |
| 30 | LeaderboardUpdatedEvent | 排行榜已更新 | Discovery | Leaderboard | ReputationChanged | 排行榜读模型 |
