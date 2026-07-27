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

## 2026-06-28: Incremental Parser Parity

Reason:
The current goal is practical Node.js feature and behavior parity, but replacing
the Java parser wholesale would be larger than the next useful compatibility
step. Upstream `slide-model.ts` has clear behavior for list blocks,
blockquotes, and thematic breaks that can be matched within the existing Java
1.8 line-based parser.

Impact:
Extend `MarkdownSlides` incrementally for concrete upstream slide-model
behaviors such as block normalization, inline marker text normalization, and
speaker notes handling. Add focused tests and mapping docs for each closed gap,
and keep the remaining `remark-gfm` parity gaps documented until a broader
parser change is justified.

## 2026-06-28: Runtime Release Shape Follows Sister Repositories

Reason:
The user noted that runtime work is still incomplete. The closest same-layer
references, `miku-md2docx-java` and `miku-md2xlsx-java`, already define a
GitHub Release CLI runtime workflow pattern for executable jar and sources jar
assets.

Impact:
Add `release-cli-runtime.yml` to `miku-md2pptx-java` using the sister workflow
shape: build with Maven, validate `v*` tag compatibility with `pom.xml`, verify
the jar under Java 8 using `--version`, and upload jar assets to the matching
GitHub Release. Keep the local Maven assembly dist zip as a package output, not
necessarily as a GitHub Release asset unless a later decision changes that.

## 2026-07-18: Follow Upstream 0.6.0 as One Compatibility Unit

Reason:
Upstream template support depends on ZIP reading, template design-part copying,
placeholder-aware slide generation, and the Office Core 0.6.0 XML/ZIP behavior.
The later CLI contract and valid OOXML layout fix are part of the same upstream
compatibility boundary.

Impact:
Track upstream commit `0fa0b5a2150793453690cead799a66a5e7960693` and version
`0.6.0`. Port template behavior into the Java core and thin CLI adapter, update
the vendored Office Core jar to `v0.6.0`, and keep existing template slides out
of generated output.

## 2026-07-27: Follow Upstream 0.7.0 CLI Contract

Reason:
Upstream `miku-md2pptx` `v0.7.0` formalizes direct Release Asset commands,
human-readable stdout, generated artifacts, exit codes, short unknown options,
option-value validation, and DEFLATE output.

Impact:
Set the Java runtime version to `0.7.0`, align its CLI parser and help with that
contract, and keep the product-side explicit DEFLATE selection. The vendored
Office core remains `v0.6.0`; the upstream application version does not imply a
new shared-core release.
