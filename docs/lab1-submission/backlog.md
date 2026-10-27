# TeamX 初始 Backlog（Lab 1 看板卡片）

> 用途：按本表在 GitHub Projects / Gitee 项目中建卡片，作为团队初始 Backlog。
> Lab 1 要求：看板截图时 `Backlog`（或 `To Do`）列至少 **5 条**可见。
> 建议列：`Backlog`、`In Progress`、`In Review`、`Done`；每人同时最多 2 张 `In Progress`。

## Sprint 0（第 1–2 周）：跑通与对齐

| ID | 标题 | 类型 | 优先级 | 负责人 | 验收标准 |
| --- | --- | --- | --- | --- | --- |
| S0-01 | 环境搭建与冒烟验证 | 任务 | P0 | 全员 | Docker 起 MySQL/Redis；`mvn test` 能执行（红是预期）；后端 dev profile 启动；前端 `npm run dev` 打开 5173；Swagger 可访问；提交命令与截图 |
| S0-02 | 架构理解笔记 | 文档 | P0 | 魏志国 | 读完 `docs/adr/` 8 条与 arc42 第 5/6/10 章；产出四上下文边界、依赖方向、一条关键架构约束的笔记 |
| S0-03 | 团队章程与角色承诺书 | 文档 | P0 | 陈明哲 | `docs/team/charter-v1.0.md` 与 `role-commitment.md` 完成；全员确认；合并提交含签名 |
| S0-04 | 分支保护与协作工具链 | 任务 | P0 | 刘俊琒 | `main` 开启保护（禁直推、PR + 1 审批、CI 必过）；看板四列建好；IM 群拉齐全员 |
| S0-05 | Git 协作首次演练 | 任务 | P0 | 全员 | 每人一个 `feature/<拼音>-hello` 分支与 `docs/team/hello-<拼音>.md`；每人一个 PR；队友 LGTM 后由 SM 合并；`main` 上 hello 文档数 = 5 |
| S0-06 | CI 基线验证 | 任务 | P1 | 房国栋 | 推送后 GitHub Actions 跑通；记录一次成功流水线链接；失败则列出原因与修复 PR |
| S0-07 | 风险与问题清单 | 文档 | P1 | 青岑 | 至少 5 条风险（技术/协作/进度），含影响、概率、应对措施、负责人 |
| S0-08 | 5 分钟演示脚本 | 文档 | P2 | 青岑 | 串起“提问 → 回答 → 采纳”演示路径，标注当前可演示/不可演示部分 |

## Sprint 1（第 3–6 周）：Identity 增量

| ID | 标题 | 类型 | 优先级 | 验收标准 |
| --- | --- | --- | --- | --- |
| S1-01 | 选课审批需求与领域设计 | 需求 | P0 | 用户故事 + Gherkin 验收标准；选课审批状态机；标注对 Identity 现有模型的影响 |
| S1-02 | 选课审批后端实现 | 功能 | P0 | 领域规则有单测；应用层用例通过；REST 接口进 Swagger；ArchUnit 通过 |
| S1-03 | 个人资料扩展 | 功能 | P1 | 头像/简介字段；前后端校验；错误码符合 `docs/错误码.md` |
| S1-04 | 前端登录注册走查 | 任务 | P1 | 登录、注册、错误提示、Token 过期逐项走查；产出至少 5 条问题并转 Issue |

## 看板截图要求

- 浏览器窗口拉宽，四列全部可见；
- `Backlog`/`To Do` 列至少 5 张卡片，标题可辨认；
- 保留项目名或地址栏，证明确实是 TeamX 的看板；
- 保存为 `docs/lab1-submission/lab1-board.png`。

## 当前仓库已有的协作证据

| 事项 | 证据 |
| --- | --- |
| 仓库 | <https://github.com/2123345397-star/CampusOverflow> |
| 团队章程 | PR #6，合并提交 `e1caa47` |
| 角色承诺书 | PR #3（内容已在本目录补全） |
| 成员 hello | PR #1 / #4 / #5 / #7 / #9，均已合并 |
| 站会记录 | PR #11，`docs/team/daily-standup-01.md` |
