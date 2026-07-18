# Upstream Class Mapping

This file maps upstream TypeScript source responsibilities to Java classes.

| Upstream file | Java class or package | Notes |
| --- | --- | --- |
| `src/ts/core.ts` | `jp.igapyon.mikumd2pptx.core.MikuMd2pptxCore`, `MikuMd2pptxMetadata`, `PptxPackageBuilder`, `PptxTemplateContext` | Public conversion API, runtime metadata, PPTX package assembly, and template layout/design-part reuse. |
| `src/ts/slide-model.ts` | `MarkdownSlides`, `SlideModel`, `SlideBlock`, `TableCell`, `TextRun` | Java first-cut parser is smaller than upstream `remark-gfm` parsing. |
| `src/ts/types.ts` | `Md2PptxOptions`, `Md2PptxResult`, `Md2PptxDiagnostic`, `ImageAsset` | Java API names follow sister Java repository style. |
| `src/ts/ooxml.ts` | `Ooxml`, `jp.igapyon.mikumsofficecore.OpcRelationship` | XML escaping and relationship XML. |
| `src/ts/pptx-static-parts.ts` | `PptxStaticParts` | Static PowerPoint package parts. |
| `src/ts/media.ts` | `PptxPackageBuilder`, `Md2PptxOptions.ImageLoader` | Java image loading is injected through options or CLI local resolution. |
| `src/ts/zip-io.ts` | `PptxPackageBuilder`, `PptxTemplateContext`, `jp.igapyon.mikumsofficecore.ZipPackage` | Java uses shared Office core 0.6.0 for deterministic ZIP writing and template ZIP reading. |
| `scripts/lib/cli-support.mjs` | `jp.igapyon.mikumd2pptx.cli.MikuMd2pptxCli`, `CliOptions` | CLI contract for `--help`, `--version`, `--out`, `--title`, `--template`, current-working-directory path resolution, overwrite, stdout/stderr, and exit codes. |
