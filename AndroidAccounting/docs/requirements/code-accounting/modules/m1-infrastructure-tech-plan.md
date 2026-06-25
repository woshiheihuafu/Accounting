# M1 基础设施模块技术方案

## 模块目标

为 CodeAccounting Android 端搭建全部业务模块共享的横切基础设施，使后续 M2~M6 能在统一的 DI、导航、视觉与公共组件基座上开发。本模块不实现任何业务功能，只交付「可编译、可预览、可被依赖」的工程骨架。

具体目标：

1. 接入 Hilt 依赖注入框架，提供 `@HiltAndroidApp` 应用入口和 DI Module 占位。
2. 接入 Navigation Compose（type-safe 路由），搭建 `AppNavHost` 与两条路由（Home / Record）。
3. 基于「墨与纸」Design Token 重写 Material3 Theme（Color / Typography / Shape）。
4. 实现通用公共组件基座：`WheelPicker`、`BottomSheetPicker`、`AccountingBottomBar`。
5. 完成 Gradle 依赖链（Hilt + KSP + Navigation + Serialization + Room 占位 + Lifecycle）配置并跑通编译。

## 范围

- `libs.versions.toml` 依赖声明（含 M2 才使用的 Room，提前声明但不初始化）。
- 根级与 `app` 级 Gradle 配置：应用 hilt / ksp / kotlin-serialization 插件，添加依赖。
- `AndroidManifest.xml`：`android:name` 指向 `AccountingApplication`。
- `AccountingApplication`（`@HiltAndroidApp`）与 `MainActivity`（`@AndroidEntryPoint` + NavHost 入口）。
- 导航：`Route`（sealed interface）+ `AppNavHost`。
- DI 占位：`DatabaseModule`、`RepositoryModule`（仅 `@Module/@InstallIn` 骨架 + 注释，实现留给 M2）。
- 墨与纸 Design System：`Color.kt`、`Type.kt`、`Shape.kt`、`Theme.kt`。
- 公共组件：`WheelPicker.kt`、`BottomSheetPicker.kt`、`AccountingBottomBar.kt`。
- 字体资源目录 `res/font/` 的占位约定。

## 非目标

- 不实现 Room 数据库实体、DAO、Repository 实现（归属 M2）。
- 不实现首页、记账页、年月/日期选择器的业务内容（归属 M3~M6）。
- 不实现网络层（首期无网络层，整体方案已确认）。
- 不实现图表、发现、我的的真实功能（仅底部 Bar 视觉入口 + Toast「即将推出」占位）。
- 不实现类目静态数据、金额格式化等数据/领域逻辑（归属 M2/M5）。
- 不嵌入最终图标资产；线性图标重绘为独立设计产出，开发期可用 Material Icons 占位（见整体方案 R8）。

## 依赖关系

- 上游依赖：无（M1 是所有模块的根基础）。
- 下游被依赖：M2~M6 全部依赖 M1（DI、Theme、Navigation、WheelPicker 基座）。
- 模块内依赖方向（单模块按包分层，逻辑约定）：
  - `ui/component` → `ui/theme`
  - `navigation` → `ui`（路由目的地占位）
  - `di` 仅声明骨架，不引用业务实现（M2 填充时再连接 `data` 层）。

### libs.versions.toml 依赖（与整体方案对齐）

```toml
[versions]
hilt = "2.56.2"
navigationCompose = "2.9.0"
room = "2.7.1"
ksp = "2.2.10-1.0.32"
lifecycleRuntimeCompose = "2.10.0"
kotlinxSerialization = "1.8.1"

[libraries]
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version = "1.2.0" }
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigationCompose" }
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycleRuntimeCompose" }
lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeCompose" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerialization" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

说明：Room 依赖（`room-runtime` / `room-ktx` / `room-compiler`）在 M1 阶段添加到 Gradle 但**不初始化**（不写 `@Database` / `@Dao`），作为占位，由 M2 填充实现。`hilt-compiler` 与 `room-compiler` 统一通过 `ksp(...)` 接入，不使用 kapt。

## UI 方案

M1 不含业务页面，只交付 Design System 与公共组件。

### Design System Token（墨与纸）

| Token 名 | 含义 | Light | Dark |
| --- | --- | --- | --- |
| PaperWhite | 背景主色 | `#FAFAF7` | `#1A1A1A` |
| InkBlack | 主要文字 | `#1A1A1A` | `#FAFAF7` |
| SageGreen | 强调色 | `#7A9E87` | `#7A9E87` |
| SurfaceGray | 次要背景（卡片） | `#F0F0EC` | `#262626`（建议值，待设计确认） |
| DividerGray | 分割线 | `#E0E0DB` | `#333330`（建议值，待设计确认） |
| ExpenseRed | 支出金额 | `#C0392B` | `#C0392B` |
| IncomeGreen | 收入金额 | `#27AE60` | `#27AE60` |

