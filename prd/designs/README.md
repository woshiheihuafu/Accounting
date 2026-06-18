# 设计索引

本文件用于追踪 `prd/` 下设计资产、页面、流程和需求覆盖关系。

## 设计资产

| 设计 ID | 资产 | 关联 REQ | 页面 | 流程 | 状态 | 备注 |
| --- | --- | --- | --- | --- | --- | --- |
| DESIGN-ACCOUNTING-001 | [`prd/code-accounting.pen`](../code-accounting.pen) | REQ-ACCOUNTING-001 至 REQ-ACCOUNTING-006 | SCREEN-ACCOUNTING-001 至 SCREEN-ACCOUNTING-006 | FLOW-ACCOUNTING-001 至 FLOW-ACCOUNTING-003 | draft | CodeAccounting 首期移动端设计稿 |

## 页面索引

| SCREEN ID | 页面 | 关联 REQ | 设计资产 | 状态 | 备注 |
| --- | --- | --- | --- | --- | --- |
| SCREEN-ACCOUNTING-001 | 首页-明细 | REQ-ACCOUNTING-001, REQ-ACCOUNTING-002 | DESIGN-ACCOUNTING-001 | draft | 无首页快捷入口，无登录提示 |
| SCREEN-ACCOUNTING-002 | 首页-年月选择弹窗 | REQ-ACCOUNTING-001, REQ-ACCOUNTING-006 | DESIGN-ACCOUNTING-001 | draft | 底部弹窗滚轮式年/月选择 |
| SCREEN-ACCOUNTING-003 | 记账-支出类目 | REQ-ACCOUNTING-003 | DESIGN-ACCOUNTING-001 | draft | 仅餐饮、购物、娱乐、日用 |
| SCREEN-ACCOUNTING-004 | 记账-收入类目 | REQ-ACCOUNTING-004 | DESIGN-ACCOUNTING-001 | draft | 仅工资、理财、礼金、其他 |
| SCREEN-ACCOUNTING-005 | 记账-金额键盘 | REQ-ACCOUNTING-003, REQ-ACCOUNTING-004, REQ-ACCOUNTING-005 | DESIGN-ACCOUNTING-001 | draft | 类目选中态、金额、备注、日期、完成 |
| SCREEN-ACCOUNTING-006 | 记账-日期选择弹窗 | REQ-ACCOUNTING-005, REQ-ACCOUNTING-006 | DESIGN-ACCOUNTING-001 | draft | 底部弹窗滚轮式年/月/日选择 |

## 流程索引

| FLOW ID | 流程 | 关联页面 | 关联 REQ | 状态 | 备注 |
| --- | --- | --- | --- | --- | --- |
| FLOW-ACCOUNTING-001 | 首页查看与切换年月 | SCREEN-ACCOUNTING-001, SCREEN-ACCOUNTING-002 | REQ-ACCOUNTING-001, REQ-ACCOUNTING-006 | draft | 切换后刷新汇总和明细 |
| FLOW-ACCOUNTING-002 | 支出记账 | SCREEN-ACCOUNTING-003, SCREEN-ACCOUNTING-005, SCREEN-ACCOUNTING-006 | REQ-ACCOUNTING-003, REQ-ACCOUNTING-005, REQ-ACCOUNTING-006 | draft | 支出保存为负金额 |
| FLOW-ACCOUNTING-003 | 收入记账 | SCREEN-ACCOUNTING-004, SCREEN-ACCOUNTING-005, SCREEN-ACCOUNTING-006 | REQ-ACCOUNTING-004, REQ-ACCOUNTING-005, REQ-ACCOUNTING-006 | draft | 收入保存为正金额 |

## 参考截图

| 截图 | 用途 | 处理策略 |
| --- | --- | --- |
| `prd/image/1.首页-明细.png` | 首页布局、底部 Bar、品牌头部、明细列表 | 参考结构；剔除快捷入口和登录提示 |
| `prd/image/2.点击记账-支出.png` | 支出类目页 | 参考顶部 Tab、4 列网格和线性图标；只保留本期类目 |
| `prd/image/3.点击记账-收入.png` | 收入类目页 | 参考顶部 Tab、图标风格；只保留本期类目 |
| `prd/image/4.首页-明细-点击左上角日期选择.png` | 首页年月选择器 | 参考遮罩、底部弹窗、取消/标题/确定和滚轮高亮 |
| `prd/image/5.点击记账-支出-选择餐饮.png` | 类目选中与金额键盘 | 参考黄色选中态、金额区、备注和键盘布局 |
| `prd/image/6.点击记账-支出-选择餐饮-点击键盘日期.png` | 日期选择器 | 参考遮罩、底部弹窗和年/月/日滚轮 |
