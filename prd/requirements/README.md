# PRD 索引

本文件是 `prd/` 下 PRD 的唯一索引入口，用于追踪多期 PRD 的状态、范围、需求 ID 和替代关系。创建、更新、替代或废弃 PRD 时必须同步更新本索引。

## 索引规则

- 每个 PRD 必须有稳定的 `PRD-*` ID。
- 每个 PRD 必须标注状态：`draft`、`reviewed`、`approved`、`implemented`、`deprecated` 或 `superseded`。
- 同一产品或功能允许存在多个历史 PRD，但同一范围内只能有一个当前有效 PRD；旧 PRD 应标记为 `superseded` 或 `deprecated`。
- 新 PRD 修改核心流程、范围边界或验收标准时，必须在“关系”中写明更新、替代或冲突关系。
- 新增 PRD 前必须先检查本索引，确认是否已存在相同范围、相同页面、相同流程或相同 `REQ-*`。

## PRD 列表

| PRD ID | 标题 | 文件 | 创建日期 | 状态 | 范围摘要 | 关联需求 | 关联设计 | 关系 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PRD-CODE-ACCOUNTING-20260617 | code记账 | [`prd/code-accounting-prd-20260617.md`](../code-accounting-prd-20260617.md) | 2026-06-17 | draft | 首页明细、记账入口、支出/收入类目、金额录入、日期选择、未实现入口占位 | REQ-ACCOUNTING-001 至 REQ-ACCOUNTING-006 | [`prd/code-accounting.pen`](../code-accounting.pen) | 关联：none；更新：none；替代：none；被替代：none；冲突：none | 当前唯一 PRD |

## 需求覆盖索引

| REQ ID | 需求摘要 | 当前来源 PRD | 状态 | 备注 |
| --- | --- | --- | --- | --- |
| REQ-ACCOUNTING-001 | 首页展示月份、收入合计、支出合计、快捷入口、登录提示、日期分组流水和底部 Tab | PRD-CODE-ACCOUNTING-20260617 | draft | none |
| REQ-ACCOUNTING-002 | 支出记账支持支出/收入 Tab，支出类目支持餐饮、购物、交通 | PRD-CODE-ACCOUNTING-20260617 | draft | none |
| REQ-ACCOUNTING-003 | 收入记账支持工资、兼职、理财、礼金、其他、设置 | PRD-CODE-ACCOUNTING-20260617 | draft | none |
| REQ-ACCOUNTING-004 | 账单录入支持金额、备注、日期和完成保存 | PRD-CODE-ACCOUNTING-20260617 | draft | none |
| REQ-ACCOUNTING-005 | 首页支持年月选择，记账页支持年月日选择 | PRD-CODE-ACCOUNTING-20260617 | draft | none |
| REQ-ACCOUNTING-006 | 预算、资产、返现、图表、发现、我的、登录提示等未实现入口提供占位反馈 | PRD-CODE-ACCOUNTING-20260617 | draft | none |

## 冲突检查清单

创建或更新 PRD 时按以下顺序检查：

1. 是否已有相同 `PRD ID` 或同名文件。
2. 是否复用已有 `REQ-*`，但改动了需求含义。
3. 是否新增页面或流程，却没有新的 `SCREEN-*` 或 `FLOW-*` 映射。
4. 是否与当前有效 PRD 的范围重叠但未写明 `更新`、`替代` 或 `冲突`。
5. 是否修改验收标准但没有同步端侧影响摘要。