映射到 Material3 `ColorScheme`：

- `background` / `surface` ← PaperWhite（Dark 为 InkBlack）
- `onBackground` / `onSurface` ← InkBlack（Dark 为 PaperWhite）
- `primary` ← SageGreen，`onPrimary` ← PaperWhite
- `surfaceVariant` ← SurfaceGray
- `outline` / `outlineVariant` ← DividerGray
- 业务语义色（ExpenseRed / IncomeGreen）通过自定义扩展（`AccountingColors` data class + `CompositionLocal`）提供，不挤占 Material 语义槽位。

> 设计输入待确认：SurfaceGray / DividerGray 的 Dark 变体未在交接 Token 中给出明确值，本方案给出建议值并标记为需设计确认（见「设计输入问题」DI-M1-1）。

### Typography 策略

- 正文系列：Noto Serif，需嵌入 `res/font/noto_serif_regular.ttf`、`noto_serif_medium.ttf`、`noto_serif_bold.ttf`。若开发阶段暂无字体文件，先用系统默认 serif fallback（`FontFamily.Serif`），后续替换。
- 金额专用等宽字体：JetBrains Mono（仅数字子集 `res/font/jetbrains_mono_regular.ttf`）。
- TextStyle 命名与用途：

| TextStyle | 用途 | 字体 |
| --- | --- | --- |
| `titleLarge` | 页面主标题 | Noto Serif |
| `titleMedium` | 区块标题 | Noto Serif |
| `bodyLarge` | 主要正文 | Noto Serif |
| `bodyMedium` | 次要正文 | Noto Serif |
| `bodySmall` | 辅助说明 | Noto Serif |
| `labelMedium` | 标签 / 按钮文字 | Noto Serif |
| `amountDisplay` | 金额专用（等宽） | JetBrains Mono |

`amountDisplay` 不属于 Material3 标准 Typography 槽位，通过 `AccountingTypography` 扩展或 `CompositionLocal` 暴露。

### Shape 策略

定义统一圆角：`small`（卡片内元素，8dp）、`medium`（卡片，12dp）、`large`（底部弹窗顶部圆角，16dp）。具体数值集中定义在 `Shape.kt`，禁止业务代码散落硬编码圆角。

### 公共组件

**WheelPicker**（无第三方依赖）

- 实现基础：`LazyColumn` + `rememberLazyListState` + `snapFlingBehavior`（`rememberSnapFlingBehavior(lazyListState)`）。
- 渐隐效果：`Modifier.graphicsLayer` + `BlendMode.DstIn`（上下渐变遮罩）实现两端淡出；如遇兼容性问题，降级为基于 item 与中心距离的简单透明度渐变。
- 选中态：中心高亮（结合 Theme 强调色 / 字号放大），通过 `derivedStateOf` 由 `firstVisibleItemIndex` + offset 推导中心索引，避免组合期重计算。
- API：

```kotlin
@Composable
fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
)
```

- 设计为无状态、可复用，M4（双列年/月）与 M6（三列年/月/日）只做配置组合，不重复实现滚轮逻辑。

**BottomSheetPicker**

- 底部弹窗容器，统一承载 WheelPicker 组合内容（标题栏 + 取消/确定 + 内容槽）。
- 基于 Material3 `ModalBottomSheet`，顶部圆角取 Shape `large`，背景取 PaperWhite。
- API 提供内容 slot（`content: @Composable () -> Unit`）、`onDismiss`、`onConfirm` 回调，由 M4/M6 复用。

