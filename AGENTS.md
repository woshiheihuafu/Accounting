# AGENTS.md

## Scope

This repository may contain multiple clients and services, such as Android, iOS, and backend code.

Keep this root file thin. Put domain-specific instructions in the nearest domain directory or repository-local skill:

- Android: `AndroidAccounting/AGENTS.md`
- iOS: add an `AGENTS.md` under the future iOS directory.
- Backend: add an `AGENTS.md` under the future backend directory.

Codex reads instruction files from the repository root down to the current working directory.

## Work Domain Launcher

When the user inputs `/start`, use the repository-local `accounting-start` skill under `.codex/skills/accounting-start/SKILL.md` to list available work domains and wait for the user to choose one.

Currently configured domains:

- Android: `AndroidAccounting/`
- iOS: planned, not configured yet.
- Backend: planned, not configured yet.
- Other: non-platform development work, ordinary conversation, PRD, documentation, research, and product analysis.

Do not automatically enter platform-specific workflows from broad words such as "app", "mobile", "screen", "implementation", feature names, or product-domain terms. Follow `accounting-start` for domain selection and domain-specific routing.

## Repository Principles

- Prefer simple, maintainable solutions over complex abstractions.
- Follow the existing architecture and code style in the area being changed.
- Do not perform unrelated refactors.
- Every change should have a clear purpose and value.
- Read the relevant implementation before modifying code.
- Keep platform-specific conventions out of this root file unless they apply to every part of the repository.
