# CodeAccounting 问题分类门台账（intake）

本文件是 `android-feature-workflow` 场景 G（问题分类门）的**轻量 triage 索引台账**。

## 职责与边界

- **只存索引，不存详情。** 每条只登记：原始诉求、分类、路由去向、指向最终事实源的指针、状态。
- 详情写在各自事实源，本台账只放指针，避免形成第二事实源：
  - **Bug**（实现 ≠ 既定 spec）→ 详情在对应模块技术方案的 `### BUG-NNN`。
  - **变更**（spec 本身要改 / PRD 欠定义需补规则）→ 详情在 Product 域 PRD、`.pen`、需求索引、设计索引（`REQ-*` / `DESIGN-*` / `SCREEN-*` / `FLOW-*`）。
  - **遗留**（已知但本轮不修）→ 详情在对应模块技术方案的 `### LEFT-NNN`。
- 分类与路由规则见 `.cursor/skills/android-feature-workflow/SKILL.md` 场景 G；派发规则见 `.cursor/rules/agent-routing.mdc`。

## 字段说明

- **ID**：`INTAKE-NNN`，稳定不复用。
- **分类**：`bug` | `变更` | `遗留`。
- **路由去向**：本条最终由哪个域 / 哪个 Agent 处理。
- **指针**：指向事实源的 ID 或文件路径（`BUG-NNN` / `LEFT-NNN` / `REQ-*` / `DESIGN-*` / `SCREEN-*` / 模块方案 / PRD）。
- **状态**：`open` | `in_progress` | `routed` | `resolved` | `won't_fix`。

## 台账

| ID | 日期 | 原始诉求 | 分类 | 路由去向 | 指针（事实源） | 状态 |
| --- | --- | --- | --- | --- | --- | --- |
| INTAKE-001 | 2026-06-24 | 字体太花哨（用了 Serif），希望系统默认字体 | bug | android-implementer（已改）；PRD 本就要求系统默认，无需改 spec | PRD 字体策略（正文=系统默认）；实现 `ui/theme/Type.kt` 改 `FontFamily.Default` | resolved |
| INTAKE-002 | 2026-06-24 | 不能选择未来日期（如今天 6.24 不能记 6.25） | 变更 | Product 域补规则 → 回写 Android 技术方案 → 实现（已实现钳制） | CHANGE-ACCOUNTING-001；REQ-ACCOUNTING-006；SCREEN-ACCOUNTING-V2-002/006；技术方案假设 A4；实现 `DatePickerDialog`/`YearMonthPickerDialog`/`RecordViewModel.ConfirmDate` | resolved |
| INTAKE-003 | 2026-06-24 | 首页明细账单左侧图标未对应实际类目 | bug | android-implementer（已改） | PRD 首页明细（类目线条图标）；实现 `Categories.iconResFor` + `HomeContent.BillItemRow` | resolved |
| INTAKE-004 | 2026-06-24 | 未使用设计稿中的图标，需从设计稿导出为 Android Resource | bug | android-implementer（已改） | DESIGN-ACCOUNTING-002（Lucide 图标）；实现 `res/drawable/ic_*`（16 个 Vector Drawable） | resolved |
| INTAKE-005 | 2026-06-24 | 等宽金额字体实现用系统 Monospace，而非技术方案 R2 所述"嵌入 DIN/JetBrains Mono" | 遗留 | 记录为遗留，按需偿还 | 技术方案风险 R2；M1 模块技术方案 `LEFT-001` | routed |
