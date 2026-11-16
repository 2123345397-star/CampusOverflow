# CampusOverflow 团队首页（TeamX）

> 团队协作入口（破冰文档）：谁是谁、怎么协作、怎么联系。
> 项目：CampusOverflow 校园问答平台　仓库：cs2412-team04-campus-overflow

## 一、团队成员与角色

| 姓名 | 角色 | GitHub 账号 | 主要职责 |
| --- | --- | --- | --- |
| 魏志国 | PO（产品负责人） | @2123345397-star | 维护 Product Backlog、排优先级、验收增量 |
| 陈明哲 | SM（Scrum Master） | @cmz666-BOT | 文档与协作规范、看板、CODEOWNERS、会议组织 |
| 刘俊琒 | SM（Scrum Master） | @Liujunfeng520 | 分支策略、CI/CD 质量门禁、部署、障碍清除 |
| 青岑 | Developer / QA | @qing963666 | 前端与后端开发、测试计划与回归 |
| 房国栋 | Developer | @2177023844 | 后端业务模块开发与单元测试 |

## 二、协作方式

- 所有改动走 `feature/<拼音>-<主题>` 分支 + Pull Request，禁止直接 push main / develop。
- 每个 PR 至少 1 名有写权限的成员 Approve（评论 LGTM），由 SM 合并，合并后删除分支。
- 评审意见 24 小时内响应，作者逐条回复。
- 领域统一术语以 `docs/ddd/glossary.md` 为准，不自行造词。
- 评审人按 `.github/CODEOWNERS` 按目录自动分配。

## 三、联系方式

- 即时沟通：IM 群「CampusOverflow-TeamX」。
- 工作日群消息 2 小时内回复；阻塞超过 2 小时必须在群内升级。
- 每日站会 12:30；Sprint Planning / Review / Retro 按课程日历进行。
- 重要决策与架构记录：`docs/adr/`。

## 四、相关文档

- 工作约定（会议/时效/分支/提交）：`docs/team-agreement.md`
- DoR / DoD：`docs/team/dor-dod.md`
- 统一语言术语表：`docs/ddd/glossary.md`
- 团队章程与角色承诺：`docs/team/charter-v1.0.md`、`docs/team/role-commitment.md`
