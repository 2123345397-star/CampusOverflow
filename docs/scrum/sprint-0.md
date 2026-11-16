# Sprint 0 · 执行记录（启动与基础设施）

> Scrum Master：陈明哲（E）、刘俊琒（A）　PO：魏志国
> 时间盒：**2026-10-22（周四）～ 2026-11-02（周一）**，共 8 个工作日
> Sprint Goal：团队、仓库、环境、CI 与 Product Backlog 全部就绪；每位成员都能在本地启动系统、完成注册/登录/加入课程，并完成一次「分支 → PR → 评审 → 合并」
> 版本标签：`v0.1.0`（指向 S0 收尾提交 `704cd60`；因 S0 结束时漏打，于 2026-11-16 补打并在此登记）
> 范围来源：[`docs/backlog/product-backlog.md`](../backlog/product-backlog.md)、[`docs/lab1-submission/README.md`](../lab1-submission/README.md)

---

## 一、Sprint 0 计划会（2026-10-22）

**结论（可查证产出）**

| 议题 | 结论 | 落地物 |
| --- | --- | --- |
| Sprint 目标 | 仓库、分支保护、模板、看板、DDD 文档、CI 与 Backlog 就绪，能注册/登录/加入课程 | 本文件 |
| 团队角色 | PO 魏志国；SM 陈明哲、刘俊琒；Dev/QA 青岑；Dev 房国栋 | [`docs/team/charter-v1.0.md`](../team/charter-v1.0.md)、[`role-commitment.md`](../team/role-commitment.md) |
| 工具链 | GitHub 组织仓库 + Projects 看板 + IntelliJ + JDK 21 + Maven 3.9 + Node 20 + Docker | [`docs/lab1-submission/env-matrix.md`](../lab1-submission/env-matrix.md) |
| 范围 | T0-01～T0-08：仓库/模板/环境/CI/ADR-009/事件风暴/Backlog/团队约定 | 本节表格 |
| 本次不做 | 业务功能实现（Q&A、Reputation 等留到 S1/S2） | — |

**Sprint 0 我们决定不做的事**：不在 S0 写业务代码；只做「让别人能开始写业务」的基础设施与文档，避免基础设施债拖到 S1。

## 二、每日站会记录

> 三问：昨天完成什么 / 今天做什么 / 有什么阻塞。时间为站会当日 10:00，时长 ≤15 分钟。

### 2026-10-24（周六）

| 成员 | 昨日完成 | 今日计划 | 阻塞 |
| --- | --- | --- | --- |
| 魏志国 PO | 组织邀请、仓库初始化 | 看板四列与 48 条 PBI 导入 | 无 |
| 陈明哲 SM | 事件风暴初稿、角色卡拆解 | CODEOWNERS / Issue 模板 / PR 模板 | 无 |
| 刘俊琒 SM | 分支保护规则草案 | 配置 develop/main 保护 | 仓库 Admin 权限未到位 |
| 青岑 Dev/QA | DDD 上下文映射草稿 | D1 事件目录（30 条） | 无 |
| 房国栋 Dev | 前端脚手架核对 | 环境版本采集 | Docker 未安装 |

**站会后动作**：PR #13（分支保护证据）合入；PR #17（D1 事件目录、D3 上下文映射）合入；陈明哲提交 PR #23（CODEOWNERS + 模板）。

### 2026-10-28（周三）

| 成员 | 昨日完成 | 今日计划 | 阻塞 |
| --- | --- | --- | --- |
| 魏志国 PO | 48 PBI 排序与 Sprint 1 筛选 | DoR/DoD 与团队约定评审 | 无 |
| 陈明哲 SM | 分支保护 API 核验、CI 初版 | ADR-009 与渐进门禁 | Actions 无运行记录 |
| 刘俊琒 SM | 团队章程 v1.0、角色承诺书合并 | 团队首页 team.md | 无 |
| 青岑 Dev/QA | 上下文映射 ACL 说明、架构笔记 | 事件风暴 30 事件复核 | 无 |
| 房国栋 Dev | 环境矩阵与采集脚本 | 前端构建脚本 | Node 版本不一致 |

**站会后动作**：Lab 1 提交物清单 PR #1 合入 `main`（`c7a6711`）；环境采集脚本 `scripts/dev-env/` 合入；陈明哲开始写 ADR-009。

### 2026-11-02（周一）

