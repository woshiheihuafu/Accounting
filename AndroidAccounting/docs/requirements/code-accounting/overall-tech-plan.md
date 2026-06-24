# CodeAccounting Android 端整体技术方案

## 需求概述

产品名称：Code记账（CodeAccounting）

首期目标：提供简洁、快速的**本地记账** MVP，核心能力为「查看当月明细」+「快速记一笔」。

视觉方案：墨与纸——纸白底、墨黑排版、鼠尾草绿点缀，以排版节奏和留白构建层级。

功能范围：

| REQ ID | 优先级 | 摘要 |
| --- | --- | --- |
| REQ-ACCOUNTING-001 | P0 | 首页展示选中年月、收入合计、支出合计和明细列表；年月切换后同步刷新 |
| REQ-ACCOUNTING-002 | P0 | 底部 Bar 五入口（明细、图表、记账、发现、我的），仅明细和记账可用，其余占位 |
| REQ-ACCOUNTING-003 | P0 | 支出记账：餐饮、购物、娱乐、日用四类目 → 金额录入 |
| REQ-ACCOUNTING-004 | P0 | 收入记账：工资、理财、礼金、其他四类目 → 金额录入 |
| REQ-ACCOUNTING-005 | P0 | 金额键盘：金额、备注、日期、删除、加减号、完成保存 |
| REQ-ACCOUNTING-006 | P0 | 底部弹窗滚轮式年月选择器和日期选择器 |

页面覆盖：SCREEN-ACCOUNTING-V2-001 ~ V2-006（6 个页面）

流程覆盖：FLOW-ACCOUNTING-V2-001 ~ V2-003（3 条主流程）

明确非目标：

- 账号、登录、注册、登出、云同步、跨设备同步
- 首页快捷入口（账单、预算、资产管家、购物返现、更多）
- 登录提示条
- 预算、资产、返现、报表、发现内容、我的设置
- 图表/发现/我的真实功能（仅保留 Bar 视觉入口，点击 Toast「即将推出」1.5 秒）
- 100% 复刻竞品视觉；图标须重绘

## 来源文档

| 文档 | 路径 | 说明 |
| --- | --- | --- |
| PRD | `prd/code-accounting-prd-20260623.md` | PRD-CODE-ACCOUNTING-20260623，墨与纸视觉方案 |
| 设计稿 | `prd/designs/code-accounting-v2.pen` | DESIGN-ACCOUNTING-002 |
| 需求索引 | `prd/requirements/README.md` | REQ-ACCOUNTING-001 ~ 006 |
| 设计索引 | `prd/designs/README.md` | SCREEN-V2-001 ~ 006, FLOW-V2-001 ~ 003 |
| Android 规范 | `AndroidAccounting/AGENTS.md` | 架构、MVI、Compose、命名约定 |
| Workflow Skill | `.codex/skills/android-feature-workflow/SKILL.md` | 场景 A 章节模板和状态模型 |

## 方案生成时 Android 工程快照

快照时间：2026-06-24

```text
AndroidAccounting/
├── AGENTS.md
├── build.gradle.kts          # 根级 Gradle，插件声明
├── settings.gradle.kts       # 仅 include(":app")
├── gradle/
│   ├── libs.versions.toml    # Version Catalog
│   └── wrapper/              # Gradle 9.4.1
└── app/
    ├── build.gradle.kts      # 应用模块配置
    └── src/main/java/com/cocos/androidaccounting/
        ├── MainActivity.kt   # 空白脚手架入口
        └── ui/theme/
            ├── Color.kt      # 默认 Purple/Pink 色
            ├── Theme.kt      # Material3 + Dynamic Color
            └── Type.kt       # 仅 bodyLarge 定制
```

关键参数：

