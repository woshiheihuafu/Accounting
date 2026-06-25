# M3 首页明细模块技术方案

## 模块目标

为 CodeAccounting Android 端交付**首页明细**完整功能（SCREEN-V2-001 / FLOW-V2-001），覆盖 REQ-ACCOUNTING-001、REQ-ACCOUNTING-002。基于 M1 基础设施（`AccountingTheme`、`AccountingBottomBar`、`WheelPicker`、`BottomSheetPicker`、Navigation、Hilt）和 M2 数据层（`BillRepository`、`Bill`、`MonthlySummary`、`BillType`），实现一套「可编译、可注入、可测试」的首页：

1. 头部品牌区（`Code记账`）+ 月份选择行（大号月份 + `▾` + 年份）。
2. 收支摘要卡片（支出 / 收入双列）。
3. 按日期分组的明细列表（日期分组头 + 当日收支净额 + 账单条目），含空态。
4. 底部 Bar（复用 M1 `AccountingBottomBar`）：明细选中态、记账入口跳转、图表/发现/我的占位 Toast「即将推出」。
5. 年月选择弹窗（`YearMonthPickerDialog`，基于 M1 `WheelPicker` + `BottomSheetPicker`），切换后刷新收支与明细。
6. `HomeViewModel`（MVI）：`combine` 合并 `observeBillsByMonth` + `observeMonthlySummary`，按日期分组，转 `StateFlow<HomeUiState>`。

## 范围

- Compose 页面（`ui/home/`）：`HomeRoute.kt`、`HomeScreen.kt`、`HomeContent.kt`、`YearMonthPickerDialog.kt`。
- MVI 契约（`ui/home/`）：`HomeUiState.kt`、`HomeUiIntent.kt`、`HomeUiEffect.kt`、`HomeViewModel.kt`。
- UI 分组载体：`BillGroup`（`date: LocalDate, bills: List<Bill>`，含当日收支净额）。
- 展示格式化工具：金额（分 → `¥xx.xx`）、日期分组头（`6月16日 周二`）、月份 / 年份标签，落在 `util/`。
- 导航接线：填充 `navigation/AppNavHost.kt` 的 `composable<Route.Home>`。
- 文案资源：`res/values/strings.xml` 新增首页相关字符串。
- 测试：`HomeViewModel` 单元测试（JVM，fake `BillRepository`）。

## 非目标

- 不实现记账页（SCREEN-V2-003 ~ V2-005，归属 M5）；记账入口仅做导航跳转，目标页为 M5 占位。
- 不实现年月选择器组件的**独立抽取与复用**（M4 后续做）；本模块在 `ui/home/YearMonthPickerDialog.kt` 内直接用 M1 `WheelPicker` + `BottomSheetPicker` 组合实现。
- 不实现日期选择器（SCREEN-V2-006，归属 M6）。
- 不实现图表 / 发现 / 我的真实功能（仅 Toast 占位，整体方案非目标）。
- 不修改 M2 数据层接口与 M1 组件实现（如需调整 `AccountingBottomBar` 记账按钮视觉，见「设计输入问题」DI-M3-3，按遗留项处理）。
- 不实现账单删除交互（M2 已提供 `deleteBill`，但 PRD 首页未定义删除入口，本期不接）。
- 不实现金额等宽字体文件替换（沿用 M1 fallback，整体方案 R2）。

## 依赖关系

- 上游依赖：
  - M1 基础设施（`✅ completed`）：`AccountingTheme`、`LocalAccountingColors`（`expenseRed` / `incomeGreen`）、`LocalAccountingTypography`（`amountDisplay`）、`AccountingShapes`、`WheelPicker`、`BottomSheetPicker`、`AccountingBottomBar`、`Route`、`AppNavHost`、Hilt、`hilt-navigation-compose`、`lifecycle-runtime-compose`。
  - M2 数据层（`✅ completed`）：`BillRepository`（`observeBillsByMonth` / `observeMonthlySummary`）、`Bill`、`MonthlySummary`、`BillType`、`util/DateUtil`。
