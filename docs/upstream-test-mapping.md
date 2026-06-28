# Upstream Test Mapping

This file maps upstream test intent to Java tests.

| Upstream test intent | Java test |
| --- | --- |
| Split Markdown into slides at level 1 and 2 headings | `MikuMd2pptxCoreTest.splitsMarkdownIntoSlidesAtLevelOneAndTwoHeadings` |
| Map upstream `tests/fixtures/smoke.md` to the expected Java slide model | `MikuMd2pptxCoreTest.mapsUpstreamSmokeFixtureToExpectedSlideModel` |
| Normalize GFM list, blockquote, and thematic break blocks | `MikuMd2pptxCoreTest.normalizesGfmListBlockquoteAndThematicBreakBlocksLikeUpstream` |
| Normalize nested blockquotes and list blocks inside blockquotes | `MikuMd2pptxCoreTest.normalizesNestedBlockquoteAndBlockquoteListBlocksLikeUpstream` |
| Merge multiline blockquote paragraphs, blockquote list continuations, blockquote fenced code blocks, heading child blocks, and table child blocks | `MikuMd2pptxCoreTest.mergesBlockquoteParagraphListAndCodeBlocksLikeUpstream` |
| Normalize inline text markers and multiline speaker notes | `MikuMd2pptxCoreTest.normalizesInlineMarkersAndMultilineSpeakerNotesLikeUpstream` |
| Parse setext headings as level 1/2 slide headings | `MikuMd2pptxCoreTest.normalizesSetextHeadingsAtLevelOneAndTwoLikeUpstream` |
| Keep level 3-6 ATX headings as text blocks and strip closing heading markers | `MikuMd2pptxCoreTest.keepsDeeperHeadingsAsTextBlocksLikeUpstream` |
| Use `Untitled slide` for blank level 1/2 headings | `MikuMd2pptxCoreTest.usesUntitledSlideForBlankHeadingLikeUpstream` |
| Create a `.pptx` package with presentation and slide parts | `MikuMd2pptxCoreTest.createsPptxPackageWithPresentationAndSlideParts` |
| Normalize tilde fenced code blocks, indented code blocks, and blank lines inside indented code blocks as code text | `MikuMd2pptxCoreTest.normalizesTildeFencedAndIndentedCodeBlocksLikeUpstream` |
| Normalize GFM task list markers | `MikuMd2pptxCoreTest.normalizesGfmTaskListMarkersLikeUpstream` |
| Skip Markdown definitions and normalize reference-style, shortcut reference link, and shortcut reference image nodes | `MikuMd2pptxCoreTest.ignoresDefinitionsAndNormalizesReferenceNodesLikeUpstream` |
| Ignore direct inline images inside paragraph text | `MikuMd2pptxCoreTest.ignoresInlineImagesInsideParagraphTextLikeUpstream` |
| Decode escaped inline punctuation, multiple-backtick inline code spans, common named HTML entities, and numeric HTML entities | `MikuMd2pptxCoreTest.decodesEscapedInlineTextAndHtmlEntitiesLikeUpstream` |
| Decode additional named HTML entities such as `&nbsp;`, `&reg;`, `&mdash;`, and `&hellip;` | `MikuMd2pptxCoreTest.decodesAdditionalNamedHtmlEntitiesLikeUpstream` |
| Normalize links nested inside emphasis/delete inline markers | `MikuMd2pptxCoreTest.normalizesLinksNestedInsideInlineMarkersLikeUpstream` |
| Normalize angle autolinks, bare GFM autolink literals, email autolinks, and Markdown hard breaks | `MikuMd2pptxCoreTest.normalizesAutolinksAndHardBreaksLikeUpstream` |
| Preserve parentheses inside Markdown link and image targets | `MikuMd2pptxCoreTest.preservesParenthesesInsideLinkAndImageTargetsLikeUpstream` |
| Preserve nested brackets in link/image labels, deeper parentheses in targets, and parentheses in bare URL autolinks | `MikuMd2pptxCoreTest.preservesNestedBracketsAndParenthesesInLinksAndImagesLikeUpstream` |
| Normalize Markdown image alt text formatting, escaped label characters, escapes, and entities | `MikuMd2pptxCoreTest.normalizesMarkdownImageAltTextLikeUpstream` |
| Preserve escaped pipes inside Markdown table cells | `MikuMd2pptxCoreTest.preservesEscapedPipesInsideMarkdownTablesLikeUpstream` |
| Handle GFM table delimiter widths | `MikuMd2pptxCoreTest.handlesGfmTableDelimiterWidthsLikeUpstream` |
| Normalize footnotes as text blocks | `MikuMd2pptxCoreTest.normalizesFootnotesLikeUpstream` |
| Merge multiline footnote definition continuations | `MikuMd2pptxCoreTest.mergesFootnoteContinuationLinesLikeUpstream` |
| Normalize escaped characters inside inline link labels | `MikuMd2pptxCoreTest.normalizesEscapedCharactersInsideLinkLabelsLikeUpstream` |
| Convert simple Markdown tables to native PowerPoint table parts | `MikuMd2pptxCoreTest.writesMarkdownTablesAsNativePowerPointTableParts` |
| Handle Markdown link/image URL title and quote syntax (quoted destinations) | `MikuMd2pptxCoreTest.stripsQuotedLinkAndImageUrlsBeforeProcessing` |
| Convert Markdown links to PowerPoint hyperlink relationships | `MikuMd2pptxCoreTest.writesMarkdownLinksAsPowerPointHyperlinkRelationships` |
| Embed resolved local Markdown images | `MikuMd2pptxCoreTest.embedsResolvedLocalMarkdownImagesAsPowerPointPictureParts` |
| Merge indented list continuation lines into parent list items | `MikuMd2pptxCoreTest.mergesIndentedListContinuationLinesLikeUpstream` |
| Merge blank-line separated paragraphs inside list items | `MikuMd2pptxCoreTest.mergesBlankSeparatedListParagraphsLikeUpstream` |
| Merge plain paragraph lines into list items | `MikuMd2pptxCoreTest.mergesPlainParagraphLinesIntoListItemsLikeUpstream` |
| Merge blockquote and heading child blocks into list items | `MikuMd2pptxCoreTest.mergesListItemBlockquoteAndHeadingChildrenLikeUpstream` |
| Merge code and table child blocks into list items and omit thematic break children | `MikuMd2pptxCoreTest.mergesListItemCodeTableAndThematicChildrenLikeUpstream` |
| Write speaker notes comments as notes slides | `MikuMd2pptxCoreTest.writesSpeakerNotesCommentsAsPowerPointNotesSlides` |
| Report skipped-image and raw HTML-like diagnostics | `MikuMd2pptxCoreTest.reportsDiagnosticsForSkippedImagesAndRawHtmlLikeText` |
| Print CLI version | `MikuMd2pptxCliTest.printsVersion` |
| Print CLI help | `MikuMd2pptxCliTest.printsAgentReadableHelp` |
| Write `.pptx` through CLI | `MikuMd2pptxCliTest.writesPptxFile` |
| Reject unknown option with code 2 | `MikuMd2pptxCliTest.rejectsUnknownOption` |
| Reject unexpected positional argument | `MikuMd2pptxCliTest.rejectsUnexpectedArgument` |
| Reject missing required CLI values | `MikuMd2pptxCliTest.rejectsMissingOutArgument`, `MikuMd2pptxCliTest.rejectsMissingInputArgument`, `MikuMd2pptxCliTest.rejectsMissingValueForOptions` |
| Print conversion diagnostics to stderr | `MikuMd2pptxCliTest.printsConversionDiagnosticsToStderr` |
| Round-trip a representative Java-generated deck through local `miku-pptx2md` when available | `MikuPptx2mdCompatibilityTest.roundTripsRepresentativeDeckThroughLocalMikuPptx2mdWhenAvailable` |

## Focused Command

```bash
mvn test
```
