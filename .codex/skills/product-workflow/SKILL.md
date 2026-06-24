---
name: product-workflow
description: Accounting 项目级产品工作流，用于创建或更新 PRD、设计稿、需求索引、设计索引、一致性审查和端侧影响摘要。适用于处理产品域需求、PRD 变更、.pen 设计稿、设计导出图、页面/流程定义，或交接给 Android、iOS、Backend 技术方案的材料。
---

# 产品工作流

## 用途

在任何端级技术方案或实现开始前，使用本 skill 处理项目级产品工作。产品资产归仓库级管理，不归 Android、iOS 或 Backend 单独拥有。

默认由单个产品工作流 Agent 使用 `gpt-5.5` 处理。不要默认使用多 Agent。仅当用户明确要求审查、产品变更规模较大、变更修改已有核心流程、多个 PRD/设计版本可能冲突，或产品变更准备交接给 Android、iOS、Backend 时，才使用 `gpt-5.5` 的产品审查 Agent。

## 边界

- 产品工作流负责 PRD、设计稿、需求索引、设计索引、一致性检查和端侧影响摘要。
- 产品工作流可以为了理解影响而查看端级文档，但不得直接更新 Android、iOS 或 Backend 技术方案。
- 端级工作流负责整体技术方案、模块方案、实现任务、代码、测试和端级进度。
- 对 `.pen` 设计稿，使用 Pencil MCP 检查节点、截图、布局、导出、变量和设计结构。
- 仅在必须生成或编辑位图素材时使用 `imagegen`，普通 PRD 编写或 `.pen` 结构工作不要使用它。
- 竞品截图、参考图和 OCR 结果只能作为参考输入，不能覆盖 PRD，也不能直接变成当前项目功能。

## 参考资料

- 创建、检查、更新、导出或对齐移动端设计稿、`.pen` 文件、截图或设计图片时，必须读取 `references/mobile-design-guidelines.md`。

## 默认项目资产

除非用户指定其他仓库级位置，否则产品级资产放在 `prd/` 下。

推荐文件：

```text
prd/requirements/README.md
prd/designs/README.md
prd/changes/README.md
prd/impact/<change-id>-platform-impact.md
```

`prd/` 下现有扁平文件仍然有效。引入索引时，优先链接现有文件，不要移动它们，除非用户明确要求重组目录。

## PRD 命名规则

新建 PRD 默认使用以下文件名：

```text
prd/<feature-slug>-prd-<YYYYMMDD>.md
```

规则：

- `<feature-slug>` 使用英文小写 kebab-case，表达产品或功能名，例如 `code-accounting`。
- `<YYYYMMDD>` 使用 PRD 创建日期，不使用相对日期，不使用 `v1`、`一期` 或 `MVP` 作为主要区分。
- 同一天同一功能确实需要多个独立 PRD 时，在日期后追加 `-v02`、`-v03`，例如 `prd/code-accounting-prd-20260617-v02.md`。
- 追加小范围变更时，优先更新当前 PRD 的变更记录；当变更会改变核心流程、范围边界、验收标准或形成新阶段时，创建新的日期后缀 PRD。
- 新 PRD 替代或更新旧 PRD 时，不删除旧文件；在新旧 PRD 的变更记录或关联关系中写明 `更新`、`替代`、`被替代`。
- 设计稿文件名应尽量与对应 PRD 使用同一 `<feature-slug>`，例如 `prd/designs/code-accounting-v2.pen`；如设计稿也需要保留多版本，可追加同样的日期后缀。

## PRD 索引规则

`prd/requirements/README.md` 是 PRD 的唯一索引入口。创建、更新、替代或废弃 PRD 时必须同步更新该文件。

索引必须包含：

```text
PRD ID | 标题 | 文件 | 创建日期 | 状态 | 范围摘要 | 关联需求 | 关联设计 | 关系 | 备注
REQ ID | 需求摘要 | 当前来源 PRD | 状态 | 备注
```

规则：

- 每个 PRD 使用稳定 `PRD-<AREA>-<YYYYMMDD>` ID；同一天同一区域多个 PRD 时追加 `-V02`、`-V03`。
- 每个 `REQ-*` 在索引中只能有一个当前来源 PRD；如果新 PRD 改写该需求，必须更新旧 PRD 状态或在关系中写明替代/冲突。
- 同一产品或功能可保留多个历史 PRD，但同一范围内只能有一个当前有效 PRD。
- 新增 PRD 前，必须检查索引中是否已有相同范围、相同页面、相同流程或相同 `REQ-*`。
- 如果发现重复范围但用户确实要新建 PRD，应将旧 PRD 标为 `superseded` 或在关系中标记 `冲突`，不得让两个 PRD 同时作为当前有效来源。
- 如果索引不存在，首次创建或整理 PRD 时应创建 `prd/requirements/README.md`，并把已有 PRD 纳入索引。

## ID 与状态

使用稳定 ID，让 PRD、设计稿、技术方案和测试都能引用同一组产品事实。

