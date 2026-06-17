# AGENTS.md

## 作用范围

这个仓库可能包含多个客户端和服务端项目，例如 Android、iOS 和后端代码。

保持这个根目录文件简洁。将领域特定说明放在最近的领域目录或仓库本地 skill 中：

- Android：`AndroidAccounting/AGENTS.md`
- iOS：在未来的 iOS 目录下新增一个 `AGENTS.md`。
- Backend：在未来的后端目录下新增一个 `AGENTS.md`。
- Product：项目级 PRD、设计稿、需求/设计索引和端侧影响摘要，由仓库本地 `product-workflow` skill 管理。

Codex 会从仓库根目录开始，沿当前工作目录路径逐级读取说明文件。

## 仓库结构

当前目录结构按“单仓库、多子域”组织：

- 根目录 `Accounting/`：项目根目录，承载跨领域说明、共享配置、PRD 和本地 skill。
- `AndroidAccounting/`：Android 客户端子项目目录。
- `prd/`：产品需求、设计稿导出和研究资料目录。
- `.codex/`：仓库本地的 Codex skills 和辅助配置目录。

目录归属判断规则：

- 仓库级信息、跨领域约束和共享资料优先放在根目录。
- 平台实现代码放在对应平台子目录，例如 Android 代码放在 `AndroidAccounting/`。
- `AndroidAccounting/`、未来的 iOS 目录、未来的 Backend 目录，默认都视为这个项目结构下的子目录，除非有明确说明它们各自独立管理。

## 工作域启动器

当用户输入 `/start` 时，使用仓库本地的 `accounting-start` skill（位于 `.codex/skills/accounting-start/SKILL.md`）列出可用工作域，并等待用户选择。

当前已配置的工作域：

- Product：项目级 PRD、设计稿、需求/设计索引和产品一致性审查。
- Android：`AndroidAccounting/`
- iOS：已规划，但尚未配置。
- Backend：已规划，但尚未配置。
- Other：非平台开发工作、普通对话、文档、研究和产品分析。

不要因为 “app”、“mobile”、“screen”、“implementation”、功能名称或产品领域词汇等宽泛表述，就自动进入某个平台的专属工作流。应按照 `accounting-start` 的流程进行工作域选择和领域路由。

## 仓库原则

- 在 `Accounting` 项目相关对话、计划、总结、审查意见和文档说明中，默认使用中文回答；代码标识符、文件名、类名、命令和必要的技术关键字保持英文。
- 优先选择简单、易维护的方案，而不是复杂抽象。
- 遵循变更所在区域既有的架构和代码风格。
- 不要进行无关重构。
- 每一次修改都应有明确目的和实际价值。
- 修改代码前先阅读相关实现。
- 除非某些平台约定适用于整个仓库，否则不要把平台特定约定写进这个根文件。
