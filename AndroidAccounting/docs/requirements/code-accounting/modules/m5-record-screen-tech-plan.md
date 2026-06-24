# M5 记账页模块技术方案

## 模块目标

为 CodeAccounting Android 端交付**记账页**完整功能（SCREEN-V2-003 ~ V2-005 / FLOW-V2-002、FLOW-V2-003），覆盖 REQ-ACCOUNTING-003（支出记账）、REQ-ACCOUNTING-004（收入记账）、REQ-ACCOUNTING-005（金额键盘），并集成 M6 已交付的 `DatePickerDialog` 完成 REQ-ACCOUNTING-006 的日期选择部分。基于 M1 基础设施（`AccountingTheme`、Navigation、Hilt）、M2 数据层（`BillRepository.insertBill`、`Bill`、`BillType`、`Categories`）、M6 日期选择器，实现一套「可编译、可注入、可测试」的记账页：

1. 支出 / 收入 Tab 切换（切换 `type`，重置类目选择）。
2. 类目网格（按 `type` 渲染 4 个类目，单选高亮）。
3. 金额显示区（等宽大字）+ 备注行 + 日期行。
4. 金额键盘（数字 0-9、小数点、删除、加减号、完成保存）。
5. `RecordViewModel`（MVI）：持有输入态，校验后调用 `insertBill`，保存成功后回退首页。
6. 集成 M6 `DatePickerDialog` 完成日期选择。
7. 填充 `AppNavHost` 中 `composable<Route.Record>` 的 `TODO(M5)`，处理保存后 `popBackStack`。

## 范围

- Compose 页面（`ui/record/`）：`RecordRoute.kt`、`RecordScreen.kt`、`RecordContent.kt`、`CategoryGrid.kt`、`AmountKeyboard.kt`。
- MVI 契约（`ui/record/`）：`RecordUiState.kt`、`RecordUiIntent.kt`、`RecordUiEffect.kt`、`RecordViewModel.kt`。
- 金额键盘按键模型：`AmountKey`（sealed interface，放在 `RecordUiIntent.kt` 或单独文件）。
- 金额字符串拼接 / 转分纯函数：扩展 `util/MoneyFormatter.kt`（新增 `appendAmountKey` 与 `amountInputToCents`）。
- 导航接线：填充 `navigation/AppNavHost.kt` 的 `composable<Route.Record>`，传入 `onNavigateBack`。
- 文案资源：`res/values/strings.xml` 新增记账页相关字符串。
- 集成 M6 `DatePickerDialog`（已存在于 `ui/record/`，本模块仅条件渲染调用）。
- 测试：`RecordViewModel` 单元测试（JVM，`FakeBillRepository`）+ 金额拼接纯函数单测。

## 非目标

- 不实现完整计算器逻辑（A8：加减号首期仅切换 `type`，详见「设计输入问题」DI-M5-1）。
- 不实现 `DatePickerDialog` 本体（归属 M6，已完成，本模块仅集成使用）。
- 不实现编辑 / 删除已有账单（PRD 记账页仅定义「新建一笔」；`deleteBill` 不在本期接入）。
- 不实现输入中间态的进程级恢复（整体方案 A7：屏幕旋转 / 进程被杀不恢复未完成输入，不使用 `SavedStateHandle` 持久化输入）。
- 不实现自定义类目、类目增删改（PRD 类目为静态 `Categories` 配置）。
- 不实现连续记账（保存后停留继续记下一笔）；本期保存成功即回退首页（FLOW-V2-002/003 终点为「保存」）。
- 不替换金额等宽字体文件（沿用 M1 `amountDisplay` fallback Monospace，整体方案 R2）。
- 不重绘类目线条图标（沿用 `Categories` 中 `android.R.drawable.*` 占位，整体方案 R8 / LEFT-M3-4）。
- 不做 instrumentation UI 测试（与 M1/M2/M3/M4/M6 策略一致）。

## 依赖关系

- 上游依赖：
  - M1 基础设施（`✅ completed`）：`AccountingTheme`、`LocalAccountingColors`（`expenseRed` / `incomeGreen`）、`LocalAccountingTypography`（`amountDisplay`）、`AccountingShapes`、`Route`、`AppNavHost`、Hilt、`hilt-navigation-compose`、`lifecycle-runtime-compose`。
  - M2 数据层（`✅ completed`）：`BillRepository.insertBill(bill: Bill): Long`、`Bill`、`BillType`、`Categories` / `Category`。
  - M6 日期选择器（`✅ completed`）：`ui/record/DatePickerDialog.kt`（签名 `DatePickerDialog(initialDate, onConfirm, onDismiss)`）。
