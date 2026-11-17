# Sprint 0 缺陷与差距清单

> 负责人：青岑（QA）
> 记录规则：严重程度（高/中/低）、复现步骤、预期、实际、负责人、状态
> 说明：部分条目基于仓库与 GitHub 页面静态核查；2026-11-15 由陈明哲（E）按仓库树、GitHub API 与 Actions 记录复核，更新了已解决项的状态与证据。

| 编号 | 严重程度 | 模块 | 现象 | 复现/证据 | 预期 | 实际 | 负责人 | 状态 |
|---|---|---|---|---|---|---|---|---|
| BUG-001 | 高 | 课程页 | 前端没有课程页，无法在 UI 完成课程列表/我的课程/加入课程 | `frontend/src/App.tsx` 已注册 `/courses`、`/courses/mine`；页面在 `frontend/src/pages/courses/`（`CourseListPage.tsx`、`MyCoursesPage.tsx`），PR #81 合入 | 存在课程页，支持列表/我的课程/加入 | 已具备 | 房国栋 | 已修复 |
| BUG-002 | 中 | CI | PR 上没有 CI 检查运行 | `.github/workflows/ci.yml` 已在 `pull_request` 上触发；PR #96、#101、#103 的 Checks 均运行且为绿（后端构建与验证 / 前端构建） | PR 触发 CI 并显示检查结果 | 已触发并显示 | 刘俊琒 | 已修复 |
| BUG-003 | 中 | 分支保护 | develop 允许管理员绕过，main/develop 未启用必需状态检查 | 已修复（2026-11-17）：陈明哲启用仓库规则集 #27436601「develop required checks」，对 `refs/heads/develop` 要求 `后端构建与验证`、`前端构建`（`bypass_actors` 为空）；classic protection 保留 `enforce_admins=true`、`required_approving_review_count=1`，`checks[].app_id=15368`（Actions）。`main` 为 2 Approve + 后端/前端/容器三项。PR #139/#140 的 check-runs 结论均 success | 不允许绕过；必需 CI 检查开启 | 已开启，管理员不可绕过 | 刘俊琒/魏志国 | 已修复 |
| BUG-004 | 中 | 文档一致性 | 事件风暴图 6 上下文/28 事件，与 V3 4 上下文和 D1 30 事件不一致 | 已修复：PR #75 合入（commit `0d7da3d`）重建 `docs/ddd/event-storming.png`（4 上下文/30 事件，与 `docs/ddd/events.md` 逐条一致）；`glossary.md:25-27` 改为“发现上下文(co-discovery)”；`context-map.md/png` 为 4 上下文、无伙伴关系 | 4 个上下文；30 事件；通知归 Discovery | 已一致 | 魏志国/陈明哲 | 已修复 |
| BUG-005 | 低 | 流程 | 房国栋提交使用 baigui 身份，Contributors 只有 4 人 | 新提交已绑定 GitHub 账号：`房国栋 <333220585+2177023844@users.noreply.github.com>`；历史 `baigui` 提交仍保留在 git 历史中（不改写历史） | 提交邮箱绑定 GitHub 账号 2177023844 | 新提交已绑定 | 房国栋 | 已修复（新提交） |
| BUG-006 | 低 | 文档 | `docs/team.md`、`docs/team-agreement.md`、ADR-009 缺失 | 三个文件均已存在：`docs/team.md`、`docs/team-agreement.md`、`docs/adr/009-渐进式质量门禁.md` | V3 C2/C3/C4/A4 要求存在 | 均存在 | 陈明哲/刘俊琒 | 已修复 |

## 复核记录

- 2026-11-15：陈明哲复核 BUG-001/002/003/005/006，依据仓库文件树、GitHub Branch Protection API、Actions 运行记录；BUG-004 保留待修复。
- 2026-11-16：魏志国复核 BUG-004：`docs/ddd/` 三份文档已统一（4 上下文/30 事件，通知归 Discovery），缺陷关闭。BUG-003 维持“部分修复”：develop 必需状态检查仍未启用（按 V3 需在 CI 稳定后开启），另发现发布阻塞项——`ci.yml` 的 `容器镜像构建` 仅 push 触发，目标为 `main` 的发布 PR 永远等不到该必需检查，已在 `chore/s2-closeout` 修正触发条件。
- T1-15 冒烟脚本已补齐：`scripts/smoke/smoke.ps1`、`scripts/smoke/smoke.sh`，用法见 `scripts/smoke/README.md`。
- 2026-11-16：陈明哲将 ADR-009 从 `docs/adv/` 迁到 `docs/adr/009-渐进式质量门禁.md`，并补 ADR 索引与 Sprint 0 记录（PR #118）。
- 2026-11-17：陈明哲通过仓库规则集 #27436601 为 develop 启用必需检查（后端构建与验证、前端构建），BUG-003 关闭。刘俊琒同日按 API 复核：classic protection 的 `checks[].app_id=15368`、规则集 `bypass_actors` 为空、PR #139/#140 的 check-runs 均 success；另一条判断依据是 `容器镜像构建` 的 `if` 限定 `push` 或 `base=main`，故只挂 `main`——若加到 `develop`，普通 PR 会永久等待一个不会上报的检查。
