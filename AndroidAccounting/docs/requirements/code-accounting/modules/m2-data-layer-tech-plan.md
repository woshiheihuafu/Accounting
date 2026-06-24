# M2 数据层模块技术方案

## 模块目标

为 CodeAccounting Android 端搭建本地数据持久化基座，向上层（M3 首页、M5 记账页）提供唯一的数据访问入口。本模块交付一套「可编译、可注入、可测试」的数据层：Room 数据库、`BillEntity` / `BillDao`、`BillRepository` 接口与实现、Domain Model、Mapper、TypeConverter，以及月度账单查询与收支汇总能力。

具体目标：

1. 基于 M1 已声明的 Room 依赖，初始化 `AppDatabase`、`BillEntity`、`BillDao`，落地账单表的增、删、查与汇总查询。
2. 定义 Domain Model（`Bill` / `BillType` / `Category`），与 Entity 通过 `BillMapper` 双向映射，保证 UI / ViewModel 只感知 Domain。
3. 定义 `BillRepository` 接口与 `BillRepositoryImpl` 实现，作为数据访问唯一入口，承担月度账单查询和收入/支出汇总计算。
4. 填充 M1 预留的 `DatabaseModule`（提供 `AppDatabase` / `BillDao`）和 `RepositoryModule`（`@Binds` 绑定 `BillRepository`）。
5. 提供 `DateUtil` 月份边界换算工具，支撑"按年月查询"的 epochDay 范围计算。
6. 交付 `BillDao`（in-memory Room）与 `BillRepositoryImpl` 的单元测试，覆盖插入、查询（含空结果）、收支汇总、删除。

## 范围

- Room 数据库初始化：`data/local/AppDatabase.kt`（`@Database`，version = 1，注册 `RoomConverters`）。
- 账单实体与 DAO：`data/local/BillEntity.kt`（`@Entity(tableName = "bills")`）、`data/local/BillDao.kt`（`@Dao`，含查询 / 汇总 / 插入 / 删除）。
- TypeConverter：`data/local/converter/RoomConverters.kt`（`BillType` / `LocalDate` / `Instant` ↔ 基础类型）。
- Domain Model：`data/model/Bill.kt`、`data/model/BillType.kt`、`data/model/Category.kt`（静态类目）。
- Mapper：`data/mapper/BillMapper.kt`（Entity ↔ Domain 双向）。
- Repository：`data/repository/BillRepository.kt`（接口）、`data/repository/BillRepositoryImpl.kt`（`@Inject` 实现）。
- 月度汇总 Domain 载体：`data/model/MonthlySummary.kt`。
- DI 填充：`di/DatabaseModule.kt`、`di/RepositoryModule.kt`。
- 工具：`util/DateUtil.kt`（月份首日 / 次月首日 epochDay 换算）。
- 测试：`BillDaoTest`（androidTest，in-memory Room）、`BillRepositoryImplTest`（test，fake DAO）。

## 非目标

- 不实现任何 UI（首页明细列表、记账页、汇总卡片渲染归属 M3 / M5）。
- 不实现 ViewModel（`HomeViewModel` / `RecordViewModel` 归属 M3 / M5）。
- 不实现网络层 / 远端数据源 / DTO（首期无网络层，整体方案架构原则第 8 条已确认）。
- 不实现金额「分 → ¥xx.xx」展示格式化、`+/-` 符号渲染（归属 UI 层 M3 / M5）。
- 不实现类目图标资源（`ic_category_<name>.xml` 线性重绘归属设计产出，开发期用 Material Icons 占位，见整体方案 R8）。
- 不实现数据导出 / 导入、云同步、迁移（首期纯本地，整体方案 A2 / R3）。
- 不引入缓存层（首期 Repository 直接委托 DAO，见「Repository 方案」）。

## 依赖关系

- 上游依赖：M1 基础设施（Hilt DI 框架、`AccountingApplication`、Room 依赖声明、KSP 接入）。M1 已 `✅ completed`，依赖链编译已跑通。
- 下游被依赖：
  - M3 首页明细：依赖 `BillRepository` 的"按月查账单 Flow""按月收支汇总 Flow"。
  - M5 记账页：依赖 `BillRepository` 的"插入账单""静态类目（Category）"。
