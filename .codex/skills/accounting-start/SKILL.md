---
name: accounting-start
description: Accounting 仓库启动器。仅当用户明确输入 /start，或明确要求使用 Accounting 启动器选择工作域时使用；不要因为普通 PRD、产品分析、截图分析、需求讨论、app、mobile、实现等词自动触发。本 skill 用于列出可选工作域并在用户选择后路由到对应流程，目前包含 Android、iOS、Backend、Other。
---

# Accounting 启动器

## 目标

作为 `Accounting` 仓库的显式入口，先让用户选择工作域，再进入对应域的规则、技能和工作目录。

不要在用户未选择域之前读取平台子目录的 `AGENTS.md` 或加载平台专用技能。

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

当用户输入 `/start` 时，优先使用可用的交互选择工具弹出选项并等待用户选择，不要直接进入任何域。

如果当前 Codex 模式没有可用的交互选择工具，再退回为文本列出选项并等待用户回复域名。

当前可选域：

| 域 | 状态 | 入口 |
| --- | --- | --- |
| Android | available | `AndroidAccounting/` |
| iOS | planned | 暂无目录 |
| Backend | planned | 暂无目录 |
| Other | available | 非平台开发工作、普通聊天、PRD、文档、调研等 |

推荐回复格式：

```text
请选择工作域：
- Android：进入 AndroidAccounting，读取 AndroidAccounting/AGENTS.md，并按 Android 本地 skills 工作。
- iOS：暂未配置。
- Backend：暂未配置。
- Other：普通聊天、PRD、文档、调研、产品分析等非平台开发工作。
```

推荐工具弹窗格式：

- 问题：请选择工作域
- 固定选项：`Android`、`iOS`、`Backend`、`Other`
- 说明：如果用户要进行普通聊天、PRD、文档、调研、产品分析等非平台开发工作，固定展示并选择 `Other`，将其按 `Other` 域处理。

## 域选择规则

### Android

当用户选择 `Android`、`android`、`安卓`，或明确说“进入 Android 域”时：

1. 切换到 Android 开发语境。
2. 读取 `AndroidAccounting/AGENTS.md`。
3. 根据任务类型选择 `.codex/skills/android-*/SKILL.md` 中的项目本地技能。
4. 只有当任务属于 Android feature delivery 时，才使用 `android-feature-workflow`。
5. 后续 Android 技术计划、模块计划、进度、Bug 记录和必要代码注释默认使用中文。

### iOS

当前未配置。用户选择 iOS 时，说明该域尚未建立目录和规则文件，并询问是否需要创建 iOS 域结构。

### Backend

当前未配置。用户选择 Backend 时，说明该域尚未建立目录和规则文件，并询问是否需要创建 Backend 域结构。

### Other

当用户选择 `Other`、`other`、`其他`，或明确说“进入其他域”时：

1. 进入通用协作语境。
2. 不读取任何平台子目录 `AGENTS.md`。
3. 不加载平台专用 skills。
4. 按用户当前问题直接回答或协作；可处理普通聊天、PRD、截图分析、文档、调研、产品分析等非平台开发工作。
5. 如果用户后续明确要求进入 Android、iOS 或 Backend，再切换到对应域。

## 退出与切换

如果用户在已选择域后输入 `/start`，重新展示域列表并等待选择。

如果用户明确要求切换域，停止沿用当前域的专用规则，重新按新域入口加载规则。
