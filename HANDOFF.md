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
  `miku-md2pptx` main application, including Java runtime/release readiness.
- Initial creation of `miku-md2pptx-java` is implemented and committed.
- Commit `c9076af` vendors `miku-ms-office-core-java` release `v0.5.1` and
  replaces local ZIP/OPC helper classes with shared Office core helpers.
- The repository has Maven runtime/CLI skeleton, Java core conversion, focused
  tests, README, miku-soft reference docs, upstream mapping docs, and AI state
  files.
- Java parser parity now includes upstream-style GFM list, blockquote, and
  thematic break slide blocks, common inline marker stripping, and multiline
  speaker notes comments.
- Java parser parity now also handles setext-style headings, indented list
  continuation lines, deeper ATX headings as text blocks, closing ATX heading
  markers, blank heading fallback to `Untitled slide`, tilde fenced code
  blocks, indented code blocks, GFM task list marker stripping, Markdown
  definition line skipping, reference-style link/image text behavior,
  direct inline image ignoring, autolinks, email autolinks, Markdown hard
  breaks, escaped table pipes, nested blockquotes, and list blocks inside
  blockquotes, Markdown punctuation escapes, and common HTML entity decoding.
  Blank-line separated paragraphs inside list items are also merged like
  upstream slide-model output, and links nested inside emphasis/delete inline
  markers keep clean hyperlink text and targets. GFM table delimiter width
  handling, footnote text projection, and plain paragraph continuations inside
  list items are also aligned with upstream output. Upstream smoke fixture
  mapping is now covered, including whitespace preservation across adjacent
  text/link runs.
- Blank lines inside indented code blocks are preserved as code text blocks like
  upstream code nodes.
- Multiline blockquote paragraphs, blockquote list continuations, and
  blockquote fenced code blocks are now normalized like upstream blockquote
  child blocks.
- Blockquote heading child blocks are normalized as prefixed heading text, and
  blockquote table child blocks are projected as native table blocks.
- Numeric HTML entities such as `&#65;` and `&#x41;` are now decoded like
  upstream slide-model output.
- Multiple-backtick inline code spans are stripped while preserving embedded
  backticks like upstream inline code nodes.
- GFM bare URL, `www`, and email autolink literals are now normalized as
  hyperlink runs like upstream slide-model output.
- Escaped characters inside inline link labels, such as
  `[escaped\]label](https://example.com)`, are now normalized like upstream and
  keep the hyperlink target.
- Shortcut reference links are normalized when a matching definition exists,
  while undefined bracket text remains unchanged.
- Shortcut reference images are omitted when a matching definition exists, while
  undefined image shortcut text remains unchanged.
- Multiline footnote definitions now merge immediate paragraph continuations
  and blank-separated indented continuations like upstream slide-model output,
  without swallowing following headings or list blocks.
- List item blockquote and heading child blocks now merge into the list item
  text like upstream slide-model output.
- List item code, table, and thematic break child blocks now normalize into or
  out of the list item text like upstream slide-model output.
- Link/image URL parsing now preserves destinations with title/quote variants
  before hyperlink and image handling.
- Link/image URL parsing also preserves targets containing parentheses, including
  escaped parentheses, like upstream slide-model output.
- Markdown image alt text now strips inline formatting and decodes escapes and
  HTML entities like upstream image nodes.
- Escaped characters inside image labels are normalized like upstream image
  nodes.
- Nested brackets inside inline link/image labels, deeper parentheses inside
  link/image targets, parentheses inside bare URL autolinks, and additional
  named HTML entities are now normalized like upstream slide-model output.
- Multiline setext headings are now merged into slide titles like upstream, and
  blockquote or other block-start lines before thematic breaks are no longer
  misclassified as setext headings.
- CLI parser behavior now has explicit tests for unknown options and missing
  required values/arguments.
- Java `--help` wording now matches the upstream Node CLI text, except for
  Java jar command examples.
- Runtime release workflow, packaged CLI smoke documentation, and optional
  Java-side reverse compatibility smoke through local `../miku-pptx2md` are now
  added. The remaining runtime-adjacent task is manual PowerPoint/LibreOffice
  repair-free checking.

## Next Action

- Continue parser parity for remaining complex `remark-gfm` nested Markdown
  cases after runtime release shape is in place.
- Complete manual PowerPoint or LibreOffice repair-free checks listed in
  `docs/remaining-migration-items.md` on a machine with one of those
  applications installed.

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
- `src/test/java/jp/igapyon/mikumd2pptx/core/MikuPptx2mdCompatibilityTest.java`:
  optional reverse compatibility smoke using local `../miku-pptx2md`.
- `docs/remaining-migration-items.md`: Compatibility follow-up items.
- `.github/workflows/release-cli-runtime.yml`: release workflow modeled after
  sister Java repositories.
- `vendor/miku-ms-office-core-java/`: vendored released Office core jar and
  checksum documentation.

## Watch Outs

- The current Java implementation is an initial companion runtime, not full
  `remark-gfm` parity.
- Preserve the miku-soft boundary: product semantics belong in Java core/API,
  not in docs or skill prose.
- Keep `workplace/` local-only except `workplace/.gitkeep`.
- Upstream Markdown parsing uses `remark-gfm`; any Java first-cut parser gaps
  should be documented explicitly.
- Keep GitHub Release assets aligned with sister Java runtime repositories:
  executable jar and sources jar are release assets; the Maven assembly dist
  zip remains a local package output unless explicitly requested.
- Manual repair-free opening verification is blocked in this local environment:
  `libreoffice`, `soffice`, and `/Applications/LibreOffice.app` are absent.

## Last Verification

- `mvn test`: success on 2026-07-02, 51 tests passed after multiline setext
  heading parity.
- `mvn package`: success on 2026-06-28, 49 tests passed, jar, sources jar, and
  dist zip created.
- `java -jar target/miku-md2pptx-java-0.2.3.jar --version`: printed `0.2.3`.
- `java -jar target/miku-md2pptx-java-0.2.3.jar src/test/resources/fixtures/smoke.md --out target/smoke.pptx --title Smoke`: success.
- `jar tf target/smoke.pptx`: confirmed presentation, slide, notes, theme, and
  relationship parts.
- `which libreoffice`, `which soffice`, and `ls -d /Applications/LibreOffice.app`:
  not available, so GUI repair-free opening verification remains external.
- Current uncommitted edits include state-management files, README/docs updates,
  and `.github/workflows/release-cli-runtime.yml`.