- 下游被依赖：无（M5 是叶子功能模块）。
- 与 M3 的关系：M3 首页底部 Bar 记账入口已通过 `HomeUiEffect.NavigateToRecord` → `AppNavHost.navigate(Route.Record)` 跳转到本页；本模块填充目标页并在保存 / 关闭后 `popBackStack` 回 M3。M3 `HomeViewModel` 数据流为 `observeBillsByMonth` 实时订阅，本页 `insertBill` 后首页自动刷新，无需手动通知。
- 模块内依赖方向：`RecordRoute` → `RecordScreen` → `RecordContent` → (`CategoryGrid` / `AmountKeyboard`)（State up, events down）；`RecordViewModel` → `BillRepository`（构造注入接口）。严禁 Composable 直接依赖 `BillRepository` / Room。
- 第三方依赖：复用 M1/M2 已接入版本，无需新增声明。测试依赖 `junit`、`kotlinx-coroutines-test`、`turbine`（M2/M3 已引入，复用）。

## UI 方案

### 整体页面布局（SCREEN-V2-003 / V2-004 / V2-005）

记账页为**全屏页面**（非弹窗），纸白底（`MaterialTheme.colorScheme.background` = `PaperWhite`）。采用 `Scaffold`（**不含底部 Bar**，记账页是从首页 push 出的独立页面，无五入口导航）承载，内容用顶部固定区 + 中部类目区 + 底部输入键盘区的纵向结构。

整页结构（从上到下）：

```text
RecordContent (Column, fillMaxSize)
├── RecordTopBar           // 关闭(×) + 居中 Tab(支出/收入)
├── CategoryGrid           // 4 类目网格（按 type 渲染，weight 占中部可用空间）
└── 底部输入键盘区（anchored bottom）:
    ├── AmountDisplayRow   // 金额大字（等宽，右对齐，含 ¥ 前缀）
    ├── RemarkRow          // 备注单行输入（BasicTextField/TextField）
    ├── DateRow            // 日期显示（clickable → OpenDatePicker）
    └── AmountKeyboard     // 金额键盘网格
+ DatePickerDialog（条件渲染：isDatePickerVisible == true）
```

### 各 Composable 职责

| Composable | 层级 | 职责 |
| --- | --- | --- |
| `RecordRoute` | Route | `hiltViewModel()` 获取 `RecordViewModel`；`collectAsStateWithLifecycle()` 收集 `uiState`；`LaunchedEffect` + `flowWithLifecycle` 收集 `effect`（`NavigateBack` → `onNavigateBack()`；`ShowToast` → Toast）；下发 state + `onIntent` |
| `RecordScreen` | Screen | 持有 `Scaffold`；拆 state 给 `RecordContent`；转发回调为 `RecordUiIntent`；按 `isDatePickerVisible` 条件渲染 `DatePickerDialog`（M6）；持有顶部关闭 `onClose`（直接调 `onNavigateBack`，纯导航不经 VM） |
| `RecordContent` | Content | 无状态；`Column` 组合顶部 Tab、类目网格、金额显示、备注、日期、键盘；仅接收 state 与回调 |
| `RecordTopBar` | Content 子项 | 左侧关闭按钮（`Icons.Default.Close`）+ 居中支出 / 收入 Tab |
| `RecordTypeTabs` | Content 子项 | 两段式 Tab：「支出」/「收入」，激活态文字着色（支出 `expenseRed` / 收入 `incomeGreen`），点击 → `onSelectType` |
| `CategoryGrid` | Content 子项 | 渲染 `Categories.byType(type)` 的 4 个类目（图标 + 名称），选中态高亮，点击 → `onSelectCategory` |
| `AmountDisplayRow` | Content 子项 | 金额等宽大字（`LocalAccountingTypography.current.amountDisplay`），右对齐，空值显示 `¥0` |
| `RemarkRow` | Content 子项 | 单行文本输入（`TextField` / `BasicTextField`），placeholder「点击填写备注」，`onValueChange` → `onChangeRemark` |
| `DateRow` | Content 子项 | 显示选中日期（`6月24日` 格式），整行 `clickable` → `onOpenDatePicker` |
| `AmountKeyboard` | Content 子项 | 数字 / 小数点 / 删除 / 加减号 / 完成 键盘网格，点击 → `onAmountKey(AmountKey)` |

