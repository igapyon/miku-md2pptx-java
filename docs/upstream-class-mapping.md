# Upstream Class Mapping

This file maps upstream TypeScript source responsibilities to Java classes.

| Upstream file | Java class or package | Notes |
| --- | --- | --- |
| `src/ts/core.ts` | `jp.igapyon.mikumd2pptx.core.MikuMd2pptxCore`, `PptxPackageBuilder` | Public conversion API and PPTX package assembly. |
| `src/ts/slide-model.ts` | `MarkdownSlides`, `SlideModel`, `SlideBlock`, `TableCell`, `TextRun` | Java first-cut parser is smaller than upstream `remark-gfm` parsing. |
| `src/ts/types.ts` | `Md2PptxOptions`, `Md2PptxResult`, `Md2PptxDiagnostic`, `ImageAsset` | Java API names follow sister Java repository style. |
| `src/ts/ooxml.ts` | `Ooxml`, `SlideRelationship` | XML escaping and relationship XML. |
| `src/ts/pptx-static-parts.ts` | `PptxStaticParts` | Static PowerPoint package parts. |
| `src/ts/media.ts` | `PptxPackageBuilder`, `Md2PptxOptions.ImageLoader` | Java image loading is injected through options or CLI local resolution. |
| `src/ts/zip-io.ts` | `PptxPackageBuilder` | Java uses `ZipOutputStream`; upstream writes a minimal stored zip. |
| `scripts/lib/cli-support.mjs` | `jp.igapyon.mikumd2pptx.cli.MikuMd2pptxCli`, `CliOptions` | CLI contract for `--help`, `--version`, `--out`, and `--title`. |
