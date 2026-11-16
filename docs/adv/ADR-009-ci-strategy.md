# ADR-009: CI 流水线集成测试容错策略
## 状态：已采纳
## 背景：Actions 免费额度有限，且初期集成测试可能不稳定。
## 决策：
1. 文档改动（`docs/**`, `*.md`）不触发 CI（通过 `paths-ignore` 实现），节省免费额度。
2. 单元测试（`mvn test`）必须通过。
3. 集成测试（`mvn verify`）设置 `continue-on-error: true`，即使失败也不会阻断合并，但会显示红叉提醒。
