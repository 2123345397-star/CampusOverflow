# Sprint 4 · 稳定化与发布 v1.0.0

> Scrum Master：陈明哲（E）　PO / 发布经理：魏志国
> 范围：稳定化、禁用账号、可观测性、v1.0.0 发布、彩排
> 状态：**已发布 v1.0.0（2026-11-17）**；T4-08 彩排待执行。
> 更新：T4-02（#127）、T4-04（#128）、T4-05（#129）、T4-07（#130）已合入；T4-01 已完成（PR #135 / tag v1.0.0 / Release）；T4-08 彩排与干净环境部署运行时验证待执行。

## 一、Sprint 目标

1. 完成 `develop → main` 发布 PR（≥2 Approve），打 `v1.0.0` 标签并发布 GitHub Release；
2. 管理员可禁用账号，被禁用者旧会话立即失效且不能再登录；
3. 四面板可观测性上线，`co_reputation_mismatches` 可演示造 1 归零；
4. 清理 9.4 交付物：覆盖率报告、docs/final 索引、彩排记录、个人总结。

## 二、任务与状态

| 任务 | 内容 | 负责人 | 状态 |
| --- | --- | --- | --- |
| T4-02 | `AdminUserService.disable` + `PUT /api/v1/admin/users/{userId}/disable` + 会话撤销 + 审计 + 测试 | 魏志国 | ✅ #127；验证见 [`s4-disable-account.md`](../reports/s4-disable-account.md) |
| T4-04 | Grafana 四面板 + `co_reputation_mismatches` 定时刷新 + HTTP P95 直方图 | 陈明哲 | ✅ #128；实测见 [`t4-04-observability.md`](../reports/t4-04-observability.md) |
| T4-05 | 管理页：待认证学生、认证、建教职工、改角色 | 青岑 | ✅ #129；截图 `docs/final/screenshots/admin/` |
| T4-07 | README、ADR-010、用户手册 | 青岑 | ✅ #130 |
| T4-01 | develop→main 发布 PR、v1.0.0 tag、GitHub Release、部署验证 | 陈明哲（SM）/ 魏志国（PO 审批） | ✅ PR #135 → `10925c7`；tag `v1.0.0`；[Release](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/releases/tag/v1.0.0) |
| T4-08 | 两次彩排：≤15 分钟、每人 ≥2 分钟、两次顺利 | 陈明哲组织 / 全员 | ⬜ 第一次 2026-11-17、第二次发布后，见 [`rehearsal-log.md`](../final/rehearsal-log.md) |
| 9.4 交付物 | 七项索引、个人总结、≤5 分钟视频 | 全员 | 🟨 索引、视频（`docs/final/demo-v1.0.0.webm`）与 3 份总结（陈明哲、青岑、刘俊琒）已入库；魏志国、房国栋 2 份待补 |

## 三、发布检查（T4-01）

- [x] `develop` 最新 CI：后端构建与验证 / 前端构建 / 容器镜像三绿
- [x] 全仓 27 模块 `mvn verify -DskipITs -Djacoco.skip=false` 通过（2026-11-17，JDK 21）
- [x] 覆盖率：qa 83.12% / identity 75.74% / reputation 80.72% / discovery 78.26%，均 ≥0.70
- [x] T1-15 冒烟 `SMOKE PASS`；demo profile 数据可加载
- [x] `develop → main` 发布 PR #135（魏志国、青岑 Approve，三检查绿）
- [x] `v1.0.0` tag 指向 main 合并提交 `10925c7`
- [x] GitHub Release（[v1.0.0](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/releases/tag/v1.0.0)）
- [ ] 干净环境 `docker compose -f deploy/docker-compose.yml up -d --build` 按 README 走通（需 Docker 机器）

## 四、验收标准

- [x] v1.0.0 tag 与 GitHub Release 可见
- [ ] 禁用账号：非 ADMIN 403、禁自己 ID-1007、禁用后旧会话失效且登录失败
- [x] `mvn verify` / CI 全绿，覆盖率报告可点开（`docs/final/screenshots/coverage/`）
- [ ] 干净环境按 README 启动并完成注册→登录→提问→回答
- [ ] `docs/final/README.md` 七项全部有可点链接
- [ ] 两次彩排记录（≤15 分钟、每人 ≥2 分钟）

## 五、已知阻塞

1. v1.0.0 发布 PR 需要 ≥2 Approve（含 SM/PO），且 main 的 `容器镜像构建` 必需检查需随 PR 触发（ci.yml 已支持 base=main）。
2. 彩排需全员到场；当前只有脚本、计时表和青岑的讲稿。
3. 5 分钟视频已入库；个人总结已交 3 份，魏志国、房国栋 2 份未提交。
4. 本机无 Docker，compose 一键部署需在彩排机器上验证。