### `CategoryGrid` 布局

4 个类目用**单行 4 列**（`Row` + 每项 `weight(1f)`）渲染，不引入 `LazyVerticalGrid`（静态小集合，简单优先）：

- 每个类目项：纵向 `Column`（图标 24~32dp + 4dp 间距 + 类目名 `bodyMedium`），整体 `clickable` → `onSelectCategory(category)`。
- 选中态：选中项图标 / 文字着 `type` 对应语义色（支出 `expenseRed` / 收入 `incomeGreen`），未选中为 `onSurfaceVariant`。
- 颜色全部取自 Design System token（`LocalAccountingColors` / `MaterialTheme.colorScheme`），不硬编码 `Color(...)`。
- `testTag`：每个类目项加 `record_category_{name}`（如 `record_category_餐饮`）。

### `AmountKeyboard` 布局

键盘为底部固定区，采用 `Row { 左侧数字 Column(weight 3f) + 右侧功能 Column(weight 1f) }` 组合。「完成」按钮纵向跨末两行：

```text
┌──────────────────────────┬─────────┐
│ 1   2   3                │   ⌫    │
│ 4   5   6                │  +/−   │
│ 7   8   9                │        │
│ .   0（跨2列）           │  完成  │
└──────────────────────────┴─────────┘
```

- 左侧数字区（`Column`，weight 3f）：4 行 `Row`，前 3 行 `1 2 3` / `4 5 6` / `7 8 9`（各 `weight(1f)`），第 4 行 `.`（weight 1f）+ `0`（weight 2f）。
- 右侧功能区（`Column`，weight 1f）：`⌫`（删除，weight 1f）、`+/−`（加减号，weight 1f）、`完成`（weight 2f，`primary` 背景、`onPrimary` 文字、圆角）。
- `testTag`：键盘容器 `record_keyboard`，关键功能键 `record_key_done` / `record_key_delete` / `record_key_plus_minus`。

> 备注和日期作为独立输入行处理（`ChangeRemark` / `OpenDatePicker` Intent），不进入键盘网格。

### Tab 切换行为

- 切换 `type` 时：更新 `type`，**重置 `selectedCategory = null`**，**保留 `amountInput` / `remark` / `date`**。
- 激活态视觉：支出 Tab 激活时 `expenseRed`；收入 Tab 激活时 `incomeGreen`；非激活为 `onSurfaceVariant`。

### 金额显示规则

- `amountInput == ""` → 显示 `¥0`（占位，`onSurfaceVariant`）。
- 非空 → 显示 `¥` + `amountInput`（原样回显，不补两位小数；保存时再转分）。
- 字体：`LocalAccountingTypography.current.amountDisplay`（等宽 32sp Medium）。

### 日期显示规则

- 格式：`{month}月{day}日`，由 `DateUtil.formatRecordDate(date: LocalDate): String` 生成（新增工具函数）。
- 默认值为今日；首期直接显示日期，不额外处理「今天」标签（简单优先）。

## ViewModel 方案

### `RecordUiState`

```kotlin
data class RecordUiState(
    val type: BillType = BillType.EXPENSE,
    val selectedCategory: Category? = null,
    val amountInput: String = "",
    val remark: String = "",
    val date: LocalDate = LocalDate.now(),
    val isDatePickerVisible: Boolean = false,
    val isSaving: Boolean = false,
)
```

### `RecordUiIntent`

```kotlin
sealed interface RecordUiIntent {
    data class SelectType(val type: BillType) : RecordUiIntent
    data class SelectCategory(val category: Category) : RecordUiIntent
    data class InputAmount(val key: AmountKey) : RecordUiIntent
    data class ChangeRemark(val text: String) : RecordUiIntent
    data object OpenDatePicker : RecordUiIntent
    data object DismissDatePicker : RecordUiIntent
    data class ConfirmDate(val date: LocalDate) : RecordUiIntent
    data object Save : RecordUiIntent
}
```