- 下游被依赖：无（M3 是叶子功能模块）。
- 与 M4 的关系：整体方案标注「M3 依赖 M4」，但同时指出「M3 的 UI 骨架（不含年月弹窗交互）可在 M4 完成前先行搭建」。本方案在 M3 内自包含实现 `YearMonthPickerDialog`，**不阻塞等待 M4**；M4 完成后可将其提取为通用组件并替换本模块引用（记为遗留项 LEFT-M3-1）。
- 与 M5 的关系：记账入口跳转到 `Route.Record`，目标页当前为 M5 占位（`AppNavHost` 中 `TODO(M5)`），不影响 M3 导航逻辑。
- 模块内依赖方向：`HomeRoute` → `HomeScreen` → `HomeContent`（State up, events down）；`HomeViewModel` → `BillRepository`（构造注入接口）；`HomeContent` / `YearMonthPickerDialog` → 展示格式化工具 + M1 组件。严禁 Composable 直接依赖 `BillRepository` / Room。
- 第三方依赖：复用 M1/M2 已接入版本，无需新增声明。测试依赖 `kotlinx-coroutines-test`、`turbine`（M2 已引入，复用）。

## UI 方案

### 整体页面布局（SCREEN-V2-001）

纸白底（`MaterialTheme.colorScheme.background` = `PaperWhite`），从上到下：头部品牌区 → 月份选择行 → 收支摘要卡片 → 明细区域（标题 + 分组列表 / 空态）；底部 Bar 由 `Scaffold` 承载。整页用 `LazyColumn` 渲染（明细可能较长），头部 / 月份行 / 摘要卡片 / 明细标题作为 `item`，分组与条目作为 `items`。

### 各 Composable 职责

| Composable | 层级 | 职责 |
| --- | --- | --- |
| `HomeRoute` | Route | `hiltViewModel()` 获取 `HomeViewModel`；`collectAsStateWithLifecycle()` 收集 `uiState`；`LaunchedEffect` 收集 `effect`（`ShowToast` → `Toast.LENGTH_SHORT`；`NavigateToRecord` → `onNavigateToRecord()`）；把 state + `onIntent` 下发给 `HomeScreen` |
| `HomeScreen` | Screen | 持有 `Scaffold`（`bottomBar = AccountingBottomBar`）；把 state 拆给 `HomeContent`；转发事件为 `HomeUiIntent`；按 `isYearMonthPickerVisible` 条件渲染 `YearMonthPickerDialog` |
| `HomeContent` | Content | 无状态；`LazyColumn` 渲染头部、月份行、摘要卡片、明细标题、分组列表 / 空态；仅接收 state 与回调 |
| `HomeHeader` | Content 子项 | 渲染 `Code记账`（16sp Medium，`InkBlack`） |
| `MonthSelectorRow` | Content 子项 | 大号月份（36sp Bold）+ `▾`（中灰）+ 年份（14sp Regular 中灰）；整区 `clickable` → `onMonthClick` |
| `SummaryCard` | Content 子项 | 纸白底 + 细描边圆角卡片（`AccountingShapes.medium` = 12dp）；水平二等分 + 中间竖向分隔线；左支出（`expenseRed`）/ 右收入（`incomeGreen`） |
| `DetailSectionTitle` | Content 子项 | `明细`（16sp Medium，`InkBlack`，左对齐） |
| `DateGroupHeader` | Content 子项 | 日期（`6月16日 周二`，14sp 中灰）+ 右侧当日收支净额（14sp 中灰，净额为 0 时不显示）|
| `BillItemRow` | Content 子项 | 类目图标（24dp 线条）+ 12dp 间隔 + 类目名 / 备注（16sp Regular `InkBlack`）+ 右侧金额（16sp Medium，支出 `-¥` 红 / 收入 `+¥` 绿） |
| `HomeEmptyState` | Content 子项 | 居中 `暂无记账`（14sp 中灰，无插画） |
| `YearMonthPickerDialog` | 弹窗 | `BottomSheetPicker` 容器 + 双列 `WheelPicker`（年 / 月） |

### `HomeContent` 组件树

```text
HomeContent (LazyColumn)
├── item: HomeHeader              // "Code记账"
├── item: MonthSelectorRow        // 月份(36sp) + ▾ + 年份, clickable → onMonthClick
├── item: SummaryCard             // 支出 | 收入
├── item: DetailSectionTitle      // "明细"
└── 明细体:
    ├── 若 groups.isEmpty() → item: HomeEmptyState   // "暂无记账"
    └── 否则 groups.forEach { group ->
            item(key = "header_${group.date}"): DateGroupHeader
            items(group.bills, key = { it.id }): BillItemRow
        }
```

### `YearMonthPickerDialog` 实现方式

基于 M1 `BottomSheetPicker`（已含遮罩、顶部 16dp 圆角、取消 / 标题 / 确定操作栏、底部细线）+ 两列 M1 `WheelPicker`：

