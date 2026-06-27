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

- The active goal is now practical feature and behavior parity with the Node.js
  `miku-md2pptx` main application.
- Initial creation of `miku-md2pptx-java` is implemented locally.
- The repository now has Maven runtime/CLI skeleton, Java core conversion,
  focused tests, README, miku-soft reference docs, upstream mapping docs, and
  AI state files.
- Java parser parity now includes upstream-style GFM list, blockquote, and
  thematic break slide blocks, common inline marker stripping, and multiline
  speaker notes comments.
- Java parser parity now also handles setext-style headings and indented list
  continuation lines, matching additional upstream markdown behaviors.
- Link/image URL parsing now preserves destinations with title/quote variants
  before hyperlink and image handling.
- CLI parser behavior now has explicit tests for unknown options and missing
  required values/arguments.

## Next Action

- Continue parser parity beyond the current line-based implementation, then add
  broader fixture or golden checks against upstream behavior.
- Run the focused parser and CLI tests now expanded in `docs/upstream-test-mapping.md`
  and continue parity work for remaining complex nested markdown cases.
- Perform manual PowerPoint or LibreOffice repair-free checks listed in
  `docs/remaining-migration-items.md`.
- Perform manual PowerPoint or LibreOffice repair-free checks listed in
  `docs/remaining-migration-items.md`.

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

- `mvn test`: success, 21 tests passed after adding CLI failure-path coverage.
- `mvn package`: success, 21 tests passed, jar and dist zip created.
- `java -jar target/miku-md2pptx-java-0.2.2.jar --version`: printed `0.2.2`.
- `java -jar target/miku-md2pptx-java-0.2.2.jar src/test/resources/fixtures/smoke.md --out target/smoke.pptx --title Smoke`: success.
- `jar tf target/smoke.pptx`: confirmed presentation, slide, notes, theme, and relationship parts.
