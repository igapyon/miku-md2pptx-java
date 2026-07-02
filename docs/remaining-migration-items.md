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
- Multiline setext headings are merged into slide titles, and block starts
  before thematic breaks are no longer mistaken for setext headings.
- Deeper ATX headings are kept as text blocks, closing ATX markers are stripped,
  and blank level 1/2 headings fall back to `Untitled slide`.
- Tilde fenced code blocks and indented code blocks are normalized like upstream
  code nodes.
- Blank lines inside indented code blocks are preserved as code text blocks like
  upstream code nodes.
- GFM task list markers are stripped from list text, Markdown definition lines
  are skipped, reference-style links become plain text, and reference-style
  images do not become image blocks.
- Shortcut reference links are normalized only when a matching definition
  exists, while undefined bracket text remains unchanged.
- Shortcut reference images are omitted only when a matching definition exists,
  while undefined image shortcut text remains unchanged.
- Autolinks, email autolinks, Markdown hard breaks, and escaped table pipes are
  normalized like upstream `remark-gfm` slide-model output.
- GFM bare URL, `www`, and email autolink literals are normalized as hyperlink
  runs like upstream slide-model output.
- Direct inline images inside paragraph text are ignored like upstream
  slide-model output instead of being treated as hyperlinks.
- Nested blockquotes and list blocks inside blockquotes are normalized with
  upstream-style text prefixes.
- Multiline blockquote paragraphs, blockquote list continuations, and
  blockquote fenced code blocks are normalized like upstream blockquote child
  blocks.
- Blockquote heading child blocks are rendered as prefixed heading text, and
  blockquote table child blocks are projected as native table blocks like
  upstream slide-model output.
- Markdown punctuation escapes, common named HTML entities, and numeric HTML
  entities are decoded in text like upstream slide-model output.
- Multiple-backtick inline code spans are stripped while preserving embedded
  backticks like upstream inline code nodes.
- Blank-line separated paragraphs inside list items are merged into the list
  item text like upstream slide-model output.
- Blockquote and heading child blocks inside list items are merged into the
  list item text like upstream slide-model output.
- Code, table, and thematic break child blocks inside list items are normalized
  into or omitted from the list item text like upstream slide-model output.
- Links nested inside emphasis or delete inline markers keep clean hyperlink
  text and targets.
- Markdown link and image targets containing parentheses, including escaped
  parentheses, are preserved like upstream slide-model output.
- Nested brackets inside inline link/image labels and deeper parentheses inside
  link/image targets are preserved like upstream slide-model output.
- Parentheses inside GFM bare URL autolink literals are preserved in hyperlink
  targets, while surrounding unmatched parentheses remain plain text like
  upstream slide-model output.
- Markdown image alt text now strips inline formatting and decodes escapes and
  HTML entities like upstream image nodes.
- Escaped characters inside image labels are normalized like upstream image
  nodes.
- Additional named HTML entities such as `&nbsp;`, `&reg;`, `&mdash;`, and
  `&hellip;` are decoded like upstream slide-model output.
- GFM table delimiter width handling, footnote text projection, and plain
  paragraph continuations inside list items are aligned with upstream
  slide-model output.
- Upstream `tests/fixtures/smoke.md` is mirrored as a Java fixture, and adjacent
  text/link run whitespace is preserved in the Java slide model.
- Escaped characters inside inline link labels and multiline footnote
  definition continuations are normalized like upstream slide-model output.
- Optional reverse compatibility smoke through local `../miku-pptx2md` is
  covered by JUnit when the reverse converter checkout, built dist files, and
  Node.js are available.
- GitHub Release CLI runtime workflow is available for executable jar and
  sources jar assets on `v*` tags or manual dispatch.
- Focused JUnit tests for core and CLI basics.

## Pending Compatibility Work

- Replace or extend the first-cut Markdown parser to better match upstream
  `remark-gfm` behavior.
- Continue closing parser gaps not covered by the current line-based Java
  implementation, especially more complex nested Markdown AST cases.
- Add parity fixtures shared with upstream `miku-md2pptx` where practical.
- Check generated decks in Microsoft PowerPoint or LibreOffice for repair-free
  opening beyond the current zip/XML unit checks.
- Decide whether any Java-side batch API is useful after the core contract is
  stable.

## Out Of Initial Scope

- Maven plugin support.
- Web UI or browser behavior.
- Full PowerPoint theme/template customization.
