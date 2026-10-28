# CampusOverflow 上下文映射（Context Map）

> 负责人：青岑（Dev-1）
> 依据：《实验指导书 V3》第 3.2 节

## 一、限界上下文

| 上下文 | 模块 | 职责 |
|---|---|---|
| Identity | `co-identity` | 用户、课程、角色、会话 |
| Q&A | `co-qa` | 问题、回答、投票、评论、采纳 |
| Reputation | `co-reputation` | 声誉账本、徽章、悬赏 |
| Discovery | `co-discovery` | 搜索、通知、排行榜（不拆成独立上下文） |

## 二、上下文关系

| 上游 → 下游 | 关系类型 | 集成方式 |
|---|---|---|
| Identity → Q&A / Reputation / Discovery | OHS/PL | 同步 IdentityApi；异步 UserRegisteredEvent；只给 UserSummary |
| Q&A → Reputation | 客户/供应商（Customer/Supplier） | Answer* / VoteCast 事件；QaQueryApi.findQuestion |
| Q&A → Discovery | 遵奉者（Conformist） | Question* / Answer* / Vote / Comment / ContentDeleted 事件 |
| Reputation → Discovery | 遵奉者（Conformist） | Bounty* / BadgeAwarded 事件 |
| 全部上下文 → Shared Kernel | 共享内核（Shared Kernel） | 依赖 `co-shared-kernel`；修改需全组评审 |

## 三、共享内核

`co-shared-kernel` 提供：Actor、DomainEvent、领域异常、分页。

## 四、说明

- 不存在独立的“检索”“通知”“内容治理”限界上下文
- 检索、通知属于 Discovery 的能力
- 内容审核属于 Q&A 核心域内的业务逻辑