**AccountingBottomBar**

- 基于 Material3 `NavigationBar` + `NavigationBarItem`。
- 五个入口：明细（Home）、图表（Chart）、记账（Record）、发现（Discover）、我的（Profile）。
- 仅 Home 和 Record 可跳转；其余三个点击触发 Toast「即将推出」（`LENGTH_SHORT` 约 2 秒，首期近似 1.5 秒，见 DI-M1-2）。
- 选中态使用 SageGreen 强调；图标开发期用 Material Icons 占位，后续替换为线性重绘资源（整体方案 R8）。
- API：

```kotlin
@Composable
fun AccountingBottomBar(
    currentRoute: Route,
    onNavigate: (Route) -> Unit,
    onComingSoon: () -> Unit,
    modifier: Modifier = Modifier,
)
```

## ViewModel 方案

M1 无 ViewModel。基础设施层不承载页面状态。`lifecycle-viewmodel-compose` 与 `hilt-navigation-compose` 依赖在 M1 配置，为后续 `hiltViewModel()` 注入提供基座。

## Repository 方案

M1 无 Repository 实现。仅在 `RepositoryModule` 中预留 `@Binds` 绑定占位（注释说明），实际绑定由 M2 填充。

## Network 方案

无。首期无网络层（整体方案架构原则第 8 条）。

## 模块架构方案

遵循 `AndroidAccounting/AGENTS.md` 分层与整体方案目标包结构，M1 落地以下包：

```text
com.cocos.androidaccounting
├── AccountingApplication.kt        # @HiltAndroidApp
├── MainActivity.kt                 # @AndroidEntryPoint, setContent { AccountingTheme { AppNavHost() } }
├── navigation/
│   ├── Route.kt                    # sealed interface Route: Home, Record
│   └── AppNavHost.kt               # NavHost, startDestination = Home
├── di/
│   ├── DatabaseModule.kt           # @Module @InstallIn(SingletonComponent::class) 占位
│   └── RepositoryModule.kt         # @Module @InstallIn(SingletonComponent::class) @Binds 占位
└── ui/
    ├── theme/
    │   ├── Color.kt                # 墨与纸 Token + AccountingColors 扩展
    │   ├── Type.kt                 # Typography + amountDisplay
    │   ├── Shape.kt                # Shapes
    │   └── Theme.kt                # AccountingTheme(light/dark)
    └── component/
        ├── WheelPicker.kt
        ├── BottomSheetPicker.kt
        └── AccountingBottomBar.kt
```

依赖方向（逻辑约定）：`navigation` / `ui.component` 依赖 `ui.theme`；`di` 不反向依赖 `ui`。

### Navigation 路由设计

```kotlin
sealed interface Route {
    @Serializable data object Home : Route
    @Serializable data object Record : Route
}
```

- 使用 kotlinx.serialization 的 type-safe navigation（Navigation Compose 2.9+）。
- `AppNavHost`：`startDestination = Route.Home`，注册 Home / Record 两条 `composable`，目的地内容占位（M3/M5 填充）。

### Hilt 接入

- `AccountingApplication` 标注 `@HiltAndroidApp`。
- `MainActivity` 标注 `@AndroidEntryPoint`。
- `DatabaseModule`：含 `// TODO(M2): provide AppDatabase / BillDao` 占位注释，不写 `@Provides` 实体。
- `RepositoryModule`：含 `// TODO(M2): @Binds BillRepository -> BillRepositoryImpl` 占位注释。

## 状态与副作用模型

M1 无 MVI 状态机。`AccountingBottomBar` 占位入口 Toast 由调用方（M3 Route 层）持有 Context 触发，组件通过 `onComingSoon` 回调上抛。M1 不引入任何 `UiState/UiIntent/UiEffect`。

## 文件变更计划

> **硬性门禁（实现顺序）**：先完成 `gradle/libs.versions.toml` + 根级与 `app` 级 Gradle 配置，执行 `./gradlew assembleDebug` 编译通过后，再编写 Theme / 组件 / 导航等业务基座代码。依赖链未跑通前不写后续代码。

