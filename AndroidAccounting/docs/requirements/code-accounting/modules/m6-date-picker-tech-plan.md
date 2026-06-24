# M6 日期选择器模块技术方案

## 模块目标

为 CodeAccounting Android 端交付**日期选择器**功能（SCREEN-V2-006），覆盖 REQ-ACCOUNTING-006（底部弹窗滚轮式日期选择器）。

核心交付物：

1. 实现 `DatePickerDialog`：底部弹窗三列滚轮组件（年 / 月 / 日），供记账页（M5）的日期选择使用。
2. 日列范围随年月联动：根据所选年月动态计算当月天数，防止无效日期（如 2 月 30 日）。
3. 补充 `testTag`，便于后续 instrumentation 测试定位。

## 范围

- `ui/record/DatePickerDialog.kt`：新建三列滚轮日期选择弹窗。
- `docs/requirements/code-accounting/overall-tech-plan.md`：更新 M6 进度。

## 非目标

- 不将 `DatePickerDialog` 放在 `ui/component/`（组件仅被 `RecordScreen` 使用，无跨模块复用需求）。
- 不实现记账页其余功能（归属 M5）。
- 不做 instrumentation UI 测试（与 M1/M2/M3/M4 策略一致）。
- 不做日期范围上下限约束（整体方案 A4）。
- 不修改 `WheelPicker` 或 `BottomSheetPicker`（M1 已实现且 M4 已修复字号）。

## 依赖关系

- 上游依赖：
  - M1 基础设施（`✅ completed`）：`WheelPicker`、`BottomSheetPicker`、`AccountingTheme`。
- 下游被依赖：
  - M5 记账页：`RecordScreen` 中条件渲染 `DatePickerDialog`。
- 无新增第三方依赖。

## UI 方案

### DatePickerDialog 结构

基于 M1 `BottomSheetPicker`（标题「选择日期」）+ 三列 M1 `WheelPicker`（年 / 月 / 日）：

```
BottomSheetPicker（标题「选择日期」+ 取消 / 确认）
└── Row（水平布局，各占 weight(1f)）
    ├── WheelPicker（年份列：当前年 ±10，显示字符串）
    ├── WheelPicker（月份列：1月 ~ 12月）
    └── WheelPicker（日期列：1日 ~ N日，N 随年月联动）
```

### 日列联动逻辑

日期列的范围依赖选中的年和月，需通过 `YearMonth.of(year, month).lengthOfMonth()` 动态计算：

```kotlin
val daysInMonth = remember(yearIndex, monthIndex) {
    val selectedYear = years[yearIndex].toInt()
    val selectedMonth = monthIndex + 1
    YearMonth.of(selectedYear, selectedMonth).lengthOfMonth()
}
val days = remember(daysInMonth) { (1..daysInMonth).map { "${it}日" } }
```

当年月切换导致当前 `dayIndex` 超出新月份范围时，`dayIndex` 截断到新月最后一天：

```kotlin
LaunchedEffect(daysInMonth) {
    if (dayIndex >= daysInMonth) {
        dayIndex = daysInMonth - 1
    }
}
```

### 初始值映射

- 传入 `initialDate: LocalDate`，分别拆解为 `initialYear`、`initialMonth`、`initialDay`。
- 年份 index = `(initialYear - (currentYear - 10)).coerceIn(0, years.lastIndex)`
- 月份 index = `(initialMonth - 1).coerceIn(0, 11)`
- 日期 index = `(initialDay - 1).coerceIn(0, daysInMonth - 1)`

### testTag

| tag | 位置 |
|-----|------|
| `date_picker_year_wheel` | 年份 WheelPicker |
| `date_picker_month_wheel` | 月份 WheelPicker |
| `date_picker_day_wheel` | 日期 WheelPicker |

### 组件签名

```kotlin
@Composable
fun DatePickerDialog(
    initialDate: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
)
```

确认时构建 `LocalDate.of(selectedYear, selectedMonth, selectedDay)` 后调用 `onConfirm`。

## ViewModel 方案

不涉及。M6 为纯 UI 组件，状态完全由 M5 `RecordViewModel` 持有。

## Repository 方案

不涉及。

## Network 方案

不涉及。

## 模块架构方案

```text
RecordScreen（M5）
    ↓ 条件渲染（isDatePickerVisible）
DatePickerDialog（ui/record/）
    ├── BottomSheetPicker（ui/component/，M1）
    └── WheelPicker × 3（ui/component/，M1）
         └── 日列联动：YearMonth.lengthOfMonth()（java.time）
```

包归属：`DatePickerDialog` 在 `ui/record/`，与 M4 中 `YearMonthPickerDialog` 在 `ui/home/` 的策略一致。

## 状态与副作用模型

- 临时状态：`yearIndex`、`monthIndex`、`dayIndex` 使用 `rememberSaveable`，支持屏幕旋转恢复。
- 联动副作用：`LaunchedEffect(daysInMonth)` 检查并截断 `dayIndex`。
- 确认后通过回调 `onConfirm(LocalDate)` 向上传递，由 `RecordViewModel` 持有（M5 实现）。
- 取消时 `onDismiss()`，弹窗关闭，状态丢弃。

## 文件变更计划

| 文件 | 变更类型 | 说明 |
| --- | --- | --- |
| `ui/record/DatePickerDialog.kt` | 新建 | 三列滚轮日期选择弹窗 |
| `res/values/strings.xml` | modify | 新增 `record_date_picker_title`（`选择日期`） |
| `overall-tech-plan.md` | modify | M6 进度更新 |

## 测试计划

### 策略说明

M6 的核心变更（DatePickerDialog + 日列联动逻辑）属于纯 Compose UI 组件，无可独立 JVM 测试的业务逻辑：

- 日列联动通过 `YearMonth.lengthOfMonth()` 实现，属 Java 标准库，无需单独测试。
- `dayIndex` 截断（`coerceIn`）过于简单，不为此抽取可测函数。
- **不新增** JVM 单元测试。
- **不新增** instrumentation UI 测试。
- **验证**：`./gradlew :app:testDebugUnitTest` 全部通过，`./gradlew :app:assembleDebug` BUILD SUCCESSFUL。

## 设计输入问题

| # | 来源 | 问题 | 处理策略 |
| --- | --- | --- | --- |
| DI-M6-1 | PRD SCREEN-V2-006 | .pen 设计稿未验证日期选择器具体样式细节 | 以 PRD 文字描述（底部弹窗 + 三列滚轮 + 年/月/日）为实现依据，与 M4 保持视觉一致 |

## 审批状态

`skipped_by_user`

用户已明确要求直接进入开发，跳过审批等待。

## 进度记录

| 日期 | 状态 | 说明 |
| --- | --- | --- |
| 2026-06-24 | technical_plan_created | M6 日期选择器模块技术方案生成；用户跳过审批，直接进入实现 |
| 2026-06-24 | in_progress | 实现 DatePickerDialog 三列滚轮 + 日列联动 |
| 2026-06-24 | completed | 实现完成，JVM 测试全部通过，BUILD SUCCESSFUL |

## Bug 与遗留问题

暂无。

## 开发记录

| 日期 | 内容 |
| --- | --- |
| 2026-06-24 | 方案生成（场景 B）：规划 DatePickerDialog 三列结构和日列联动逻辑 |
| 2026-06-24 | 实现完成（场景 C）：新建 DatePickerDialog.kt（三列 WheelPicker + 日列联动 + testTag），strings.xml 追加 record_date_picker_title，JVM 测试全部通过，BUILD SUCCESSFUL |
