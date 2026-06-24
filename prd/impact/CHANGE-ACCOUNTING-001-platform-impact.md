# CHANGE-ACCOUNTING-001 端侧影响摘要

## 来源

- 变更：CHANGE-ACCOUNTING-001「禁止未来日期」。
- PRD：[`prd/code-accounting-prd-20260623.md`](../code-accounting-prd-20260623.md)（日期/年月选择弹窗、数据与规则 → 日期规则、验收标准、变更记录）。
- 来源决策：用户产品决策（2026-06-24），经 `android-feature-workflow` 问题分类门归类为变更（intake INTAKE-002）。

## 涉及需求

- REQ-ACCOUNTING-006：选择器不可选择未来日期/未来月。

## 涉及设计

- SCREEN-ACCOUNTING-V2-002（首页-年月选择弹窗）、SCREEN-ACCOUNTING-V2-006（记账-日期选择弹窗）。
- FLOW-ACCOUNTING-V2-001/002/003。
- 注意：设计稿 `.pen`（DESIGN-ACCOUNTING-002）两个弹窗滚轮仍画有未来示意值（如 2027 年 / 7 月），属静态示意，不修改设计稿；实现以本摘要与 PRD 日期规则为准。

## Android 影响

- 选择器可选范围上限必须**动态绑定 `LocalDate.now()`**，不得硬编码年份/月份上限：
  - 年月选择器：年上限为当前年，所选当前年时月上限为当前月。
  - 日期选择器：年/月/日三列联动，当前年月时日上限为今天。
  - 超出范围的项不可选并响应式 clamp。
- 数据层防御：保存前对未来日期 clamp 到今天，禁止写入未来日期账单（`RecordViewModel.ConfirmDate` 已实现钳制）。
- 新建账单初始账单日期默认今天。
- 现状：Android 端已实现（见 `overall-tech-plan.md` 假设 A4 与设计还原变更记录），本摘要用于留痕与跨端对齐。

## iOS 影响

- 未来 iOS 端实现时需遵循同一规则（动态绑定当天上限 + 保存前 clamp）。

## Backend 影响

- 首期无后台依赖，无影响。

## 不确定项

- 无阻塞项（product-reviewer 一致性审查未发现阻塞问题）。

## 建议下一步

- 无需新增 Android 任务：规则已落地，技术方案假设 A4 已回写。后续如有 iOS 实现，再以本摘要交接。
