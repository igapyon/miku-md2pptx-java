# Development

## Repository Shape

This repository is a single-module Maven Java runtime and CLI for
`miku-md2pptx`.

- Java source compatibility: 1.8
- Build tool: Maven
- Test framework: JUnit Jupiter
- Primary verification: `mvn test`
- Runtime artifact: executable jar
- Distribution artifact: Maven assembly zip
- Shared Office package helper: `vendor/miku-ms-office-core-java/`

## Local Commands

```bash
mvn test
mvn package
java -jar target/miku-md2pptx-java-0.6.0.jar --version
java -jar target/miku-md2pptx-java-0.6.0.jar src/test/resources/fixtures/smoke.md --out target/smoke.pptx --title Smoke
jar tf target/smoke.pptx
```

## Local Workspace

`workplace/` is local scratch space for temporary outputs, upstream checkouts,
and sister reference repositories. Git tracks only `workplace/.gitkeep`.

## Vendored Runtime Helper

`vendor/miku-ms-office-core-java/` contains the shared Office core `v0.6.0` release jar
used for low-level ZIP / OPC / XML package helpers. PPTX document assembly,
PowerPoint-specific templates, Markdown conversion semantics, and diagnostics
remain in this repository.

## Release Assets

GitHub Release runtime assets are produced by
`.github/workflows/release-cli-runtime.yml`.

The workflow runs for `v*` tags or manual `workflow_dispatch`, builds with
Maven, checks that the tag version matches `pom.xml` version or a dot-suffixed
variant, and uploads:

- `miku-md2pptx-java-<version>.jar`
- `miku-md2pptx-java-sources-<version>.jar`

The executable jar is verified with Java 8 using `--version` before upload. The
Maven package still builds the local dist zip, but the dist zip is not attached
to GitHub Releases.

The upstream Node.js release now publishes both CLI and importable runtime
bundles. The Java counterpart is the executable runtime jar plus sources jar;
`MikuMd2pptxCore.METADATA` carries the upstream runtime artifact vocabulary for
downstream adapters.

## Sister References Checked

- `/Users/igapyon/Documents/git/miku-md2docx-java`
  - Used for the single-module Maven runtime/CLI shape and OOXML package style.
- `/Users/igapyon/Documents/git/miku-md2xlsx-java`
  - Used for the newer miku-soft reference document shape.

## miku-soft Reference Check

- Checked date: 2026-06-27
- Latest upstream follow-up check: 2026-07-18
- Main workflow: `references/30-java-straight-conversion-workflow.md`
- Installed skill path:
  `/Users/igapyon/.codex/skills/igapyon-miku-soft-developer`
