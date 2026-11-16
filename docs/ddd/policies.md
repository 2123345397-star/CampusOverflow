# 策略（Policy）

> 负责人：青岑（Dev-1）
> 格式：当 X 事件发生，则 Y

1. 当 UserRegisteredEvent 发生，则 Reputation 开立声誉账户，并按角色发放初始积分。
2. 当 AnswerSubmittedEvent 发生，则 Discovery 回答数 +1 并通知提问者；Reputation 检查“初来乍到”徽章。
3. 当 AnswerAcceptedEvent 发生，则 Reputation 给回答者 +15 并检查徽章，Discovery 标记“已采纳”并通知回答者。
4. 当 VoteCastEvent 发生，则 Reputation 按票值差值计分并受每日上限约束，Discovery 同步问题/回答得分。
5. 当 CommentPostedEvent 发生，则 Discovery 解析 @提及并创建通知。
6. 当 ContentDeletedEvent 发生，则 Discovery 标记投影删除并将回答数 −1。
7. 当 BountyExpiredEvent 或 BountyClosedEvent 发生，则 Reputation 解冻/退还积分，Discovery 移除“悬赏中”。