| 项 | 值 |
| --- | --- |
| applicationId | `com.cocos.androidaccounting` |
| minSdk | 29 |
| targetSdk / compileSdk | 36 |
| Gradle | 9.4.1 |
| AGP | 9.2.1 |
| Kotlin | 2.2.10（含 Compose Compiler Plugin） |
| Compose BOM | 2026.02.01 |
| JDK | 11（compileOptions）/ 21（Daemon toolchain） |

当前状态：单模块 `:app` 空白脚手架。无 DI 框架、无导航框架、无本地数据库、无网络层、无业务代码。Theme 为 Android Studio 默认 Material3 模板。

## 技术栈

| 层 | 选型 | 说明 |
| --- | --- | --- |
| 语言 | Kotlin 2.2.10 | 项目已配置 |
| UI 框架 | Jetpack Compose + Material3 | BOM 2026.02.01 已配置 |
| 架构模式 | MVI | UiState（data class + StateFlow）/ UiIntent（sealed interface）/ UiEffect（SharedFlow） |
| DI | Hilt | AGENTS.md 推荐 `@Inject` 构造注入；需新增依赖 |
| 导航 | Navigation Compose | Type-safe routes；需新增依赖 |
| 本地存储 | Room | Bill 实体持久化；需新增依赖 |
| 异步 | Kotlin Coroutines + Flow | lifecycle-runtime-ktx 已配置 |
| 状态收集 | `collectAsStateWithLifecycle()` | 需新增 `lifecycle-runtime-compose` 依赖 |
| Design System | 自定义 Material3 Theme | 基于墨与纸 Design Token 重写 Color/Typography/Shape |
| 金额字体 | 嵌入等宽字体 | DIN Alternate / JetBrains Mono 等开源替代 |
| 序列化 | kotlinx.serialization | Navigation type-safe routes 需要 |

需新增的 `libs.versions.toml` 依赖：

```toml
[versions]
hilt = "2.56.2"              # 或最新稳定版
navigationCompose = "2.9.0"
room = "2.7.1"
ksp = "2.2.10-1.0.32"       # 匹配 Kotlin 版本
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

## 架构原则

遵循 `AndroidAccounting/AGENTS.md` 定义的分层架构：

```text
UI (Compose)
    ↓ UiIntent
ViewModel (MVI)
    ↓ suspend / Flow
Repository
    ↓
DataSource (Local)
    ↓