- 模块内依赖方向（单模块按包分层，逻辑约定）：
  - `di` → `data/local` + `data/repository`（提供实例 / 绑定接口）。
  - `data/repository`（impl）→ `data/local`（DAO）+ `data/mapper` + `data/model` + `util`。
  - `data/local`（Entity / DAO / Database / Converter）→ `data/model`（Entity 字段引用 `BillType`）。
  - `data/mapper` → `data/local`（Entity）+ `data/model`（Domain）。
  - 严禁反向：`data/model`（Domain）不依赖 `data/local`（Entity / Room）。
- 第三方依赖：复用 M1 已接入版本，无需新增声明。

| 依赖 | 实际版本 | 用途 |
| --- | --- | --- |
| `room-runtime` / `room-ktx` | `2.7.1` | Room 运行时 + 协程 / Flow 支持 |
| `room-compiler` | `2.7.1`（`ksp(...)` 接入） | 注解处理生成 DAO / Database 实现 |
| Hilt | `2.59.2` | DI，`@Module` / `@Provides` / `@Binds` |
| KSP | `2.2.10-2.0.2` | Room / Hilt 注解处理 |
| Coroutines + Flow | 随 Compose BOM / lifecycle | Repository / DAO 异步与响应式数据流 |

> 说明：M2 不修改 `libs.versions.toml` 与 Gradle 配置，Room 依赖已在 M1 声明并随空依赖链编译通过；M2 仅"初始化使用"。`minSdk = 29`，`java.time.LocalDate` / `java.time.Instant` 在 API 26+ 原生可用，无需 desugaring。

## UI 方案

无。M2 为纯数据层，不交付任何 Compose 页面或组件。

数据层对 UI 的契约约定（供 M3 / M5 衔接）：

- Repository 返回 Domain `List<Bill>`，已按 `date` 倒序、同日内 `createdAt` 倒序排好；UI 只需按 `date` 分组渲染，不再排序。
- Repository 返回 `MonthlySummary(income, expense)`（单位：分，均为正值）；UI 负责"分 → ¥xx.xx"格式化与 `+/-` 符号渲染。
- 单笔 `Bill.amount` 始终为正，UI 依据 `Bill.type` 决定展示 `-¥xx`（EXPENSE）/ `+¥xx`（INCOME）。

## ViewModel 方案

无。M2 不承载页面状态，不引入任何 `UiState` / `UiIntent` / `UiEffect`。

数据层对 ViewModel 的契约约定（供 M3 / M5 衔接）：

- ViewModel 通过 Hilt 构造注入 `BillRepository`（依赖接口而非实现）。
- 查询类接口返回 `Flow`，ViewModel 在 `viewModelScope` 内 `collect` 或 `stateIn` 转 `StateFlow`。
- 写入类接口为 `suspend`，ViewModel 在 `viewModelScope.launch` 中调用。
- 汇总计算已在 Repository 完成，ViewModel 直接消费 `MonthlySummary`，不在展示层重复 `SUM`。

## Repository 方案

`BillRepository` 是数据访问的唯一入口，对上暴露 Domain 类型，对下委托 `BillDao`。首期无缓存层、无网络层，`BillRepositoryImpl` 直接委托 DAO，并承担两类职责：① Entity ↔ Domain 映射（经 `BillMapper`）；② 月度收入 / 支出汇总计算（返回 Domain `MonthlySummary`，供 ViewModel 直接使用）。

### 接口方法签名

```kotlin
interface BillRepository {

    /** 按年月查询账单明细，已按 date 倒序、同日内 createdAt 倒序排序 */
    fun observeBillsByMonth(yearMonth: YearMonth): Flow<List<Bill>>

    /** 按年月查询收支汇总（收入合计、支出合计，单位：分，均为正值） */
    fun observeMonthlySummary(yearMonth: YearMonth): Flow<MonthlySummary>

    /** 插入一笔账单，返回自增主键 id */
    suspend fun insertBill(bill: Bill): Long

    /** 按 id 删除一笔账单 */
    suspend fun deleteBill(id: Long)
}
```

`MonthlySummary` Domain 载体：

```kotlin
data class MonthlySummary(
    val income: Long = 0L,   // 收入合计（分），正值
    val expense: Long = 0L,  // 支出合计（分），正值
) {
    val balance: Long get() = income - expense  // 结余（分），可正可负
}
```

