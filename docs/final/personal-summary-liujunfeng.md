# 个人总结 · 刘俊琒

| 项 | 内容 |
| --- | --- |
| 姓名 | 刘俊琒（学号 2024121117，GitHub `Liujunfeng520`） |
| 角色 | Developer A（Q&A 问题侧 → Reputation 徽章与查询）/ SM（Sprint 1）/ V3 分支保护与 CI 工作流 |
| 负责模块 | `co-qa` 问题侧（`Question` 聚合、命令/查询服务、`QaQueryApiImpl`）、`co-reputation` 徽章与声誉查询、`.github/workflows`、`docs/scrum` |
| 参与 Sprint | S0 / S1 / S2 / S3 / S4 |

## 一、我负责的交付物（带证据）

| 交付物 | 证据（文件 / PR / CI） | 结果 |
| --- | --- | --- |
| 分支保护与协作工具链（S0-04） | GitHub Branch Protection API 2026-11-17 核验：`main` = 2 Approve + 后端/前端/容器三项必需检查；`develop` = 1 Approve + 后端/前端两项必需检查（规则集 `develop required checks`，`app_id=15368`），两分支 `enforce_admins=true`；`docs/qa/defects.md` BUG-002 / BUG-003 | 管理员不可绕过、CI 未过不可合；BUG-002 已修复，BUG-003 已补齐必需检查 |
| Q&A 问题侧领域与应用层（T1-01/02/07/10/11） | PR #86（合并提交 `3f96258`，14 文件 +1093/−22）；`Question`、`MarkdownBody`、`Tags`、`QuestionCommandService`、`QuestionQueryService`、`QaQueryApiImpl.findQuestion`；`QuestionTest` 33 条（本次新增 12 条边界） | `co-qa-domain` 42 条、`co-qa-application` 28 条全绿；POST→GET 端到端 201/200 且 `qa_question` 核对落行（`docs/lab4-submission/`） |
| Sprint 1 执行记录与燃尽图（SM） | PR #90；`docs/scrum/sprint-1.md`、`sprint-1-burndown.png`（40 SP → 11-05 剩 25）、`docs/sprint-1-backlog.png` | 计划会结论、两次站会、风险与对策可逐条复查 |
| CI 工作流与容错策略（BUG-002） | PR #25 / #26 / #79 / #80 / #82 / #83 / #84 / #93；`.github/workflows/ci.yml`、`docs/adr/009-渐进式质量门禁.md` | PR 上稳定出现「后端构建与验证 / 前端构建」；根目录重复 `ci.yaml` 清理，工作流只留一份 |
| S2 角色 A：徽章授予与声誉查询 | #91（`ReputationAccount` / `BadgePolicy` / 四个事件处理器）；PR #109（授权用例 13 条 + `http/reputation.http` + 流水拆箱 NPE 修复） | `co-reputation-application` 34/34、`ArchitectureTest` 1/1、CI 全绿；「他人看流水 403 REP-3002 / 他人看不到积分」由人工点页面变成回归用例 |
| Lab 1–4 交付文档与证据归档 | `docs/lab1-submission/README.md`（分支保护核验表与 PR 记录）、`docs/lab4-submission/README.md`（E2E 截图 + Redis 键 + 数据库核对） | 每份 lab 都有可点证据、可复现命令与遗留项，评审不需要口头解释 |

> 分工说明：S2 的 `ReputationAccount`、`BadgePolicy` 与事件处理器由我实现、随 #91（`feat/reputation-core`）合入 `develop`；我提的 #107、#108 因与 `develop` 已有实现重复而关闭并附逐条对照，改为用 #109 补验收缺口，不把队友的 CI 与测试工作写进个人贡献。

## 二、技术收获（每条一句话结论 + 一个例子）

