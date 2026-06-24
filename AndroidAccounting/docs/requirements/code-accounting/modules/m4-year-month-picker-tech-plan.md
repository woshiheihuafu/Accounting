# M4 年月选择器模块技术方案

## 模块目标

为 CodeAccounting Android 端交付**年月选择器**功能（SCREEN-V2-002），覆盖 REQ-ACCOUNTING-006（底部弹窗滚轮式年月选择器）。

核心交付物：

1. 确认并正式交付 `YearMonthPickerDialog`（在 M3 阶段自包含实现于 `ui/home/`，本模块予以确认和关闭）。
2. 修复 `WheelPicker` 选中态字号差异（M3 遗留 LEFT-M3-2）：18sp Bold → 20sp Bold，与 PRD 对齐。
3. 补充 `YearMonthPickerDialog` 的 `testTag`，便于后续 instrumentation 测试定位。
4. 更新 M3 遗留项状态，关闭 LEFT-M3-1 和 LEFT-M3-2。

## 范围

- `ui/component/WheelPicker.kt`：修复选中态 `fontSize` 18sp → 20sp（影响全部滚轮选择器）。
- `ui/home/YearMonthPickerDialog.kt`：补充 `testTag`（`year_month_picker_year_wheel` / `year_month_picker_month_wheel`）。
- `docs/requirements/code-accounting/modules/m3-home-screen-tech-plan.md`：将 LEFT-M3-1、LEFT-M3-2 标记为已关闭。
- `docs/requirements/code-accounting/overall-tech-plan.md`：更新 M4 进度。

## 非目标

- 不将 `YearMonthPickerDialog` 迁移到 `ui/component/`（组件仅被 `HomeScreen` 使用，无复用需求，不做无价值重构）。
- 不引入新的 Compose 依赖或第三方滚轮库（M1 基座 `WheelPicker` 已满足需求）。
- 不做 instrumentation UI 测试（与 M1/M2/M3 策略一致，整体无 instrumentation 测试基础设施）。
- 不修改 `BottomSheetPicker`（M1 已实现，无需改动）。
- 不实现日期选择器（归属 M6）。
- 不修改 `YearMonthPickerDialog` 的年份范围逻辑（当前 ±10 年，PRD 无明确约束，保持现状）。

## 依赖关系

- 上游依赖：
  - M1 基础设施（`✅ completed`）：`WheelPicker`、`BottomSheetPicker`、`AccountingTheme`、Navigation、Hilt。
  - M3 首页明细（`✅ completed`）：`YearMonthPickerDialog` 在 M3 中首次实现并集成到 `HomeScreen`。
- 下游被依赖：
  - M3（HomeScreen 使用 `YearMonthPickerDialog`）：WheelPicker 字号修复惠及 M3。
  - M6（日期选择器）：WheelPicker 字号修复惠及 M6。
- 无新增第三方依赖。

## UI 方案

### YearMonthPickerDialog（已实现，确认交付）

位于 `ui/home/YearMonthPickerDialog.kt`，基于 M1 `BottomSheetPicker` + 两列 `WheelPicker` 组合：

```
BottomSheetPicker（标题「选择月份」+ 取消 / 确认）
└── Row（水平布局，各占 weight(1f)）
    ├── WheelPicker（年份列：当前年 ±10，显示字符串）
    └── WheelPicker（月份列：1月 ~ 12月）
```

状态管理：
- `yearIndex`、`monthIndex` 使用 `rememberSaveable`，支持屏幕旋转恢复。
- 确认时提取 `years[yearIndex].toInt()` 和 `monthIndex + 1` 回调给调用方。
- 初始值通过 `(initialYear - (currentYear - 10)).coerceIn(0, years.lastIndex)` 安全映射。

### WheelPicker 字号修复（LEFT-M3-2）

将 `WheelPicker.kt` 选中态 `fontSize` 从 18sp 修改为 20sp，与 PRD 「滚轮选中行 20sp Bold」对齐：

```kotlin
// 修改前
fontSize = if (isSelected) 18.sp else 16.sp,
// 修改后
fontSize = if (isSelected) 20.sp else 16.sp,
```

此改动为 M1 基座组件，惠及所有使用 `WheelPicker` 的场景（当前为 `YearMonthPickerDialog`，后续 M6 `DatePickerDialog`）。

### testTag 补充

在 `YearMonthPickerDialog` 的两列 `WheelPicker` 上补充 `testTag`：