Room Database
```

核心约束：

1. UI 不直接访问数据库或网络。
2. ViewModel 不创建 Repository；通过 Hilt 构造注入。
3. Repository 是数据访问的唯一入口，管理缓存、刷新和回退策略。
4. DataSource 仅处理数据读写。
5. Compose 页面结构：Route → Screen → Content。
6. 状态上提、事件下发（State up, events down）。
7. 不混用 DTO、Entity 和 UI Model；遵循 Entity → Mapper → Domain → UI 映射链（首期无网络层，无 DTO）。
8. 首期无网络层，DataSource 仅包含 Local（Room）。
9. 单模块按包分层下，模块间依赖方向为逻辑约定（非编译器强制），需通过代码审查保证。后续拆 Gradle 模块时，包边界即为模块边界。

### 数据模型

Bill 实体（与 PRD 7 字段对齐）：

| 字段 | Domain 类型 | Room 存储类型 | 说明 |
| --- | --- | --- | --- |
| id | `Long` | `Long`（`@PrimaryKey autoGenerate`） | 账单唯一 ID |
| type | `BillType` enum（`EXPENSE` / `INCOME`） | `String`（TypeConverter） | 账单类型 |
| category | `String` | `String` | 类目名称 |
| amount | `Long` | `Long` | 金额，以**分**为单位；支出存正值，收入存正值 |
| date | `LocalDate` | `Long`（epochDay，TypeConverter） | 账单日期 |
| remark | `String` | `String` | 备注，可为空 |
| createdAt | `Instant` | `Long`（epochMillis，TypeConverter） | 创建时间 |

金额存储策略：

- 统一使用 `Long`（分）存储，避免浮点精度问题，Room 原生支持。
- UI 层格式化为 `¥xx.xx` 显示。
- **amount 始终存正值**，正负由 `type` 字段决定：`type = EXPENSE` 时 UI 展示为 `-¥xx`，`type = INCOME` 时展示为 `+¥xx`。消除 amount 符号与 type 的冗余。
- 汇总规则归属 Repository：收入合计 = `SUM(amount) WHERE type = INCOME`；支出合计 = `SUM(amount) WHERE type = EXPENSE`；Repository 返回格式化后的 Domain Model，ViewModel 直接使用。
- 明细按 `date` 倒序分组，同日内按 `createdAt` 倒序。

TypeConverter 策略：

| 类型 | 转换方式 |
| --- | --- |
| `BillType` enum | `String` ↔ `name()` / `valueOf()` |
| `LocalDate` | `Long` ↔ `toEpochDay()` / `ofEpochDay()` |
| `Instant` | `Long` ↔ `toEpochMilli()` / `ofEpochMilli()` |

类目数据归属：

- 支出类目（餐饮、购物、娱乐、日用）和收入类目（工资、理财、礼金、其他）为静态配置，定义在 `data/model/Category.kt`。
- 类目线性图标为 Vector Drawable 资源，放在 `res/drawable/`，命名规则：`ic_category_<name>.xml`。
- 图标须重绘为线条风格（stroke 1.5px、不填充），不复制竞品受保护素材。

## 模块化策略

首期功能范围较小（6 个页面、1 个数据实体），在 `:app` 模块内按包分层，不拆分 Gradle 模块。后续功能增长时可按 feature module 拆分。

目标包结构：

```text
com.cocos.androidaccounting
├── AccountingApplication.kt           # @HiltAndroidApp
├── MainActivity.kt                    # @AndroidEntryPoint, NavHost 入口
│
├── navigation/
│   ├── AppNavHost.kt                  # NavHost 和路由注册
│   └── Route.kt                      # sealed interface, type-safe 路由定义
│
├── di/
│   ├── DatabaseModule.kt             # @Module, 提供 Room DB 和 DAO
│   └── RepositoryModule.kt           # @Module, @Binds BillRepository → BillRepositoryImpl
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt            # @Database
│   │   ├── BillDao.kt                # @Dao
│   │   └── BillEntity.kt             # @Entity
│   ├── local/converter/
│   │   └── RoomConverters.kt         # BillType/LocalDate/Instant TypeConverters
│   ├── mapper/
│   │   └── BillMapper.kt             # Entity ↔ Domain 映射
│   ├── model/
│   │   ├── Bill.kt                   # Domain model
│   │   ├── BillType.kt               # enum: EXPENSE, INCOME
│   │   └── Category.kt               # 静态类目定义（支出/收入各 4 类）
│   └── repository/
│       ├── BillRepository.kt         # Interface
│       └── BillRepositoryImpl.kt     # @Inject 实现
│
├── ui/
│   ├── theme/
│   │   ├── Color.kt                  # 墨与纸色彩 Token
│   │   ├── Type.kt                   # 字体策略（含等宽金额字体）
│   │   ├── Shape.kt                  # 圆角定义
│   │   └── Theme.kt                  # AccountingTheme
│   │
│   ├── component/
│   │   ├── WheelPicker.kt            # 通用滚轮选择器
│   │   ├── BottomSheetPicker.kt      # 底部弹窗容器
│   │   └── AccountingBottomBar.kt    # 底部导航栏
│   │
│   ├── home/
│   │   ├── HomeRoute.kt              # Navigation entry + Effect collection
│   │   ├── HomeScreen.kt             # State dispatch + Event forwarding
│   │   ├── HomeContent.kt            # Stateless 明细列表渲染
│   │   ├── HomeViewModel.kt          # MVI ViewModel
│   │   ├── HomeUiState.kt            # data class
│   │   ├── HomeUiIntent.kt           # sealed interface
│   │   ├── HomeUiEffect.kt           # sealed interface
│   │   └── YearMonthPickerDialog.kt  # 年月选择弹窗
│   │
│   └── record/
│       ├── RecordRoute.kt
│       ├── RecordScreen.kt
│       ├── RecordContent.kt
│       ├── RecordViewModel.kt
│       ├── RecordUiState.kt
│       ├── RecordUiIntent.kt
│       ├── RecordUiEffect.kt
│       ├── CategoryGrid.kt           # 类目网格
│       ├── AmountKeyboard.kt         # 金额键盘
│       └── DatePickerDialog.kt       # 日期选择弹窗
│
└── util/
    └── DateUtil.kt                   # 日期格式化工具