| 成员 | 昨日完成 | 今日计划 | 阻塞 |
| --- | --- | --- | --- |
| 魏志国 PO | 组织仓库 V3 对齐评审 | S0 验收与 S1 计划输入 | 无 |
| 陈明哲 SM | team.md / team-agreement.md / dor-dod.md（PR #24） | 打 v0.1.0 标签、S0 记录 | `main` 无发布 PR |
| 刘俊琒 SM | G1 分支保护复核 | S1 分支策略 | 无 |
| 青岑 Dev/QA | 事件风暴对齐 4 上下文 / 30 事件（`c37b09c`） | S1 测试计划 | 无 |
| 房国栋 Dev | 环境矩阵 2/5 证据 | 其余成员环境截图 | Docker 未安装 |

**站会后动作**：PR #24 合入（`324a1d1`），团队文档 C1～C4 到位；ADR-009 合入（`704cd60`），S0 文档收尾。**遗留**：v0.1.0 标签未打、环境截图仅 2/5、`develop` 必需状态检查未配置。

## 三、交付物与验收

| # | S0 交付物 | 证据 | 状态 |
| --- | --- | --- | --- |
| 1 | 组织仓库 + develop/main 分支保护（不允许绕过） | `docs/lab1-submission/screenshots/branch-protection.png`、PR #13；API 核验见 [lab1 README 第 1 节](../lab1-submission/README.md) | ✅ |
| 2 | Issue/PR 模板 + CODEOWNERS + 看板四列 | `.github/`（PR #23）、`docs/lab1-submission/screenshots/lab1-board.png` | ✅ |
| 3 | 团队章程、角色承诺书、团队约定、DoR/DoD | `docs/team/charter-v1.0.md`、`role-commitment.md`（5 人签署 2026-10-26）、PR #24 | ✅ |
| 4 | DDD 文档：事件风暴 30 事件 / 4 上下文、上下文映射、词汇表 | `docs/ddd/event-storming.png`、`events.md`、`context-map.md`、`glossary.md`、PR #17/#18 | ✅ |
| 5 | 架构理解笔记 | `docs/notes/architecture.md`；T0-05 讲解分工见其第 8 节 | ✅ |
| 6 | 渐进式 CI 门禁 + ADR-009 | `.github/workflows/ci.yml`、`docs/adr/009-渐进式质量门禁.md` | ✅ |
| 7 | Product Backlog（48 PBI）与 Sprint 1 DoR | `docs/backlog/product-backlog.md`、Projects 看板 | ✅ |
| 8 | 环境版本矩阵 + 每人截图 + `mvn clean test` 记录 | `docs/lab1-submission/env-matrix.md`、`env/` | ⚠️ S0 时 2/5，后续补采（见 §五） |
| 9 | 版本标签 v0.1.0 | 指向 `704cd60`，2026-11-16 补打 | ⚠️ 补打 |

## 四、Sprint Review（2026-11-02）

- 演示：任一成员电脑启动系统 → 注册 → 登录 → 加入课程 → Swagger UI 可访问；主链路可用。
- 评审结论：S0 目标基本达成，进入 S1（Q&A 核心）。
- 未达标项与处理：
  1. `develop` 必需状态检查为空（BUG-003）→ 转 S1 由 E 处理（后续已开 `enforce_admins` 与 1 个 Approve，必需检查仍待补）。
  2. v0.1.0 标签缺失 → 2026-11-16 补打并登记在本文件。
  3. 环境截图仅归档房国栋、青岑 2/5 → 其余成员按 `scripts/dev-env/collect-env.ps1` 补采，陈明哲的于 2026-11-16 归档。

## 五、回顾与行动项

| 做得好 | 可以改进 | 行动项（负责人/截止） |
| --- | --- | --- |
| 文档先行：DDD、团队约定、ADR 在写代码前就位 | S0 收尾没有形成「发布 PR + 标签」的固定动作 | 每个 Sprint 末固定 `develop → main` 发布 PR 并打标签（SM） |
| 分支保护、模板、看板一次配齐 | 环境证据采集启动晚，只有 2/5 | 环境采集脚本随仓库下发，S1 前补齐（房国栋） |
| CI 渐进门禁 + ADR-009 提前落地 | 分支保护缺少必需状态检查 | S1 补齐 `required_status_checks`（陈明哲） |

## 六、证据索引

- Lab 1 提交物清单：`docs/lab1-submission/README.md`
- 环境矩阵与原始证据：`docs/lab1-submission/env-matrix.md`、`docs/lab1-submission/env/`
- 团队文档：`docs/team/`
- DDD：`docs/ddd/`
- ADR-009：`docs/adr/009-渐进式质量门禁.md`
- CI：`.github/workflows/ci.yml`
- 版本标签：`v0.1.0` → `704cd60`