| 文件 | 操作 | 说明 |
| --- | --- | --- |
| `gradle/libs.versions.toml` | 修改 | 添加 versions / libraries / plugins 条目 |
| `build.gradle.kts`（根级） | 修改 | 声明 hilt / ksp / kotlin-serialization 插件（`apply false`） |
| `app/build.gradle.kts` | 修改 | 应用 hilt / ksp / kotlin-serialization 插件；添加全部依赖 |
| `app/src/main/AndroidManifest.xml` | 修改 | `android:name` 改为 `.AccountingApplication` |
| `com/cocos/androidaccounting/AccountingApplication.kt` | 新建 | `@HiltAndroidApp class AccountingApplication : Application()` |
| `com/cocos/androidaccounting/MainActivity.kt` | 修改 | 加 `@AndroidEntryPoint`，`setContent { AccountingTheme { AppNavHost() } }` |
| `navigation/Route.kt` | 新建 | sealed interface Route + Home/Record（`@Serializable`） |
| `navigation/AppNavHost.kt` | 新建 | NavHost + type-safe 路由注册，目的地占位 |
| `di/DatabaseModule.kt` | 新建 | `@Module @InstallIn(SingletonComponent::class)` 占位 |
| `di/RepositoryModule.kt` | 新建 | `@Module @InstallIn(SingletonComponent::class)` `@Binds` 占位 |
| `ui/theme/Color.kt` | 修改 | 替换默认 Purple/Pink 为墨与纸 Token + AccountingColors 扩展 |
| `ui/theme/Type.kt` | 修改 | Typography 策略（含 amountDisplay 等宽） |
| `ui/theme/Shape.kt` | 新建 | small/medium/large 圆角 |
| `ui/theme/Theme.kt` | 修改 | AccountingTheme（light/dark colorScheme + 关闭 Dynamic Color） |
| `ui/component/WheelPicker.kt` | 新建 | 通用滚轮选择器 |
| `ui/component/BottomSheetPicker.kt` | 新建 | 底部弹窗容器 |
| `ui/component/AccountingBottomBar.kt` | 新建 | 底部导航栏（五入口 + Toast 占位） |
| `app/src/main/res/font/` | 新建目录 | Noto Serif 与 JetBrains Mono 字体文件占位；无文件时正文 fallback 系统 serif |

## 测试计划

M1 无 ViewModel / Repository，不写 JUnit 单元测试。验证以编译与可预览为主：

- 验证点 1（硬性门禁）：`./gradlew assembleDebug` 编译通过。
- 验证点 2：Theme Preview（`@Preview` 函数）在 Android Studio 可正常渲染。
- 验证点 3：Hilt Component 生成无报错。

补充建议（非强制）：为 `WheelPicker` 预留 `TestTag`（如 `wheel_picker`），为底部 Bar item 预留稳定 tag，方便后续 UI 测试。

## 设计输入问题

| # | 来源 | 问题 | 处理策略 |
| --- | --- | --- | --- |
| DI-M1-1 | Design Token 交接 | SurfaceGray / DividerGray 未给出 Dark 变体明确值 | 给出建议值（`#262626` / `#333330`），标记为需设计确认，确认前用建议值 |
| DI-M1-2 | PRD 占位入口 Toast | 「即将推出」要求 1.5 秒，`Toast.LENGTH_SHORT` 约 2 秒 | 首期用 `LENGTH_SHORT` 近似；如需精确 1.5 秒由 M3 自定义实现 |
| DI-M1-3 | 字体资产 | Noto Serif / JetBrains Mono 字体文件与许可尚未落地 | 开发期 fallback 系统 serif，正式资源就位后替换；仅嵌入 JetBrains Mono 数字子集 |
| DI-M1-4 | 图标资产 | 底部 Bar 五入口图标须线性重绘、不复制受保护素材 | 开发期用 Material Icons 占位，设计产出后替换（整体方案 R8） |

## 审批状态

`skipped_by_user`

> 用户明确要求「编写完成后开始开发」，按 workflow 审批模型跳过审批，方案完成后直接进入场景 C 实现。

## 进度记录

