---
purpose: ai-agent-decisions
read_when:
  - before_starting_work
  - when_making_decision
  - when_looping_or_repeating_work
update_when:
  - important_decision_is_made
  - option_is_rejected
  - work_is_deferred
---

# Decisions

This file records important decisions for the AI agent.
Read this before making or revisiting decisions, especially when the work seems to loop.

## 2026-06-27: Java Companion Scope

Reason:
The user clarified that they want a Java version of `miku-md2pptx`, with
`miku-md2xlsx-java` and `miku-md2docx-java` as sister applications. The
`igapyon-miku-soft-developer` Java straight-conversion workflow therefore
applies.

Impact:
Use a single-module Maven Java 1.8 runtime/CLI repository by default, following
the sister Java repositories. Maven plugin support remains out of initial scope.

## 2026-06-27: Upstream Reference

Reason:
Local `/Users/igapyon/Documents/git/miku-md2pptx` exists and matches remote
`devel` commit `760bf08631fcccd6962862f5b1f8d6bce935cc83`. It is the best
available compatibility anchor for this initial conversion.

Impact:
Mapping docs and tests should refer to that upstream state unless the human
selects a different branch, tag, or commit.

## 2026-06-27: Initial Implementation Shape

Reason:
`miku-md2docx-java` is closest for OOXML package/CLI structure, while
`miku-md2xlsx-java` has the newer `docs/miku-soft-reference.md` convention.

Impact:
Adopt the Maven/CLI/runtime shape from the sister Java repositories, but keep
shared miku-soft reference files out of this repository and link to the skill
through `docs/miku-soft-reference.md`.
