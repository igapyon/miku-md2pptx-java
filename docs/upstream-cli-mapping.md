# Upstream CLI Mapping

The Java CLI preserves the first upstream command shape where practical.

## Supported

```bash
java -jar target/miku-md2pptx-java-0.2.2.jar <input.md> --out <output.pptx>
java -jar target/miku-md2pptx-java-0.2.2.jar <input.md> --out <output.pptx> --title "Project brief"
java -jar target/miku-md2pptx-java-0.2.2.jar --help
java -jar target/miku-md2pptx-java-0.2.2.jar --version
```

## Behavior

- Missing input or `--out` exits with code `2`.
- Unknown options exit with code `2`.
- I/O or conversion failures exit with code `1`.
- Conversion diagnostics are printed to stderr as:
  `severity: code: message`.
- A successful conversion prints `Wrote <absolute-output-path>` to stdout.

## Differences From Upstream Node CLI

- Help text keeps the upstream wording, but command examples use the executable
  Java jar instead of `miku-md2pptx` / `npm run cli --`.
- The upstream CLI resolves input and output paths from its package root.
- The Java CLI resolves paths from the current process working directory.
- The Java CLI output path message currently uses an absolute path.