| 日期 | 状态 | 说明 |
| --- | --- | --- |
| 2026-06-24 | technical_plan_created | M1 基础设施模块技术方案生成；审批 skipped_by_user，待实现 |
| 2026-06-24 | implemented | M1 全部基础设施代码实现完成；`./gradlew assembleDebug` 编译通过 |
| 2026-06-24 | in_progress → completed | 修复代码审查 BUG-001/002/003，编译通过，模块完成 |
| 2026-06-24 | completed → completed_with_issues | 用户报告 INTAKE-006：WheelPicker 轮盘居中问题，登记 BUG-004，待修复 |
| 2026-06-24 | completed_with_issues → completed | 修复 BUG-004：WheelPicker 添加首尾空占位项（EdgePadding=2），边界项可居中；`assembleDebug` BUILD SUCCESSFUL |

## Bug 与遗留问题

### BUG-001：WheelPicker 缺少 snapFlingBehavior

- 来源：agent_found（code-reviewer）
- 状态：resolved
- 严重级别：medium
- 描述：方案要求 `rememberSnapFlingBehavior(lazyListState)` 实现滚轮吸附，初版实现仅靠 `LaunchedEffect` + `animateScrollToItem` 补吸附，快速滑动会停在两个 item 之间
- 位置：`ui/component/WheelPicker.kt`
- 解决方案：添加 `flingBehavior = rememberSnapFlingBehavior(lazyListState)`；`LaunchedEffect` 改为在 `!isScrollInProgress` 时触发
- 创建时间：2026-06-24
- 更新时间：2026-06-24

### BUG-002：BottomSheetPicker / AccountingBottomBar 硬编码用户可见字符串

- 来源：agent_found（code-reviewer）
- 状态：resolved
- 严重级别：medium
- 描述：「取消」「确定」及底部 Bar 五个导航标签和 contentDescription 直接硬编码在代码中，违反 AGENTS.md 国际化规范
- 位置：`ui/component/BottomSheetPicker.kt`、`ui/component/AccountingBottomBar.kt`
- 解决方案：抽取到 `res/values/strings.xml`，改用 `stringResource(R.string.*)` 引用
- 创建时间：2026-06-24
- 更新时间：2026-06-24

### BUG-003：junit SNAPSHOT 版本 / WheelPicker index-based key / BottomBar items 未 remember

- 来源：agent_found（code-reviewer）
- 状态：resolved
- 严重级别：low
- 描述：libs.versions.toml 中 junit 版本为 SNAPSHOT；WheelPicker itemsIndexed key 使用 index；AccountingBottomBar items 列表每次重组重建
- 位置：`gradle/libs.versions.toml`、`ui/component/WheelPicker.kt`、`ui/component/AccountingBottomBar.kt`
- 解决方案：junit 改为 4.13.2；key 改为 item 内容；items 包裹 `remember`
- 创建时间：2026-06-24
- 更新时间：2026-06-24

### BUG-004：WheelPicker 选中项未居中 / 边界项无法滚到中部

- 来源：user_reported
- 状态：resolved
- 严重级别：medium
- 描述：M1 方案要求 WheelPicker「中心高亮」。当前实现无上下占位，`animateScrollToItem` 将选中项对齐到可见区顶部而非中间行；边界项（如今年、当月末、当日）无下方可滑内容，无法滚入视口正中。
- 位置：`ui/component/WheelPicker.kt`（`initialFirstVisibleItemIndex`、`animateScrollToItem`、无 `contentPadding`）
- 解决方案：在 `WheelPicker.kt` 内部添加 `EdgePadding = VisibleItemCount / 2 = 2`；`LazyColumn` 渲染 `paddedSize = items.size + EdgePadding * 2` 个条目，前后各 2 个空 `Box` 占位；`items[i]` 对应 `paddedIndex = i + EdgePadding`，当 `firstVisibleItemIndex = k`，视口中心 `paddedIndex = k + EdgePadding = items[k]`，`+EdgePadding` 与 `-EdgePadding` 相消，`centerIndex` 公式保持 `firstVisibleItemIndex`（含半格偏移修正），`initialFirstVisibleItemIndex` 和 `animateScrollToItem` 均传 `selectedIndex`（无需加减偏移）；边界项（第 1 项和末项）也能准确居中。调用方 `YearMonthPickerDialog` / `DatePickerDialog` 无需任何修改。`./gradlew assembleDebug` BUILD SUCCESSFUL（用时 ~8s）。
- 创建时间：2026-06-24
- 更新时间：2026-06-24

