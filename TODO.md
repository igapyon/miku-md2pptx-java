---
purpose: ai-agent-todo
read_when:
  - before_starting_work
  - during_work
  - before_finishing_work
update_when:
  - task_status_changes
  - new_task_is_found
  - blocker_is_found
  - repeated_failure_is_found
---

# Todo

This file tracks the active tasks, blockers, and repeated failures for the AI agent.
Use `HANDOFF.md` for compact resume notes for the next human or agent.

## AI Agent Current Tasks

This section tracks active work items for AI agents.
Update this section while working. Do not rewrite unrelated TODO items.

### Tasks

- [x] Confirm the work goal: create `miku-md2pptx-java` as the Java companion of `miku-md2pptx`.
- [x] Identify same-layer sister references: `/Users/igapyon/Documents/git/miku-md2xlsx-java` and `/Users/igapyon/Documents/git/miku-md2docx-java`.
- [x] Identify upstream reference: `/Users/igapyon/Documents/git/miku-md2pptx` at commit `760bf08631fcccd6962862f5b1f8d6bce935cc83`.
- [x] Start repository skeleton: `.gitignore`, `.mvn/jvm.config`, `pom.xml`, `src/assembly/dist.xml`, `workplace/.gitkeep`, and initial core Java model files.
- [x] Complete Java PPTX package builder and public `MikuMd2pptxCore` API.
- [x] Complete CLI parser and `MikuMd2pptxCli`.
- [x] Add focused JUnit tests and fixture(s).
- [x] Add README and miku-soft/upstream mapping docs.
- [x] Run `mvn test` and fix failures.
- [x] Review `git diff` and `git status --short`.
- [x] Retarget `GOAL.md` from initial companion creation to practical Node.js
  feature and behavior parity.
- [x] Add Java parser parity for upstream-style GFM list, blockquote, and
  thematic break slide blocks.
- [x] Add focused test mapping for the new parser parity coverage.
- [x] Add Java parser parity for common inline marker stripping and multiline
  speaker notes comments.
- [x] Handle Markdown link and image URL title/quote variants (for quoted and
  angled URLs).
- [x] Add CLI parser failure-path coverage for unknown options and missing required
  arguments.
- [x] Add parser parity for setext headings and indented list continuation lines.

### Blockers

- None at the time of writing.

### Retry Log

Use this section only when the same task or error is repeated.
If the same failure appears 3 times, stop and ask the user.

- None.

## Project Follow-up

- Continue improving Java Markdown parsing parity with upstream `remark-gfm`
  beyond the current block, inline marker, and multiline speaker notes coverage.
- Add reverse compatibility checks through `miku-pptx2md` where practical.
- Perform manual PowerPoint or LibreOffice repair-free opening checks on
  representative generated decks.
