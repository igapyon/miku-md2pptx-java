---
purpose: ai-agent-goal
read_when:
  - before_starting_work
  - before_finishing_work
  - when_scope_is_unclear
update_when:
  - goal_changes
  - done_conditions_change
  - stop_conditions_change
---

# Goal

This file defines what the AI agent is trying to accomplish.
Read this before starting work, before deciding that work is complete, and whenever scope becomes unclear.

## Objective

Create the initial Java companion version of `miku-md2pptx` in this repository,
following `igapyon-miku-soft-developer` Java straight-conversion guidance.

The target repository is `miku-md2pptx-java`. The upstream source is the local
and GitHub `miku-md2pptx` main application. Same-layer sister references are
`miku-md2xlsx-java` and `miku-md2docx-java`.

## Done

- The repository has the expected miku-soft Java companion skeleton:
  Maven build, Java 1.8 settings, CLI entrypoint, core package, docs,
  `README.md`, `TODO.md`, `DECISIONS.md`, `HANDOFF.md`, `.gitignore`,
  `.mvn/jvm.config`, and `workplace/.gitkeep`.
- The Java runtime can convert a small Markdown file to a `.pptx` package.
- CLI behavior covers at least `--help`, `--version`, `<input.md> --out <output.pptx>`,
  and optional `--title`.
- Focused tests cover slide splitting, PPTX package entries, tables, links,
  images or skipped-image diagnostics, speaker notes where implemented, and CLI basics.
- `mvn test` passes, or any verification blocker is recorded in `TODO.md` and
  summarized in `HANDOFF.md`.
- Final `git status --short` and diff are reviewed for unintended changes.

## Stop

- Upstream `miku-md2pptx` behavior needed for compatibility is unclear enough
  that implementation would become guesswork.
- Sister reference differences conflict and require a human decision.
- Build or test dependency resolution repeatedly fails for the same underlying
  cause.
- `TODO.md` の `Retry Log` に同じ原因の失敗が3回記録された。