### LEFT-001：等宽金额字体未嵌入自定义字体

- 类型：deferred
- 状态：deferred
- 严重级别：low
- 描述：技术方案 R2 推荐嵌入 DIN/JetBrains Mono，当前实现使用系统 `FontFamily.Monospace` 替代，未嵌入自定义字体文件
- 位置：`ui/theme/Type.kt`
- 触发 / 偿还条件：需要像素级对齐设计稿等宽数字、或产品要求统一品牌字体时再嵌入子集字体
- 关联：风险 R2；REQ-ACCOUNTING-005
- 创建时间：2026-06-24
- 更新时间：2026-06-24

> 已知风险继承自整体方案：R1（WheelPicker 自研）、R2（金额等宽字体嵌入）、R5（Hilt/Room/Navigation 在 Kotlin 2.2.10 + AGP 9.x 的兼容性，由硬性门禁拦截）、R8（图标资产合规）。

## 开发记录

### 实现摘要（2026-06-24）

按技术方案完成全部 16 个文件的创建/修改，`./gradlew assembleDebug` 编译通过（BUILD SUCCESSFUL）。

### 实际依赖版本调整

| 依赖 | 方案版本 | 实际版本 | 原因 |
| --- | --- | --- | --- |
| `ksp` | `2.2.10-1.0.32` | `2.2.10-2.0.2` | KSP 版本格式已更新，`1.0.x` 系列不存在对应 Kotlin 2.2.10 的版本 |
| `hilt` | `2.56.2` | `2.59.2` | Hilt 2.56.x 不兼容 AGP 9.x（`BaseExtension` 已移除），2.59.2 正式支持 |
| `coreKtx` | `1.19.0` | `1.16.0` | `core-ktx:1.19.0` 要求 compileSdk 37，项目固定在 SDK 36.1 |

### 额外配置

- `gradle.properties` 添加 `android.disallowKotlinSourceSets=false`：Hilt + KSP + AGP 9.x 兼容性所需（实验性标志）。
- 移除 `kotlin-android` 插件：与 AGP 9.x 的内置 Kotlin 支持冲突（"Cannot add extension 'kotlin'"）。
- 移除 `kotlinOptions { jvmTarget = "11" }`：该块已在 AGP 9 中删除。
- `material-icons-extended` 依赖：底部导航栏使用了 extended 图标集（PieChart、AddCircle、Explore 等）。
- `BottomSheetPicker` 的 `rememberModalBottomSheetState` 移除 `skipPartialExpansion` 参数：M3 1.4.0（BOM 2026.02.01）中该参数已移除。

### 验证结果

- `./gradlew assembleDebug`：BUILD SUCCESSFUL（首次通过用时约 2 分钟，包含完整依赖下载）。
- Hilt 组件生成无报错（`hiltAggregateDepsDebug` / `hiltJavaCompileDebug` 任务均成功）。
- KSP 处理无报错（`kspDebugKotlin` 任务成功）。

### 代码审查与修复（2026-06-24）

发现 3 medium + 3 low 问题，全部已修复（见「Bug 与遗留问题」BUG-001/002/003）：

- **BUG-001**：`WheelPicker` 添加 `flingBehavior = rememberSnapFlingBehavior(listState)`；`LaunchedEffect` 改为 `!isScrollInProgress` 时触发
- **BUG-002**：`BottomSheetPicker`「取消」「确定」、`AccountingBottomBar` 五个导航标签和 contentDescription 全部抽取到 `res/values/strings.xml`，改用 `stringResource` 引用；`BottomBarItem` 重构为 `@param:StringRes labelRes/descRes`
- **BUG-003**：`libs.versions.toml` junit 改为 `4.13.2`；`WheelPicker` key 改为 item 内容；`AccountingBottomBar` items 包裹 `remember`

修复后 `./gradlew assembleDebug` BUILD SUCCESSFUL（用时约 8s，无 warning）。
