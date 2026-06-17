---
name: android-create-viewmodel
description: 创建或修改 Android ViewModel，使用 MVI 风格 UiState、UiIntent、UiEffect、StateFlow、SharedFlow 或 Flow、viewModelScope、构造函数注入依赖、协程错误处理和聚焦单元测试。适用于新增 ViewModel 逻辑、页面状态处理、Intent、Effect、加载/错误流程、重试行为或展示层业务逻辑。
---

# Android ViewModel 创建

## 用途

创建或修改 Android ViewModel。适用于新增状态逻辑、用户意图处理、副作用、加载/错误流程、重试行为，或页面层业务逻辑。

## 工作流程

1. 先阅读仓库根 `AGENTS.md`、`AndroidAccounting/AGENTS.md`、现有 ViewModel、DI 风格、协程调度器、Result 包装和测试写法。
2. 实现前先定义公开契约：
   - 不可变 `XxxUiState`
   - `sealed interface XxxUiIntent`
   - 需要一次性事件时定义 `sealed interface XxxUiEffect`
3. 只暴露不可变 Flow：
   - `StateFlow<XxxUiState>`
   - `Flow<XxxUiEffect>` 或 `SharedFlow<XxxUiEffect>`
4. 使用统一用户事件入口：
   - `fun onIntent(intent: XxxUiIntent)`
5. 通过构造函数注入依赖，依赖 Repository 接口而不是实现类。
6. 异步工作使用 `viewModelScope`。
7. 显式建模 Loading、Success、Empty、Error 状态。
8. ViewModel 负责 presentation logic 和 UI decision；数据编排、缓存、刷新和回退策略放在 Repository 或 domain/usecase 层。
9. 按项目约定把异常转换为日志、领域结果或 UI 错误，不吞异常。
10. 导航、Toast、Snackbar、Dialog 使用 effect，而不是 state。
11. 添加或更新单元测试，覆盖本次变更相关的初始、加载、成功、失败、重试和边界场景。

## 实现检查

- 不公开 `MutableStateFlow` 或 `MutableSharedFlow`。
- ViewModel 不创建 Retrofit、Room、DataStore 或具体 DataSource。
- 生产代码不使用 `runBlocking` 或 `Thread.sleep()`。
- 长耗时任务可取消。
- 状态更新使用不可变 copy。
- Effect 是一次性的，不会意外重复播放。
- 测试使用 fake 依赖和确定性 dispatcher。

## 输出要求

最终回复说明改动文件、已运行测试，以及有意留到后续处理的行为。
