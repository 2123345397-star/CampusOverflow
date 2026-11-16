# 发布验证记录：v0.2.0 / v0.3.0

> 验证人：陈明哲（E · 测试与质量门禁）
> 验证时间：2026-11-16
> 对应任务：Sprint 1 / Sprint 2 版本发布参与验证
> 关联 PR：[#115 release: v0.3.0（Sprint 2 · Reputation / 悬赏）](https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/pull/115)

## 一、结论

| 版本 | Tag | 指向提交 | 位置 | CI 结果 | 结论 |
| --- | --- | --- | --- | --- | --- |
| v0.2.0 | `v0.2.0`（annotated） | `a381bb8`（S1 收尾，2026-11-12） | develop 历史（S1 发布 PR 当时跳过，本 tag 补打） | 后端 ✅、前端 ✅、镜像 ⏭ skipped | 通过 |
| v0.3.0 | `v0.3.0`（annotated） | `a4114dc`（PR #115 合入 main，2026-11-16） | **main** | 后端 ✅、前端 ✅、容器镜像 ✅ | 通过 |

CI 检查页：

- v0.2.0：<https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/commit/a381bb8/checks>
- v0.3.0：<https://github.com/hbuas-se-2026fall/cs2412-team04-campus-overflow/commit/a4114dc/checks>

## 二、Tag 校验

```text
$ git tag -l -n1
v0.1.0  Sprint 0 验收基线：仓库/CI/DDD/团队文档/ADR-009 就绪
v0.2.0  Sprint 1 验收基线（Q&A 核心）：2026-11-12 补打；pipeline 已绿
v0.3.0  v0.3.0 发布（Sprint 2 Reputation / 悬赏）：PR #115 合入 main

$ git describe --tags --exact-match a381bb8
v0.2.0
$ git describe --tags --exact-match a4114dc
v0.3.0
$ git cat-file -t v0.3.0
tag
```

三个 tag 均为 annotated tag（`objecttype=tag`）；未做 GPG 签名（团队未配置签名密钥），校验以 tag 对象与 tagger 为准。

## 三、CI 与测试口径

- CI 的 `SPRINT_MODULES` 已含 `co-reputation-domain` / `co-reputation-application`，v0.3.0 的后端 job 覆盖 Q&A + Reputation 的单元测试与覆盖率门禁；
- 门禁：`domain.coverage.minimum=0.70`，`co-reputation-domain` 行覆盖 80.72%（本地 `mvn verify` 实测，2026-11-15）；
- v0.3.0 的 `容器镜像构建` 首次为 successful（后端/前端镜像均可构建），满足“随时可部署”的发布检查。

## 四、端到端冒烟

发布前用 T1-15 冒烟脚本在 `demo` profile（MySQL 8.4 + Redis 5.0）跑过完整链路：

```text
SMOKE PASS
  用户 A=smokeA1115164825187 (id=18)  用户 B=smokeB1115164825187 (id=19)
  问题 id=8  回答 id=6  score=1  acceptedAnswerId=6
  耗时 5s  base=http://127.0.0.1:18080
```

脚本与用法：[`scripts/smoke/README.md`](../../scripts/smoke/README.md)。

## 五、说明与遗留

- **v0.2.0 为补打**：S1 结束时未执行 `develop → main` 发布 PR，tag 落在 S1 收尾提交 `a381bb8`（develop 历史）；S2 的发布 PR #115 才把 develop 合入 main，因此 `v0.3.0` 指向 main 的 `a4114dc`。
- 后续 Sprint 3（v0.9.0 RC）与 Sprint 4（v1.0.0）应严格走「develop → main 发布 PR → 打 tag → Release Notes」流程，避免再次补打。
- 本次验证不包含 GPG 签名与镜像推送 GHCR（属 S4 扩展项）。

## 六、签署

| 角色 | 成员 | 验证内容 | 结论 | 日期 |
| --- | --- | --- | --- | --- |
| E · 测试与质量门禁 | 陈明哲 | tag 指向、CI 三阶段、覆盖率门禁、冒烟脚本 | 通过 | 2026-11-16 |
