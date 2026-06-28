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

Node版相当(実質的に Node.js 版相当)に至ることを目標として、`miku-md2pptx-java`
を `miku-md2pptx` の実装と比較しながら実用レベルの機能・振る舞い同等性へ近づける。

Bring `miku-md2pptx-java` to practical feature and behavior parity with the
Node.js `miku-md2pptx` main application, following
`igapyon-miku-soft-developer` Java straight-conversion guidance.

The target repository is `miku-md2pptx-java`. The upstream source is the local
and GitHub `miku-md2pptx` main application. Same-layer sister references are
`miku-md2xlsx-java` and `miku-md2docx-java`.

The initial Java companion skeleton already exists. The current goal is to
close the remaining compatibility gap until the Java CLI/runtime can be treated
as the Java-side equivalent of the Node.js version for normal supported use.

Runtime readiness is part of this goal. The Java repository should not only
compile locally; it should also provide the release/runtime shape used by the
sister Java repositories, including reproducible packaged CLI assets and
documented release operation.

## Done

- Supported Markdown and PPTX output behavior is mapped against the Node.js
  `miku-md2pptx` upstream, with unsupported or intentionally different behavior
  documented explicitly.
- The Java Markdown parsing and conversion behavior covers the practical
  upstream feature set, including slide splitting, paragraphs, lists, code
  blocks, tables, links, images, speaker notes, diagnostics, and metadata/title
  handling where supported by the Node.js version.
- CLI behavior and exit behavior are equivalent to the Node.js version for
  supported options and common failure cases, including `--help`, `--version`,
  input/output handling, overwrite behavior, and diagnostics.
- Generated `.pptx` files are structurally valid and open without repair in
  PowerPoint or LibreOffice for representative upstream-compatible fixtures.
- Compatibility tests or golden fixture checks compare Java output expectations
  against upstream behavior where practical, and focused regression tests cover
  every supported feature.
- Public docs describe Java usage, compatibility status, known differences, and
  remaining limitations without overstating parity.
- `mvn test` passes, or any verification blocker is recorded in `TODO.md` and
  summarized in `HANDOFF.md`.
- `mvn package` passes and the packaged CLI is smoke-tested on representative
  Markdown input.
- Runtime release support follows the same-layer sister repository pattern:
  a GitHub Actions workflow builds the executable jar and sources jar from
  `v*` tags or manual dispatch, checks tag/POM version compatibility, verifies
  the generated jar with Java 8 using `--version`, and uploads the runtime
  assets to the matching GitHub Release.
- README or developer docs explain the runtime release assets, local dist zip,
  and the relationship between GitHub Release assets and Maven package outputs.
- The vendored `miku-ms-office-core-java` jar is treated as a released upstream
  runtime dependency, with source repository, release tag, filename, and
  checksum recorded in `vendor/miku-ms-office-core-java/README.md`.
- Final `git status --short` and diff are reviewed for unintended changes.

## Stop

- Upstream `miku-md2pptx` behavior needed for compatibility is unclear enough
  that implementation would become guesswork.
- Sister reference differences conflict and require a human decision.
- Exact Node.js parity requires a dependency, file format behavior, or runtime
  assumption that is unsuitable for the Java 1.8 companion boundary.
- Build or test dependency resolution repeatedly fails for the same underlying
  cause.
- `TODO.md` の `Retry Log` に同じ原因の失敗が3回記録された。
