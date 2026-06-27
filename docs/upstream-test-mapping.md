# Upstream Test Mapping

This file maps upstream test intent to Java tests.

| Upstream test intent | Java test |
| --- | --- |
| Split Markdown into slides at level 1 and 2 headings | `MikuMd2pptxCoreTest.splitsMarkdownIntoSlidesAtLevelOneAndTwoHeadings` |
| Normalize GFM list, blockquote, and thematic break blocks | `MikuMd2pptxCoreTest.normalizesGfmListBlockquoteAndThematicBreakBlocksLikeUpstream` |
| Normalize inline text markers and multiline speaker notes | `MikuMd2pptxCoreTest.normalizesInlineMarkersAndMultilineSpeakerNotesLikeUpstream` |
| Parse setext headings as level 1/2 slide headings | `MikuMd2pptxCoreTest.normalizesSetextHeadingsAtLevelOneAndTwoLikeUpstream` |
| Create a `.pptx` package with presentation and slide parts | `MikuMd2pptxCoreTest.createsPptxPackageWithPresentationAndSlideParts` |
| Convert simple Markdown tables to native PowerPoint table parts | `MikuMd2pptxCoreTest.writesMarkdownTablesAsNativePowerPointTableParts` |
| Handle Markdown link/image URL title and quote syntax (quoted destinations) | `MikuMd2pptxCoreTest.stripsQuotedLinkAndImageUrlsBeforeProcessing` |
| Convert Markdown links to PowerPoint hyperlink relationships | `MikuMd2pptxCoreTest.writesMarkdownLinksAsPowerPointHyperlinkRelationships` |
| Embed resolved local Markdown images | `MikuMd2pptxCoreTest.embedsResolvedLocalMarkdownImagesAsPowerPointPictureParts` |
| Merge indented list continuation lines into parent list items | `MikuMd2pptxCoreTest.mergesIndentedListContinuationLinesLikeUpstream` |
| Write speaker notes comments as notes slides | `MikuMd2pptxCoreTest.writesSpeakerNotesCommentsAsPowerPointNotesSlides` |
| Report skipped-image and raw HTML-like diagnostics | `MikuMd2pptxCoreTest.reportsDiagnosticsForSkippedImagesAndRawHtmlLikeText` |
| Print CLI version | `MikuMd2pptxCliTest.printsVersion` |
| Print CLI help | `MikuMd2pptxCliTest.printsAgentReadableHelp` |
| Write `.pptx` through CLI | `MikuMd2pptxCliTest.writesPptxFile` |
| Reject unknown option with code 2 | `MikuMd2pptxCliTest.rejectsUnknownOption` |
| Reject unexpected positional argument | `MikuMd2pptxCliTest.rejectsUnexpectedArgument` |
| Reject missing required CLI values | `MikuMd2pptxCliTest.rejectsMissingOutArgument`, `MikuMd2pptxCliTest.rejectsMissingInputArgument`, `MikuMd2pptxCliTest.rejectsMissingValueForOptions` |
| Print conversion diagnostics to stderr | `MikuMd2pptxCliTest.printsConversionDiagnosticsToStderr` |

## Focused Command

```bash
mvn test
```
