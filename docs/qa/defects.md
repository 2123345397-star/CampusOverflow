# Sprint 0 缺陷与差距清单

> 负责人：青岑（QA）
> 记录规则：严重程度（高/中/低）、复现步骤、预期、实际、负责人、状态
> 说明：部分条目基于仓库与 GitHub 页面静态核查，待环境就绪后复现并更新状态。

| 编号 | 严重程度 | 模块 | 现象 | 复现/证据 | 预期 | 实际 | 负责人 | 状态 |
|---|---|---|---|---|---|---|---|---|
| BUG-001 | 高 | 课程页 | 前端没有课程页，无法在 UI 完成课程列表/我的课程/加入课程 | `frontend/src/App.tsx` 只有 `/`、`/questions/:id`、`/ask`、`/login`、`/leaderboard`、`/notifications`，无 `/courses` | 存在课程页，支持列表/我的课程/加入 | 缺失 | 房国栋 | 待修复 |
| BUG-002 | 中 | CI | PR 上没有 CI 检查运行 | PR #17 / #18 / #19 均显示 Checks 0 | PR 触发 CI 并显示检查结果 | Checks 0 | 刘俊琒 | 待修复 |
| BUG-003 | 中 | 分支保护 | develop 允许管理员绕过，main/develop 未启用必需状态检查 | `docs/lab1-submission/README.md:27-34` 的 API 核验表 | 不允许绕过；必需 CI 检查开启 | 未开启 | 刘俊琒/魏志国 | 待修复 |
| BUG-004 | 中 | 文档一致性 | 事件风暴图仍是 6 个上下文、28 个事件，与 V3 4 上下文和 D1 30 事件不一致 | `docs/ddd/event-storming.png`；`docs/ddd/glossary.md` 仍写“通知上下文” | 4 个上下文；通知归 Discovery | 6 个上下文 | 魏志国/陈明哲 | 待修复 |
| BUG-005 | 低 | 流程 | 房国栋提交使用 baigui 身份，Contributors 只有 4 人 | `git log --all --author=baigui`；没有 fangguodong 提交 | 提交邮箱绑定 GitHub 账号 2177023844 | 未绑定 | 房国栋 | 待修复 |
| BUG-006 | 低 | 文档 | `docs/team.md`、`docs/team-agreement.md`、ADR-009 缺失 | 仓库树中没有这些文件 | V3 C2/C3/C4/A4 要求存在 | 缺失 | 陈明哲/刘俊琒 | 待修复 |