### `AmountKey`（放在 `RecordUiIntent.kt`）

```kotlin
sealed interface AmountKey {
    data class Digit(val n: Int) : AmountKey
    data object Dot : AmountKey
    data object Delete : AmountKey
    data object PlusMinus : AmountKey  // A8：切换 type
    data object Done : AmountKey       // 等价 Save
}
```

### `RecordUiEffect`

```kotlin
sealed interface RecordUiEffect {
    data object NavigateBack : RecordUiEffect
    data class ShowToast(val messageRes: Int) : RecordUiEffect
}
```

### ViewModel 核心逻辑

```kotlin
@HiltViewModel
class RecordViewModel @Inject constructor(
    private val repository: BillRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<RecordUiEffect>()
    val effect: SharedFlow<RecordUiEffect> = _effect.asSharedFlow()

    fun onIntent(intent: RecordUiIntent) {
        when (intent) {
            is RecordUiIntent.SelectType -> _uiState.update {
                it.copy(type = intent.type, selectedCategory = null)
            }
            is RecordUiIntent.SelectCategory -> _uiState.update {
                it.copy(selectedCategory = intent.category)
            }
            is RecordUiIntent.InputAmount -> handleAmountKey(intent.key)
            is RecordUiIntent.ChangeRemark -> _uiState.update {
                it.copy(remark = intent.text.take(MAX_REMARK_LENGTH))
            }
            RecordUiIntent.OpenDatePicker -> _uiState.update { it.copy(isDatePickerVisible = true) }
            RecordUiIntent.DismissDatePicker -> _uiState.update { it.copy(isDatePickerVisible = false) }
            is RecordUiIntent.ConfirmDate -> _uiState.update {
                it.copy(date = intent.date, isDatePickerVisible = false)
            }
            RecordUiIntent.Save -> save()
        }
    }

    private fun handleAmountKey(key: AmountKey) {
        when (key) {
            is AmountKey.Digit -> _uiState.update {
                it.copy(amountInput = appendDigit(it.amountInput, key.n))
            }
            AmountKey.Dot -> _uiState.update {
                it.copy(amountInput = appendDot(it.amountInput))
            }
            AmountKey.Delete -> _uiState.update {
                it.copy(amountInput = it.amountInput.dropLast(1))
            }
            AmountKey.PlusMinus -> _uiState.update {
                val newType = if (it.type == BillType.EXPENSE) BillType.INCOME else BillType.EXPENSE
                it.copy(type = newType, selectedCategory = null)
            }
            AmountKey.Done -> save()
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.isSaving) return
        val category = state.selectedCategory
        if (category == null) {
            emitEffect(RecordUiEffect.ShowToast(R.string.record_error_no_category)); return
        }
        val cents = amountInputToCents(state.amountInput)
        if (cents == null || cents <= 0L) {
            emitEffect(RecordUiEffect.ShowToast(R.string.record_error_no_amount)); return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                repository.insertBill(
                    Bill(
                        type = state.type,
                        category = category.name,
                        amount = cents,
                        date = state.date,
                        remark = state.remark,
                    )
                )
                emitEffect(RecordUiEffect.NavigateBack)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                emitEffect(RecordUiEffect.ShowToast(R.string.record_error_save_failed))
            }
        }
    }

    private fun emitEffect(e: RecordUiEffect) = viewModelScope.launch { _effect.emit(e) }

    companion object { const val MAX_REMARK_LENGTH = 20 }
}
```

### `InputAmount` 字符串拼接规则

抽取为 `MoneyFormatter` 纯函数（JVM 可测）：

**`appendDigit(input, n)`**：
- `input == "0"` 且 `n != 0` → 替换为 `"$n"`（消除前导 0）
- `input == "0"` 且 `n == 0` → 忽略（避免 `"00"`）
- 含 `.`，小数部分长度 ≥ 2 → 忽略
- 不含 `.`，`input.length >= 7` → 忽略（整数最多 7 位）
- 其余 → `input + n`

**`appendDot(input)`**：
- 已含 `.` → 忽略；`input == ""` → `"0."`；否则 → `input + "."`

**`amountInputToCents(input): Long?`**：
```kotlin
BigDecimal(input).movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()
```
解析失败或结果 ≤ 0 返回 `null`。

