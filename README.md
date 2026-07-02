# miku-md2pptx-java

`miku-md2pptx-java` is the Java companion runtime and CLI for
[`miku-md2pptx`](https://github.com/igapyon/miku-md2pptx).

It converts Markdown files into editable PowerPoint `.pptx` decks using a local
Java runtime. Files are processed on your machine and are not uploaded to a
server.

This repository follows the miku-soft Java straight-conversion style. The Java
version preserves the upstream product boundary where practical, while exposing
a Maven-built executable jar and a small public Java core API.

## Current Scope

The initial Java version supports:

- heading level 1 and 2 sections as slides
- paragraphs and simple list text
- fenced code blocks as slide text
- Markdown links as external PowerPoint hyperlinks
- local PNG, JPEG, and GIF images referenced by relative Markdown paths
- speaker notes using `<!-- speaker-notes: ... -->` HTML comments
- simple Markdown tables as native PowerPoint table parts
- conversion diagnostics for skipped images and HTML-like text warnings

Known first-cut differences from the upstream TypeScript implementation are
tracked in [docs/remaining-migration-items.md](docs/remaining-migration-items.md).
Most notably, the Java Markdown parser is intentionally small and does not yet
match the full `remark-gfm` AST behavior.

## CLI Use

Build and test:

```bash
mvn test
```

Package the executable jar:

```bash
mvn package
```

Convert a Markdown file:

```bash
java -jar target/miku-md2pptx-java-0.2.3.jar ./sample.md --out ./sample.pptx
```

Override the presentation title:

```bash
java -jar target/miku-md2pptx-java-0.2.3.jar ./sample.md --out ./sample.pptx --title "Project brief"
```

Show help or version:

```bash
java -jar target/miku-md2pptx-java-0.2.3.jar --help
java -jar target/miku-md2pptx-java-0.2.3.jar --version
```

## Development Notes

The main Java entrypoints are:

- `jp.igapyon.mikumd2pptx.core.MikuMd2pptxCore`
- `jp.igapyon.mikumd2pptx.core.MikuMd2pptxMetadata`
- `jp.igapyon.mikumd2pptx.cli.MikuMd2pptxCli`

`workplace/` is a local scratch area for upstream and sister repository
checkouts, generated verification files, and temporary artifacts. Only
`workplace/.gitkeep` is tracked.

`vendor/miku-ms-office-core-java/` contains the shared Office core release jar
used for product-neutral ZIP / OPC / XML package helpers.

Generated Maven outputs under `target/` are ignored.

GitHub Release runtime assets are built by
`.github/workflows/release-cli-runtime.yml` for `v*` tags or manual workflow
dispatch. The workflow uploads the executable jar and sources jar. The Maven
package also creates a local assembly dist zip under `target/`; that zip is a
local package output rather than a GitHub Release asset.

The Java runtime exposes `MikuMd2pptxCore.METADATA` with the same product,
artifact role, input/output, and core API vocabulary as the upstream Node.js
runtime metadata.

Developer notes are in [docs/development.md](docs/development.md).
Shared miku-soft reference information is in
[docs/miku-soft-reference.md](docs/miku-soft-reference.md).

## AI Agent Notes

Before working in this repository, check [GOAL.md](GOAL.md),
[TODO.md](TODO.md), [DECISIONS.md](DECISIONS.md), and [HANDOFF.md](HANDOFF.md).
These files record the current objective, active tasks, decisions, and handoff
notes for AI agent work.

## License

Apache License 2.0.

See [LICENSE](LICENSE).
