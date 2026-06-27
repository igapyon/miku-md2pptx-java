# Remaining Migration Items

## Completed Initial Work

- Single-module Maven Java runtime skeleton.
- Java 1.8 compilation settings.
- Executable CLI entrypoint.
- Core API for converting Markdown text to `.pptx` bytes.
- PPTX package generation for slides, basic text, links, tables, images, and
  speaker notes.
- GFM-style list, blockquote, and thematic break block normalization aligned
  with upstream slide model behavior.
- Inline marker stripping for common emphasis, delete, and code markers, plus
  multiline speaker notes comments.
- Setext heading handling and indented list continuation behavior aligned with
  upstream markdown semantics.
- Focused JUnit tests for core and CLI basics.

## Pending Compatibility Work

- Replace or extend the first-cut Markdown parser to better match upstream
  `remark-gfm` behavior.
- Continue closing parser gaps not covered by the current line-based Java
  implementation, especially more complex nested Markdown AST cases.
- Add parity fixtures shared with upstream `miku-md2pptx` and reverse checks
  through `miku-pptx2md` where practical.
- Check generated decks in Microsoft PowerPoint or LibreOffice for repair-free
  opening beyond the current zip/XML unit checks.
- Add a GitHub Release asset workflow if requested.
- Decide whether any Java-side batch API is useful after the core contract is
  stable.

## Out Of Initial Scope

- Maven plugin support.
- Web UI or browser behavior.
- Full PowerPoint theme/template customization.
