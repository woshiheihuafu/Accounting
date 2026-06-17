---
name: android-feature-workflow
description: Manage Android feature delivery from PRD to implementation with Chinese overall technical plans, per-module technical plans, approval gates, progress updates, module-local bug records, and default multi-agent coordination. Use when the user asks to generate an Android technical plan from a PRD, start or continue a module, approve a module plan, develop an Android module, fix Android feature bugs, update requirement progress, or resume Android work across days.
---

# Android Feature Workflow

## Purpose

Use this skill as the orchestration layer for Android feature work. Keep implementation details in the specialized Android skills:

- `android-module-architecture`
- `android-create-compose-screen`
- `android-create-viewmodel`
- `android-create-repository`
- `android-network-request`

Always report which rules, documents, skills, and agents were used.

Write technical plans, progress records, approval records, bug records, and necessary code comments in Chinese by default. Keep code identifiers, package names, class names, function names, resource keys, and file names in English unless Android conventions require otherwise.

This skill is the source of truth for Android feature workflow state, approval gates, feature document roles, progress table markers, bug record format, and default agent assignment. `AGENTS.md` files route tasks and define durable engineering rules. Specialized Android skills provide execution checklists only and should not duplicate workflow state, approval, or agent configuration.

## Project-Local Skill Priority

For this repository, prefer project-local skills under:

```text
.codex/skills/android-*/SKILL.md
```

If a global skill conflicts with a project-local skill, follow the project-local skill.

## Default Documents

Store Android requirement documents under:

```text
AndroidAccounting/docs/requirements/<feature-name>/overall-tech-plan.md
AndroidAccounting/docs/requirements/<feature-name>/modules/<module-name>-tech-plan.md
```

If the user provides another location, use that location while keeping the same document roles.

## Status Model

Use these stable status values:

```text
not_started
technical_plan_created
in_progress
completed
completed_with_issues
```

Use these human-readable markers in progress tables:

| Marker | Status | Meaning |
| --- | --- | --- |
| ⏳ | not_started | 未开发 |
| 📝 | technical_plan_created | 已生成技术方案但未开发 |
| 🚧 | in_progress | 开发中 |
| ✅ | completed | 开发完成且无已知未解决问题 |
| ⚠️ | completed_with_issues | 开发完成但存在遗留问题 |

## Approval Model

Use these approval values for module technical plans:

```text
pending
approved_by_user
skipped_by_user
not_required
```

Default behavior:

1. Generate the module technical plan.
2. Set module status to `technical_plan_created`.
3. Set approval to `pending`.
4. Stop before coding and ask for approval.

Skip approval only when the user explicitly says to skip approval or directly start development after generating the plan.

## Default Agents

Use multi-agent coordination by default for Android feature workflow tasks when sub-agents are available.

Model assignment:

```text
Workflow Agent: gpt-5.5
Architecture Agent: gpt-5.5
Implementation Agent: gpt-5.3-codex
Review / QA Agent: gpt-5.5
Fallback / Bulk Coding Agent: gpt-5.4
```

Fallback rules:

- If `gpt-5.5` is unavailable, use `gpt-5.4`.
- If `gpt-5.3-codex` is unavailable, use `gpt-5.4`.
- If sub-agents are unavailable, proceed as a single agent and explicitly report the fallback.

Agent responsibilities:

- Workflow Agent: read PRD, create and maintain plans, update statuses, record approvals, and coordinate progress.
- Architecture Agent: review module boundaries, dependency direction, Gradle/module structure, and technical risks.
- Implementation Agent: implement approved module code using specialized Android implementation skills.
- Review / QA Agent: review code and documents for consistency with `AGENTS.md`, module plans, status updates, and tests.
- Fallback / Bulk Coding Agent: handle low-risk repetitive code when useful.

## Scenario A: Generate Overall Technical Plan

Use when the user asks to generate an Android technical plan from a PRD.

Steps:

