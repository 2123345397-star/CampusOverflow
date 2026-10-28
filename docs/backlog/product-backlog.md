# Product Backlog 产品待办列表
项目：CampusOverflow 校园问答平台
范围：Sprint0 启动 + Sprint1 问答核心

| ID    | 用户故事 | 优先级 | SP | 归属迭代 |
| ----- | -------- | ------ | -- | -------- |
| PB-01 | 搭建团队私有仓库、配置受保护分支与CI看板 | Must | 3 | Sprint0 |
| PB-02 | 本地环境一键启动MySQL/Redis、前后端跑通 | Must | 2 | Sprint0 |
| PB-03 | 完成DDD事件风暴、产出统一语言与上下文映射 | Must | 3 | Sprint0 |
| PB-04 | 配置渐进式CI质量门禁流水线 | Must | 2 | Sprint0 |
| PB-05 | 完成注册、登录、加入课程基础身份流程 | Must | 3 | Sprint0 |
| PB-06 | 学生发布带标签、关联课程的求助问题 | Must | 5 | Sprint1 |
| PB-07 | 用户分页浏览公开问题列表 | Must | 3 | Sprint1 |
| PB-08 | 用户查看问题详情（含回答、评论、投票状态） | Must | 5 | Sprint1 |
| PB-09 | 学生对未删除问题提交回答 | Must | 3 | Sprint1 |
| PB-10 | 提问者/课程管理者采纳最佳答案标记问题已解决 | Must | 5 | Sprint1 |
| PB-11 | 用户对问题/回答投票、改投、撤销 | Must | 5 | Sprint1 |
| PB-12 | 作者/课程管理者编辑问题和回答内容 | Should | 3 | Sprint1 |
| PB-13 | 作者/课程管理者软删除问题和回答 | Should | 3 | Sprint1 |
| PB-15 | 用户对问题/回答发表一层评论回复 | Must | 3 | Sprint1 |

---

## 与验收标准文件的对应

`.feature` 文件按故事命名，与本文档编号对应如下（无对应文件的条目在 Sprint 2 补齐）：

| Backlog | 说明 | 验收标准 |
| --- | --- | --- |
| PB-05 | 注册、登录、加入课程 | `US-01.feature` |
| PB-06 | 发布问题 | `US-02.feature` |
| PB-07 | 问题列表 | `US-03.feature` |
| PB-08 | 问题详情 | `US-04.feature` |
| PB-09 | 提交回答 | 待补 `US-09.feature` |
| PB-10 | 采纳答案 | `US-08.feature` |
| PB-11 | 投票 | `US-05.feature` |
| PB-15 | 评论 | 待补 |
