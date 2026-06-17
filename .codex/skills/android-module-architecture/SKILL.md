---
name: android-module-architecture
description: 规划、创建或调整 Android 模块架构，包括 feature module、core module、Gradle 模块边界、依赖方向、公共 API、包结构和归属规则。适用于新增 feature module、拆分代码模块、设计 Android 项目结构、跨模块移动代码、定义 core/data/design-system 边界，或审查模块化方案。
---

# Android 模块架构

## 用途

规划、创建或调整 Android 模块架构。适用于新增 feature module、拆分模块、设计 Android 项目结构、跨模块移动代码、定义 core/data/design-system 边界，或审查模块化方案。

## 工作流程

1. 先阅读仓库根 `AGENTS.md`、`AndroidAccounting/AGENTS.md`、`settings.gradle.kts`、版本目录、根 Gradle 文件、模块 Gradle 文件和现有模块布局。
2. 判断需求是需要新模块，还是放入现有模块即可。不要创建没有明确职责的空模块。
3. 定义模块职责：
   - 负责什么业务能力
   - 不负责什么
   - 对外暴露哪些最小入口
4. 保持依赖方向：
   - `app` 依赖 feature modules
   - feature modules 依赖 core modules
   - feature modules 不应依赖其他 feature 的内部实现
   - 共享行为在有充分理由时下沉到 core module
5. 选择最小可用模块形态：
   - 轻量 feature：UI、navigation、少量 model
   - 标准 feature：presentation、必要时 domain、如果数据归该模块所有则包含 data
   - core module：共享 API、设计系统、数据库、网络、通用工具
6. 按项目约定和 version catalog alias 新增或更新 Gradle 配置。
7. 保持公共 API 最小化，不跨模块暴露 ViewModel 或实现类。
8. 检查循环依赖和过度暴露 API。
9. 运行能验证模块图的最小 Gradle sync/build/test 命令。

## 实现检查

- 模块名符合现有命名规范。
- 职责和非职责清晰。
- 依赖方向朝向 shared/core modules。
- 公共 API 是有意暴露的。
- 实现细节尽量保持 internal。
- Gradle include、plugins 和 dependencies 完整。
- 不创建只有占位意义的模块。

## 输出要求

最终回复说明依赖图、模块边界、改动文件、验证结果和风险。