1. Read root `AGENTS.md`.
2. Read `AndroidAccounting/AGENTS.md`, even when current working directory is the repository root.
3. Read this project-local workflow skill.
4. Read the PRD and referenced product/design docs that are available.
5. Use Workflow Agent with `gpt-5.5` for the plan when sub-agents are available.
6. Use Architecture Agent with `gpt-5.5` to review technical stack, module split, dependencies, risks, and development order when sub-agents are available.
7. Create or update `overall-tech-plan.md`.
8. Set overall feature status to `technical_plan_created`.
9. Initialize every module status as `not_started`.
10. Include a change log entry.
11. Do not write production code in this scenario.

Overall technical plan sections should be concise and stable:

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
## 本次生成记录
## 变更记录
```

Do not add bug indexes, unresolved issue summaries, legacy issue summaries, or long-term skill/agent configuration sections to the overall technical plan.

The Android project snapshot is a point-in-time snapshot. Do not update it after every code change. Update it only when core architecture, module structure, core dependencies, or project layout changes materially.

## Scenario B: Generate Module Technical Plan

Use when the user asks to start or plan a specific module.

Steps:

1. Read root `AGENTS.md`.
2. Read `AndroidAccounting/AGENTS.md`.
3. Read `overall-tech-plan.md`.
4. Locate the target module and confirm scope.
5. Use Architecture Agent for module boundary review when relevant and available.
6. Create or update `modules/<module-name>-tech-plan.md`.
7. Set module status to `technical_plan_created`.
8. Set approval to `pending`, unless the user explicitly skips approval.
9. Update the progress table in `overall-tech-plan.md`.
10. Stop before implementation when approval is pending.

Module technical plan sections:

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
## 审批状态
## 进度记录
## Bug 与遗留问题
## 开发记录
```

Detailed bug and legacy issue records belong in the related module technical plan.

## Scenario C: Develop Approved Module

Use when the user approves a module plan or asks to skip approval and develop.

Steps:

1. Read root `AGENTS.md`, `AndroidAccounting/AGENTS.md`, `overall-tech-plan.md`, and the module technical plan.
2. Verify approval is `approved_by_user`, `skipped_by_user`, or `not_required`.
3. If approval is still `pending`, stop and ask for approval unless the user explicitly says to continue.
4. Set module status to `in_progress` in both the module and overall plan.
5. Choose specialized skills based on the module plan:
   - module boundaries: `android-module-architecture`
   - Compose UI: `android-create-compose-screen`
   - ViewModel: `android-create-viewmodel`
   - Repository/data flow: `android-create-repository`
   - HTTP/API: `android-network-request`
6. Use Implementation Agent with `gpt-5.3-codex` for implementation when sub-agents are available and the work can be safely delegated.
7. Use Review / QA Agent with `gpt-5.5` for review when sub-agents are available.
8. Run the narrowest available validation commands.
9. Update the module development log.
10. Set module status to `completed` or `completed_with_issues`.
11. Update `overall-tech-plan.md` progress only; do not add detailed bug records to the overall plan.

## Scenario D: Record Or Fix Bug

Use when the user reports a bug or Codex finds a bug during work.

Steps:

1. Read root `AGENTS.md`, `AndroidAccounting/AGENTS.md`, `overall-tech-plan.md`, and the related module technical plan.
2. Add a bug entry to the module technical plan before fixing.
3. If the module was `completed`, change it to `completed_with_issues`.
4. Set bug status to `open`.
5. Fix the bug.
6. Run focused validation.
7. Update the bug status to `resolved`, `in_progress`, or keep `open`.
8. If all known module issues are resolved, set module status to `completed`; otherwise keep `completed_with_issues`.
9. Update `overall-tech-plan.md` progress only.

Bug format in module technical plans:

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

## Scenario E: Resume Work

Use when the user returns after interruption or asks to continue a module.

Steps:

1. Read root `AGENTS.md`.
2. Read `AndroidAccounting/AGENTS.md`.
3. Read `overall-tech-plan.md`.
4. Read the target module technical plan.
5. Report current status, approval state, open module bugs, and the next recommended action.
6. Continue according to the current state:
   - `not_started`: generate module technical plan.
   - `technical_plan_created` with `pending`: ask for approval.
   - `technical_plan_created` with approved or skipped approval: begin development.
   - `in_progress`: continue implementation.
   - `completed`: do not modify unless the user asks for change or bug fix.
   - `completed_with_issues`: work on open module issues.

## Required Reporting

At the start of each workflow task, report:

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

At completion, report:

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