```kotlin
@Composable
fun YearMonthPickerDialog(
    initialYear: Int,
    initialMonth: Int,           // 1..12
    onConfirm: (year: Int, month: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentYear = remember { Year.now().value }
    val years = remember { (currentYear - 10..currentYear + 10).map { it.toString() } }
    val months = remember { (1..12).map { "${it}月" } }

    var yearIndex by rememberSaveable {
        mutableIntStateOf((initialYear - (currentYear - 10)).coerceIn(0, years.lastIndex))
    }
    var monthIndex by rememberSaveable { mutableIntStateOf((initialMonth - 1).coerceIn(0, 11)) }

    BottomSheetPicker(
        title = stringResource(R.string.home_year_month_picker_title), // "选择月份"
        onDismiss = onDismiss,
        onConfirm = { onConfirm(currentYear - 10 + yearIndex, monthIndex + 1) },
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            WheelPicker(
                items = years, selectedIndex = yearIndex,
                onIndexChange = { yearIndex = it }, modifier = Modifier.weight(1f),
            )
            WheelPicker(
                items = months, selectedIndex = monthIndex,
                onIndexChange = { monthIndex = it }, modifier = Modifier.weight(1f),
            )
        }
    }
}
```

- **年范围**：当前年 −10 ~ 当前年 +10（共 21 项）。
- **月范围**：固定 `1月` ~ `12月`。
- **默认值**：`initialYear` / `initialMonth` 来自当前首页选中的 `YearMonth`；选中行的视觉由 M1 `WheelPicker` 内置渲染（选中 Bold、非选中渐隐）。
- 取消 / 确定文案与样式复用 M1 `BottomSheetPicker`（取消中灰、确定 `primary` = `SageGreen`、标题 `InkBlack`）。
- 确定时把滚轮索引换算回 `year` / `month` 通过 `onConfirm` 上抛，由 ViewModel 处理。

> 说明：M1 `WheelPicker` 选中态当前用 `MaterialTheme.colorScheme.primary` + 18sp，PRD 要求选中 20sp Bold。差异较小，记为 LEFT-M3-2（M4 抽取组件时统一调优），不阻塞 M3。

### 金额展示规则

- 统一格式化：分 → `¥xx.xx`（如 `2300` → `¥23.00`），由纯函数 `formatYuan(cents: Long)` 完成（`util/MoneyFormatter.kt`），`Composable` 仅渲染字符串。
- 摘要卡片：支出 / 收入均展示 `¥xx.xx`（无符号），颜色分别为 `LocalAccountingColors.current.expenseRed` / `incomeGreen`。
- 明细条目金额：按 `Bill.type` 决定前缀与颜色——`EXPENSE` → `-¥xx.xx`（`expenseRed`）；`INCOME` → `+¥xx.xx`（`incomeGreen`）。前缀由 `formatSignedYuan(type, cents)` 拼接。
- 当日收支净额（分组头右侧）：当日收入合计 − 当日支出绝对值合计，由 `BillGroup.dayNetAmount` 提供；净额 > 0 展示 `+¥xx.xx`（中灰），净额 < 0 展示 `-¥xx.xx`（中灰），净额 = 0 不显示。颜色固定中灰（不区分红绿）。
- 颜色全部取自 Design System token（`LocalAccountingColors`），不在业务代码硬编码 `Color(...)`。

> PRD 条目示例为 `-¥23` / `+¥50`（省略小数），摘要为 `¥23.00`。本方案统一用两位小数 `¥xx.xx`，差异记为 DI-M3-2 待产品确认；若确认条目省略整数小数，仅需调整 `formatSignedYuan`。

### 日期分组头格式

- 格式 `6月16日 周二`：`{month}月{day}日 周{weekday}`，由 `DateUtil.formatGroupHeader(date: LocalDate)` 生成（含中文星期 `周一`~`周日`）。
- 月份行：月份 `formatMonthLabel(yearMonth)` → `6月`；年份 `yearMonth.year.toString()` → `2026`，分两个 `Text` 渲染。

### 空态展示

`groups` 为空（当月无账单）时渲染 `HomeEmptyState`：居中 `暂无记账`（`bodyMedium` 14sp，中灰 `onSurfaceVariant`），无插画。摘要卡片照常展示 `¥0.00` / `¥0.00`。空态容器加 `testTag("home_empty")`。

## ViewModel 方案

### `HomeUiState`

```kotlin
data class HomeUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val summary: MonthlySummary = MonthlySummary(),   // 收支汇总（分），M2 Domain
    val groups: List<BillGroup> = emptyList(),         // 按日期分组的明细
    val isLoading: Boolean = true,
    val error: String? = null,
    val isYearMonthPickerVisible: Boolean = false,
)
```

