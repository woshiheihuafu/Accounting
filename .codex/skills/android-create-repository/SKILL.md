---
name: android-create-repository
description: Create Android repository-centered data flows that separate UI/domain code from Retrofit, Room, DataStore, file storage, memory cache, and other data sources. Use when the user asks to add a Repository, local/remote data source, mapper, DTO/entity/domain conversion, cache policy, Flow-based data stream, refresh behavior, or data-layer abstraction.
---

# Android Create Repository

## 用途

创建以 Repository 为中心的 Android 数据流。适用于新增 Repository、本地/远端 DataSource、Mapper、DTO/Entity/Domain 转换、缓存策略、Flow 数据流、刷新行为或数据层抽象。

## 工作流程

1. 先阅读仓库根 `AGENTS.md`、`AndroidAccounting/AGENTS.md`、现有 Repository、DataSource 模式、模型命名、DI 模块、错误包装和测试。
2. 先从调用方需求定义 Repository 接口，再实现数据源。
3. 谨慎选择返回类型：
   - 可观察或带缓存的状态使用 `Flow<T>`
   - 一次性操作使用 `suspend fun`
   - 错误属于契约时，使用项目里的 `Result` / `Resource` 类型
4. 明确模型边界：
   - DTO 属于网络层
   - Entity 属于数据库层
   - Domain model 是 Repository 对外输出
   - UI model 只属于展示层
5. 使用 mapper 函数，不把 DTO 或 Entity 向上传递。
6. 按职责拆分数据源：
   - `RemoteDataSource` 负责 API
   - `LocalDataSource` 负责 Room / DataStore / 文件
   - `RepositoryImpl` 负责编排和策略
7. 编码前明确缓存和刷新行为：
   - 无缓存
   - 内存缓存
   - 本地优先
   - 远端优先
   - stale-while-revalidate
8. 使用项目现有 DI 框架添加绑定。
9. 添加聚焦测试，覆盖映射、回退、刷新、错误和缓存行为。

## 实现检查

- UI 和 ViewModel 只依赖 Repository 接口。
- Repository 不暴露 DTO 或 Entity。
- DataSource 不包含展示逻辑。
- Repository 负责编排、合并、回退和刷新策略。
- Mapper 有明确命名且可测试。
- 错误处理保持一致：记录、抛出或转换。
- 不引入 Service Locator 模式。

## 输出要求

最终回复说明 Repository 契约、数据流摘要、改动文件和验证结果。
