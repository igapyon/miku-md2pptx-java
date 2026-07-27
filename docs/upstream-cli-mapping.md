# Upstream CLI Mapping

The Java CLI preserves the first upstream command shape where practical.

## Supported

```bash
java -jar miku-md2pptx-java-0.7.0.jar <input.md> --out <output.pptx>
java -jar miku-md2pptx-java-0.7.0.jar <input.md> --out <output.pptx> --template <template.pptx>
java -jar miku-md2pptx-java-0.7.0.jar <input.md> --out <output.pptx> --title "Project brief"
java -jar miku-md2pptx-java-0.7.0.jar --help
java -jar miku-md2pptx-java-0.7.0.jar --version
```

These are Release Asset commands and work from the directory containing the
downloaded jar. For a local Maven build, prepend `target/` to the jar filename.

## Behavior

- Missing input or `--out` exits with code `2`.
- Unknown long or short options exit with code `2`.
- An option token in place of an `--out`, `--template`, or `--title` value
  exits with code `2`.
- I/O or conversion failures exit with code `1`.
- `--help`, `--version`, and successful conversion exit with code `0`.
- Conversion diagnostics are printed to stderr as:
  `severity: code: message`.
- A successful conversion prints `Wrote <path>` to stdout, relative to the
  current working directory when possible.
- stdout is human-readable and is not a stable machine-readable data format.
- Relative input, output, template, and local image paths resolve from the
  current working directory.
- Output parent directories are created, and existing output files are replaced
  without prompting.
- `--template` reuses design parts and the first title+body/content layout but
  does not copy existing template slides.
- A normal conversion generates only the `.pptx` selected by `--out`.

## Differences From Upstream Node CLI

- Help text keeps the upstream contract wording, but command examples use the
  executable Java jar instead of `miku-md2pptx` / `npm run cli --`.