- `summary` 直接复用 M2 `MonthlySummary`（`income` / `expense` / 派生 `balance`，单位分），格式化在 UI 层完成。
- `groups` 为空 + `isLoading == false` → UI 展示空态。
- `isYearMonthPickerVisible` 控制弹窗显隐，属于持续 UI 状态（非一次性事件，放在 State 合规）。
- `error` 为查询失败时的可展示信息（首期可用统一文案）。

### `HomeUiIntent`

```kotlin
sealed interface HomeUiIntent {
    data object OpenYearMonthPicker : HomeUiIntent
    data object DismissYearMonthPicker : HomeUiIntent
    data class ConfirmYearMonthSelection(val year: Int, val month: Int) : HomeUiIntent
    data object NavigateToRecord : HomeUiIntent
    data object OnBottomBarComingSoon : HomeUiIntent
}
```

### `HomeUiEffect`

```kotlin
sealed interface HomeUiEffect {
    data object NavigateToRecord : HomeUiEffect
    data class ShowToast(val messageRes: Int) : HomeUiEffect   // 「即将推出」等
}
```

> Effect 仅承载一次性事件（导航、Toast），不放入 `UiState`（遵循 AGENTS.md）。`ShowToast` 用 `messageRes`（String 资源 id）而非裸字符串，保证文案集中管理与国际化预留。

### ViewModel 核心逻辑

```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BillRepository,
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val pickerVisible = MutableStateFlow(false)

    private val _effect = MutableSharedFlow<HomeUiEffect>()
    val effect: SharedFlow<HomeUiEffect> = _effect.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val dataState: Flow<DataState> = selectedMonth
        .flatMapLatest { ym ->
            combine(
                repository.observeBillsByMonth(ym),
                repository.observeMonthlySummary(ym),
            ) { bills, summary ->
                DataState(groups = bills.toBillGroups(), summary = summary)
            }
                .map { Result.success(it) }
                .onStart { emit(LOADING) }
                .catch { emit(Result.failure(it)) }
        }

    val uiState: StateFlow<HomeUiState> = combine(
        selectedMonth, pickerVisible, dataState,
    ) { ym, picker, data ->
        // 依据 data 是 loading / success / failure 组装 isLoading / groups / summary / error
        HomeUiState(
            yearMonth = ym,
            isYearMonthPickerVisible = picker,
            isLoading = data.isLoading,
            groups = data.groups,
            summary = data.summary,
            error = data.error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onIntent(intent: HomeUiIntent) {
        when (intent) {
            HomeUiIntent.OpenYearMonthPicker -> pickerVisible.value = true
            HomeUiIntent.DismissYearMonthPicker -> pickerVisible.value = false
            is HomeUiIntent.ConfirmYearMonthSelection -> {
                selectedMonth.value = YearMonth.of(intent.year, intent.month)
                pickerVisible.value = false
            }
            HomeUiIntent.NavigateToRecord -> emitEffect(HomeUiEffect.NavigateToRecord)
            HomeUiIntent.OnBottomBarComingSoon ->
                emitEffect(HomeUiEffect.ShowToast(R.string.coming_soon))
        }
    }

    private fun emitEffect(e: HomeUiEffect) = viewModelScope.launch { _effect.emit(e) }
}
```

- **数据流**：`selectedMonth.flatMapLatest { combine(bills, summary) }`——切月时自动取消旧月订阅、订阅新月，天然满足「切换年月后刷新」。
- **loading / error**：`onStart` 发 loading、`catch` 转 failure，组装进 `HomeUiState`，不吞异常。
- **分组**：`bills.toBillGroups()`（扩展函数，见模块架构方案）在 ViewModel 侧完成；输入已由 M2 按 `date` 倒序 / 同日 `createdAt` 倒序排好，分组保持顺序即可。
- **StateFlow**：`stateIn` + `WhileSubscribed(5_000)`，避免后台无谓查询、旋转后快速恢复。

## Repository 方案

不新增 / 不修改 Repository。M3 直接消费 M2 已交付的 `BillRepository`：

```kotlin
fun observeBillsByMonth(yearMonth: YearMonth): Flow<List<Bill>>     // 已排序
fun observeMonthlySummary(yearMonth: YearMonth): Flow<MonthlySummary>
```

约定复用（M2 契约）：