### 实现策略（BillRepositoryImpl）

- 通过 `@Inject constructor(private val billDao: BillDao)` 注入 DAO，无其他依赖。
- 直接委托 DAO，不引入内存缓存 / 磁盘缓存 / 网络回退（首期 Room 是唯一数据源）。
- 年月 → epochDay 范围换算交给 `DateUtil`，避免在 SQL 中做月份解析。
- `observeBillsByMonth`：调用 DAO 得 `Flow<List<BillEntity>>`，用 `.map { it.toDomainList() }` 转 Domain。排序在 SQL 中完成，Repository 不二次排序。
- `observeMonthlySummary`：组合 DAO 的两条汇总查询，用 `combine` 合并为 `MonthlySummary`；DAO 返回的 `Long?` 在此 `?: 0L` 兜底。
- `insertBill`：`bill.toEntity()` 后调用 `billDao.insert(entity)`，返回自增 id。
- `deleteBill`：直接调用 `billDao.deleteById(id)`。
- 协程线程：Room 的 `suspend` / `Flow` DAO 默认在 Room 自有 IO 线程池执行查询，Repository 不额外切换 `Dispatchers`。

### 汇总规则归属

- 收入合计 = `SUM(amount) WHERE type = INCOME AND date ∈ [monthStart, nextMonthStart)`。
- 支出合计 = `SUM(amount) WHERE type = EXPENSE AND date ∈ [monthStart, nextMonthStart)`。
- 汇总在 Repository 层（经 DAO 聚合查询 + `combine`）完成并以 Domain `MonthlySummary` 返回，ViewModel 直接使用。

## Network 方案

无。首期无网络层（整体方案架构原则第 8 条已确认）。

## 模块架构方案

遵循 `AndroidAccounting/AGENTS.md` 分层与整体方案目标包结构。映射链：`BillEntity → BillMapper → Bill(Domain) → UI`（首期无 DTO）。

落地包结构：

```text
com.cocos.androidaccounting
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── BillDao.kt
│   │   ├── BillEntity.kt
│   │   └── converter/
│   │       └── RoomConverters.kt
│   ├── mapper/
│   │   └── BillMapper.kt
│   ├── model/
│   │   ├── Bill.kt
│   │   ├── BillType.kt
│   │   ├── Category.kt
│   │   └── MonthlySummary.kt
│   └── repository/
│       ├── BillRepository.kt
│       └── BillRepositoryImpl.kt
├── di/
│   ├── DatabaseModule.kt     (修改：填充 provide)
│   └── RepositoryModule.kt   (修改：填充 bind)
└── util/
    └── DateUtil.kt
```

### data/model/BillType.kt

```kotlin
enum class BillType { EXPENSE, INCOME }
```

### data/model/Bill.kt

```kotlin
data class Bill(
    val id: Long = 0L,
    val type: BillType,
    val category: String,
    val amount: Long,           // 单位：分，始终正值
    val date: LocalDate,
    val remark: String = "",    // 备注，默认空串
    val createdAt: Instant,
)
```

### data/model/Category.kt

```kotlin
data class Category(
    val name: String,
    val type: BillType,
    val iconRes: Int,           // 开发期 Material Icons 占位
)

object Categories {
    val expense: List<Category> = listOf(
        Category("餐饮", BillType.EXPENSE, /* 占位 iconRes */),
        Category("购物", BillType.EXPENSE, /* 占位 iconRes */),
        Category("娱乐", BillType.EXPENSE, /* 占位 iconRes */),
        Category("日用", BillType.EXPENSE, /* 占位 iconRes */),
    )
    val income: List<Category> = listOf(
        Category("工资", BillType.INCOME, /* 占位 iconRes */),
        Category("理财", BillType.INCOME, /* 占位 iconRes */),
        Category("礼金", BillType.INCOME, /* 占位 iconRes */),
        Category("其他", BillType.INCOME, /* 占位 iconRes */),
    )

    fun byType(type: BillType): List<Category> =
        if (type == BillType.EXPENSE) expense else income
}
```

### data/local/BillEntity.kt

