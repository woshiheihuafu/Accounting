# PRD 索引

本文件是 `prd/` 下 PRD 的唯一索引入口，用于追踪多期 PRD 的状态、范围、需求 ID 和替代关系。创建、更新、替代或废弃 PRD 时必须同步更新本索引。

## 索引规则

- 每个 PRD 必须有稳定的 `PRD-*` ID。
- 每个 PRD 必须标注状态：`draft`、`reviewed`、`approved`、`implemented`、`deprecated` 或 `superseded`。
- 同一产品或功能允许存在多个历史 PRD，但同一范围内只能有一个当前有效 PRD。
- 新 PRD 修改核心流程、范围边界或验收标准时，必须在“关系”中写明更新、替代或冲突关系。
- 新增 PRD 前必须检查本索引，确认是否已存在相同范围、相同页面、相同流程或相同 `REQ-*`。

## PRD 列表

| PRD ID | 标题 | 文件 | 创建日期 | 状态 | 范围摘要 | 关联需求 | 关联设计 | 关系 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PRD-CODE-ACCOUNTING-20260619 | CodeAccounting 首期记账体验 | [`prd/code-accounting-prd-20260619.md`](../code-accounting-prd-20260619.md) | 2026-06-19 | draft | 首页明细、底部 Bar、支出/收入类目、金额键盘、年月和日期选择器 | REQ-ACCOUNTING-001 至 REQ-ACCOUNTING-006 | [`prd/code-accounting.pen`](../code-accounting.pen) | 关联：DESIGN-ACCOUNTING-001；更新：none；替代：none；被替代：none；冲突：none | 当前唯一有效 PRD |

## 需求覆盖索引

| REQ ID | 需求摘要 | 当前来源 PRD | 状态 | 备注 |
| --- | --- | --- | --- | --- |
| REQ-ACCOUNTING-001 | 首页展示选中年月、收入合计、支出合计和“我的记账”明细，年月切换后同步刷新数据 | PRD-CODE-ACCOUNTING-20260619 | draft | none |
| REQ-ACCOUNTING-002 | 底部 Bar 保留明细、图表、记账、发现、我的五入口结构，仅明细和记账可用，其余为未实现占位 | PRD-CODE-ACCOUNTING-20260619 | draft | none |
| REQ-ACCOUNTING-003 | 支出记账仅支持餐饮、购物、娱乐、日用四个类目，并在选择后进入金额录入 | PRD-CODE-ACCOUNTING-20260619 | draft | none |
| REQ-ACCOUNTING-004 | 收入记账仅支持工资、理财、礼金、其他四个类目，并在选择后进入金额录入 | PRD-CODE-ACCOUNTING-20260619 | draft | none |
| REQ-ACCOUNTING-005 | 金额键盘支持金额、备注、日期、删除、加减号和完成保存 | PRD-CODE-ACCOUNTING-20260619 | draft | none |
| REQ-ACCOUNTING-006 | 首页年月选择器和记账日期选择器使用底部弹窗滚轮样式，支持上下滑动选择 | PRD-CODE-ACCOUNTING-20260619 | draft | none |

## 冲突检查清单

1. 已检查当前工作区：本次创建新的当前有效 PRD。
2. 每个 `REQ-*` 只有一个当前来源 PRD。
3. 每个面向用户的核心需求均有对应 `SCREEN-*` 或 `FLOW-*` 映射。
4. 本期明确剔除登录、账号、云同步、营销入口和首页快捷入口。
5. 端侧影响仅在 PRD 中摘要说明，不直接更新端级技术方案。