- `observeBillsByMonth` 返回的 `List<Bill>` 已按 `date` 倒序、同日内 `createdAt` 倒序，UI / ViewModel 不再排序，仅分组。
- `observeMonthlySummary` 返回的 `income` / `expense` 为正值（分），UI 负责格式化与符号。
- 写入（`insertBill`）/ 删除（`deleteBill`）本期首页不触发。

## Network 方案

无。首期无网络层（整体方案架构原则第 8 条），M3 数据全部来自 M2 本地 Room。

## 模块架构方案

遵循 `AndroidAccounting/AGENTS.md` 分层与 Compose `Route → Screen → Content`。落地包结构（`ui/home/` 为本模块主体）：

```text
com.cocos.androidaccounting
├── navigation/
│   └── AppNavHost.kt                 (修改：填充 composable<Route.Home>)
├── ui/home/
│   ├── HomeRoute.kt                  (新建)
│   ├── HomeScreen.kt                 (新建)
│   ├── HomeContent.kt                (新建)
│   ├── YearMonthPickerDialog.kt      (新建)
│   ├── HomeViewModel.kt              (新建)
│   ├── HomeUiState.kt               (新建：含 BillGroup)
│   ├── HomeUiIntent.kt              (新建)
│   └── HomeUiEffect.kt              (新建)
├── util/
│   ├── MoneyFormatter.kt            (新建：分 → ¥xx.xx)
│   └── DateUtil.kt                  (修改：新增日期/月份/星期格式化)
└── res/values/strings.xml           (修改：首页文案)
```

### `BillGroup`（UI 分组载体）

放在 `ui/home/HomeUiState.kt`（与 State 同文件，属展示层模型）：

```kotlin
data class BillGroup(
    val date: LocalDate,
    val bills: List<Bill>,
    val dayNetAmount: Long,   // 当日收支净额（分），= 收入之和 − 支出之和；正=净收入，负=净支出
)
```

### 分组扩展函数

```kotlin
fun List<Bill>.toBillGroups(): List<BillGroup> =
    groupBy { it.date }                       // 输入已排序，LinkedHashMap 保序
        .map { (date, bills) ->
            BillGroup(
                date = date,
                bills = bills,
                dayNetAmount = bills.sumOf { if (it.type == BillType.INCOME) it.amount else -it.amount },
            )
        }
```

放在 `HomeViewModel.kt`（或 `HomeUiState.kt`）作为内部工具，仅 ViewModel 调用。

### `HomeViewModel` 关键方法签名

```kotlin
val uiState: StateFlow<HomeUiState>
val effect: SharedFlow<HomeUiEffect>
fun onIntent(intent: HomeUiIntent)
```

### 金额格式化函数（位置）

`util/MoneyFormatter.kt`，纯函数、无状态、无业务规则，UI 层与 ViewModel 均可调用：

```kotlin
object MoneyFormatter {
    /** 分 → "¥xx.xx" */
    fun formatYuan(cents: Long): String =
        "¥" + BigDecimal(cents).movePointLeft(2).setScale(2, RoundingMode.HALF_UP).toPlainString()

    /** 按类型加符号："-¥xx.xx"（支出）/ "+¥xx.xx"（收入） */
    fun formatSignedYuan(type: BillType, cents: Long): String {
        val sign = if (type == BillType.EXPENSE) "-" else "+"
        return sign + formatYuan(cents)
    }
}
```

### 日期格式化函数（位置）

扩展 `util/DateUtil.kt`（与 M2 epochDay 工具并存）：

```kotlin
/** "6月16日 周二" */
fun formatGroupHeader(date: LocalDate): String

/** "6月" */
fun formatMonthLabel(yearMonth: YearMonth): String

/** 中文星期：周一..周日 */
fun weekdayLabel(date: LocalDate): String
```

年份直接 `yearMonth.year.toString()`，不另设函数。

## 状态与副作用模型

- **State**：单一 `StateFlow<HomeUiState>`，由 `selectedMonth` + `pickerVisible` + `dataState` 三源 `combine` 派生，唯一可信状态源。
- **Intent**：UI 全部交互经 `onIntent(HomeUiIntent)` 单入口下发（月份点击、弹窗确认 / 取消、记账跳转、占位 Toast）。
- **Effect**：一次性事件经 `SharedFlow<HomeUiEffect>` 暴露，仅在 `HomeRoute` 用 `LaunchedEffect` + `repeatOnLifecycle`（或 `flowWithLifecycle`）收集一次：
  - `NavigateToRecord` → 调用 `onNavigateToRecord()`（NavHost 跳 `Route.Record`）。
  - `ShowToast(messageRes)` → `Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show()`（≈1.5 秒，对齐 PRD「LENGTH_SHORT 近似」）。
