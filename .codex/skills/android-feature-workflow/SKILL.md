---
name: android-feature-workflow
description: 管理 Android 功能从 PRD/Product 交接到实现的交付流程，包括中文整体技术方案、模块技术方案、审批门禁、进度更新、模块内 bug 记录、Product 变更后的 Android 技术方案更新，以及默认多 Agent 协作。适用于从 PRD/设计稿/Product 影响摘要生成或更新 Android 技术方案、启动或继续模块、审批模块方案、开发 Android 模块、修复 Android 功能 bug、更新需求进度或跨天恢复工作。
---

# Android 功能工作流

## 用途

使用本 skill 作为 Android 功能工作的编排层。实现细节放在专门的 Android skills 中：

- `android-module-architecture`
- `android-create-compose-screen`
- `android-create-viewmodel`
- `android-create-repository`
- `android-network-request`

始终报告使用了哪些规则、文档、skills 和 agents。

技术方案、进度记录、审批记录、bug 记录和必要代码注释默认使用中文。代码标识符、package 名、class 名、function 名、resource key 和文件名保持英文，除非 Android 约定另有要求。

本 skill 是 Android 功能工作流状态、审批门禁、功能文档职责、进度表标记、bug 记录格式和默认 Agent 分工的事实来源。`AGENTS.md` 负责路由任务并定义长期工程规则。专门的 Android skills 只提供执行检查清单，不应重复定义 workflow 状态、审批或 Agent 配置。

Product PRD、设计稿、需求索引、设计索引和端侧影响摘要是项目级输入，由 `product-workflow` 管理。只有当用户要求 Android 技术方案或 Android 实现工作时，本 Android workflow 才消费这些输入。消费 Product 输入时，PRD 优先于竞品截图、参考图和 OCR 结果；设计输入包含 PRD 非目标内容时，必须在技术方案中标记为不实现或需确认。

## 项目本地 Skill 优先级

在本仓库中，优先使用以下项目本地 skills：

```text
.codex/skills/android-*/SKILL.md
```

如果全局 skill 与项目本地 skill 冲突，遵循项目本地 skill。

## 默认文档

Android 需求文档默认放在：

```text
AndroidAccounting/docs/requirements/<feature-name>/overall-tech-plan.md
AndroidAccounting/docs/requirements/<feature-name>/modules/<module-name>-tech-plan.md
```

如果用户提供其他位置，使用用户指定位置，但保持相同的文档职责。

每个功能只保留一个当前有效的 `overall-tech-plan.md`。后续 Product 变更必须在同一文件中通过变更记录和任务调整进行增量更新；不要为同一功能创建并行的整体技术方案。

## 状态模型

使用以下稳定状态值：

```text
not_started
technical_plan_created
in_progress
completed
completed_with_issues
```

进度表中使用以下可读标记：

| Marker | Status | Meaning |
| --- | --- | --- |
| ⏳ | not_started | 未开发 |
| 📝 | technical_plan_created | 已生成技术方案但未开发 |
| 🚧 | in_progress | 开发中 |
| ✅ | completed | 开发完成且无已知未解决问题 |
| ⚠️ | completed_with_issues | 开发完成但存在遗留问题 |

## 审批模型

模块技术方案使用以下审批值：

```text
pending
approved_by_user
skipped_by_user
not_required
```

默认行为：

1. Generate the module technical plan.
2. Set module status to `technical_plan_created`.
3. Set approval to `pending`.
4. Stop before coding and ask for approval.

只有当用户明确要求跳过审批，或生成方案后直接开始开发时，才跳过审批。

## 默认 Agents

当 sub-agents 可用时，Android 功能工作流任务默认使用多 Agent 协作。

模型分配：

```text
Workflow Agent: gpt-5.5
Architecture Agent: gpt-5.5
Implementation Agent: gpt-5.3-codex
Review / QA Agent: gpt-5.5
Fallback / Bulk Coding Agent: gpt-5.4
```

降级规则：

- 如果 `gpt-5.5` 不可用，使用 `gpt-5.4`。
- 如果 `gpt-5.3-codex` 不可用，使用 `gpt-5.4`。
- 如果 sub-agents 不可用，以单 Agent 继续，并明确报告降级。

Agent 职责：

- Workflow Agent：读取 PRD，创建和维护方案，更新状态，记录审批并协调进度。
- Architecture Agent：审查模块边界、依赖方向、Gradle/module 结构和技术风险。
- Implementation Agent：使用专门的 Android 实现 skills 开发已审批的模块代码。
- Review / QA Agent：审查代码和文档是否与 `AGENTS.md`、模块方案、状态更新和测试一致。
- Fallback / Bulk Coding Agent：在有价值时处理低风险重复代码。

## 场景 A：生成整体技术方案