```kotlin
WheelPicker(
    ...
    modifier = Modifier.weight(1f).testTag("year_month_picker_year_wheel"),
)
WheelPicker(
    ...
    modifier = Modifier.weight(1f).testTag("year_month_picker_month_wheel"),
)
```

## ViewModel 方案

不涉及。M4 为纯 UI 组件，无需独立 ViewModel。年月状态由 `HomeViewModel` 持有（M3 已实现）。

## Repository 方案

不涉及。

## Network 方案

不涉及。

## 模块架构方案

M4 为薄配置层组件，架构简单：

```text
HomeScreen（M3）
    ↓ 条件渲染
YearMonthPickerDialog（ui/home/）
    ├── BottomSheetPicker（ui/component/，M1）
    └── WheelPicker × 2（ui/component/，M1）
```

包归属决策：`YearMonthPickerDialog` 保留在 `ui/home/`（与 M3 整体方案包结构一致，组件无跨模块复用需求，避免无价值重构）。

## 状态与副作用模型

- `YearMonthPickerDialog` 内部持有临时选择状态（`yearIndex`、`monthIndex`），通过 `rememberSaveable` 保持。
- 确认后通过回调 `onConfirm(year, month)` 向上传递，由 `HomeViewModel` 处理（M3 已实现）。
- 取消时调用 `onDismiss()`，弹窗关闭，状态丢弃。
- 无副作用（无网络、无数据库、无导航）。

## 文件变更计划

| 文件 | 变更类型 | 说明 |
| --- | --- | --- |
| `ui/component/WheelPicker.kt` | modify | 选中态 fontSize 18sp → 20sp（修复 LEFT-M3-2） |
| `ui/home/YearMonthPickerDialog.kt` | modify | 两列 WheelPicker 补充 testTag |
| `m3-home-screen-tech-plan.md` | modify | LEFT-M3-1、LEFT-M3-2 标记为已关闭 |
| `overall-tech-plan.md` | modify | M4 进度更新 |

## 测试计划

### 策略说明

M4 的核心变更（WheelPicker 字号 + testTag）均为视觉/标记调整，无业务逻辑可单独用 JVM 测试覆盖。`YearMonthPickerDialog` 的 index coercion 逻辑（`coerceIn`）过于简单，无需为此抽取可测试函数。

- **不新增** JVM 单元测试（无有价值的纯 JVM 逻辑可测）。
- **不新增** instrumentation UI 测试（无基础设施支持）。
- **复用** M3 的 28 个 JVM 测试作为回归（`HomeViewModelTest` 覆盖年月选择器的 ViewModel 交互）：
  - `openPicker_setsVisibleTrue`
  - `dismissPicker_setsVisibleFalse`
  - `confirmSelection_updatesYearMonth_andRequeries`
  - `confirmSelection_refreshesSummaryAndBills`
- **验证**：`./gradlew :app:testDebugUnitTest` 全部通过，`./gradlew :app:assembleDebug` BUILD SUCCESSFUL。

## 设计输入问题

| # | 来源 | 问题 | 处理策略 |
| --- | --- | --- | --- |
| DI-M4-1 | PRD 与 M1 差异（继承自 LEFT-M3-2） | WheelPicker 选中态 PRD 要求 20sp Bold，M1 实现为 18sp Bold | 本模块修复：18sp → 20sp（已决策） |
| DI-M4-2 | PRD SCREEN-V2-002 | .pen 设计稿未验证年月选择器具体样式细节 | 以 PRD 文字描述（底部弹窗 + 双列滚轮 + 年/月）为实现依据，现有实现与描述一致 |

## 审批状态

`skipped_by_user`

用户已明确要求直接进入开发，跳过审批等待。

## 进度记录

| 日期 | 状态 | 说明 |
| --- | --- | --- |
| 2026-06-24 | technical_plan_created | M4 年月选择器模块技术方案生成；用户跳过审批，直接进入实现 |
| 2026-06-24 | in_progress | 实现 WheelPicker 字号修复（18sp→20sp）和 testTag 补充 |
| 2026-06-24 | completed | 实现完成，JVM 测试全部通过，BUILD SUCCESSFUL |

## Bug 与遗留问题

暂无。

## 开发记录

| 日期 | 内容 |
| --- | --- |
| 2026-06-24 | 方案生成（场景 B）：确认 YearMonthPickerDialog 现有实现，规划 WheelPicker 字号修复和 testTag 补充 |
| 2026-06-24 | 实现完成（场景 C）：WheelPicker 选中态 fontSize 18sp→20sp，YearMonthPickerDialog 两列 WheelPicker 补充 testTag，28 个 JVM 测试通过，BUILD SUCCESSFUL |
