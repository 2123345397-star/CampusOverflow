# 限界上下文映射
1. 身份与课程（Identity，对应模块`co-identity`）
2. 问答核心（Q&A，对应模块`co-qa`）
3. 声誉与激励（Reputation，对应模块`co-reputation`）
4. 内容发现（Discovery，对应模块`co-discovery`，包含站内通知、全文检索能力）

## 上下文关系
- Identity为上游，向Q&A/Reputation/Discovery提供用户与课程身份信息
- Q&A是领域核心，发布问题/回答/投票/评论事件
- Reputation订阅Q&A事件，完成声誉计分、徽章发放、悬赏结算逻辑
- Discovery订阅Q&A/Reputation事件，实现全文搜索、相关推荐、站内通知能力