## Repository 方案

不新增 / 不修改 Repository。直接消费 M2 `BillRepository.insertBill`。

- `amount` 始终存正值（分），正负由 `type` 决定。
- `id = 0`（Room autoGenerate），`createdAt = Instant.now()`（默认值）。
- 写入后 M3 `observeBillsByMonth` 实时流自动刷新，M5 无需通知。

## Network 方案

不涉及。

## 模块架构方案

```text
com.cocos.androidaccounting
├── navigation/
│   └── AppNavHost.kt (修改：填充 composable<Route.Record>)
├── ui/record/
│   ├── RecordRoute.kt (新建)
│   ├── RecordScreen.kt (新建)
│   ├── RecordContent.kt (新建)
│   ├── CategoryGrid.kt (新建)
│   ├── AmountKeyboard.kt (新建)
│   ├── RecordViewModel.kt (新建)
│   ├── RecordUiState.kt (新建)
│   ├── RecordUiIntent.kt (新建：含 AmountKey)
│   ├── RecordUiEffect.kt (新建)
│   └── DatePickerDialog.kt (M6 已存在，本模块集成调用，不修改)
├── util/
│   ├── MoneyFormatter.kt (修改：新增 appendDigit/appendDot/amountInputToCents)
│   └── DateUtil.kt (修改：新增 formatRecordDate)
└── res/values/strings.xml (修改：记账页文案)
```

### 导航接线

```kotlin
// AppNavHost.kt
composable<Route.Record> {
    RecordRoute(onNavigateBack = { navController.popBackStack() })
}
```

## 状态与副作用模型

- **State**：单一 `StateFlow<RecordUiState>`，输入态全部内聚。
- **Intent**：全部交互经 `onIntent(RecordUiIntent)` 单入口。
- **Effect**：一次性事件经 `SharedFlow<RecordUiEffect>` 暴露，在 `RecordRoute` 用 `LaunchedEffect` + `flowWithLifecycle` 收集（对齐 M3 H2 修复写法）。
- **错误处理**：`insertBill` 包 `try/catch`，失败复位 `isSaving` 并发 `ShowToast`，不吞异常。
- **A7 一致性**：不使用 `SavedStateHandle`，旋转时 ViewModel 实例存活即保留状态。

## 文件变更计划

| 文件 | 操作 | 说明 |
| --- | --- | --- |
| `util/MoneyFormatter.kt` | 修改 | 新增 `appendDigit` / `appendDot` / `amountInputToCents` |
| `util/DateUtil.kt` | 修改 | 新增 `formatRecordDate(date: LocalDate): String` |
| `ui/record/RecordUiState.kt` | 新建 | `RecordUiState` |
| `ui/record/RecordUiIntent.kt` | 新建 | 8 个 Intent + `AmountKey` sealed interface |
| `ui/record/RecordUiEffect.kt` | 新建 | `NavigateBack` / `ShowToast` |
| `ui/record/RecordViewModel.kt` | 新建 | `@HiltViewModel`，输入态处理 + 校验 + `insertBill` + Effect |
| `ui/record/CategoryGrid.kt` | 新建 | 4 类目网格（按 type 渲染，单选高亮） |
| `ui/record/AmountKeyboard.kt` | 新建 | 数字 / 小数点 / 删除 / 加减号 / 完成 键盘 |
| `ui/record/RecordContent.kt` | 新建 | 无状态 `Column` 组合（Tab / 类目 / 金额 / 备注 / 日期 / 键盘） |
| `ui/record/RecordScreen.kt` | 新建 | `Scaffold` + 条件渲染 M6 `DatePickerDialog` |
| `ui/record/RecordRoute.kt` | 新建 | `hiltViewModel` + 状态收集 + Effect 收集 |
| `navigation/AppNavHost.kt` | 修改 | 填充 `composable<Route.Record>` |
| `res/values/strings.xml` | 修改 | 新增记账页文案 |

需新增的字符串资源：

| key | 值 |
| --- | --- |
| `record_tab_expense` | `支出` |
| `record_tab_income` | `收入` |
| `record_remark_placeholder` | `点击填写备注` |
| `record_key_done` | `完成` |
| `record_close_desc` | `关闭` |
| `record_delete_desc` | `删除` |
| `record_error_no_category` | `请选择类目` |
| `record_error_no_amount` | `请输入金额` |
| `record_error_save_failed` | `保存失败，请重试` |

