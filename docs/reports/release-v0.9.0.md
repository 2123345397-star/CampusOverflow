# Release v0.9.0 RC · Sprint 3（Discovery）

> 状态：**RC（Release Candidate）**　日期：2026-11-16
> 目标分支：`develop`（PR #126）；正式发布仍按「develop → main 发布 PR → 打 tag」流程复核
> 范围：PB-31～PB-40（搜索 / 相关推荐 / 通知中心）

## 一、本版本包含

| 项 | 内容 |
| --- | --- |
| 搜索投影 | `SearchIndexProjector` 10 个方法：QuestionPosted/Edited、AnswerSubmitted、ContentDeleted、AnswerAccepted、VoteCast(QUESTION)、BountyOpened/Awarded/Closed；`ProcessedEventStore` 幂等（PR #120） |
| 搜索/推荐 | `SearchQuery.terms`、`RelatedQuestionPolicy`、`DiscoveryQueryService.search/related`（PR #117） |
| 通知中心 | `MentionParser`、`NotificationProjector`、`NotificationService`；通知测试 `NotificationProjectorTest` 5 条 + `NotificationServiceTest` 3 条（PR #126） |
| CI | `SPRINT_MODULES` 纳入 `co-discovery-domain` / `co-discovery-application`（PR #126） |
| HTTP 冒烟 | `http/discovery.http`：中文搜索、筛选、相关问题、通知列表/未读/已读 |

## 二、验证

- `co-discovery-application` 全模块 **14/14** 通过（含 `DiscoveryQueryServiceTest` 3、`SearchIndexProjectorTest` 3、`NotificationProjectorTest` 5、`NotificationServiceTest` 3）
- `co-discovery-domain` **6/6** 通过
- 搜索冒烟证据：`docs/scrum/sprint-3-search.md` 与 `sprint-3-search-*.png`
- CI discovery 覆盖率门禁（`domain.coverage.minimum=0.70`）由 PR #126 的 Actions 结果确认

## 三、已知限制

1. 本 tag 为 **RC**，指向 `s3/completion` 的最终提交；PR #126 合入 `develop` 后，正式发布仍应走 `develop → main` 并复核 tag/Tag 对象。
2. 旧 walking-skeleton 时期已投递的事件不会自动补搜索索引；需要新问题或单独 reindex。
3. `env/env-fangguodong.txt` 的 Docker 二采来自青岑机器代跑记录，房国栋本机截图仍待本人补交。
4. `docs/scrum/sprint-3.md` 仍是计划草案，S3 结束后由房国栋主持计划会确认并更新为执行记录。