当用户要求从 PRD 生成 Android 技术方案时使用。

步骤：

1. 读取根 `AGENTS.md`。
2. 读取 `AndroidAccounting/AGENTS.md`，即使当前工作目录是仓库根目录也要读取。
3. 读取本项目本地 workflow skill。
4. 读取可用的 PRD 和引用的产品/设计文档。
5. 检查 PRD 与设计稿、竞品截图、参考图或 OCR 结果是否冲突；发现 PRD 未声明的登录提示、账号提示、营销提示、引流入口或其他竞品内容时，记录为设计输入问题。
6. 当 sub-agents 可用时，使用 `gpt-5.5` 的 Workflow Agent 生成方案。
7. 当 sub-agents 可用时，使用 `gpt-5.5` 的 Architecture Agent 审查技术栈、模块拆分、依赖、风险和开发顺序。
8. 创建或更新 `overall-tech-plan.md`。
9. 将整体功能状态设为 `technical_plan_created`。
10. 将每个模块状态初始化为 `not_started`。
11. 包含一条变更记录。
12. 本场景不得编写生产代码。

仅在首次创建整体技术方案，或目标功能不存在有效 `overall-tech-plan.md` 时使用场景 A。如果整体方案已经存在，且用户提供新的 Product PRD/设计/影响输入，改用场景 F。

整体技术方案章节应简洁且稳定：

```text
# <Feature Name> Android 端整体技术方案

## 需求概述
## 来源文档
## 方案生成时 Android 工程快照
## 技术栈
## 架构原则
## 模块化策略
## 需求模块划分
## 模块依赖关系
## 开发顺序
## 进度记录
## 风险与假设
## 设计输入问题
## 本次生成记录
## 变更记录
```

不要把 bug 索引、未解决问题摘要、历史遗留问题摘要，或长期 skill/agent 配置章节加入整体技术方案。

Android 工程快照是某个时间点的快照。不要在每次代码变更后更新它；只有核心架构、模块结构、核心依赖或项目布局发生实质变化时才更新。

## 场景 B：生成模块技术方案

当用户要求启动或规划某个具体模块时使用。

步骤：

1. 读取根 `AGENTS.md`。
2. 读取 `AndroidAccounting/AGENTS.md`。
3. 读取 `overall-tech-plan.md`。
4. 定位目标模块并确认范围。
5. 检查模块相关设计输入是否包含 PRD 未声明内容；若包含，必须在模块方案中标记为不实现或需确认。
6. 当相关且可用时，使用 Architecture Agent 审查模块边界。
7. 创建或更新 `modules/<module-name>-tech-plan.md`。
8. 将模块状态设为 `technical_plan_created`。
9. 将审批状态设为 `pending`，除非用户明确跳过审批。
10. 更新 `overall-tech-plan.md` 中的进度表。
11. 当审批为 pending 时，在实现前停止。

模块技术方案章节：

```text
# <Module Name> 模块技术方案

## 模块目标
## 范围
## 非目标
## 依赖关系
## UI 方案
## ViewModel 方案
## Repository 方案
## Network 方案
## 模块架构方案
## 状态与副作用模型
## 文件变更计划
## 测试计划
## 设计输入问题
## 审批状态
## 进度记录
## Bug 与遗留问题
## 开发记录
```

详细 bug 和遗留问题记录放在对应模块技术方案中。

## 场景 C：开发已审批模块

当用户审批模块方案，或要求跳过审批并开发时使用。

步骤：

1. 读取根 `AGENTS.md`、`AndroidAccounting/AGENTS.md`、`overall-tech-plan.md` 和模块技术方案。
2. 验证审批状态为 `approved_by_user`、`skipped_by_user` 或 `not_required`。
3. 如果审批仍为 `pending`，停止并请求审批，除非用户明确要求继续。
4. 在模块方案和整体方案中都将模块状态设为 `in_progress`。
5. 根据模块方案选择专门 skills：
   - 模块边界：`android-module-architecture`
   - Compose UI：`android-create-compose-screen`
   - ViewModel：`android-create-viewmodel`
   - Repository/data flow：`android-create-repository`
   - HTTP/API：`android-network-request`
6. 当 sub-agents 可用且工作可以安全委派时，使用 `gpt-5.3-codex` 的 Implementation Agent 实现。
7. 当 sub-agents 可用时，使用 `gpt-5.5` 的 Review / QA Agent 审查。
8. 运行范围最窄的可用验证命令。
9. 更新模块开发记录。
10. 将模块状态设为 `completed` 或 `completed_with_issues`。
11. 只更新 `overall-tech-plan.md` 的进度；不要把详细 bug 记录加入整体方案。

## 场景 D：记录或修复 Bug