- **职责边界**：业务 / 展示决策（分组、当日合计、月份切换、loading / error）在 ViewModel；`Composable` 仅渲染 state、上抛 Intent、做纯格式化。State 不含一次性事件、不暴露 `MutableState`。
- **错误处理**：数据流 `catch` 不吞异常，转 `error` 文案；不使用 `runBlocking` / `Thread.sleep`。

## 文件变更计划

> 实现顺序建议：`MoneyFormatter` + `DateUtil` 扩展 → `HomeUiState`（含 `BillGroup`）/ `HomeUiIntent` / `HomeUiEffect` → `HomeViewModel`（先跑通单测）→ `YearMonthPickerDialog` → `HomeContent` → `HomeScreen` → `HomeRoute` → 接 `AppNavHost` → `strings.xml` → `./gradlew assembleDebug` + `testDebugUnitTest` 验证。

| 文件 | 操作 | 说明 |
| --- | --- | --- |
| `util/MoneyFormatter.kt` | 新建 | 分 → `¥xx.xx`、按类型加符号 |
| `util/DateUtil.kt` | 修改 | 新增 `formatGroupHeader` / `formatMonthLabel` / `weekdayLabel` |
| `ui/home/HomeUiState.kt` | 新建 | `HomeUiState` + `BillGroup` |
| `ui/home/HomeUiIntent.kt` | 新建 | 5 个 Intent 变体 |
| `ui/home/HomeUiEffect.kt` | 新建 | `NavigateToRecord` / `ShowToast` |
| `ui/home/HomeViewModel.kt` | 新建 | `@HiltViewModel`，combine + flatMapLatest + 分组 + Effect |
| `ui/home/YearMonthPickerDialog.kt` | 新建 | M1 `BottomSheetPicker` + 双列 `WheelPicker` |
| `ui/home/HomeContent.kt` | 新建 | 无状态 `LazyColumn` 渲染（头部 / 月份 / 卡片 / 明细 / 空态） |
| `ui/home/HomeScreen.kt` | 新建 | `Scaffold` + `AccountingBottomBar` + 条件弹窗 |
| `ui/home/HomeRoute.kt` | 新建 | `hiltViewModel` + 状态收集 + Effect 收集 |
| `navigation/AppNavHost.kt` | 修改 | `composable<Route.Home> { HomeRoute(onNavigateToRecord = { navController.navigate(Route.Record) }) }` |
| `res/values/strings.xml` | 修改 | 新增首页文案（见下） |

需新增的字符串资源：

| key | 值 |
| --- | --- |
| `home_title` | `Code记账` |
| `home_label_expense` | `支出` |
| `home_label_income` | `收入` |
| `home_section_detail` | `明细` |
| `home_empty` | `暂无记账` |
| `home_year_month_picker_title` | `选择月份` |
| `coming_soon` | `即将推出` |

## 测试计划

### 测试依赖

| 依赖 | 用途 | source set |
| --- | --- | --- |
| `junit:junit` `4.13.2` | 测试框架 | test |
| `kotlinx-coroutines-test` | `runTest` / `StandardTestDispatcher` / `Dispatchers.setMain` | test |
| `app.cash.turbine:turbine` | `StateFlow` / `SharedFlow` 断言 | test |

> M2 测试已引入以上依赖，复用即可。`HomeViewModel` 不依赖 Android 框架，可在 JVM `test` 跑。

### `HomeViewModelTest`（JVM test，FakeBillRepository）

`FakeBillRepository` 以 `MutableStateFlow<List<Bill>>` / `MutableStateFlow<MonthlySummary>` 模拟数据源，按 `yearMonth` 返回对应流。

