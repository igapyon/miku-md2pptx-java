# Upstream CLI Mapping

The Java CLI preserves the first upstream command shape where practical.

## Supported

```bash
java -jar target/miku-md2pptx-java-0.6.0.jar <input.md> --out <output.pptx>
java -jar target/miku-md2pptx-java-0.6.0.jar <input.md> --out <output.pptx> --template <template.pptx>
java -jar target/miku-md2pptx-java-0.6.0.jar <input.md> --out <output.pptx> --title "Project brief"
java -jar target/miku-md2pptx-java-0.6.0.jar --help
java -jar target/miku-md2pptx-java-0.6.0.jar --version
```

## Behavior

- Missing input or `--out` exits with code `2`.
- Unknown options exit with code `2`.
- I/O or conversion failures exit with code `1`.
- Conversion diagnostics are printed to stderr as:
  `severity: code: message`.
- A successful conversion prints `Wrote <path>` to stdout, relative to the
  current working directory when possible.
- Relative input, output, template, and local image paths resolve from the
  current working directory.
- Output parent directories are created, and existing output files are replaced
  without prompting.
- `--template` reuses design parts and the first title+body/content layout but
  does not copy existing template slides.

## Differences From Upstream Node CLI

- Help text keeps the upstream contract wording, but command examples use the
  executable Java jar instead of `miku-md2pptx` / `npm run cli --`.
