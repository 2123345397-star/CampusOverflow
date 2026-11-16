# 团队工作约定（Team Agreement）

> 团队：TeamX　项目：CampusOverflow　版本：v1.0
> 覆盖会议、响应时效、分支命名、提交规范；全员签字后生效。

## 一、会议约定

| 会议 | 时间/频率 | 要求 | 产出 |
| --- | --- | --- | --- |
| 每日站会 | 每个工作日 12:30 | 控制在 15 分钟内，说明昨天做了什么、今天做什么、有什么阻塞 | 阻塞项登记并指定处理人 |
| Sprint Planning | Sprint 开始时 | 全员参加，确认 Sprint 目标与承诺故事 | Sprint 计划与任务拆分 |
| Sprint Review | Sprint 结束时 | 演示可工作增量，由 PO 验收 | 通过/不通过结论 |
| Sprint Retro | Review 后 | 复盘流程，至少落地 1 条可验证改进项 | 改进项清单 |

确需缺席须提前在群里请假，并补看会议纪要。

## 二、响应时效

- 工作日群消息：2 小时内回复。
- PR 评审意见：24 小时内给出或响应。
- 遇到阻塞：超过 2 小时无法自行解决，必须在群内升级，不隐瞒、不拖延。

## 三、分支命名

- main、develop 为受保护分支，禁止直接 push。
- 功能分支命名：`feature/<姓名拼音>-<主题>`，例如 `feature/chenmingzhe-codeowners`。
- 杂项/配置类用 `chore/<主题>`，文档类用 `docs/<主题>`。
- 分支合并后立即删除，保持仓库整洁。

## 四、提交规范

- Commit message 格式：`<type>: <描述>`，type 取 `feat / fix / docs / test / refactor / chore`。
- 描述简洁说明“做了什么”，中文或英文均可，全组保持一致。
- 小步提交，单个 PR 尽量不超过 400 行。
- 每个 PR 至少 1 人 Approve（LGTM）后由 SM 合并；新代码必须带单元测试。
- 禁止提交明文密码、密钥、node_modules/ 与构建产物。

## 五、全员签字

| 姓名 | 角色 | GitHub 账号 | 签字 | 日期 |
| --- | --- | --- | --- | --- |
| 魏志国 | PO | @2123345397-star | 已阅读并同意 | 2026-11-03 |
| 陈明哲 | SM | @cmz666-BOT | 已阅读并同意 | 2026-11-03 |
| 刘俊琒 | SM | @Liujunfeng520 | 已阅读并同意 | 2026-11-03 |
| 青岑 | Dev/QA | @qing963666 | 已阅读并同意 | 2026-11-03 |
| 房国栋 | Developer | @2177023844 | 已阅读并同意 | 2026-11-03 |

全员在本 PR 中评论「同意 + 姓名」即视为签字；本文件合并后约定正式生效。

## 六、CI 红灯与事件重放

- **红灯认领**：CI 变红后 30 分钟内在群里认领，先 revert 恢复绿色，再排查原因；不允许 @Disabled 或删测试换绿色。
- **重放单个事件**（把 `<id>` 换成目标事件 ID）：

  ```sql
  UPDATE outbox_event SET published_at = NULL, attempts = 0 WHERE id = <id>;
  ```

- **重放全部未投递事件**：`UPDATE outbox_event SET published_at = NULL, attempts = 0;`
- 消费端按 `(event_id, user_id, reason)` 唯一键去重，重放不会重复记账或重复发徽章（见 `ReputationEventHandlersTest` 的幂等用例）。