| 用例 | 验证点 |
| --- | --- |
| `initialState_isLoading` | 订阅前 / 首发 `isLoading == true`，`groups` 空 |
| `dataLoaded_mapsToGroupsAndSummary` | 多笔账单 → `uiState.groups` 正确分组、`summary` 透传、`isLoading == false` |
| `grouping_preservesRepositoryOrder` | 输入已排序（跨日 + 同日多笔）→ 分组保序，组内顺序不变 |
| `grouping_dayNetAmount_incomeMinusExpense` | 某日含支出 + 收入 → `BillGroup.dayNetAmount` = 收入之和 − 支出之和 |
| `emptyMonth_groupsEmpty` | 当月无账单 → `groups` 空、`summary` 为 0、`isLoading == false` |
| `openPicker_setsVisibleTrue` | `OpenYearMonthPicker` → `isYearMonthPickerVisible == true` |
| `dismissPicker_setsVisibleFalse` | `DismissYearMonthPicker` → `false` |
| `confirmSelection_updatesYearMonth_andRequeries` | `ConfirmYearMonthSelection(2026,5)` → `yearMonth` 更新、重新订阅对应月数据、弹窗关闭 |
| `confirmSelection_refreshesSummaryAndBills` | 切月后 `summary` / `groups` 切换为新月数据 |
| `navigateToRecord_emitsEffect` | `NavigateToRecord` → `effect` 发 `NavigateToRecord` |
| `comingSoon_emitsShowToast` | `OnBottomBarComingSoon` → `effect` 发 `ShowToast(R.string.coming_soon)` |
| `repositoryError_setsErrorState` | 数据流抛异常 → `uiState.error != null`、`isLoading == false` |

辅助工具单测：

| 用例 | 验证点 |
| --- | --- |
| `MoneyFormatter_formatYuan` | `2300 → "¥23.00"`、`0 → "¥0.00"`、`5 → "¥0.05"` |
| `MoneyFormatter_formatSignedYuan` | EXPENSE → `-¥..`、INCOME → `+¥..` |
| `DateUtil_formatGroupHeader` | `2026-06-16(周二) → "6月16日 周二"` |
| `DateUtil_formatMonthLabel` | `YearMonth.of(2026,6) → "6月"` |

> 关键 Composable 已规划 `testTag`（`home_empty`、`wheel_picker`、列表项 `home_item_{id}`），便于后续补 instrumentation 测试。

## 设计输入问题

| # | 来源 | 问题 | 处理策略 |
| --- | --- | --- | --- |
| DI-M3-1 | 色值差异 | PRD 警示红 `#C75C5C` / 鼠尾草绿 `#7D9B76`，但 M1 token 为 `ExpenseRed #C0392B` / `IncomeGreen #27AE60` / `SageGreen #7A9E87` | 本方案统一使用 M1 Design System token（遵循「不硬编码颜色」）。若需精确对齐 PRD 色值，应改 M1 `Color.kt`（跨模块改动）。标记需确认。 |
| DI-M3-2 | 金额格式 | PRD 条目示例 `-¥23` / `+¥50`（无小数），摘要 `¥23.00`（两位小数） | 本方案统一两位小数 `¥xx.xx`。若产品要求条目省略整数小数，调整 `formatSignedYuan`。标记需确认。 |
| DI-M3-3 | 底部 Bar 记账按钮 | PRD 要求 44×44 鼠尾草绿圆角方形 + 白色加号「内嵌不突出」；M1 `AccountingBottomBar` 现用标准 `NavigationBarItem` | M3 复用 M1 现状，不在本模块改 M1 组件。特殊记账按钮视觉记为遗留项 LEFT-M3-3，由 M1/Design System 后续增强。标记需确认。 |
| DI-M3-4 | 选中态字号 | PRD 滚轮选中行 20sp Bold；M1 `WheelPicker` 选中 18sp Bold | 复用 M1 现状，差异记 LEFT-M3-2，M4 抽取组件时统一。 |
| DI-M3-5 | Toast 时长 | PRD「1.5 秒自动消失」，实现用 `Toast.LENGTH_SHORT`（约 2 秒） | 按整体方案 / 任务说明用 `LENGTH_SHORT` 近似，不自定义计时。 |
| DI-M3-6 | 类目图标 | 明细条目左侧 24×24 线条类目图标资产未就位 | 开发期用 Material Icons 占位（整体方案 R8），设计产出后替换。 |

## 审批状态

`approved_by_user`

已确认决策：
- DI-M3-1：更新 M1 `Color.kt`，精确对齐 PRD 色值（`expenseRed = #C75C5C`，`incomeGreen = #7D9B76`，`SageGreen = #7D9B76`）。
- DI-M3-2：统一两位小数，金额格式为 `¥xx.00`。
- DI-M3-3：本期修改 M1 `AccountingBottomBar`，按 PRD 实现 44×44 鼠尾草绿圆角方形记账按钮。

## 进度记录

