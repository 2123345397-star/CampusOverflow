# Lab 1 提交物清单 · TeamX

> 团队：TeamX　项目：CampusOverflow 校园问答平台
> 仓库：<https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow>
> 提交日期：2026-10-27　对应实验：Lab 1 团队组建与协作工具链（Sprint 0）

本文件是 Lab 1 的**提交索引**：老师按本页即可找到全部材料，不必翻聊天记录。

---

## 1. 团队代码仓库 URL

- 仓库地址：<https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow>
- 默认分支：`develop`（集成分支；`main` 为发布分支）
- 成员：5 人均已参与分支推送与 PR 协作
- 分支保护：已配置，截图见 [`screenshots/branch-protection.png`](screenshots/branch-protection.png)
  （截图为规则列表页，仅证明 `main`、`develop` 存在保护规则；规则内容以 2026-10-28 的 API 核验结果为准）

分支保护规则（2026-10-28 通过 GitHub API 核验）：

| 分支 | 规则 | 实际配置 |
| --- | --- | --- |
| `main` | PR 审批人数 | ≥ 2 个 Approve；“至少 1 名 SM/PO”为团队约定，GitHub 无法按角色强制 |
| `main` | 禁止绕过 | ✅ Enforce for administrators 已开启 |
| `main` | 直推 | ❌ 禁止，必须走 PR |
| `main` | 强推 / 删除分支 | ❌ 均禁止 |
| `main` | 必需状态检查（CI） | ✅ 已启用：后端构建与验证、前端构建、容器镜像构建 |
| `develop` | PR 审批人数 | ≥ 1 个 Approve，且作者不能批准自己的 PR |
| `develop` | 直推 | ❌ 禁止，必须走 PR |
| `develop` | 强推 / 删除分支 | ❌ 均禁止 |
| `develop` | 管理员绕过 | ✅ 已关闭（Enforce for administrators 已开启） |
| `develop` | 必需状态检查（CI） | ✅ 已启用：后端构建与验证、前端构建 |
| 两分支 | 新提交使旧 Approve 失效 | ✅ Dismiss stale approvals 已开启 |
| 两分支 | 合并前必须解决会话 | ✅ 已开启 |

设置入口：仓库 **Settings → Branches → Branch protection rules**（新版为 **Rules → Rulesets**）。
当前仓库没有 Ruleset，仅使用经典分支保护规则。

**核验方式**：2026-10-28 调用 GitHub API `GET /repos/{owner}/{repo}/branches/{branch}/protection`
读取 `main`、`develop` 的实际配置，上表据此整理。

**生效验证**：直接向受保护分支推送会被拒绝，返回
`GH006: Protected branch update failed ... Changes must be made through a pull request.`

## 2. 团队看板截图

- 文件：`lab1-board.png`　状态：✅ 已完成
- 要求：四列（`Backlog`/`To Do`、`In Progress`、`In Review`、`Done`）可见，`Backlog`/`To Do` 至少 5 条卡片。
- 卡片来源：[`backlog.md`](backlog.md)（Sprint 0 共 8 条）。

## 3. 团队章程 v1.0

