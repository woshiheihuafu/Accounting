---
name: android-network-request
description: 使用 Retrofit 或项目现有网络栈新增或修改 Android HTTP 网络请求，包括 API interface、DTO、序列化、RemoteDataSource、DI 接入、错误处理和 Repository 集成。适用于新增 endpoint、调用 API、接入后端请求、解析网络响应、处理 HTTP 错误，或把 Android 数据代码连接到后端契约。
---

# Android 网络请求

## 用途

新增或修改 Android HTTP 网络请求。适用于新增接口、调用 API、接入后端请求、解析网络响应、处理 HTTP 错误，或把 Android 数据层连接到后端契约。

## 工作流程

1. 先阅读仓库根 `AGENTS.md`、`AndroidAccounting/AGENTS.md`、现有网络栈、API interface、DTO 风格、序列化库、DI 模块、Base URL 配置和错误处理方式。
2. 如果有 API 契约，先阅读契约；如果没有，根据用户需求谨慎推断，并明确说明假设。
3. DTO 只新增或修改在 data/network 层。
4. 用清晰的请求和响应类型定义 Retrofit 或项目现有网络栈的 API 方法。
5. 网络调用必须放在 `RemoteDataSource` 后面。
6. 网络 DTO 转成 Domain model 后再向上暴露。
7. 通过 Repository 集成，不允许 UI 或 ViewModel 直接调用 API。
8. 按项目约定处理 HTTP、解析、取消和网络连接错误。
9. 使用现有模块模式接入 DI。
10. 添加聚焦测试，尽量覆盖成功、HTTP 错误、异常响应和 Repository 集成。

## 实现检查

- UI 代码中不出现网络请求。
- ViewModel 或 UI 中不手动创建 Retrofit client。
- DTO 不暴露给 Composable。
- 序列化注解与项目使用的库一致。
- Base URL 和 headers 跟随现有配置。
- 鉴权、分页和重试行为在相关场景下必须明确。
- 错误映射确定且可测试。

## 输出要求

最终回复说明接口假设/契约、改动文件、已运行测试和未解决的后端问题。