## 测试计划

### `RecordViewModelTest`（JVM test，FakeBillRepository）

| 用例 | 验证点 |
| --- | --- |
| `initialState` | `type == EXPENSE`、`selectedCategory == null`、`amountInput == ""`、`date == LocalDate.now()`、`isDatePickerVisible == false`、`isSaving == false` |
| `selectType_switchesCategories` | `SelectType(INCOME)` → `type == INCOME`、`selectedCategory == null`；金额 / 备注 / 日期保留 |
| `selectCategory` | `SelectCategory(餐饮)` → `selectedCategory == 餐饮` |
| `inputAmount_digits` | 依次输入 1,2,3 → `amountInput == "123"` |
| `inputAmount_leadingZeroReplaced` | 输入 0,5 → `"5"`；0,0 → `"0"` |
| `inputAmount_decimal` | 输入 1,Dot,5 → `"1.5"`；空串 Dot → `"0."` |
| `inputAmount_maxDecimal` | 输入 1,Dot,2,3,4 → `"1.23"` |
| `inputAmount_singleDot` | 输入 1,Dot,2,Dot → `"1.2"` |
| `inputAmount_maxInteger` | 输入超 7 位整数 → 截断不超过 7 位 |
| `inputAmount_delete` | 输入 123 → Delete → `"12"` |
| `inputAmount_deleteEmpty` | 空串 → Delete → `""` |
| `plusMinus_togglesType` | `InputAmount(PlusMinus)`（type=EXPENSE）→ `type == INCOME`、`selectedCategory == null` |
| `changeRemark` | `ChangeRemark("午餐")` → `remark == "午餐"`；超长截断到 20 |
| `openDatePicker` | `OpenDatePicker` → `isDatePickerVisible == true` |
| `dismissDatePicker` | `DismissDatePicker` → `isDatePickerVisible == false` |
| `confirmDate` | `ConfirmDate(date)` → `date` 更新、`isDatePickerVisible == false` |
| `save_withoutCategory_showsError` | `category == null` → `Save` → `ShowToast(record_error_no_category)`，`insertBill` 未调用 |
| `save_withoutAmount_showsError` | `amountInput == ""` → `Save` → `ShowToast(record_error_no_amount)`，`insertBill` 未调用 |
| `save_zeroAmount_showsError` | `amountInput == "0"` → `Save` → `ShowToast` |
| `save_success_navigatesBack` | 有效输入（类目 + `"12.30"`）→ `Save` → `insertBill` 调用且 `amount == 1230`，`effect` 发 `NavigateBack` |
| `save_failure_showsToast_andResetsSaving` | `insertBill` 抛异常 → `ShowToast(record_error_save_failed)`、`isSaving == false` |
| `done_keyTriggersSave` | `InputAmount(Done)` 有效输入 → `NavigateBack` |

辅助纯函数单测（`MoneyFormatterTest` 扩展）：

| 用例 | 验证点 |
| --- | --- |
| `appendDigit_basic` | `appendDigit("12", 3) == "123"` |
| `appendDigit_leadingZero` | `appendDigit("0", 5) == "5"`、`appendDigit("0", 0) == "0"`、`appendDigit("", 0) == "0"` |
| `appendDigit_maxDecimal` | `appendDigit("1.23", 4) == "1.23"` |
| `appendDigit_maxInteger` | `appendDigit("9999999", 9) == "9999999"` |
| `appendDot_first` | `appendDot("") == "0."`、`appendDot("12") == "12."`、`appendDot("1.2") == "1.2"` |
| `amountInputToCents` | `"12.30" → 1230`、`"5" → 500`、`"0.05" → 5`、`"" → null`、`"0" → null`、`"." → null` |

## 设计输入问题

