# Sprint 3 · 计划（Discovery：搜索投影 / 通知中心 / 相关推荐）

> Scrum Master：房国栋（D，角色卡 S3 既定）　PO：魏志国
> 时间盒：2026-11-23（周一）～ 2026-12-04（周五），共 10 个工作日
> 范围：PB-31～PB-40（Discovery / 搜索与通知）；范围来源：[`docs/backlog/product-backlog.md`](../backlog/product-backlog.md)
> 状态：**草案（PO 代拟，2026-11-16）—— 待房国栋主持 S3 计划会确认后去掉本标记**
> S3 目标（角色卡口径）：**移除全部行走骨架，`mvn verify` 全绿**；搜索、推荐、通知三处在界面可用。

---

## 一、Sprint 目标与范围

| 议题 | 结论 | 备注 |
| --- | --- | --- |
| Sprint 目标 | 搜索能搜到中文、相关问题可推荐、通知中心可用；CI 恢复完整门禁 | 角色卡 S3 |
| 迭代范围 | PB-31～PB-40；骨架清理（`DiscoveryQueryService` 2 个、`SearchIndexProjector` 10 个 TODO） | `docs/ddd/` 与缺陷台账 |
| 本次不做 | S4 发布项（v1.0.0、Grafana 面板、禁用账号）；新功能面不做扩张 | T4 系列留给 Sprint 4 |

## 二、当前基础（S2 期间已提前完成）

| 项 | 状态 | 证据 |
| --- | --- | --- |
| `SearchQuery.terms` / `RelatedQuestionPolicy`（青岑 T3-01/02） | ✅ 已合入 develop | PR #117（`5f3caaa`） |
| 通知后端（房国栋 T3-03/06/07：MentionParser / NotificationProjector / NotificationService） | ✅ 已在 develop，0 TODO | 文件树核查 2026-11-16 |
| 搜索边界证据（单字 422 / 空词处理） | ✅ 截图 | [`sprint-3-search-422.png`](sprint-3-search-422.png)、[`sprint-3-search-empty.png`](sprint-3-search-empty.png) |
| 搜索冒烟记录 | ✅ | [`sprint-3-search.md`](sprint-3-search.md) |
| 搜索投影 `SearchIndexProjector`（T3-04，B 代 A 实现） | 🟨 PR #120 | 10 个 TODO 实现 + `SearchIndexProjectorTest` 3 条幂等用例 |
| 通知侧测试 | ✅ 本次补充 | `NotificationProjectorTest` 5 条 + `NotificationServiceTest` 3 条 |
| CI 纳入 discovery | ✅ 本次补充 | `SPRINT_MODULES` 增加 `co-discovery-domain/application` |

## 三、任务分解（待计划会确认到 ≤1 天粒度）

| 成员 | 任务 | 内容 | 验收 |
| --- | --- | --- | --- |
| 刘俊琒（A） | T3-04 | `SearchIndexProjector` 十个方法：firstTime 幂等 + Posted/Edited/AnswerSubmitted/ContentDeleted/Accepted/VoteCast(仅 QUESTION)/Bounty 事件投影 | 同一事件投递两次只生效一次 |
| 青岑（C） | T3-05 | `DiscoveryQueryService` search/related：SearchPort → 批量作者 → 视图；中文词命中、单字 422 DIS-1001 | 域测试 + 应用测试全绿 |
| 房国栋（D） | T3-10 | 前端搜索页（标签/未回答/悬赏筛选 + 四排序）、相关问题侧栏、通知单条已读、@昵称高亮 | 三处在界面可用；**T4-03 之外的 S3 前端全部收口** |
| 房国栋（D） | SM | 主持计划会（确认本文件）+ 每周一/三/五站会 + 燃尽图归档 | `docs/scrum/sprint-3-burndown.png` |
| 陈明哲（E） | T3-08 | 删除 Discovery 剩余骨架；本地与 CI 跑 `mvn verify`（含 CampusOverflowFlowIT）；CI 恢复完整门禁；ADR-009 状态改“已完成” | 5 个后端阶段 + 前端阶段全绿 |
| 陈明哲（E） | T3-09 | 根 POM `domain.coverage.minimum` 收口 0.70 并保证通过 | JaCoCo 报告 ≥70% |
| 魏志国（B，PO） | PO | PB-31~40 验收口径与 Milestone 描述；评审会 9.1 七维度自查 | 任意证据当场可指出 |
| 全员 | T3-11 | demo profile 完整走查演示数据，走不通记缺陷 Issue | 演示路径 0 阻断缺陷 |
| 全员 | T3-12 | 回归测试与缺陷修复，功能冻结 | `mvn verify` 全绿且无 P0/P1 缺陷 |

## 四、每日站会（10:00，≤15 分钟，三问）

> 开始后按日填写；SM 房国栋维护。

| 日期 | 成员 | 昨日完成 | 今日计划 | 阻塞 |
| --- | --- | --- | --- | --- |
| （待填） | | | | |

## 五、验收清单

- [x] `mvn verify` 全绿（5 个后端阶段 + 前端阶段），CI 完整门禁恢复（E 复核 2026-11-16）
- [x] co-discovery 模块 **0 TODO**（`DiscoveryQueryService`、`SearchIndexProjector`）
- [x] `domain.coverage.minimum=0.70` 通过；ADR-009 状态改“已完成”
- [ ] 中文搜索命中（ADR-005 ngram 口径）、相关问题返回有序列表
- [ ] 通知：同一事件不重复发；只能标记自己的；未读数正确
- [ ] 前端搜索页 + 相关侧栏 + 通知已读 + @高亮 可演示
- [ ] T3-11 demo 走查记录（0 阻断缺陷）；T3-12 功能冻结
- [ ] 燃尽图归档 `docs/scrum/sprint-3-burndown.png`


## 六、CI 与验证（E 复核，2026-11-16）

| 检查项 | 结果 | 证据 |
| --- | --- | --- |
| 全仓 `mvn verify`（27 模块） | **BUILD SUCCESS** | 2026-11-16 本地 JDK 21 实跑；含 ArchitectureTest |
| CI 阶段 | 编译 / 单元测试 / ArchUnit / 覆盖率门禁 / 集成测试（容错）+ 前端 typecheck & build | [`.github/workflows/ci.yml`](../../.github/workflows/ci.yml) |
| SPRINT_MODULES 扩容 | 加入 `co-discovery-domain` / `co-discovery-application` | 同上 |
| co-discovery 测试 | domain 6 条 + application 6 条，全绿 | `DiscoveryDomainTest`、`DiscoveryQueryServiceTest`、`SearchIndexProjectorTest` |
| co-discovery 覆盖率 | 行覆盖 **78.26%**（72/92）≥ 0.70 | JaCoCo `jacoco.csv`；`check-domain-coverage` 通过 |
| ADR-009 | 状态改为“已完成” | [`docs/adr/009-渐进式质量门禁.md`](../adr/009-渐进式质量门禁.md) |

> 复核人：陈明哲（E）　日期：2026-11-16。S3 的搜索/通知/前端验收项仍以 C、D 的实测记录为准。
>
> 青岑复核（2026-11-17）：按 `co-discovery-domain/target/site/jacoco/jacoco.csv` 重算 LINE 72/92 = **78.26%**；
> 报告截图与 CSV 见 [`docs/final/screenshots/coverage/`](../final/screenshots/coverage/README.md)。

## 七、关联

- tech-debt：#44（行走骨架移除）
- 缺陷台账：[`docs/qa/defects.md`](../qa/defects.md)
- 上一版本：v0.3.0（Release：<https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/releases/tag/v0.3.0>）