```

## 需求模块划分

| 模块 | 覆盖需求 | 覆盖页面 | 覆盖流程 | 职责 |
| --- | --- | --- | --- | --- |
| M1: 基础设施 | 全部（横切） | — | — | Hilt DI 配置、Navigation 框架、墨与纸 Design System Theme、Application 类、公共组件基座 |
| M2: 数据层 | REQ-001（数据支撑） | — | — | Room 数据库、BillEntity / BillDao、BillRepository 接口与实现、Domain Model、Mapper |
| M3: 首页明细 | REQ-001, REQ-002 | SCREEN-V2-001 | FLOW-V2-001 | 首页月份显示/收支汇总/明细列表、底部 Bar（含占位 Toast）、HomeViewModel |
| M4: 年月选择器 | REQ-006 | SCREEN-V2-002 | — | 底部弹窗双列滚轮年月选择组件 |
| M5: 记账页 | REQ-003, REQ-004, REQ-005 | SCREEN-V2-003 ~ V2-005 | FLOW-V2-002, V2-003 | 支出/收入 Tab 切换、类目网格、金额键盘、备注输入、保存逻辑、RecordViewModel |
| M6: 日期选择器 | REQ-006 | SCREEN-V2-006 | — | 底部弹窗三列滚轮日期选择组件 |

## 模块依赖关系

```text
M3: 首页明细 ──→ M2: 数据层 ──→ M1: 基础设施
     │                              ↑
     └──→ M4: 年月选择器 ───────────┘
                                    ↑
M5: 记账页 ──→ M2: 数据层 ──────────┘
     │                              ↑
     └──→ M6: 日期选择器 ───────────┘
