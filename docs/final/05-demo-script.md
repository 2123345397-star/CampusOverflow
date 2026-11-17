# 5. 演示脚本（≤15 分钟彩排 / ≤5 分钟视频）

> 录制文件：[`demo-v1.0.0.webm`](demo-v1.0.0.webm)（55 秒，无旁白，覆盖登录/提问/回答/投票/采纳/通知/搜索/排行榜）
> 目标：15 分钟内讲完六段，每人 ≥2 分钟；留 1 分钟给提问与异常分支。
> 前置：`docker compose -f deploy/docker-compose.yml --profile observability up -d --build`，demo profile 已加载数据；用两个浏览器窗口（学生 A / 学生 B）。

## 5.1 分段与计时

| 时间 | 段落 | 主讲 | 内容 | 兜底 |
| --- | --- | --- | --- | --- |
| 0:00–2:00 | 架构 | 陈明哲 | 模块化单体 + 六边形；4 上下文；outbox + 幂等 + 对账 | 打开展示 [`docs/final/01-architecture.md`](01-architecture.md) |
| 2:00–6:00 | 问答主流程 | 刘俊琒 | 登录 → 发问题（标签小写）→ B 回答 → 投票 → 采纳 → 详情 | 提前用 `http/questions.http` 预热一条问题 |
| 6:00–9:00 | 声誉与悬赏 | 魏志国 / 青岑 | 采纳后 +15；发起 50 分悬赏（冻结 50）；结算/退款；排行榜 | 用 demo 教师账号 T2001；数据已由 seeder 准备 |
| 9:00–12:00 | 搜索、通知与管理页 | 青岑 / 房国栋 | 中文搜索（红黑树）→ 相关问题 → 通知未读/已读 → 管理员认证/无权限 | 搜索无结果时展示 `sprint-3-search.md` 的 422/空结果证据并说明索引；管理页用截图兜底 |
| 12:00–14:00 | CI 与质量 | 陈明哲 | Actions 五阶段 + 前端；覆盖率四模块；Grafana 四面板 | 提前打开 Actions 与 Grafana 页面各一个标签 |
| 14:00–15:00 | 收尾 / 提问 | 全员 | v1.0.0 范围、已知风险（BUG-003）、下一步 | 用 `docs/final/03-quality.md` §3.7 |

## 5.2 逐步点击路径

**A. 问答（刘俊琒）**

1. 浏览器 1 登录 `2023001 / Campus2026`；
2. `/ask` 发问题：标题 ≥10 字、正文 ≥20 字、2 个标签（如 `#Java`、`Spring`），确认标签显示为小写；
3. 浏览器 2 登录 `2023002`，打开问题 → 提交回答；
4. 浏览器 1 给回答投票（赞/踩/撤），刷新看分数；
5. 浏览器 1 采纳该回答 → 详情显示“已采纳”，回答者声誉 +15（浏览器 2 打开 `/leaderboard` 或 `/users/{id}/reputation` 对照）。

**B. 悬赏（魏志国 / 青岑）**

1. 教师 `T2001` 登录 → 问题详情 → 「发起悬赏」→ 50 分 / 7 天；
2. 数据库或页面确认：发起人可用 950、冻结 50；
3. 提问者采纳回答 → 悬赏结算：获奖者 +50、发起人冻结归零；
4. 备用：另发一个悬赏后等 7 天不现实，展示 `sprint-2-bounty-expiry.log` 的到期退款记录。

**C. 搜索、通知与管理页（青岑 / 房国栋）**

搜索（约 1 分钟）：

1. `/search?q=红黑树` → 结果含作者/标签/采纳状态；
2. 打开结果 → 右侧「相关问题」；
3. 单字 `q=树` 显示 422 `DIS-1001`（边界口径）。

通知（约 1 分钟）：

4. 触发一次回答/评论 → 右上角通知未读数 +1 → 点开一条 → 未读数变化。

管理页（约 1 分钟）：

5. `admin01 / Campus2026` 登录 → 导航「管理」→ `/admin`；
6. 「待认证学生」列表 → 对 `SeedUser` 点「认证」→ 列表人数减少
   （`TestStudent` 已在 2026-11-17 截图时认证，现场改用仍在待认证列表的 `SeedUser`）；
7. 学生 `2023002` 直接访问 `/admin` → 「无权限访问」；
8. 时间够再演示「建教职工」/「改角色」，不够则口述并展示
   [`screenshots/admin/admin-users-pending.png`](screenshots/admin/admin-users-pending.png)
   与 [`screenshots/admin/admin-forbidden.png`](screenshots/admin/admin-forbidden.png)。

> 青岑的完整计时讲稿见 [`rehearsal-qingcen.md`](rehearsal-qingcen.md)，刘俊琒（问答主流程 4:00）见 [`rehearsal-liujunfeng.md`](rehearsal-liujunfeng.md)。

**D. CI 与可观测性（陈明哲）**

1. Actions 最近一次 `develop`：五个后端阶段 + 前端；
2. Coverage：四个 domain 模块 70%+（qa 83.12 / identity 75.74 / reputation 80.72 / discovery 78.26）；
3. Grafana `CampusOverflow · 质量与可观测性`：
   - 发件箱积压 `co_outbox_backlog`
   - 死信 `co_outbox_dead_letters`
   - 对账不一致 `co_reputation_mismatches`
   - 接口 P95 `http_server_requests_seconds`
4. 现场“造 1 归零”：手动改一个账户余额（或插一条不一致流水），等 ≤60s 看 `co_reputation_mismatches=1`，修复后回到 0。

## 5.3 ≤5 分钟视频分镜

| 时间 | 画面 | 旁白 |
| --- | --- | --- |
| 0:00–0:40 | 架构图 + 模块树 | 模块化单体 + 4 上下文，跨上下文走 api/事件 |
| 0:40–2:20 | 问答主流程（提问→回答→采纳） | 一条链路的真实操作，标签规范化、采纳权限 |
| 2:20–3:20 | 声誉/悬赏 + 排行榜 | +15、悬赏冻结/结算、幂等 |
| 3:20–4:10 | 搜索/相关/通知/管理页 | ngram 中文检索、单字 422、通知未读、管理页权限 |
| 4:10–5:00 | CI + Grafana | 五阶段、覆盖率、四面板、对账归零 |

## 5.4 彩排检查清单

- [ ] demo profile 数据完整（6 账号 / 1 课程 / 3 问题）
- [ ] 两个浏览器窗口已分别登录 A/B，不共享 Cookie
- [ ] Actions / Grafana / Swagger 标签页提前打开
- [ ] 每人至少讲 2 分钟；总时长 ≤15 分钟（计时见 [`rehearsal-log.md`](rehearsal-log.md)）
- [ ] 管理页：ADMIN 有入口、学生无入口且 `/admin` 显示无权限
- [ ] 准备 1 个异常分支（如搜索无结果）的话术