| # | 来源 | 问题 | 处理策略 |
| --- | --- | --- | --- |
| DI-M5-1 | 整体方案 A8 | 金额键盘 +/− 语义 | **已决策**：首期 `+/−` 键切换 `type`（EXPENSE ⇄ INCOME），与 Tab 同步、重置类目，不做数值运算 |
| DI-M5-2 | PRD vs MVI | REQ-005 将「备注」「日期」列入「金额键盘」功能 | 备注 / 日期作为独立输入行，不进键盘网格；语义覆盖一致 |
| DI-M5-3 | PRD 视觉 | 记账页是否显示底部五入口 Bar | 本方案记账页**不显示底部 Bar**（顶部有关闭按钮），标记需确认 |
| DI-M5-4 | PRD 交互 | 保存成功后「继续记账」vs「回退首页」 | 本方案保存即回退（`NavigateBack`），标记需确认 |
| DI-M5-5 | 切换 type 时金额 | Tab/+/− 切换 type 后金额是否清空 | 保留 `amountInput` / `remark` / `date`，仅重置 `selectedCategory` |
| DI-M5-6 | 备注字数 | PRD 未强制上限 | 取 `MAX_REMARK_LENGTH = 20`，在 `ChangeRemark` 截断 |
| DI-M5-7 | 类目图标 | 线条图标资产未就位 | 沿用 `android.R.drawable.*` 占位（整体方案 R8）|
| DI-M5-8 | 金额回显格式 | 输入中是否实时补两位小数 | 原样回显，保存时转分；不强制实时补 0 |

## 审批状态

`skipped_by_user`

用户已明确要求直接进入开发，跳过审批等待。

## 进度记录

| 日期 | 状态 | 说明 |
| --- | --- | --- |
| 2026-06-24 | technical_plan_created | M5 记账页模块技术方案生成；用户跳过审批，直接进入实现 |
| 2026-06-24 | in_progress | 实现 RecordViewModel、CategoryGrid、AmountKeyboard、RecordContent/Screen/Route |
| 2026-06-24 | completed | 实现完成，JVM 测试全部通过，BUILD SUCCESSFUL |
| 2026-06-24 | completed | 代码审查修复：M1（Categories 数据流）、M2（键盘字体 token）、L2（数字键 testTag）、L3（防重复提交测试）、L4（完成按钮圆角）、L5（边界测试），JVM 测试全部通过，BUILD SUCCESSFUL |

## Bug 与遗留问题

### BUG-001：RecordContent 中 Categories.byType() 绕过 ViewModel 数据流

- 来源：agent_found（code-reviewer）
- 状态：resolved
- 严重级别：medium
- 描述：RecordContent 直接调用 Categories.byType(uiState.type)，违反「Composable 不含业务逻辑」约定
- 位置：ui/record/RecordContent.kt
- 解决方案：RecordUiState 增加 categories 字段，SelectType/PlusMinus 时 ViewModel 同步更新
- 创建时间：2026-06-24
- 更新时间：2026-06-24

### BUG-002：AmountKeyboard 硬编码字体大小

- 来源：agent_found（code-reviewer）
- 状态：resolved
- 严重级别：medium
- 描述：数字键/功能键使用硬编码 sp 值，违反 Design System 字体 token 规范
- 位置：ui/record/AmountKeyboard.kt
- 解决方案：替换为 MaterialTheme.typography.titleLarge/titleMedium
- 创建时间：2026-06-24
- 更新时间：2026-06-24

## 开发记录

| 日期 | 内容 |
| --- | --- |
| 2026-06-24 | 方案生成（场景 B）：规划 RecordContent 全屏布局、RecordViewModel MVI 契约（含 AmountKey）、金额字符串拼接规则、保存校验与 insertBill 集成、M6 DatePickerDialog 集成、AppNavHost 接线、单测计划；A8 决策为「+/− 切换 type」 |
| 2026-06-24 | 实现完成（场景 C）：MoneyFormatter 金额拼接函数、DateUtil.formatRecordDate、RecordUiState/Intent/Effect、RecordViewModel、CategoryGrid、AmountKeyboard、RecordContent/Screen/Route 全部新建，AppNavHost 接线，strings.xml 追加 9 条文案，JVM 测试全部通过，BUILD SUCCESSFUL |
| 2026-06-24 | 代码审查修复（场景 D）：修复 M1（RecordUiState 增加 categories 字段，ViewModel 同步更新）、M2（AmountKeyboard 字体改用 MaterialTheme.typography token）、L2（数字键/点键加 testTag）、L3（补充防重复提交测试用例）、L4（完成按钮加 clip 圆角）、L5（amountInputToCents("0.") 边界测试），JVM 测试全部通过，BUILD SUCCESSFUL |
