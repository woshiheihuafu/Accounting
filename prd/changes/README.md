# 产品变更索引

本文件追踪 `prd/` 下的产品变更（`CHANGE-*`），用于记录每次产品规则/范围/验收变化的来源、影响和落地状态。

## 索引规则

- 每个变更使用稳定 `CHANGE-<AREA>-NNN` ID，不复用、不删除。
- 状态：`draft`、`reviewed`、`approved`、`handed_off`、`implemented`、`superseded`。
- 每个变更应链接来源、受影响的 `REQ-*` / `SCREEN-*` / `FLOW-*` 和端侧影响摘要。

## 变更列表

| CHANGE ID | 标题 | 创建日期 | 状态 | 来源 | 涉及 REQ/SCREEN/FLOW | 影响摘要 | 关联 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| CHANGE-ACCOUNTING-001 | 禁止未来日期 | 2026-06-24 | implemented | 用户产品决策，经问题分类门归类为变更 | REQ-ACCOUNTING-006；SCREEN-ACCOUNTING-V2-002/006；FLOW-ACCOUNTING-V2-001/002/003 | [`prd/impact/CHANGE-ACCOUNTING-001-platform-impact.md`](../impact/CHANGE-ACCOUNTING-001-platform-impact.md) | PRD-CODE-ACCOUNTING-20260623；intake INTAKE-002 | 年月/日期选择器不可选未来，保存前 clamp 到今天，新建账单初始日期默认今天；Android 端已实现 |