| 日期 | 状态 | 说明 |
| --- | --- | --- |
| 2026-06-24 | technical_plan_created | M3 首页明细模块技术方案生成，审批 pending，待用户确认 DI-M3-1/2/3 后实现 |
| 2026-06-24 | approved_by_user | 用户确认：更新 M1 色值对齐 PRD、金额统一两位小数、本期修复 AccountingBottomBar 记账按钮 |
| 2026-06-24 | in_progress | 实现完成，代码审查发现 2H/3M/6L 问题，修复进行中 |
| 2026-06-24 | completed | 代码审查全部修复，28 个 JVM 测试通过，BUILD SUCCESSFUL |
| 2026-06-25 | completed → completed_with_issues | CHANGE-ACCOUNTING-002：DateGroupHeader 右侧金额改为当日收支净额（INTAKE-007），BillGroup.dayExpense → dayNetAmount，方案更新，待实现 |
| 2026-06-25 | completed_with_issues → completed | CHANGE-ACCOUNTING-002 实现完成：HomeUiState.BillGroup.dayExpense→dayNetAmount，toBillGroups() 改为收入−支出净额，DateGroupHeader 展示带符号金额（净额为0不显示），HomeViewModelTest 用例 grouping_dayNetAmount_incomeMinusExpense 更新并通过，全部 JVM 测试 BUILD SUCCESSFUL |

## Bug 与遗留问题

### BUG-001：isLoading 时收支摘要无骨架屏、明细区无加载指示

- 来源：agent_found（code-reviewer，L4 审查项）
- 状态：open
- 严重级别：low
- 描述：`isLoading=true` 时，`SummaryCard` 未接收 `isLoading` 参数，直接以 `MonthlySummary()` 默认值（0分）渲染，显示 `¥0.00`；明细区通过 `!isLoading` 守卫跳过空态，但无 `CircularProgressIndicator`，呈现为空白区域。视觉上缺少 loading 反馈。
- 位置：`ui/home/HomeContent.kt`（`SummaryCard` 调用处、`groups.isEmpty()` 判断块）
- 解决方案：`SummaryCard` 接收 `isLoading` 参数，loading 时显示占位符（骨架屏或 `¥--`）；明细区 loading 时显示 `CircularProgressIndicator`
- github_issue：#1
- 创建时间：2026-06-24
- 更新时间：2026-06-24

### 遗留项

| # | 类型 | 描述 | 处理时机 |
| --- | --- | --- | --- |
| ~~LEFT-M3-1~~ | ~~架构~~ | ~~`YearMonthPickerDialog` 在 M3 内自包含实现；M4 完成后提取为通用组件并替换引用~~ | ~~M4 实现后~~ → 已关闭：M4 决策保留在 ui/home/（无跨模块复用需求，避免无价值重构） |
| ~~LEFT-M3-2~~ | ~~视觉~~ | ~~`WheelPicker` 选中态 18sp，与 PRD 20sp 有差~~ | ~~M4 抽取组件统一调优~~ → 已在 M4 修复：WheelPicker.kt 选中态 fontSize 18sp→20sp |
| ~~LEFT-M3-3~~ | ~~视觉~~ | ~~底部 Bar 记账按钮未按 PRD 做 44×44 鼠尾草绿圆角方形~~ | ~~已在 M3 实现：`AccountingBottomBar` 44dp + SageGreen + RoundedCornerShape(12dp)~~ |
| LEFT-M3-4 | 资源 | 类目线条图标占位，待设计产出替换 | 设计资产就位后 |
| LEFT-M3-5 | 视觉 | 摘要卡片「细描边 + 中间竖向分隔线」用 `DividerGray` token 近似 PRD `#E8E8E4` | DI-M3-1 确认色值后（已决策：维持现状，色差可接受） |
| LEFT-M3-6 | UI | `error` 状态未接线 UI：`HomeUiState.error` 有赋值，`HomeViewModelTest` 有覆盖，但 `HomeContent`/`HomeScreen` 未渲染 error 态 | 后续迭代 |
| LEFT-M3-7 | 测试 | testTag 命名与方案有差：方案规划 `home_item_{id}`，代码实际为 `bill_item_${bill.id}` | 无需修改（代码一致性优先），更新文档记录 |

## 开发记录

| 日期 | 内容 |
| --- | --- |
| 2026-06-24 | 实现完成（场景 C）：17 个文件新建/修改，assembleDebug BUILD SUCCESSFUL，18 个 JVM 测试通过 |
| 2026-06-24 | 代码审查修复（场景 D）：修复 H1（catch 移入 flatMapLatest 内部）、H2（flowWithLifecycle）、M1（onStart loading）、M2（补充 FakeBillRepository 按月数据 + 2 个测试用例）、M3（移除冗余排序）和 L1/L2/L3/L5/L6，28 个 JVM 测试通过；BUG-001（L4 loading 视觉）记为遗留 |
