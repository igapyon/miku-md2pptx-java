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