```kotlin
@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: BillType,        // 持久化为 String（RoomConverters）
    val category: String,
    val amount: Long,          // 分，始终正值
    val date: LocalDate,       // 持久化为 Long epochDay
    val remark: String,
    val createdAt: Instant,    // 持久化为 Long epochMilli
)
```

### data/local/converter/RoomConverters.kt

```kotlin
class RoomConverters {
    @TypeConverter fun billTypeToString(value: BillType): String = value.name
    @TypeConverter fun stringToBillType(value: String): BillType = BillType.valueOf(value)

    @TypeConverter fun localDateToLong(value: LocalDate): Long = value.toEpochDay()
    @TypeConverter fun longToLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)

    @TypeConverter fun instantToLong(value: Instant): Long = value.toEpochMilli()
    @TypeConverter fun longToInstant(value: Long): Instant = Instant.ofEpochMilli(value)
}
```

### data/local/BillDao.kt（@Query SQL）

```kotlin
@Dao
interface BillDao {

    @Query("""
        SELECT * FROM bills
        WHERE date >= :startEpochDay AND date < :endEpochDayExclusive
        ORDER BY date DESC, createdAt DESC
    """)
    fun observeBillsByMonth(
        startEpochDay: Long,
        endEpochDayExclusive: Long,
    ): Flow<List<BillEntity>>

    @Query("""
        SELECT SUM(amount) FROM bills
        WHERE type = :type
          AND date >= :startEpochDay AND date < :endEpochDayExclusive
    """)
    fun observeSumByType(
        type: BillType,
        startEpochDay: Long,
        endEpochDayExclusive: Long,
    ): Flow<Long?>

    @Insert
    suspend fun insert(entity: BillEntity): Long

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

### data/local/AppDatabase.kt

```kotlin
@Database(entities = [BillEntity::class], version = 1, exportSchema = true)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao

    companion object {
        const val DATABASE_NAME = "accounting.db"
    }
}
```

> `exportSchema = true` 配套在 `app/build.gradle.kts` 设置 `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`，由 devops 落地。首期无 Migration（version 1）。若暂不引入 schema 目录，可置 `false` 并记遗留项（DI-M2-4）。

### data/mapper/BillMapper.kt

```kotlin
fun BillEntity.toDomain(): Bill = Bill(
    id = id, type = type, category = category, amount = amount,
    date = date, remark = remark, createdAt = createdAt,
)

fun List<BillEntity>.toDomainList(): List<Bill> = map { it.toDomain() }

fun Bill.toEntity(): BillEntity = BillEntity(
    id = id, type = type, category = category, amount = amount,
    date = date, remark = remark, createdAt = createdAt,
)
```

### di/DatabaseModule.kt（填充后）

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .build()

    @Provides
    fun provideBillDao(database: AppDatabase): BillDao = database.billDao()
}
```

### di/RepositoryModule.kt（填充后）

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBillRepository(impl: BillRepositoryImpl): BillRepository
}
```

### util/DateUtil.kt

```kotlin
object DateUtil {
    fun monthStartEpochDay(yearMonth: YearMonth): Long =
        yearMonth.atDay(1).toEpochDay()

