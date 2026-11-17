# 个人总结 · 魏志国

| 项 | 内容 |
| --- | --- |
| 姓名 | 魏志国 |
| 角色 | PO / B（回答与采纳、声誉计分、仓库与发布管理） |
| 负责 | 48 条 PBI 与 Backlog 口径、分支保护与仓库规则、Q&A 采纳链路、声誉计分规则与账户、Release 发布、V3 看板 |
| 参与 Sprint | S0 / S1 / S2 / S3 / S4 |

## 一、我负责的交付物（带证据）

| 交付物 | 证据 | 结果 |
| --- | --- | --- |
| 48 条 PBI Issue 与 Backlog 单一口径 | Issue #27~#74（`[PB-xx]` + User Story + Given/When/Then）、`docs/backlog/product-backlog.md` | 看板 49 项状态归位（Done 31 / Sprint Backlog 10 / Backlog 8），API 复核零误差 |
| 分支保护与必需状态检查（BUG-003 关闭） | Branch Protection API 核验记录 `docs/lab1-submission/README.md`；`develop` 勾上 `后端构建与验证`、`前端构建`（1 Approve + dismiss stale + enforce admins） | main/develop 双分支保护完整，CI 失败阻断合并 |
| Question.accept 五步校验（T1-03） | `QuestionTest.Accepting` 5 条 | 全绿 |
| Answer 聚合与 AnswerCommandService（T1-04/08） | `AnswerTest` 3 条、`AnswerAndVoteServiceTest` 4/4；事件参数顺序修复 PR #89 | 全绿 |
| 声誉计分规则/徽章/查询 API（T2-01/05/09） | `ReputationRulebookTest` 9/9、PR #91（`(10,195)→5`、`(10,200)→0` 日上限边界） | 合入 develop |
| 声誉账户与采纳/投票计分（T2-02/06） | `ReputationAccount` 七方法、`ReputationEventHandlers` 幂等（6/6）、PR #102 | 合入 develop |
| Lab4 端到端证据 | `docs/lab4-submission/answer-e2e.md` + 4 张截图（POST 问题→回答→采纳→详情 answerCount=1 + SQL 落库） | 入库 |
| D3 文档统一（4 上下文 / 30 事件） | PR #75：`event-storming.png` 重建、`glossary.md:25-27`、`context-map` | BUG-004 关闭 |
| v0.1.0 / v0.2.0 / v0.3.0 发布 | `docs/release/v0.3.0.md`、PR #115（develop→main）、GitHub Releases 三份 | tag 与 Release Notes 齐全 |
| E3/E4 环境与身份核查表 | `env-weizhiguo.png/txt`、`mvn-test-weizhiguo.png`、PR #123（核查表 5/5） | 三处表口径一致 |
| Sprint 3 计划草案 | PR #124：`docs/scrum/sprint-3.md`（PO 代拟、SM=房国栋） | 已合并 |
| T1-15 qa.http 问题侧 | PR #101：发布/编辑/详情/删除问题请求 | 一键跑通 |

## 二、技术收获

1. **聚合校验顺序本身就是接口契约。** `Question.accept` 的“未删→有权限→未采纳过→答案属于本题→答案未删”五步不是业务细节，而是 403/409/422 错误码的来源——领域测试写的就是验收单。
2. **事件计分要在“边界值”上证明正确，而不是只证明正向。** 每日上限 `applyDailyCap` 用 `(10,195)→5`、`(10,200)→0` 两条边界锁死；采纳 +15 用“自问自答 delta=0 不写流水”证明幂等不变量“余额=流水之和”不被破坏。
3. **仓库规则要靠 API 核验，不能靠截图。** 分支保护、必需检查、看板状态都能用 API 读写并复核；截图只能证明“当时看过”，核验记录才能证明“配置存在”。

## 三、协作与流程

- 发起/参与 PR：#75（D3 统一）、#89（事件参数修复）、#91/#102（声誉）、#101（qa.http）、#114（S2 收尾）、#115（发布）、#123（E3/E4）、#124（S3 计划）等；均经队友 Approve 后合并。
- 主持/参与：Sprint 计划会、Planning Poker 口径确认（PB-11=5 基准）、Sprint 评审会 9.1 七维度自查；代查刘俊琒身份绑定（E4）并标注“代查”。
- 流程改进：把“两份文档互相‘以对方为准’”改为 **backlog 单一来源**（Sprint1 点数曾出现 13 vs 16 不一致）；把“发布 PR + tag + Release Notes”固化为 Sprint 末动作（与陈明哲共同沉淀）；对外代拟内容一律标注“XX 代整理，待本人复核”。

## 四、不足与改进

| 不足 | 影响 | 下次怎么做 |
| --- | --- | --- |
| Planning Poker 估点会没有留会议记录 | 评审时只能给“口径已回写”的间接证据 | 估点会当场把每人牌面与分歧记进 Milestone 描述 |
| 起始模板的 `characterEncoding=utf8mb4` 配置到 S2 联调才发现 | 后端首次以默认配置启动直接失败 | 收到模板代码先做一次“默认配置可启动”冒烟，而不是先写业务 |
| 对队友 TODO 的依赖（如 QuestionCommandService）发现偏晚 | Lab4 端到端一度卡住 | 每周扫一次全仓 `TODO(负责人)`，提前一周暴露依赖 |
| 评审批准曾被外部自动化批量撤销，应急时临时放宽保护合并 | 流程上有 ~3 秒保护空窗 | 事后补了核验记录；下次先查撤销来源、再按预案操作并全程留痕 |

## 五、自评

- 对团队目标的贡献：守住“做什么、先做哪个”的需求口径与 48 条 PBI 的验收标准；保证采纳/计分链路正确并有端到端证据；仓库规则（分支保护、必需检查、发布流程）从口头约定变成可核验配置；v0.1.0~v1.0.0 的发布链路可追溯。
- 需要团队/老师支持的地方：无；若老师需要看板/Issue/Release 的核验方式，都可按本文档的 API 口径复现。
- 签字 / 日期：魏志国 / 2026-11-17