- 文件：[`docs/team/charter-v1.0.md`](../team/charter-v1.0.md)
- 合并记录：PR [#6](https://github.com/2123345397-star/CampusOverflow/pull/6)，合并提交 `e1caa47`
- 签署：全体 5 人（魏志国、陈明哲、刘俊琒、青岑、房国栋），评审中含 1 个 Approve
- 覆盖章节：团队价值观、角色与职责、沟通约定、开发规约、DoD 初版、签字

## 4. 角色承诺书

- 文件：[`docs/team/role-commitment.md`](../team/role-commitment.md)
- 内容：共同承诺 + PO / SM / Developer / QA 分角色承诺 + 全员签署表
- 状态：已按 5 人实际角色补全，并随对应 PR 合并

## 5. 成员 PR 合并截图

每人一个 hello PR，均在**旧个人仓库**（`2123345397-star/CampusOverflow`）合并。本章两个表格中的 PR 编号与合并提交均属于该仓库，链接已指向旧仓库；截图文件命名 `pr-<拼音>.png`，放在本目录。

| 成员 | 角色 | PR | 分支 | 合并提交 | 评审记录 | 截图 |
| --- | --- | --- | --- | --- | --- | --- |
| 魏志国 | PO | [#5](https://github.com/2123345397-star/CampusOverflow/pull/5) | `feature/weizhiguo-hello` | `ee0b1a3` | Liujunfeng520 评论 | `pr-weizhiguo.png` |
| 陈明哲 | SM | [#9](https://github.com/2123345397-star/CampusOverflow/pull/9) | `cmz666-BOT-patch-1` | `bc82971` | 无正式 Review ⚠️ | `pr-chenmingzhe.png` |
| 刘俊琒 | SM | [#7](https://github.com/2123345397-star/CampusOverflow/pull/7) | `feature/liujunfeng-hello` | `35c711e` | cmz666-BOT **Approve** | `pr-liujunfeng.png` |
| 青岑 | Developer / QA | [#4](https://github.com/2123345397-star/CampusOverflow/pull/4) | `feature/qing963666-hello` | `c6b16d3` | 无正式 Review ⚠️ | `pr-qingcen.png` |
| 房国栋 | Developer | [#1](https://github.com/2123345397-star/CampusOverflow/pull/1) | `feature/fangguodong-hello` | `e8f1d8b` | 2123345397-star 评论 | `pr-fangguodong.png` |

其他团队协作 PR：

| PR | 内容 | 合并提交 | 评审 |
| --- | --- | --- | --- |
| [#6](https://github.com/2123345397-star/CampusOverflow/pull/6) | 团队章程 v1.0 | `e1caa47` | 青岑 **Approve** |
| [#3](https://github.com/2123345397-star/CampusOverflow/pull/3) | 角色承诺书 | `1a1e1a9` | 青岑 评论 |
| [#11](https://github.com/2123345397-star/CampusOverflow/pull/11) | 第一次站会纪要 | `e9a1224` | 2123345397-star 评论 |
| [#2](https://github.com/2123345397-star/CampusOverflow/pull/2) | 青岑 hello 初版（改名后关闭） | — | 房国栋 Request changes |


## 6. 开发环境版本汇总（E3）

- 汇总表：[`env-matrix.md`](env-matrix.md)（五人 JDK/Maven/Node/Docker 版本 + `mvn clean test` 结果登记）
- 原始证据目录：[`env/`](env/)（目录说明见 [`env/README.md`](env/README.md)）
  - 已归档（5/5 齐）：`env/env-fangguodong`、`env/env-qingcen`、`env/env-chenmingzhe`、`env/env-weizhiguo`、`env/env-liujunfeng`，每人 `.png` + `.txt` 成对
  - 待补：房国栋的 `mvn` 测试截图（已有完整日志）；其余 4 人的 `screenshots/mvn-test-<拼音>.png` 已齐
- 采集脚本：[`scripts/dev-env/collect-env.ps1`](../../scripts/dev-env/collect-env.ps1) / [`collect-env.sh`](../../scripts/dev-env/collect-env.sh)
- 达标线：Git 2.40+、JDK 21、Node.js 20+、Docker 可运行
- 已知情况：Lab 1 时期 `backend` 整体 `mvn clean test` 为红（`co-qa-domain` 的 `TODO(S2)` 桩未实现），
  当时按「整体红 + 附原因」与「已实现模块绿」两段取证，避免被误判为环境问题；日志见 [`logs/mvn-test-fangguodong.log`](logs/mvn-test-fangguodong.log)。
  **2026-11-17 更新**：S4 收口后桩已全部落地，本机全量复跑为 `BUILD SUCCESS`（27 模块 / 165 用例 / 0 失败），
  见 [`env/env-liujunfeng.txt`](env/env-liujunfeng.txt) §3 与 [`screenshots/mvn-test-liujunfeng.png`](screenshots/mvn-test-liujunfeng.png)。

## 7. 身份绑定核查（E4）

- 核查报告：[`identity-audit.md`](identity-audit.md)
- 结论：提交邮箱 `W13636786666@163.com` 已验证但绑在个人小号 `Baigui-Yexing` 上，
  课程账号 `2177023844` 贡献为 0；已改用课程账号 noreply 邮箱
- 本仓库新提交（2026-10-29 之后）作者均为
  `房国栋 <333220585+2177023844@users.noreply.github.com>`
- 历史提交（旧仓库 `main`）的处置方案见 `identity-audit.md` §9：推荐把邮箱从小号换绑到课程账号，
  GitHub 会按邮箱自动重新归属，无需改写提交
- 审计脚本：[`scripts/dev-env/audit-authors.ps1`](../../scripts/dev-env/audit-authors.ps1) / [`audit-authors.sh`](../../scripts/dev-env/audit-authors.sh)

---

## 提交物状态

| # | 提交物 | 路径 | 状态 | 负责人 |
| --- | --- | --- | --- | --- |
| 1 | 仓库 URL + 分支保护 | 本页第 1 节（含 2026-10-28 API 核验表）+ `screenshots/branch-protection.png` | ✅ 已配置并核验 | 刘俊琒 |
| 2 | 看板截图 | `lab1-board.png` | ✅ | 陈明哲 |
| 3 | 团队章程 v1.0 | `docs/team/charter-v1.0.md` | ✅ 已合并 | 魏志国 |
| 4 | 角色承诺书 | `docs/team/role-commitment.md` | ✅ 已补全 | 陈明哲 |
| 5 | 成员 PR 合并截图 | `pr-<拼音>.png` ×5 | ✅ | 各成员 |
| + | 初始 Backlog | `backlog.md` | ✅ | 魏志国 |
| + | 成员环境截图（青岑） | `env/env-qingcen.png` | ✅ | 青岑 |
| + | 成员环境截图（陈明哲） | `env/env-chenmingzhe.png` + `.txt` | ✅ 2026-11-16 补采 | 陈明哲 |

---

## 验收自检

```bash
# 1) main 上 hello 文档数量应为 5
git switch main && git pull --rebase
git ls-tree -r --name-only main docs/team | grep '^docs/team/hello-'

# 2) 章程与承诺书已合并
git log --oneline -- docs/team/charter-v1.0.md docs/team/role-commitment.md

# 3) 协作历史：每人一个合并 PR
git log --oneline --graph --all --decorate -30

# 4) 无敏感文件
git ls-files | grep -E '(^|/)\.env$|node_modules/|/target/|/dist/'
```

检查结果（2026-10-27 复核）：

- [x] `main` 上共有 5 份 `hello-*.md`：fangguodong、chenmingzhe、liujunfeng、qing963666、weizhiguo
- [x] 章程经 PR #6 合并，评审含 Approve
- [x] 角色承诺书内容已补全
- [x] 看板截图 `lab1-board.png` 已就位
- [x] 分支保护截图 `screenshots/branch-protection.png` 已就位（规则列表页；规则内容见第 1 节 API 核验表）
- [x] 每位成员的 PR 合并截图：5 张齐全
- [x] 青岑环境截图 `env/env-qingcen.png` 已就位（JDK 21.0.10 / Maven 3.9.2 / Node v26.13.0 / Docker 29.9.2）

---

## 2026-10-27 修订记录

针对评审反馈（批次 2）修订：

1. 仓库地址更新为组织仓库 `hbuas-se-2026fall/cs2412-team04-campus-overflow`
2. 默认分支更正为 `develop`（`main` 为发布分支）
3. 分支保护规则按 V3 更新：`main` ≥2 Approve（“含 SM/PO”为约定项）、`develop` ≥1 非作者 Approve、不允许绕过
4. 分支保护截图路径为 `screenshots/branch-protection.png`
5. 青岑 PR 截图文件名更正为 `pr-qingcen.png`
6. 删除“角色承诺书（内容待补全）”等过期表述

---

## 2026-10-28 修订记录

针对评审反馈（分支保护截图在，但 Settings 规则未确认）：

1. 通过 GitHub API 核验 `main`、`develop` 的实际保护配置，更新第 1 节规则表；
2. 明确“至少 1 名 SM/PO”为团队约定——GitHub 无法按角色强制审批（Code Owner 审批未开启）；
3. 按 V3 要求配置必需状态检查：`develop` 为“后端构建与验证、前端构建”，`main` 另加“容器镜像构建”；同时开启 `develop` 的 Enforce for administrators，撤回“不开启 CI 门禁”的旧决定；
4. 原截图仅包含规则列表，不能单独证明规则内容，第 1 节已补充核验方式与核验日期；
5. 待办：GitHub Actions 当前无运行记录（0 次），需 A4 恢复 CI 触发；恢复前 PR 会停留在“等待必需检查”。

---

## 评分要点对照

| 评分项（占比） | 对应材料 | 状态 |
| --- | --- | --- |
| 工具链配置完整性（30%） | 仓库、分支保护、看板、IM 群 | ✅ 均已就位 |
| 团队章程质量（30%） | 章程 v1.0 是否具体可执行 | ✅ 含价值观、沟通、规约、DoD、签字 |
| Git 协作熟练度（30%） | 分支、PR、评审、合并流程 | ✅ 8 个 PR 已合并；2 个 PR 缺正式 Review（已在第 5 节标注） |
| 团队氛围与协作（10%） | 课堂观察、站会记录 | ✅ `docs/team/daily-standup-01.md` |