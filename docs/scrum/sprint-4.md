# Sprint 4 · 稳定化与发布 v1.0.0（草案）

> Scrum Master：魏志国（PO 代拟）　发布经理：魏志国
> 范围：稳定化、禁用账号、可观测性、v1.0.0 发布
> 状态：**草案 / 进行中**；计划会确认后由 SM 去掉本标记。
> 2026-11-17 更新：T4-02（#127）、T4-05（#129）、T4-07（#130）已合入 develop；T4-01 发布 PR、T4-08 彩排仍待执行。

## 一、Sprint 目标

1. 完成 `develop → main` 发布 PR（≥2 Approve，含 SM/PO），打 `v1.0.0` 标签并发布 Release；
2. 管理员可禁用账号，被禁用者旧会话立即失效且不能再登录；
3. 清理 release 阻塞项：覆盖率报告、9.4 交付物索引、彩排记录。

## 二、任务与状态

| 任务 | 内容 | 负责人 | 状态 |
| --- | --- | --- | --- |
| T4-02 | `AdminUserService.disable` + `PUT /api/v1/admin/users/{userId}/disable` + 会话撤销 + 审计 + 3 条测试 | 魏志国 | ✅ 已合入 #127；彩排时做一次真实 Redis 会话验证 |
| T4-05 | 管理页：待认证学生、认证、建教职工、改角色 | 青岑 | ✅ 已合入 #129；截图见 `docs/final/screenshots/admin/` |
| T4-07 | README、ADR-010、用户手册 | 青岑 | ✅ 已合入 #130 |
| T4-01 | develop→main 发布 PR、v1.0.0 tag、Release Notes | 魏志国 + 房国栋 | ⬜ 待 S4 收口与 Approve |
| 可观测性 | Grafana/Prometheus 面板、发件箱/对账指标 | 房国栋/陈明哲 | ✅ 已合入 #128 |
| 最终彩排 | `docker compose up -d --build` 干净环境走 README + 5 分钟演示 | 陈明哲 | ⬜ |
| 9.4 交付物 | `docs/final/README.md` 七项索引、个人总结、视频 | 全员 | 🟨 索引已建，内容待补 |

## 三、验收标准

- [ ] v1.0.0 tag 与 GitHub Release 可见，Release Notes 含新功能/已知问题/部署步骤
- [ ] 禁用账号：非 ADMIN 403、禁自己 ID-1007、禁用后旧会话失效且登录失败
- [ ] `mvn verify` / CI 全绿，覆盖率报告可点开
- [ ] 干净环境按 README 能启动并完成注册→登录→提问→回答
- [ ] `docs/final/README.md` 七项全部有可点链接

## 四、已知阻塞

1. S3/S4 的 PR 需要非作者 Approve；v1.0.0 发布 PR 需要 ≥2 Approve（含 SM/PO）。
2. 陈明哲的最终彩排与 CI 全绿确认。
3. 个人总结、5 分钟视频、最终覆盖率报告链接。
