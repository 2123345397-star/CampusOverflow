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

分支保护规则（按 V3 要求配置）：

| 分支 | 规则 | 配置 |
| --- | --- | --- |
| `main` | PR 审批人数 | ≥ 2 个 Approve（其中至少 1 名 SM/PO） |
| `main` | 禁止绕过 | ✅ 不允许 bypass |
| `main` | 直推 | ❌ 禁止，必须走 PR |
| `develop` | PR 审批人数 | ≥ 1 个 Approve，且不能由作者本人批准 |
| `develop` | 直推 | ❌ 禁止，必须走 PR |

设置入口：仓库 **Settings → Branches → Branch protection rules**（新版为 **Rules → Rulesets**）。

**生效验证**：直接向受保护分支推送会被拒绝，返回
`GH006: Protected branch update failed ... Changes must be made through a pull request.`

## 2. 团队看板截图

- 文件：`lab1-board.png`　状态：✅ 已完成
- 要求：四列（`Backlog`/`To Do`、`In Progress`、`In Review`、`Done`）可见，`Backlog`/`To Do` 至少 5 条卡片。
- 卡片来源：[`backlog.md`](backlog.md)（Sprint 0 共 8 条）。

## 3. 团队章程 v1.0

- 文件：[`docs/team/charter-v1.0.md`](../team/charter-v1.0.md)
- 合并记录：PR [#6](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/6)，合并提交 `e1caa47`
- 签署：全体 5 人（魏志国、陈明哲、刘俊琒、青岑、房国栋），评审中含 1 个 Approve
- 覆盖章节：团队价值观、角色与职责、沟通约定、开发规约、DoD 初版、签字

## 4. 角色承诺书

- 文件：[`docs/team/role-commitment.md`](../team/role-commitment.md)
- 内容：共同承诺 + PO / SM / Developer / QA 分角色承诺 + 全员签署表
- 状态：已按 5 人实际角色补全，并随对应 PR 合并

## 5. 成员 PR 合并截图

每人一个 hello PR，均已合并。截图文件命名 `pr-<拼音>.png`，放在本目录。

| 成员 | 角色 | PR | 分支 | 合并提交 | 评审记录 | 截图 |
| --- | --- | --- | --- | --- | --- | --- |
| 魏志国 | PO | [#5](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/5) | `feature/weizhiguo-hello` | `ee0b1a3` | Liujunfeng520 评论 | `pr-weizhiguo.png` |
| 陈明哲 | SM | [#9](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/9) | `cmz666-BOT-patch-1` | `bc82971` | 无正式 Review ⚠️ | `pr-chenmingzhe.png` |
| 刘俊琒 | SM | [#7](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/7) | `feature/liujunfeng-hello` | `35c711e` | cmz666-BOT **Approve** | `pr-liujunfeng.png` |
| 青岑 | Developer / QA | [#4](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/4) | `feature/qing963666-hello` | `c6b16d3` | 无正式 Review ⚠️ | `pr-qingcen.png` |
| 房国栋 | Developer | [#1](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/1) | `feature/fangguodong-hello` | `e8f1d8b` | 2123345397-star 评论 | `pr-fangguodong.png` |

其他团队协作 PR：

| PR | 内容 | 合并提交 | 评审 |
| --- | --- | --- | --- |
| [#6](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/6) | 团队章程 v1.0 | `e1caa47` | 青岑 **Approve** |
| [#3](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/3) | 角色承诺书 | `1a1e1a9` | 青岑 评论 |
| [#11](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/11) | 第一次站会纪要 | `e9a1224` | 2123345397-star 评论 |
| [#2](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/2) | 青岑 hello 初版（改名后关闭） | — | 房国栋 Request changes |

---

## 提交物状态

| # | 提交物 | 路径 | 状态 | 负责人 |
| --- | --- | --- | --- | --- |
| 1 | 仓库 URL + 分支保护 | 本页第 1 节 + `screenshots/branch-protection.png` | ✅ 已配置并截图 | 刘俊琒 |
| 2 | 看板截图 | `lab1-board.png` | ✅ | 陈明哲 |
| 3 | 团队章程 v1.0 | `docs/team/charter-v1.0.md` | ✅ 已合并 | 魏志国 |
| 4 | 角色承诺书 | `docs/team/role-commitment.md` | ✅ 已补全 | 陈明哲 |
| 5 | 成员 PR 合并截图 | `pr-<拼音>.png` ×5 | ✅ | 各成员 |
| + | 初始 Backlog | `backlog.md` | ✅ | 魏志国 |

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
- [x] 分支保护截图 `screenshots/branch-protection.png` 已就位
- [x] 每位成员的 PR 合并截图：5 张齐全

---

## 2026-10-27 修订记录

针对评审反馈（批次 2）修订：

1. 仓库地址更新为组织仓库 `hbuas-se-2026fall/cs2412-team04-campus-overflow`
2. 默认分支更正为 `develop`（`main` 为发布分支）
3. 分支保护规则按 V3 更新：`main` ≥2 Approve（含 SM/PO）、`develop` ≥1 非作者 Approve、不允许绕过
4. 分支保护截图路径为 `screenshots/branch-protection.png`
5. 青岑 PR 截图文件名更正为 `pr-qingcen.png`
6. 删除“角色承诺书（内容待补全）”等过期表述

---

## 评分要点对照

| 评分项（占比） | 对应材料 | 状态 |
| --- | --- | --- |
| 工具链配置完整性（30%） | 仓库、分支保护、看板、IM 群 | ✅ 均已就位 |
| 团队章程质量（30%） | 章程 v1.0 是否具体可执行 | ✅ 含价值观、沟通、规约、DoD、签字 |
| Git 协作熟练度（30%） | 分支、PR、评审、合并流程 | ✅ 8 个 PR 已合并；2 个 PR 缺正式 Review（已在第 5 节标注） |
| 团队氛围与协作（10%） | 课堂观察、站会记录 | ✅ `docs/team/daily-standup-01.md` |