1. **幂等要按「事件真的被消费两次」设计，而不是只在单测里 mock 一次失败。** 声誉账户用账本 `(event_id, user_id, reason)` 唯一键 + 处理前 `exists` 判断，`AnswerAccepted` 连调两次只产生一条流水、一枚徽章；评审时把 `outbox_event.published_at` 置空重放，结果不变。
2. **必需检查必须「真的会在该分支的 PR 上报」，否则等于永久阻塞。** `容器镜像构建` 的 `if` 限定 `push` 或 `base=main`，所以 `main` 挂三项、`develop` 只挂两项；把镜像检查也加到 `develop`，每个 PR 都会等一个永远不会出现的状态。
3. **NPE 常藏在读路径的拆箱处，要靠「换一个视角看数据」才暴露。** `LedgerEntry.id()` 是 `Long` 且落库前为 `null`，直接传给 `LedgerEntryView.id`（`long`）让「本人看自己的流水」返回 500；写「本人/同学/管理员/未登录」四种视角的授权用例才踩出来。

## 三、协作与流程（PR / 评审 / 会议）

- **发起或被评审的 PR**：#2、#8、#14（DDD 上下文图）、#25/#26（CI 与 ADR-009）、#79–#84 与 #93（工作流收敛）、#86（Q&A 问题侧，@qing963666 与 @2123345397-star 双 Approve）、#90（S1 执行记录）、#109（声誉查询验收）。#107/#108 关闭并在说明里逐条对照 `develop` 已有实现。
- **主持或参与的会议**：S0 计划会与收尾会；S1 两次站会（作为 SM 主持并落文档）；S2 站会（Reputation 联调进展）；S4 彩排排期与讲稿分工。
- **对团队流程的改进**：① 把「Sprint 执行记录 + 燃尽图」固定进 `docs/scrum/`，每次站会当天更新，评审直接看文件而不是翻聊天记录；② 一个 PR 只写一条 `Closes`（#32/#33/#34 写在一行只自动关了首个编号，后来手工补关）；③ CI 工作流合并为唯一一份 `ci.yml`，删掉根目录会跑 `mvn clean test` 的重复文件——那正是 S2 早期 CI 红灯的根因。

## 四、不足与改进（具体、可执行）

| 不足 | 影响 | 下次怎么做 |
| --- | --- | --- |
| 分支保护的必需状态检查拖到 S4 才补齐（BUG-003 长期「部分修复」） | 期间红灯 PR 理论上仍可合，只能靠人盯 review | S0 就把「保护配置 + API 核验截图」写进 DoD；每次改 CI 作业名后同步核对 `required_status_checks`（jobs 改名会让必需检查永远 pending） |
| S2 开工前没有先 `git fetch` 对齐，`ReputationAccount` / `BadgePolicy` / 事件处理器与队友 #91 重复实现 | 半天返工，贡献归属还要额外解释 | 接角色卡先查 `develop` 与在看板认领情况，同名方法先 `git grep` 再动手；实现前同步一次「谁写哪几个类」 |
| 个人总结、彩排材料集中到最后一天补 | 评审前材料堆叠，质量与核对时间都被压缩 | 每个 Sprint 末留 30 分钟写「交付物 + 证据 + 一条结论」，S4 只做汇总不做回忆 |

## 五、自评

- **对团队目标的贡献**：把 Q&A 问题侧从「接口能通」推进到「有 33 条领域用例 + 端到端证据」；把 CI 与分支保护从「有工作流文件」推进到「PR 上真的跑、跑不过真的合不了」；S1 作为 SM 让迭代过程可复查；S2 之后把声誉查询的越权边界固化成 13 条回归用例。
- **需要团队/老师支持的地方**：S4 两次彩排需要全员按时到场、各自控制在 2 分钟内（我负责讲「问答主链路 + 声誉查询权限」这一段）；干净环境 `docker compose up -d --build` 的验证需要有 Docker 的机器，我本机没有；Projects 看板 48 张卡的导入状态需要 Project 读权限（我的 token 只有 `repo`/`workflow`/`gist`），需 PO 或有权限的同学确认。
- **签字 / 日期**：刘俊琒 / 2026-11-17
