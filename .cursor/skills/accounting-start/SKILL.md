---
name: accounting-start
description: Accounting 仓库启动器。仅当用户明确输入 /start，或明确要求使用 Accounting 启动器选择工作域时使用；不要因为普通 PRD、产品分析、截图分析、需求讨论、app、mobile、实现等词自动触发。本 skill 用于列出可选工作域并在用户选择后路由到对应流程，目前包含 Product、Android、iOS、Backend、Other。
---

# Accounting 启动器

## 目标

作为 `Accounting` 仓库的显式入口，先让用户选择工作域，再进入对应域的规则、技能和工作目录。

不要在用户未选择域之前读取平台子目录的规则文件或加载平台专用技能。

## 触发方式

仅在以下场景使用：

- 用户输入 `/start`。
- 用户明确要求使用 `accounting-start` 或 Accounting 启动器。
- 用户明确要求列出本仓库可用工作域。

以下场景不要触发：

- 普通 PRD 编写、截图分析、竞品分析、产品讨论。
- 用户只提到 `app`、`mobile`、`screen`、`实现`、`页面`、`记账` 等泛化词。
- 用户没有明确要求进入某个工作域。

## 启动流程

当用户输入 `/start` 时，优先使用 AskQuestion 工具弹出选项并等待用户选择，不要直接进入任何域。

如果 AskQuestion 工具不可用，退回为文本列出选项并等待用户回复域名。

当前可选域：

| 域 | 状态 | 入口 |
| --- | --- | --- |
| Product | available | 项目级 PRD、设计稿、需求/设计索引、一致性审查 |
| Android | available | `AndroidAccounting/` |
| iOS | planned | 暂无目录 |
| Backend | planned | 暂无目录 |
| Other | available | 普通聊天、调研、非结构化文档等 |

推荐 AskQuestion 工具格式：

- 问题：请选择工作域
- 固定选项：`Product`、`Android`、`iOS`、`Backend`、`Other`
- 说明：如果用户要进行项目级 PRD、设计稿、需求索引、设计索引或产品一致性审查，固定展示并选择 `Product`，将其按 `Product` 域处理。

## 域选择规则

### Product

当用户选择 `Product`、`product`、`产品`，或明确说"进入 Product 域 / 产品域"时：

1. 进入项目级产品工作语境。
2. 读取根级 Cursor 规则（`.cursor/rules/accounting-root.mdc`）。
3. 不读取 Android、iOS、Backend 子目录规则。
4. 使用 `.cursor/skills/product-workflow/SKILL.md`。
5. 处理项目级 PRD、设计稿、需求索引、设计索引、一致性审查和端侧影响摘要。
6. 默认由单个 Agent 连续处理；仅在用户明确要求审查、需求/设计规模较大、修改核心流程、存在多版本冲突风险，或准备端侧交接时，才启动额外审查 Agent（通过 Task 工具）。
7. Product 域不得直接更新 Android、iOS 或 Backend 技术方案；端侧技术方案更新必须切换到对应平台域。

### Android

当用户选择 `Android`、`android`、`安卓`，或明确说"进入 Android 域"时：

1. 切换到 Android 开发语境。
2. 读取 `.cursor/rules/android-development.mdc`。
3. 根据任务类型选择 `.cursor/skills/android-*/SKILL.md` 中的项目本地技能。
4. 只有当任务属于 Android feature delivery 时，才使用 `android-feature-workflow`。
5. 后续 Android 技术计划、模块计划、进度、Bug 记录和必要代码注释默认使用中文。

### iOS

当前未配置。用户选择 iOS 时，说明该域尚未建立目录和规则文件，并询问是否需要创建 iOS 域结构。

### Backend

当前未配置。用户选择 Backend 时，说明该域尚未建立目录和规则文件，并询问是否需要创建 Backend 域结构。

### Other

当用户选择 `Other`、`other`、`其他`，或明确说"进入其他域"时：

1. 进入通用协作语境。
2. 不读取任何平台子目录规则。
3. 不加载平台专用 skills。
4. 按用户当前问题直接回答或协作；可处理普通聊天、调研、非结构化文档等非平台开发工作。
5. 如果用户后续明确要求进入 Product、Android、iOS 或 Backend，再切换到对应域。

## 退出与切换

如果用户在已选择域后输入 `/start`，重新展示域列表并等待选择。

如果用户明确要求切换域，停止沿用当前域的专用规则，重新按新域入口加载规则。
