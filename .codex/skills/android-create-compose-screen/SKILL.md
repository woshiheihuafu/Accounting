---
name: android-create-compose-screen
description: 创建或修改 Android Jetpack Compose 页面，使用 Route、Screen、无状态 Content、MVI 契约、生命周期感知状态收集、Preview 和可测试回调。适用于新增 Compose 页面、表单、列表/详情流、设置页、空/加载/错误状态，或把 UI 连接到 Android ViewModel。
---

# Android Compose 页面创建

## 用途

创建或修改 Android Jetpack Compose 页面。适用于新增页面、表单、列表/详情、设置页、空/加载/错误状态，或需要连接 ViewModel 的 UI。

## 工作流程

1. 修改前先阅读仓库根 `AGENTS.md`、`AndroidAccounting/AGENTS.md`、现有页面写法、主题、导航和模块结构。
2. 判断页面应该放在现有模块，还是需要新模块；如果模块边界不清晰，使用 `android-module-architecture`。
3. 先定义 UI 契约，再写布局：
   - `XxxUiState`：不可变 `data class`
   - `XxxUiIntent`：`sealed interface`
   - `XxxUiEffect`：一次性事件
4. 按 Compose 分层实现：
   - `XxxRoute`：收集状态/副作用，处理导航/副作用，传递回调
   - `XxxScreen`：分发状态和回调
   - `XxxContent`：无状态渲染
5. 在 Route 层使用 `collectAsStateWithLifecycle()`。
6. Composable 不写业务逻辑，只做状态下传和事件上传。
7. 使用设计系统的颜色、字体和形状，避免硬编码颜色、文字样式和圆角。
8. 给关键 UI 状态、操作和列表项添加稳定的 `Modifier.testTag(...)`。
9. `LazyColumn` 和 `LazyRow` 必须提供稳定 key。
10. 如果项目已有 Preview 习惯，为关键状态添加 Preview。
11. 运行与改动文件匹配的最小格式化、lint 或测试命令。

## 实现检查

- `Route` 只收集一次 `StateFlow`。
- 副作用只通过一个 `LaunchedEffect` 收集。
- 导航、Toast、Snackbar、Dialog 使用 `UiEffect` 表达。
- `Content` 只接收 state 和 callback。
- 用户可见文案来自资源文件。
- Loading、Empty、Error、Success 状态显式建模。
- 列表使用稳定 item key 和稳定 test tag。
- Composable 中不访问 Repository、API、Room、DataStore。

## 输出要求

最终回复说明改动文件、验证结果，以及仍未完成或存在风险的点。
