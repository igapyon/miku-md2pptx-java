---
purpose: ai-agent-handoff
read_when:
  - before_resuming_work
  - before_handing_off_work
  - when_context_is_missing
update_when:
  - work_is_paused
  - handoff_summary_changes
  - verification_status_changes
---

# Handoff

This file summarizes the current working state for the next human or AI agent.
Keep it concise. Do not use this as a full work log or a replacement for `TODO.md` and `DECISIONS.md`.

## Current State

- Initial creation of `miku-md2pptx-java` is implemented locally.
- The repository now has Maven runtime/CLI skeleton, Java core conversion,
  focused tests, README, miku-soft reference docs, upstream mapping docs, and
  AI state files.
- Current files are still untracked because this repository started from
  `LICENSE` only.

## Next Action

- Review final diff/status, then decide whether to commit.
- For product hardening, continue with parser parity and manual PowerPoint
  repair-free checks listed in `docs/remaining-migration-items.md`.

## Relevant Files

- `GOAL.md`: Current objective and completion criteria.
- `TODO.md`: Active task checklist and blockers.
- `DECISIONS.md`: Scope and reference decisions already made.
- `/Users/igapyon/Documents/git/miku-md2pptx`: Upstream Node/TypeScript source.
- `/Users/igapyon/Documents/git/miku-md2docx-java`: Closest OOXML Java sister reference.
- `/Users/igapyon/Documents/git/miku-md2xlsx-java`: Newer miku-soft reference/docs sister reference.
- `pom.xml`: Maven single-module runtime scaffold.
- `src/main/java/jp/igapyon/mikumd2pptx/core/`: Java core conversion and PPTX package builder.
- `src/main/java/jp/igapyon/mikumd2pptx/cli/`: Java CLI entrypoint and parser.
- `src/test/java/`: Focused JUnit core and CLI tests.
- `docs/remaining-migration-items.md`: Compatibility follow-up items.

## Watch Outs

- The current Java implementation is an initial companion runtime, not full
  `remark-gfm` parity.
- Preserve the miku-soft boundary: product semantics belong in Java core/API,
  not in docs or skill prose.
- Keep `workplace/` local-only except `workplace/.gitkeep`.
- Upstream Markdown parsing uses `remark-gfm`; any Java first-cut parser gaps
  should be documented explicitly.

## Last Verification

- `mvn package`: success, 11 tests passed, jar and dist zip created.
- `java -jar target/miku-md2pptx-java-0.2.2.jar --version`: printed `0.2.2`.
- `java -jar target/miku-md2pptx-java-0.2.2.jar src/test/resources/fixtures/smoke.md --out target/smoke.pptx --title Smoke`: success.
- `jar tf target/smoke.pptx`: confirmed presentation, slide, notes, theme, and relationship parts.
- `mvn test`: success, 11 tests passed.