```

说明：

- M1 是所有模块的基础依赖（DI、Theme、Navigation）。
- M2 依赖 M1（Hilt Module 提供 Room 实例）。
- M3 依赖 M1 + M2（数据查询）+ M4（年月选择器组件）。
- M4 依赖 M1（Theme + WheelPicker 基座组件）。
- M5 依赖 M1 + M2（数据写入）+ M6（日期选择器组件）。
- M6 依赖 M1（Theme + WheelPicker 基座组件）。
- M4 和 M6 共享 WheelPicker 基础组件（在 M1 中实现）。
- M4/M6 单独拆分的理由：二者虽共享 WheelPicker 基座，但配置不同（双列 vs 三列）、触发场景不同（首页 vs 键盘）、可独立开发和测试。本质是 WheelPicker 的薄配置层，不会产生重复实现。

## 开发顺序

| 阶段 | 模块 | 前置 | 是否可并行 | 说明 |
| --- | --- | --- | --- | --- |
| 1 | M1: 基础设施 | 无 | — | 搭建 Hilt、Navigation、Design System、WheelPicker 基座。**硬性门禁**：先跑通 Hilt + Room + Navigation + KSP 依赖链编译，再写业务代码 |
| 2 | M2: 数据层 | M1 | — | Room DB、Entity、DAO、Repository |
| 3 | M4: 年月选择器 + M6: 日期选择器 | M1 | 可并行 | 独立 UI 组件，复用 WheelPicker；M4 双列年/月，M6 三列年/月/日 |
| 4 | M3: 首页明细 | M1 + M2 + M4 | — | 首页完整功能 |
| 5 | M5: 记账页 | M1 + M2 + M6 | — | 记账完整功能 |

阶段 4 和阶段 5 理论上也可并行（无直接依赖），但建议先完成 M3 首页以便端到端验证数据流。

补充：M3 的 UI 骨架（不含年月弹窗交互）可在 M4 完成前先行搭建，进一步压缩关键路径。

## 进度记录

| 模块 | 状态 | 审批 | 最近更新 | 备注 |
| --- | --- | --- | --- | --- |
| M1: 基础设施 | ✅ completed | skipped_by_user | 2026-06-24 | 模块方案已生成，实现完成，代码审查通过，BUILD SUCCESSFUL |
| M2: 数据层 | ✅ completed | approved_by_user | 2026-06-24 | 实现完成，代码审查通过，JVM 测试全部通过，BUILD SUCCESSFUL |
| M3: 首页明细 | ✅ completed | approved_by_user | 2026-06-24 | 实现完成，代码审查通过，2 High + 3 Medium + 6 Low 全部修复，28 个 JVM 测试通过，BUILD SUCCESSFUL |
| M4: 年月选择器 | ✅ completed | skipped_by_user | 2026-06-24 | 实现完成，WheelPicker 字号修复（18sp→20sp），YearMonthPickerDialog testTag 补充，28 个 JVM 测试通过，BUILD SUCCESSFUL |
| M5: 记账页 | ✅ completed | skipped_by_user | 2026-06-24 | 实现完成，RecordViewModel/CategoryGrid/AmountKeyboard/RecordContent 全部新建，JVM 测试全部通过，BUILD SUCCESSFUL |
| M6: 日期选择器 | ✅ completed | skipped_by_user | 2026-06-24 | 实现完成，DatePickerDialog 三列滚轮 + 日列联动，JVM 测试通过，BUILD SUCCESSFUL |

整体功能状态：`technical_plan_created`

## 风险与假设

| # | 类型 | 描述 | 影响 | 缓解措施 |
| --- | --- | --- | --- | --- |
| R1 | 技术风险 | Compose 无官方 WheelPicker 组件，需自定义实现嵌套 `LazyColumn` + snap + 渐隐效果 | M4、M6 开发周期 | 提前在 M1 基础设施阶段实现通用 WheelPicker；必要时评估开源库（如 compose-wheelview） |
| R2 | 技术风险 | 等宽金额字体（PRD 推荐 DIN Next / SF Mono）在 Android 端需嵌入自定义字体文件 | 包体积、字体许可 | 使用开源等宽字体（JetBrains Mono / DM Mono）替代；仅嵌入数字子集 |
| R3 | 数据风险 | 首期纯本地存储，应用卸载即丢失数据 | 用户体验 | PRD 已明确首期无云同步；Room Migration 预留 version 管理 |
| R4 | 架构假设 | 单模块 `:app` 内按包分层，不拆 Gradle 模块 | 后续模块化成本 | 包结构按 feature 组织，保持边界清晰；功能增长后可低成本拆分 |
| R5 | 依赖风险 | Hilt、Room、Navigation Compose 版本兼容性（Kotlin 2.2.10 + AGP 9.2.1）；AGP 9.x 属于大版本更新，Hilt Gradle 插件对 AGP 9.x 的兼容性尚需验证 | 编译失败 | M1 阶段设为**硬性门禁**：先跑通空依赖链编译再写业务；`hilt-compiler` 通过 `ksp()` 接入而非 kapt |
| R6 | 设计假设 | .pen 设计稿无法通过 MCP 直接验证内容完整性 | 实现可能遗漏设计细节 | 依赖 PRD 文字描述和设计索引作为实现依据；开发阶段逐页对照 |
| R8 | 资源/合规风险 | PRD 要求类目图标和底部 Bar 图标为"线性风格重绘、不复制受保护素材"；图标资产的设计产出和合规审查是真实工作量 | 阻塞 M1（Design System）和 M5（类目网格）的 UI 还原 | 开发阶段可先用 Material Icons 占位；设计产出后替换为正式资源 |

假设：

- A1：首期仅支持中文界面，国际化预留但不实现。
- A2：首期不实现数据导出/导入。
- A3：金额使用 `Long`（分为单位）存储，避免浮点精度问题；UI 层格式化为 `¥xx.xx` 显示。
- A4：日期范围不设上下限约束，用户可选择任意日期。
- A5：底部 Bar 结构固定为五个入口，后续功能逐步解锁。
- A6：实现依据为 PRD-CODE-ACCOUNTING-20260623（墨与纸方案），该 PRD 为本期唯一有效需求来源。
- A7：记账页输入过程中进程被杀或屏幕旋转，首期不恢复未完成输入（不使用 `SavedStateHandle` 保存中间态）。
- A8：金额键盘的加减号（+/−）首期定义为简单连加/连减操作（非计算器模式），具体交互在 M5 模块方案中细化。

## 设计输入问题

| # | 来源 | 问题 | 处理策略 |
| --- | --- | --- | --- |
| DI-1 | 竞品截图 `prd/image/1.首页-明细.png` | 包含快捷入口（账单、预算、资产管家、购物返现、更多）和登录提示 | PRD 已明确排除，不实现 |
| DI-2 | 竞品截图 `prd/image/2.点击记账-支出.png` | 包含 20+ 支出类目（远超 PRD 定义的 4 个） | 仅实现 PRD 定义的餐饮、购物、娱乐、日用 |
| DI-3 | 竞品截图 `prd/image/3.点击记账-收入.png` | 包含多个收入类目 | 仅实现 PRD 定义的工资、理财、礼金、其他 |
| DI-4 | 设计稿 `code-accounting-v2.pen` | .pen 文件未在编辑器中打开，无法通过 Pencil MCP 直接验证是否包含 PRD 非目标内容 | 以 PRD 文字描述和设计索引为实现依据；建议后续打开 .pen 文件做逐页验证 |
| DI-5 | PRD 占位入口规格 | 图表、发现、我的入口点击显示 Toast「即将推出」1.5 秒 | 按 PRD 实现 Toast |

## 本次生成记录

```text
日期：2026-06-24
场景：A - 生成整体技术方案
触发：用户要求从 PRD 生成 Android 技术方案

