# Upstream Test Mapping

This file maps upstream test intent to Java tests.

| Upstream test intent | Java test |
| --- | --- |
| Split Markdown into slides at level 1 and 2 headings | `MikuMd2pptxCoreTest.splitsMarkdownIntoSlidesAtLevelOneAndTwoHeadings` |
| Create a `.pptx` package with presentation and slide parts | `MikuMd2pptxCoreTest.createsPptxPackageWithPresentationAndSlideParts` |
| Convert simple Markdown tables to native PowerPoint table parts | `MikuMd2pptxCoreTest.writesMarkdownTablesAsNativePowerPointTableParts` |
| Convert Markdown links to PowerPoint hyperlink relationships | `MikuMd2pptxCoreTest.writesMarkdownLinksAsPowerPointHyperlinkRelationships` |
| Embed resolved local Markdown images | `MikuMd2pptxCoreTest.embedsResolvedLocalMarkdownImagesAsPowerPointPictureParts` |
| Write speaker notes comments as notes slides | `MikuMd2pptxCoreTest.writesSpeakerNotesCommentsAsPowerPointNotesSlides` |
| Report skipped-image and raw HTML-like diagnostics | `MikuMd2pptxCoreTest.reportsDiagnosticsForSkippedImagesAndRawHtmlLikeText` |
| Print CLI version | `MikuMd2pptxCliTest.printsVersion` |
| Print CLI help | `MikuMd2pptxCliTest.printsAgentReadableHelp` |
| Write `.pptx` through CLI | `MikuMd2pptxCliTest.writesPptxFile` |
| Print conversion diagnostics to stderr | `MikuMd2pptxCliTest.printsConversionDiagnosticsToStderr` |

## Focused Command

```bash
mvn test
```