当用户报告 bug，或 Codex 在工作中发现 bug 时使用。

步骤：

1. 读取根 `AGENTS.md`、`AndroidAccounting/AGENTS.md`、`overall-tech-plan.md` 和相关模块技术方案。
2. 修复前先在模块技术方案中添加 bug 记录。
3. 如果模块原状态为 `completed`，改为 `completed_with_issues`。
4. 将 bug 状态设为 `open`。
5. 修复 bug。
6. 运行聚焦验证。
7. 将 bug 状态更新为 `resolved`、`in_progress`，或保持 `open`。
8. 如果模块所有已知问题都已解决，将模块状态设为 `completed`；否则保持 `completed_with_issues`。
9. 只更新 `overall-tech-plan.md` 的进度。

模块技术方案中的 bug 格式：

```text
### BUG-001：<标题>

- 来源：user_reported | agent_found
- 状态：open | in_progress | resolved | won't_fix
- 严重级别：low | medium | high | critical
- 描述：
- 位置：
- 解决方案：
- 创建时间：
- 更新时间：
```

## 场景 E：恢复工作

当用户在中断后返回，或要求继续某个模块时使用。

步骤：

1. 读取根 `AGENTS.md`。
2. 读取 `AndroidAccounting/AGENTS.md`。
3. 读取 `overall-tech-plan.md`。
4. 读取目标模块技术方案。
5. 报告当前状态、审批状态、未关闭模块 bug 和下一步建议。
6. 按当前状态继续：
   - `not_started`：生成模块技术方案。
   - `technical_plan_created` 且审批为 `pending`：请求审批。
   - `technical_plan_created` 且已审批或跳过审批：开始开发。
   - `in_progress`：继续实现。
   - `completed`：除非用户要求变更或修 bug，否则不修改。
   - `completed_with_issues`：处理未关闭模块问题。

## 场景 F：将 Product 变更应用到 Android 技术方案

当 Product 域 PRD、设计稿、需求索引、设计索引或端侧影响摘要发生变化，且用户要求更新 Android 技术方案或 Android 任务时使用。

步骤：

1. 读取根 `AGENTS.md`。
2. 读取 `AndroidAccounting/AGENTS.md`。
3. 读取本项目本地 workflow skill。
4. 读取 Product 输入：相关 PRD、设计资产或设计索引、需求索引、设计索引、变更记录，以及可用的端侧影响摘要。
5. 以 PRD 为优先级最高的产品事实，检查设计稿、竞品截图、参考图和 OCR 结果是否引入 PRD 非目标内容。
6. 读取当前 Android `overall-tech-plan.md`。
7. 读取 `modules/` 下已有的受影响模块技术方案。
8. 将 Product 变更归类为以下一种或多种：
   - 需要新增模块
   - 扩展已有模块
   - 修改已有模块行为
   - 废弃或替代任务
   - 仅 UI/设计变更
   - 仅文案/验收变更
   - 对 Android 无影响
9. 更新同一个 `overall-tech-plan.md`；不要创建第二份整体技术方案。
10. 追加变更记录，并在可用时引用来源 Product 文档和受影响的 `REQ-*`、`DESIGN-*`、`SCREEN-*` 或 `FLOW-*` ID。
11. 对 PRD 未声明的登录提示、账号提示、营销提示、引流入口或其他竞品内容，明确标记为不实现；无法判断时标记为需确认。
12. 为新增模块在进度表新增任务，状态为 `not_started`。
13. 对受影响的已有模块，更新或创建相关模块技术方案，并将审批状态设为 `pending`，除非用户明确跳过审批。
14. 保留已有模块状态。不要因为 Product 变更而重新初始化 `completed`、`completed_with_issues`、`in_progress` 或已审批模块状态。
15. 如果已完成模块需要后续工作，新增变更任务；只有存在未解决实现问题时，才将模块标记为 `completed_with_issues`。
16. 本场景不得编写生产代码。

场景 F 可更新以下整体方案章节：

```text
## 需求概述
## 来源文档
## 需求模块划分
## 模块依赖关系
## 开发顺序
## 进度记录
## 风险与假设
## 设计输入问题
## 变更记录
```

除非 Product 变更实质影响核心架构、模块结构、核心依赖或项目布局，否则场景 F 不得更新 Android 工程快照。

## 必需汇报

每个 workflow 任务开始时报告：

```text
已读取规则/文档：
- ...

计划使用 Skills：
- ...

计划使用 Agents：
- Workflow Agent: <model>
- Architecture Agent: <model>
- Implementation Agent: <model>
- Review / QA Agent: <model>
```

完成时报告：

```text
更新文档：
- ...

状态变化：
- ...

实际使用 Skills：
- ...

实际使用 Agents：
- ...

验证：
- ...
```