已读取规则/文档：
- AGENTS.md（仓库根）
- AndroidAccounting/AGENTS.md
- .codex/skills/android-feature-workflow/SKILL.md
- prd/code-accounting-prd-20260623.md
- prd/requirements/README.md
- prd/designs/README.md
- prd/image/1~6 竞品截图（设计输入检查）

计划使用 Skills：
- android-feature-workflow（场景 A）

计划使用 Agents：
- Workflow Agent: 主 Agent（当前执行）
- Architecture Agent: android-architect（审查）

实际使用 Agents：
- Workflow Agent: 主 Agent（生成方案 + 修订）
- Architecture Agent: android-architect（审查，结论：有条件通过 → 已修订）

输出文档：
- AndroidAccounting/docs/requirements/code-accounting/overall-tech-plan.md

状态变化：
- 整体功能：→ technical_plan_created
- M1 ~ M6：→ not_started
```

## 变更记录

| 日期 | 类型 | 描述 | 来源 |
| --- | --- | --- | --- |
| 2026-06-24 | 初始创建 | 基于 PRD-CODE-ACCOUNTING-20260623（墨与纸）生成首版整体技术方案，包含 6 个模块划分、技术栈选型、开发顺序和风险评估 | 场景 A |
| 2026-06-24 | 架构审查修订 | 根据 Architecture Agent 审查意见修订：补全 Bill 数据模型（id/createdAt/TypeConverter）、金额存储定稿为 Long（分）、amount 正值 + type 决定正负、补充 RepositoryModule 和 Category 归属、补充 R7/R8 风险和 A6/A7/A8 假设、M1 设硬性门禁、M4/M6 拆分理由 | 架构审查 |
| 2026-06-24 | 模块方案 | 生成 M1 基础设施模块技术方案；用户跳过审批，直接进入实现 | 场景 B |
| 2026-06-24 | 模块实现 | M1 基础设施模块实现完成；Gradle 依赖链（Hilt 2.59.2/KSP 2.2.10-2.0.2/Navigation 2.9.0/Room 2.7.1/Serialization 1.8.1）接入，Design System、WheelPicker、BottomSheetPicker、AccountingBottomBar 实现，代码审查 3 medium+3 low 问题全部修复，BUILD SUCCESSFUL | 场景 C |
| 2026-06-24 | 模块方案 | 生成 M2 数据层模块技术方案；审批 pending，待确认 DI-M2-1/4/5 后实现 | 场景 B |
| 2026-06-24 | 模块实现 | M2 数据层模块实现完成；Room DB、BillEntity/BillDao/AppDatabase、BillMapper、BillRepository、DateUtil 全部实现，DI 填充，JVM 测试（7 用例）全部通过，代码审查 1 medium+1 low 修复，BUILD SUCCESSFUL | 场景 C |
| 2026-06-24 | 模块方案 | 生成 M3 首页明细模块技术方案；审批 pending，待确认 DI-M3-1/2/3 后实现 | 场景 B |
| 2026-06-24 | 模块实现 | M3 首页明细模块实现完成；Color.kt 色值更新（DI-M3-1）、AccountingBottomBar 记账按钮修复（DI-M3-3）、HomeViewModel/HomeContent/HomeRoute/YearMonthPickerDialog/MoneyFormatter/DateUtil 全部实现，18 个 JVM 测试全部通过，BUILD SUCCESSFUL；代码审查进行中 | 场景 C |
| 2026-06-24 | 代码审查修复 | M3 代码审查发现 2 High/3 Medium/6 Low 问题，全部修复：catch 移入 flatMapLatest 内部（H1）、Effect 收集改用 flowWithLifecycle（H2）、切月 loading 过渡（M1）、补充测试用例（M2）、移除冗余排序（M3），Low 级：VerticalDivider 替换、onPrimary 替换、testTag 位置等；28 个测试全部通过，BUILD SUCCESSFUL | 场景 D |
| 2026-06-24 | 模块方案 | 生成 M4 年月选择器模块技术方案；用户跳过审批，直接进入实现 | 场景 B |
| 2026-06-24 | 模块实现 | M4 年月选择器模块实现完成；WheelPicker 选中态 fontSize 18sp→20sp（关闭 LEFT-M3-2），YearMonthPickerDialog 两列 WheelPicker 补充 testTag（关闭 LEFT-M3-1），28 个 JVM 测试通过，BUILD SUCCESSFUL | 场景 C |
| 2026-06-24 | 模块方案 | 生成 M5 记账页、M6 日期选择器模块技术方案；用户跳过审批，直接进入实现 | 场景 B |
| 2026-06-24 | 模块实现 | M5 记账页模块实现完成；RecordViewModel（MVI）、CategoryGrid、AmountKeyboard、RecordContent/Screen/Route 全部新建，MoneyFormatter 金额拼接函数、AppNavHost 接线，JVM 测试全部通过，BUILD SUCCESSFUL | 场景 C |
| 2026-06-24 | 模块实现 | M6 日期选择器模块实现完成；DatePickerDialog.kt 新建（三列 WheelPicker + 日列联动 + testTag），JVM 测试通过，BUILD SUCCESSFUL | 场景 C |
| 2026-06-24 | 代码审查修复 | M5 代码审查修复 2 Medium/5 Low 问题（Categories 数据流规范、键盘字体 token 化、testTag 补全、防重复提交测试、完成按钮圆角、边界测试），JVM 测试全部通过，BUILD SUCCESSFUL | 场景 D |
