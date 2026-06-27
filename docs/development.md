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

## Local Commands

```bash
mvn test
mvn package
java -jar target/miku-md2pptx-java-0.2.2.jar --version
```

## Local Workspace

`workplace/` is local scratch space for temporary outputs, upstream checkouts,
and sister reference repositories. Git tracks only `workplace/.gitkeep`.

## Sister References Checked

- `/Users/igapyon/Documents/git/miku-md2docx-java`
  - Used for the single-module Maven runtime/CLI shape and OOXML package style.
- `/Users/igapyon/Documents/git/miku-md2xlsx-java`
  - Used for the newer miku-soft reference document shape.

## miku-soft Reference Check

- Checked date: 2026-06-27
- Main workflow: `references/30-java-straight-conversion-workflow.md`
- Installed skill path:
  `/Users/igapyon/.codex/skills/igapyon-miku-soft-developer`
