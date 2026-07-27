# Upstream Follow-up Log

## 2026-06-27

- Initial Java companion work anchored to upstream commit
  `760bf08631fcccd6962862f5b1f8d6bce935cc83`.
- Accepted an initial Java Markdown parser gap versus upstream `remark-gfm` so
  the first Java runtime can establish Maven, CLI, package generation, tests,
  and mapping documents.
- Recorded parser parity and PowerPoint repair checks as follow-up items in
  `docs/remaining-migration-items.md`.

## 2026-07-02

- Followed local upstream `miku-md2pptx` from
  `760bf08631fcccd6962862f5b1f8d6bce935cc83` to
  `5a8581aea680645b3ce62da7a07f6422051d9822`.
- Upstream `0.2.2` added importable runtime bundle release assets and exported
  `mikuMd2PptxMetadata` for downstream adapters.
- Java already had executable runtime jar and sources jar release assets; added
  `MikuMd2pptxMetadata` and `MikuMd2pptxCore.METADATA` as the Java counterpart
  to the upstream runtime metadata.

## 2026-07-18

- Followed local upstream `miku-md2pptx` from
  `5a8581aea680645b3ce62da7a07f6422051d9822` (`0.2.2`) to
  `0fa0b5a2150793453690cead799a66a5e7960693` (`0.6.0`).
- Added `--template <pptx>` and core `templatePptx` support. The Java runtime
  reuses slide size, theme, master, layout, placeholder, and related design
  parts, while excluding existing template slides from generated output.
- Updated the built-in slide layout to valid OOXML type `obj` with title and
  body placeholders.
- Updated the vendored `miku-ms-office-core-java` runtime from `v0.5.1` to
  `v0.6.0`, including supplementary-plane Unicode preservation and invalid XML
  character sanitization.
- Aligned relative path resolution, output parent creation, overwrite behavior,
  stdout/stderr roles, exit-code wording, and agent-readable help with the
  upstream CLI contract.
- Added core and CLI regressions for template reuse, existing-slide exclusion,
  valid layout type, and XML sanitization.

## 2026-07-27

- Followed local and remote upstream `miku-md2pptx` from
  `0fa0b5a2150793453690cead799a66a5e7960693` (`0.6.0`) to
  `dd59e38c2b608e2e521aa4b941b8ab05543b9716` (`0.7.0`).
- Aligned the Java Release Asset help with the upstream input/output,
  generated-artifact, overwrite, machine-readable-output, and exit-code
  contract.
- Aligned short unknown-option handling and rejected option tokens used in
  place of `--out`, `--template`, or `--title` values.
- Followed the upstream DEFLATE behavior while retaining the shared Office core
  dependency at `v0.6.0`.
