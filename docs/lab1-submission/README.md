# Lab 1 提交物清单 · TeamX

> 团队：TeamX　项目：CampusOverflow 校园问答平台
> 仓库：<https://github.com/2123345397-star/CampusOverflow>
> 提交日期：2026-10-27　对应实验：Lab 1 团队组建与协作工具链（Sprint 0）

本文件是 Lab 1 的**提交索引**：老师按本页即可找到全部材料，不必翻聊天记录。

---

## 1. 团队代码仓库 URL

- 仓库地址：<https://github.com/2123345397-star/CampusOverflow>
- 默认分支：`main`
- 成员：5 人均已参与分支推送与 PR 协作
- **分支保护：已开启**（截图见 `branch-protection.png`）

规则配置（Settings → Branches → 规则名 `main`）：

| 选项 | 设置 |
| --- | --- |
| Require a pull request before merging | ✅ 开启 |
| Require approvals | ✅ 1 人 |
| Dismiss stale pull request approvals when new commits are pushed | ✅ 开启 |
| Require conversation resolution before merging | ✅ 开启 |

**生效验证**：直接向 `main` 推送会被拒绝，返回
`GH006: Protected branch update failed ... Changes must be made through a pull request.`

## 2. 团队看板截图

- 文件：`lab1-board.png`　状态：✅ **已完成**
- 要求：四列（`Backlog`/`To Do`、`In Progress`、`In Review`、`Done`）可见，`Backlog`/`To Do` 至少 5 条卡片。
- 卡片来源：[`backlog.md`](backlog.md)（Sprint 0 共 8 条）。
- 看板链接：仓库 → Projects → CampusOverflow Sprint（如已建 Projects 可在此补链接）

## 3. 团队章程 v1.0

- 文件：[`docs/team/charter-v1.0.md`](../team/charter-v1.0.md)
- 合并记录：PR [#6](https://github.com/2123345397-star/CampusOverflow/pull/6)，合并提交 `e1caa47`
- 签署：全体 5 人（魏志国、陈明哲、刘俊琒、青岑、房国栋），评审中含 1 个 Approve
- 覆盖章节：团队价值观、角色与职责、沟通约定、开发规约、DoD 初版、签字

## 4. 角色承诺书

- 文件：[`docs/team/role-commitment.md`](../team/role-commitment.md)
- 内容：共同承诺 + PO / SM / Developer / QA 分角色承诺 + 全员签署表
- 说明：PR [#3](https://github.com/2123345397-star/CampusOverflow/pull/3) 建立了该文件但内容为空；本次提交按 5 人实际角色补全，随本 PR 合并。

## 5. 成员 PR 合并截图

每人一个 hello PR，均已合并到 `main`。截图文件命名 `pr-<拼音>.png`，放在本目录。

| 成员 | 角色 | PR | 分支 | 合并提交 | 评审记录 | 截图 |
| --- | --- | --- | --- | --- | --- | --- |
| 魏志国 | PO | [#5](https://github.com/2123345397-star/CampusOverflow/pull/5) | `feature/weizhiguo-hello` | `ee0b1a3` | Liujunfeng520 评论 | `pr-weizhiguo.png` ✅ |
| 陈明哲 | SM | [#9](https://github.com/2123345397-star/CampusOverflow/pull/9) | `cmz666-BOT-patch-1` | `bc82971`  | `pr-chenmingzhe.png` ✅ |
| 刘俊琒 | SM | [#7](https://github.com/2123345397-star/CampusOverflow/pull/7) | `feature/liujunfeng-hello` | `35c711e` | cmz666-BOT **Approve** | `pr-liujunfeng.png` ✅ |
| 青岑 | Developer / QA | [#4](https://github.com/2123345397-star/CampusOverflow/pull/4) | `feature/qing963666-hello` | `c6b16d3`  | `pr-qing963666.png` ✅ |
| 房国栋 | Developer | [#1](https://github.com/2123345397-star/CampusOverflow/pull/1) | `feature/fangguodong-hello` | `e8f1d8b` | 2123345397-star 评论 | `pr-fangguodong.png` ✅ |

其他团队协作 PR（非个人 hello，作为过程证据）：

| PR | 内容 | 合并提交 | 评审 |
| --- | --- | --- | --- |
| [#6](https://github.com/2123345397-star/CampusOverflow/pull/6) | 团队章程 v1.0 | `e1caa47` | 青岑 **Approve** |
| [#3](https://github.com/2123345397-star/CampusOverflow/pull/3) | 角色承诺书（内容待补全） | `1a1e1a9` | 青岑 评论 |
| [#11](https://github.com/2123345397-star/CampusOverflow/pull/11) | 第一次站会纪要 | `e9a1224` | 2123345397-star 评论 |
| [#2](https://github.com/2123345397-star/CampusOverflow/pull/2) | 青岑 hello 初版（改名后关闭） | — | 房国栋 Request changes |

---

## 提交物状态

| # | 提交物 | 路径 | 状态 | 负责人 |
| --- | --- | --- | --- | --- |
| 1 | 仓库 URL + 分支保护 | 本页第 1 节 + `branch-protection.png` | ✅ 已开启并截图 | 魏志国 |
| 2 | 看板截图 | `lab1-board.png` | ✅ 已截图 | 陈明哲 |
| 3 | 团队章程 v1.0 | `docs/team/charter-v1.0.md` | ✅ 已合并 | 魏志国 |
| 4 | 角色承诺书 | `docs/team/role-commitment.md` | ✅ 已补全 | 陈明哲 |
| 5 | 成员 PR 合并截图 | `pr-<拼音>.png` ×5 | ✅ 5 张齐全 | 各成员 |
| + | 初始 Backlog | `backlog.md` | ✅ | 魏志国 |
