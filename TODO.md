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
- [x] Add parser parity for deeper ATX headings as text blocks, closing ATX
  heading markers, and blank slide heading fallback.
- [x] Add parser parity for tilde fenced code blocks and indented code blocks.
- [x] Add parser parity for blank lines inside indented code blocks.
- [x] Add parser parity for GFM task list marker stripping, Markdown definition
  line skipping, and reference-style link/image text behavior.
- [x] Add parser parity for shortcut reference links when a matching definition
  exists.
- [x] Add parser parity for shortcut reference images when a matching definition
  exists.
- [x] Add parser parity for autolinks, email autolinks, Markdown hard breaks,
  and escaped table pipes.
- [x] Add parser parity for GFM bare URL, `www`, and email autolink literals.
- [x] Add parser parity for ignoring direct inline images inside paragraph text.
- [x] Add parser parity for nested blockquotes and list blocks inside
  blockquotes.
- [x] Add parser parity for Markdown punctuation escapes and common HTML entity
  decoding in text.
- [x] Add parser parity for multiple-backtick inline code spans.
- [x] Add parser parity for blank-line separated paragraphs inside list items.
- [x] Add parser parity for links nested inside emphasis/delete inline markers.
- [x] Add parser parity for GFM table delimiter width handling, footnotes, and
  plain paragraph continuations inside list items.
- [x] Add upstream smoke fixture mapping coverage and preserve whitespace across
  adjacent text/link runs.
- [x] Add parser parity for escaped characters inside inline link labels.
- [x] Add parser parity for multiline footnote definition continuations.
- [x] Add parser parity for numeric HTML entity decoding.
- [x] Add parser parity for parentheses inside Markdown link and image targets.
- [x] Add parser parity for Markdown formatting, escapes, and entities inside
  image alt text.
- [x] Add parser parity for escaped characters inside image labels.
- [x] Add parser parity for multiline blockquote paragraphs, blockquote list
  continuations, and blockquote fenced code blocks.
- [x] Add parser parity for blockquote heading and table child blocks.
- [x] Add parser parity for list item blockquote and heading child blocks.
- [x] Add parser parity for list item code, table, and thematic break child
  blocks.
- [x] Add parser parity for nested brackets inside inline link/image labels,
  deeper parentheses inside link/image targets, parentheses inside bare URL
  autolinks, and additional named HTML entities.
- [x] Add GitHub Release CLI runtime workflow modeled after
  `/Users/igapyon/Documents/git/miku-md2docx-java/.github/workflows/release-cli-runtime.yml`
  and `/Users/igapyon/Documents/git/miku-md2xlsx-java/.github/workflows/release-cli-runtime.yml`.
- [x] Ensure the release workflow builds with Maven, validates `v*` tag version
  compatibility against `pom.xml`, uploads the executable jar and sources jar,
  and verifies the runtime jar with Java 8 using `--version`.
- [x] Document runtime release assets in `README.md` and/or
  `docs/development.md`, including the distinction between GitHub Release jar
  assets and the local Maven assembly dist zip.
- [x] Add or document a packaged CLI smoke check using
  `java -jar target/miku-md2pptx-java-0.2.2.jar` on a representative Markdown
  fixture and verify the generated `.pptx` structure.
- [x] Add a Java-side reverse compatibility test through
  local `../miku-pptx2md`, following the upstream Node compatibility test shape
  when the sister reverse converter checkout is available.
- [ ] Continue Node parser parity review against
  `/Users/igapyon/Documents/git/miku-md2pptx/src/ts/slide-model.ts` and
  `/Users/igapyon/Documents/git/miku-md2pptx/src/ts/markdown-parser.ts`,
  especially complex `remark-gfm` nested Markdown AST cases.
- [x] Attempt manual PowerPoint or LibreOffice repair-free opening checks on
  representative generated decks, or record the blocker if local GUI
  verification is unavailable.

### Blockers

- Manual PowerPoint/LibreOffice repair-free opening verification is not
  completed because this local environment does not have `libreoffice`,
  `soffice`, or `/Applications/LibreOffice.app`. A human should open
  `target/smoke.pptx` or another representative generated deck in PowerPoint or
  LibreOffice on a machine with the application installed.

### Retry Log

Use this section only when the same task or error is repeated.
If the same failure appears 3 times, stop and ask the user.

- None.

## Project Follow-up

- Continue improving Java Markdown parsing parity with upstream `remark-gfm`
  beyond the current block, heading, code, inline marker, and multiline speaker
  notes coverage.
- Complete the manual PowerPoint/LibreOffice repair-free opening check on a
  machine with PowerPoint or LibreOffice installed.