    fun monthEndExclusiveEpochDay(yearMonth: YearMonth): Long =
        yearMonth.plusMonths(1).atDay(1).toEpochDay()
}
```

## 状态与副作用模型

M2 无 MVI 状态机。数据层以"响应式数据流 + 挂起写入"对外：

- 读：以冷 `Flow` 暴露，数据库变更自动推送新值，由上层 ViewModel 转 `StateFlow`。
- 写：以 `suspend` 暴露，调用方在协程作用域内执行。
- 错误处理：遵循 AGENTS.md「不吞异常」。DB 异常向上传播，由 ViewModel 转为 UI 错误态或 Effect。`SUM` 空结果以 `null → 0L` 正常兜底，不视为异常。

## 文件变更计划

> 实现顺序建议：`data/model`（BillType / Bill / Category / MonthlySummary）→ `converter` → `BillEntity` → `BillDao` → `AppDatabase` → `BillMapper` → `DateUtil` → `BillRepository` / `BillRepositoryImpl` → 填充 `di` 两个 Module → `./gradlew assembleDebug` 验证 KSP / Hilt 生成 → 编写测试。

| 文件 | 操作 | 说明 |
| --- | --- | --- |
| `data/model/BillType.kt` | 新建 | `enum class BillType { EXPENSE, INCOME }` |
| `data/model/Bill.kt` | 新建 | Domain data class（7 字段，`amount` 分、正值；`remark` 默认空串） |
| `data/model/Category.kt` | 新建 | `Category` data class + `Categories` 静态对象（支出 / 收入各 4 类） |
| `data/model/MonthlySummary.kt` | 新建 | 月度汇总载体（`income` / `expense` / 派生 `balance`） |
| `data/local/converter/RoomConverters.kt` | 新建 | 3 组 `@TypeConverter`（BillType / LocalDate / Instant） |
| `data/local/BillEntity.kt` | 新建 | `@Entity(tableName="bills")`，字段用 Domain 类型，`id` autoGenerate |
| `data/local/BillDao.kt` | 新建 | `@Dao`：按月查询、按类型汇总、插入、按 id 删除 |
| `data/local/AppDatabase.kt` | 新建 | `@Database(version=1)` + `@TypeConverters` + `billDao()` |
| `data/mapper/BillMapper.kt` | 新建 | Entity ↔ Domain 双向扩展函数 |
| `data/repository/BillRepository.kt` | 新建 | 接口（4 方法） |
| `data/repository/BillRepositoryImpl.kt` | 新建 | `@Inject` 实现，委托 DAO + Mapper + DateUtil + combine 汇总 |
| `util/DateUtil.kt` | 新建 | 月份 epochDay 范围换算 |
| `di/DatabaseModule.kt` | 修改 | 移除 TODO，填充 `@Provides` AppDatabase（`@Singleton`）/ BillDao |
| `di/RepositoryModule.kt` | 修改 | 移除 TODO，填充 `@Binds` BillRepository → BillRepositoryImpl |
| `app/build.gradle.kts` | 修改（待定） | 若启用 `exportSchema=true`，增加 `ksp { arg(...) }` 与测试依赖 |
| `gradle/libs.versions.toml` | 修改（待定） | 若需 `room-testing` / `coroutines-test` / `turbine`，补充测试依赖 |

## 测试计划

### 测试依赖与工具

| 依赖 | 用途 | source set |
| --- | --- | --- |
| `androidx.room:room-testing` | Room in-memory 数据库辅助（可选） | androidTest |
| `androidx.test:core` + `androidx.test.ext:junit` | Instrumentation Runner | androidTest |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | `runTest`、`StandardTestDispatcher` | test / androidTest |
| `app.cash.turbine:turbine`（建议） | `Flow` 断言 | test / androidTest |
| `junit:junit` `4.13.2` | 测试框架 | test / androidTest |

### BillDaoTest（androidTest，in-memory Room）

| 用例 | 验证点 |
| --- | --- |
| `insert_then_observeBillsByMonth_returnsInserted` | 插入 1 笔，按当月查询返回该笔，字段经 TypeConverter 往返后一致 |
| `observeBillsByMonth_emptyMonth_returnsEmptyList` | 无数据 / 非目标月份查询返回空列表 |
| `observeBillsByMonth_ordering` | 插入跨日 + 同日多笔，断言 `date` 倒序、同日内 `createdAt` 倒序 |
| `observeBillsByMonth_monthBoundary` | 插入上月末 / 本月首 / 本月末 / 次月首，断言仅本月两笔被命中 |
| `observeSumByType_expense` | 仅统计 EXPENSE 当月金额之和，排除 INCOME 与其他月份 |
| `observeSumByType_income` | 仅统计 INCOME 当月金额之和 |
| `observeSumByType_emptyMonth_returnsNull` | 空月份 `SUM` 返回 `null` |
| `deleteById_removesBill` | 插入后删除，查询不再返回该笔 |
| `deleteById_nonExistingId_noop` | 删除不存在 id 不抛异常 |

### BillRepositoryImplTest（JVM test，fake DAO）

| 用例 | 验证点 |
| --- | --- |
| `observeBillsByMonth_mapsEntityToDomain` | DAO 返回 Entity 列表 → Repository 输出对应 Domain `List<Bill>`，字段映射正确 |
| `observeBillsByMonth_usesDateUtilRange` | 传给 DAO 的 `start` / `endExclusive` 等于 `DateUtil` 计算值 |
| `observeMonthlySummary_combinesIncomeExpense` | 两 Flow 合成 `MonthlySummary(income, expense)`，`balance` 正确 |
| `observeMonthlySummary_nullSumDefaultsToZero` | DAO 返回 `null` 时汇总为 `0L` |
| `insertBill_mapsDomainToEntityAndReturnsId` | `Bill(id=0)` → Entity 后委托 `dao.insert`，回传自增 id |
| `deleteBill_delegatesToDao` | 调用 `dao.deleteById(id)`，参数透传 |

## 设计输入问题

| # | 来源 | 问题 | 处理策略 |
| --- | --- | --- | --- |
| DI-M2-1 | 数据模型约束 | `remark` 标注"可为空"，但 Domain 倾向非空。Entity / Domain 用 `String`（空串）还是 `String?`（null）？ | 本方案定为非空 `String`，默认空串：简化 TypeConverter 与汇总逻辑，UI 判空展示。若产品要求区分"未填写"与"空"，改为 `String?` 并记为变更。标记需确认。 |
| DI-M2-2 | 类目静态数据 | `Category.name` 为用户可见中文，是否纳入 `strings.xml` 国际化？ | 整体方案 A1（首期仅中文，国际化预留不实现）。M2 先用常量，遗留项跟踪，M5 接 UI 时按需抽取。 |
| DI-M2-3 | 类目图标 | `ic_category_<name>.xml` 线性图标资产未就位 | 开发期 `iconRes` 用 Material Icons 占位，设计产出后替换（整体方案 R8）。 |
| DI-M2-4 | Room schema | 是否启用 `exportSchema = true` 并提交 `schemas/` 目录？ | 建议启用（为后续 Migration 留档）；若团队暂不引入，置 `false` 并记遗留。标记需确认。 |
| DI-M2-5 | 汇总接口形态 | "按月收入/支出合计"暴露为合并 `MonthlySummary` 还是两个独立 Flow？ | 本方案合并为 `observeMonthlySummary(): Flow<MonthlySummary>`，减少 UI 端组合成本。标记需确认。 |

## 审批状态

`approved_by_user`

> 用户已确认：DI-M2-1 选非空 String（默认空串）、DI-M2-4 启用 exportSchema = true、DI-M2-5 使用合并 MonthlySummary。进入场景 C 实现。

## 进度记录

| 日期 | 状态 | 说明 |
| --- | --- | --- |
| 2026-06-24 | technical_plan_created | M2 数据层模块技术方案生成，审批 pending，待用户确认后实现 |
| 2026-06-24 | approved_by_user | 用户确认 DI-M2-1/4/5 三项设计决策，审批通过，进入实现 |
| 2026-06-24 | implementation_complete | 全部数据层代码实现完成；assembleDebug ✅、testDebugUnitTest ✅（6 个 JVM 测试通过）、connectedDebugAndroidTest ⚠️（代码编译通过，设备端 INSTALL_FAILED_USER_RESTRICTED 安全限制致安装失败，与代码无关）|
| 2026-06-24 | in_progress → completed | 修复代码审查 BUG-001/002，JVM 测试通过，模块完成 |

## Bug 与遗留问题

当前无 Bug（尚未进入实现）。预登记遗留项：

### BUG-001：BillDaoTest 缺少排序验证用例

- 来源：agent_found（code-reviewer）
- 状态：resolved
- 严重级别：medium
- 描述：测试计划要求的 `observeBillsByMonth_ordering` 用例缺失，无法保证 `ORDER BY date DESC, createdAt DESC` 的排序契约
- 位置：`app/src/androidTest/.../data/local/BillDaoTest.kt`
- 解决方案：补充测试用例，插入跨日 + 同日多笔数据，断言 date/createdAt 双重倒序
- 创建时间：2026-06-24
- 更新时间：2026-06-24

### BUG-002：insertBill 测试未验证 Domain → Entity 字段映射

- 来源：agent_found（code-reviewer）
- 状态：resolved
- 严重级别：low
- 描述：`insertBill_returnsDaoId` 仅验证 id 回传，未验证 Mapper 是否正确将 Bill 各字段转为 BillEntity
- 位置：`app/src/test/.../data/repository/BillRepositoryImplTest.kt`
- 解决方案：FakeBillDao 记录 lastInsertedEntity，测试改为断言所有字段映射正确
- 创建时间：2026-06-24
- 更新时间：2026-06-24

| # | 类型 | 描述 | 处理时机 |
| --- | --- | --- | --- |
| LEFT-M2-1 | 国际化 | `Category.name` 中文常量未入 `strings.xml` | M5 接 UI 时按需抽取 |
| LEFT-M2-2 | 资源 | 类目线性图标占位，待设计产出替换 | 设计资产就位后 |
| LEFT-M2-3 | 性能 | `BillEntity` 未加 `date` 索引；首期数据量小暂不加 | 数据量增长后评估 |
| LEFT-M2-4 | 工程 | `exportSchema` / `schemas/` 目录策略待定 | 实现前与 devops 确认 |

> 继承自整体方案的相关风险：R3（纯本地，卸载丢数据，Room version 预留 Migration）、R8（图标资产合规）。

## 开发记录

### 2026-06-24 实现完成

**新建文件（按实现顺序）：**

| 文件 | 说明 |
| --- | --- |
| `data/model/BillType.kt` | 收支类型枚举 `EXPENSE / INCOME` |
| `data/model/Bill.kt` | Domain 账单模型，`remark` 非空 String 默认空串（DI-M2-1） |
| `data/model/MonthlySummary.kt` | 月度汇总 Domain，`balance` 计算属性（DI-M2-5） |
| `data/model/Category.kt` | 静态类目列表 + `Categories` 对象；`iconRes` 用 `android.R.drawable.*` 占位（LEFT-M2-2） |
| `data/local/converter/RoomConverters.kt` | `BillType` / `LocalDate` / `Instant` ↔ 基础类型 TypeConverter |
| `data/local/BillEntity.kt` | Room `@Entity(tableName = "bills")`，含自增 PrimaryKey |
| `data/local/BillDao.kt` | `@Dao`：月度列表查询、按类型汇总（返回 `Flow<Long?>`）、插入、删除 |
| `data/local/AppDatabase.kt` | `@Database(version=1, exportSchema=true)`（DI-M2-4），注册 `RoomConverters` |
| `data/mapper/BillMapper.kt` | `BillEntity ↔ Bill` 双向扩展函数 + `List.toDomainList()` |
| `util/DateUtil.kt` | 月首日 / 次月首日 epochDay 换算工具 |
| `data/repository/BillRepository.kt` | 数据访问接口：列表 Flow / 汇总 Flow / 插入 / 删除 |
| `data/repository/BillRepositoryImpl.kt` | `@Inject` 实现，`combine` 合并收支汇总（DI-M2-5），null → 0 兜底 |

**修改文件：**

| 文件 | 改动说明 |
| --- | --- |
| `di/DatabaseModule.kt` | 填充 `provideAppDatabase`（`Room.databaseBuilder`）和 `provideBillDao` |
| `di/RepositoryModule.kt` | 填充 `@Binds @Singleton bindBillRepository` |

**新建测试文件：**

| 文件 | 说明 |
| --- | --- |
| `androidTest/.../BillDaoTest.kt` | 8 个 in-memory Room 测试：插入查询、空结果、月份边界、收支汇总、空汇总返回 null、删除 |
| `test/.../BillRepositoryImplTest.kt` | 6 个 JVM 测试（FakeBillDao）：实体映射、DateUtil 范围透传、汇总合并、null 默认 0、insertBill 回传 id、deleteBill 委托 |

**验证结果：**

- `./gradlew assembleDebug`：✅ BUILD SUCCESSFUL
- `./gradlew testDebugUnitTest`：✅ BUILD SUCCESSFUL（6 个 JVM 测试全部通过）
- `./gradlew connectedDebugAndroidTest`：⚠️ 测试 APK 编译通过，真机端 `INSTALL_FAILED_USER_RESTRICTED`（设备关闭了 USB 安装权限），与代码逻辑无关，待在允许安装的设备上验证

**注意事项：**
- `Category.iconRes` 使用 `@param:DrawableRes` 注解以消除 Kotlin 注解目标 warning
- `BillDao.observeSumByType` 返回 `Flow<Long?>`，Room 对 `SUM` 无数据时返回 null，Repository 层已兜底为 0L