```text
REQ-<AREA>-NNN
DESIGN-<AREA>-NNN
SCREEN-<AREA>-NNN
FLOW-<AREA>-NNN
CHANGE-<AREA>-NNN
```

使用简洁状态：

```text
draft
reviewed
approved
implemented
deprecated
superseded
```

每个需求或设计变更都应记录相关关系：

```text
关联：
更新：
替代：
被替代：
冲突：
```

如果已经检查但没有相关关系，显式填写 `none`。

## 场景 A：创建或更新 PRD

当用户要求创建、扩展、拆分、合并或修订 PRD 时使用。

步骤：

1. 读取根 `AGENTS.md`。
2. 读取 `prd/` 下已有 PRD、`prd/requirements/README.md`、设计索引和相关变更记录；如果索引不存在，本次应创建。
3. 判断请求属于新增需求、扩展、修改、替换还是废弃。
4. 分配或保留稳定的 `REQ-*` ID。
5. 根据 `prd/requirements/README.md` 检查范围重复、`REQ-*` 重复、规则冲突、验收标准缺失，以及与已有设计资产的重叠。
6. 按“PRD 命名规则”选择文件名：新阶段或核心范围变化创建新的日期后缀 PRD；小范围变更更新当前 PRD。
7. 创建或更新 PRD，并同步更新 `prd/requirements/README.md` 的 PRD 列表、需求覆盖索引和关系字段。
8. 本场景不得更新端级技术方案。
9. 输出是否使用产品审查 Agent，或说明已按规则跳过。

PRD 应包含：

```text
# <Feature Name> PRD

## 背景与目标
## 范围
## 非目标
## 需求列表
## 页面与交互
## 数据与规则
## 验收标准
## 关联设计
## 端侧影响摘要
## 变更记录
```

## 场景 B：创建或更新设计稿

当用户要求创建、检查、更新、导出或对齐设计稿、`.pen` 文件、截图或设计图片时使用。

步骤：

1. 读取根 `AGENTS.md`。
2. 读取 `references/mobile-design-guidelines.md`。
3. 读取关联 PRD、需求索引和设计索引。
4. 将每个设计资产绑定到 `REQ-*`、`SCREEN-*` 和 `FLOW-*` ID。
5. 使用 Pencil MCP 检查 `.pen`、截图、布局快照、导出、变量和节点。
6. 清洗竞品截图和 OCR 误识别内容：图标不得大面积是 `text` 节点；可复用 UI 应使用 reusable component；OCR 误识别的图标必须替换为图标节点、path、icon font 或语义相近组件。
7. 检查并移除 PRD 未声明的登录提示、账号提示、营销提示、引流入口或其他竞品内容；如无法判断，记录为待确认问题。
8. 检查必需页面、主流程、空态、错误态、加载态和关键交互的覆盖情况。
9. 当索引已存在或本次引入索引时，更新 `prd/designs/README.md`。
10. 本场景不得更新端级技术方案。
11. 在端级交接前输出未解决的产品/设计问题。

设计索引行应记录：

```text
设计 ID | 资产 | 关联 REQ | 页面 | 流程 | 状态 | 备注
```

## 场景 C：产品一致性审查

当用户要求审查 PRD/设计一致性，或重大产品变更准备交给端级工作流前使用。

步骤：

1. 读取相关 PRD、设计资产、需求索引、设计索引和变更记录。
2. 如果审查移动端设计稿、`.pen`、截图或设计图片，读取 `references/mobile-design-guidelines.md`。
3. 验证每个已实现或拟新增的设计页面都能映射到需求。
4. 验证每个面向用户的需求都有设计覆盖，或有明确的无需设计原因。
5. 检查重复需求、冲突业务规则、重复页面、缺失状态、不清晰的验收标准、文字冒充图标，以及 PRD 未声明的登录、账号、营销等竞品内容。
6. 将发现记录为产品级问题，不记录为端级实现 bug。
7. 如果使用产品审查 Agent，必须明确报告。

审查输出应优先列出阻塞问题，然后列出非阻塞缺口和建议的索引更新。

## 场景 D：端级交接

当产品变更准备交给 Android、iOS 或 Backend 技术方案时使用。

步骤：

1. 读取已批准或已审查的 PRD/设计输入。
2. 当用户要求生成持久交接文件时，在 `prd/impact/` 下产出端侧影响摘要。
3. 识别受影响的平台和可能影响区域，但不决定端级模块边界。
4. 包含来源 PRD/设计链接、受影响的 `REQ-*`、受影响的 `SCREEN-*` 或 `FLOW-*`、未决问题和验收标准。
5. 告知用户下一步应使用哪个端级工作流。

影响摘要应包含：

```text
# <Change ID> 端侧影响摘要

## 来源
## 涉及需求
## 涉及设计
## Android 影响
## iOS 影响
## Backend 影响
## 不确定项
## 建议下一步
```

## 汇报要求

完成时报告：

```text
更新文档：
- ...

一致性检查：
- ...

端侧影响：
- ...

实际使用 Skills / MCP：
- ...

实际使用 Agents：
- 产品工作流 Agent: gpt-5.5
- 产品审查 Agent: used | skipped
```
