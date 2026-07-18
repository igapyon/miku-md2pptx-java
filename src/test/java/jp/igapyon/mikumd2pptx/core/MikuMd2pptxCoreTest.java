package jp.igapyon.mikumd2pptx.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MikuMd2pptxCoreTest {
    private static final byte[] ONE_PIXEL_PNG = new byte[] {
            (byte) 137, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82,
            0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 31, 21, (byte) 196,
            (byte) 137, 0, 0, 0, 13, 73, 68, 65, 84, 120, (byte) 156, 99, (byte) 248, (byte) 255, (byte) 255,
            63, 0, 5, (byte) 254, 2, (byte) 254, (byte) 167, 53, (byte) 129, (byte) 132, 0, 0, 0, 0,
            73, 69, 78, 68, (byte) 174, 66, 96, (byte) 130
    };

    @Test
    void exportsRuntimeMetadataForDownstreamAdaptersLikeUpstream() {
        MikuMd2pptxMetadata metadata = MikuMd2pptxCore.METADATA;

        assertEquals("miku-md2pptx", metadata.getProductName());
        assertEquals("markdown-to-pptx-runtime", metadata.getArtifactRole());
        assertEquals("markdown", metadata.getPrimaryInput());
        assertEquals("pptx", metadata.getPrimaryOutput());
        assertEquals(3, metadata.getCoreApi().size());
        assertEquals("markdownToSlides", metadata.getCoreApi().get(0));
        assertEquals("markdownToPptx", metadata.getCoreApi().get(1));
        assertEquals("markdownToPptxResult", metadata.getCoreApi().get(2));
    }

    @Test
    void splitsMarkdownIntoSlidesAtLevelOneAndTwoHeadings() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides("# One\n\nBody\n\n## Two\n\n- Item\n", new Md2PptxOptions());

        assertEquals(2, slides.size());
        assertEquals("One", slides.get(0).title);
        assertEquals("Body", slides.get(0).blocks.get(0).text);
        assertEquals("Two", slides.get(1).title);
        assertEquals("- Item", slides.get(1).blocks.get(0).text);
    }

    @Test
    void mapsUpstreamSmokeFixtureToExpectedSlideModel() throws Exception {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(readResource("fixtures/upstream-smoke.md"), new Md2PptxOptions());

        assertEquals(2, slides.size());
        assertEquals("Sales memo", slides.get(0).title);
        assertEquals("Intro paragraph for the deck.", slides.get(0).blocks.get(0).text);
        assertEquals("See project site.", slides.get(0).blocks.get(1).text);
        assertEquals("https://example.com/project", slides.get(0).blocks.get(1).runs.get(1).href);
        assertEquals("Numbers", slides.get(1).title);
        assertEquals(SlideBlock.Kind.TABLE, slides.get(1).blocks.get(0).kind);
        assertEquals("Item", slides.get(1).blocks.get(0).rows.get(0).get(0).text);
        assertEquals("Orange", slides.get(1).blocks.get(0).rows.get(2).get(0).text);
        assertEquals("- Confirm quantities", slides.get(1).blocks.get(1).text);
        assertEquals("- Share with the team", slides.get(1).blocks.get(2).text);
        assertEquals("    plain code", slides.get(1).blocks.get(3).text);
    }

    @Test
    void normalizesSetextHeadingsAtLevelOneAndTwoLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "Deck summary\n=====\n\nIntro line.\n\nNumbers\n---\n\n- Item\n", new Md2PptxOptions());

        assertEquals(2, slides.size());
        assertEquals("Deck summary", slides.get(0).title);
        assertEquals("Intro line.", slides.get(0).blocks.get(0).text);
        assertEquals("Numbers", slides.get(1).title);
        assertEquals("- Item", slides.get(1).blocks.get(0).text);
    }

    @Test
    void normalizesMultilineSetextHeadingsLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "Deck summary\ncontinued\n=====\n\nBody\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("Deck summary continued", slides.get(0).title);
        assertEquals("Body", slides.get(0).blocks.get(0).text);
    }

    @Test
    void doesNotTreatBlockStartsBeforeThematicBreakAsSetextHeadingsLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides("> Quote\n---\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("Markdown deck", slides.get(0).title);
        assertEquals("> Quote", slides.get(0).blocks.get(0).text);
        assertEquals("---", slides.get(0).blocks.get(1).text);
    }

    @Test
    void keepsDeeperHeadingsAsTextBlocksLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck ###\n\n### Detail **section** ###\n\nBody\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("Deck", slides.get(0).title);
        assertEquals("Detail section", slides.get(0).blocks.get(0).text);
        assertEquals("Body", slides.get(0).blocks.get(1).text);
    }

    @Test
    void usesUntitledSlideForBlankHeadingLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides("# \n\nBody\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("Untitled slide", slides.get(0).title);
        assertEquals("Body", slides.get(0).blocks.get(0).text);
    }

    @Test
    void normalizesGfmListBlockquoteAndThematicBreakBlocksLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n1. First\n  - Child\n\n> Quoted **text**\n\n---\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("- First", slides.get(0).blocks.get(0).text);
        assertEquals("  - Child", slides.get(0).blocks.get(1).text);
        assertEquals("> Quoted text", slides.get(0).blocks.get(2).text);
        assertEquals("---", slides.get(0).blocks.get(3).text);
    }

    @Test
    void normalizesNestedBlockquoteAndBlockquoteListBlocksLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n> Outer\n> > Inner\n\n> - Quoted item\n> > - Inner item\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("> Outer", slides.get(0).blocks.get(0).text);
        assertEquals("> > Inner", slides.get(0).blocks.get(1).text);
        assertEquals("> - Quoted item", slides.get(0).blocks.get(2).text);
        assertEquals("> > - Inner item", slides.get(0).blocks.get(3).text);
    }

    @Test
    void mergesBlockquoteParagraphListAndCodeBlocksLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n> first\n> second\n\n"
                        + "> first\n>\n> second\n\n"
                        + "> - item\n>   continued\n> - next\n\n"
                        + "> ```\n> code\n> ```\n\n"
                        + "> ### Detail\n\n"
                        + "> | A | B |\n> | --- | --- |\n> | 1 | 2 |\n",
                new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("> first second", slides.get(0).blocks.get(0).text);
        assertEquals("> first", slides.get(0).blocks.get(1).text);
        assertEquals("> second", slides.get(0).blocks.get(2).text);
        assertEquals("> - item continued", slides.get(0).blocks.get(3).text);
        assertEquals("> - next", slides.get(0).blocks.get(4).text);
        assertEquals(">     code", slides.get(0).blocks.get(5).text);
        assertEquals("> Detail", slides.get(0).blocks.get(6).text);
        assertEquals(SlideBlock.Kind.TABLE, slides.get(0).blocks.get(7).kind);
        assertEquals("A", slides.get(0).blocks.get(7).rows.get(0).get(0).text);
        assertEquals("2", slides.get(0).blocks.get(7).rows.get(1).get(1).text);
    }

    @Test
    void normalizesTildeFencedAndIndentedCodeBlocksLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n~~~java\nSystem.out.println(\"hi\");\n~~~\n\n    indented code\n\n    after blank\n",
                new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("    System.out.println(\"hi\");", slides.get(0).blocks.get(0).text);
        assertEquals("    indented code", slides.get(0).blocks.get(1).text);
        assertEquals("    ", slides.get(0).blocks.get(2).text);
        assertEquals("    after blank", slides.get(0).blocks.get(3).text);
    }

    @Test
    void normalizesGfmTaskListMarkersLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n- [x] Done\n- [ ] Todo\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("- Done", slides.get(0).blocks.get(0).text);
        assertEquals("- Todo", slides.get(0).blocks.get(1).text);
    }

    @Test
    void ignoresDefinitionsAndNormalizesReferenceNodesLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\nSee [site][ref].\n\nSee [shortcut].\n\nSee [missing].\n\n"
                        + "![Alt][img]\n\n![ShortcutImage]\n\n![MissingImage]\n\n"
                        + "Image ![Alt][img] text\n\nImage ![ShortcutImage] text\n\n"
                        + "[ref]: https://example.com\n"
                        + "[shortcut]: https://example.com/shortcut\n"
                        + "[ShortcutImage]: assets/shortcut.png\n"
                        + "[img]: assets/chart.png\n",
                new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals(6, slides.get(0).blocks.size());
        assertEquals("See site.", slides.get(0).blocks.get(0).text);
        assertEquals("See shortcut.", slides.get(0).blocks.get(1).text);
        assertEquals("See [missing].", slides.get(0).blocks.get(2).text);
        assertEquals("![MissingImage]", slides.get(0).blocks.get(3).text);
        assertEquals("Image text", slides.get(0).blocks.get(4).text);
        assertEquals("Image text", slides.get(0).blocks.get(5).text);
    }

    @Test
    void ignoresInlineImagesInsideParagraphTextLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\nImage ![Alt](assets/chart.png \"title\") text\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals(1, slides.get(0).blocks.size());
        assertEquals("Image text", slides.get(0).blocks.get(0).text);
        assertTrue(slides.get(0).blocks.get(0).runs.get(0).href == null);
    }

    @Test
    void decodesEscapedInlineTextAndHtmlEntitiesLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n\\*not emphasis\\* and \\[brackets\\] and \\`code\\`\n\n"
                        + "``code ` inner`` and `a   b` and `unclosed\n\n"
                        + "Tom &amp; Jerry &lt;3\n\nA &#65; &#x41; &copy;\n",
                new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("*not emphasis* and [brackets] and `code`", slides.get(0).blocks.get(0).text);
        assertEquals("code ` inner and a b and `unclosed", slides.get(0).blocks.get(1).text);
        assertEquals("Tom & Jerry <3", slides.get(0).blocks.get(2).text);
        assertEquals("A A A \u00a9", slides.get(0).blocks.get(3).text);
    }

    @Test
    void decodesAdditionalNamedHtmlEntitiesLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\nA&nbsp;B &reg; &mdash; &hellip;\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("A B \u00ae \u2014 \u2026", slides.get(0).blocks.get(0).text);
    }

    @Test
    void normalizesLinksNestedInsideInlineMarkersLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n**[link](https://example.com)**\n\n~~[old](https://example.com/old)~~\n",
                new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("link", slides.get(0).blocks.get(0).text);
        assertEquals("https://example.com", slides.get(0).blocks.get(0).runs.get(0).href);
        assertEquals("old", slides.get(0).blocks.get(1).text);
        assertEquals("https://example.com/old", slides.get(0).blocks.get(1).runs.get(0).href);
    }

    @Test
    void normalizesEscapedCharactersInsideLinkLabelsLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n[escaped\\]label](https://example.com)\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("escaped]label", slides.get(0).blocks.get(0).text);
        assertEquals("https://example.com", slides.get(0).blocks.get(0).runs.get(0).href);
    }

    @Test
    void preservesParenthesesInsideLinkAndImageTargetsLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\nSee [x](https://example.com/a_(b)).\n\n"
                        + "Escaped [x](https://example.com/a_\\(b\\)).\n\n"
                        + "![Alt](assets/a_(b).png)\n",
                new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("See x.", slides.get(0).blocks.get(0).text);
        assertEquals("https://example.com/a_(b)", slides.get(0).blocks.get(0).runs.get(1).href);
        assertEquals("Escaped x.", slides.get(0).blocks.get(1).text);
        assertEquals("https://example.com/a_(b)", slides.get(0).blocks.get(1).runs.get(1).href);
        assertEquals(SlideBlock.Kind.IMAGE, slides.get(0).blocks.get(2).kind);
        assertEquals("assets/a_(b).png", slides.get(0).blocks.get(2).url);
    }

    @Test
    void preservesNestedBracketsAndParenthesesInLinksAndImagesLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n[a [b]](https://example.com/nested)\n\n"
                        + "See [x](https://example.com/a_(b_(c))).\n\n"
                        + "Visit https://example.com/a_(b).\n\n"
                        + "(https://example.com/wrapped)\n\n"
                        + "![Alt [nested]](assets/nested.png)\n",
                new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("a [b]", slides.get(0).blocks.get(0).text);
        assertEquals("https://example.com/nested", slides.get(0).blocks.get(0).runs.get(0).href);
        assertEquals("See x.", slides.get(0).blocks.get(1).text);
        assertEquals("https://example.com/a_(b_(c))", slides.get(0).blocks.get(1).runs.get(1).href);
        assertEquals("Visit https://example.com/a_(b).", slides.get(0).blocks.get(2).text);
        assertEquals("https://example.com/a_(b)", slides.get(0).blocks.get(2).runs.get(1).href);
        assertEquals("(https://example.com/wrapped)", slides.get(0).blocks.get(3).text);
        assertEquals("https://example.com/wrapped", slides.get(0).blocks.get(3).runs.get(1).href);
        assertEquals(SlideBlock.Kind.IMAGE, slides.get(0).blocks.get(4).kind);
        assertEquals("Alt [nested]", slides.get(0).blocks.get(4).altText);
        assertEquals("assets/nested.png", slides.get(0).blocks.get(4).url);
    }

    @Test
    void normalizesMarkdownImageAltTextLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n![**Alt** &amp; &quot;q&quot; A\\*B escaped\\]label](assets/a.png)\n",
                new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals(SlideBlock.Kind.IMAGE, slides.get(0).blocks.get(0).kind);
        assertEquals("Alt & \"q\" A*B escaped]label", slides.get(0).blocks.get(0).altText);
    }

    @Test
    void normalizesAutolinksAndHardBreaksLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n<https://example.com>\n\n<user@example.com>\n\n"
                        + "Visit https://example.com/path.\n\n"
                        + "Visit www.example.com/path.\n\n"
                        + "Mail user@example.com.\n\n"
                        + "First\\\nSecond\n\nThird  \nFourth\n",
                new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("https://example.com", slides.get(0).blocks.get(0).text);
        assertEquals("https://example.com", slides.get(0).blocks.get(0).runs.get(0).href);
        assertEquals("user@example.com", slides.get(0).blocks.get(1).text);
        assertEquals("mailto:user@example.com", slides.get(0).blocks.get(1).runs.get(0).href);
        assertEquals("Visit https://example.com/path.", slides.get(0).blocks.get(2).text);
        assertEquals("https://example.com/path", slides.get(0).blocks.get(2).runs.get(1).href);
        assertEquals(".", slides.get(0).blocks.get(2).runs.get(2).text);
        assertEquals("Visit www.example.com/path.", slides.get(0).blocks.get(3).text);
        assertEquals("http://www.example.com/path", slides.get(0).blocks.get(3).runs.get(1).href);
        assertEquals("Mail user@example.com.", slides.get(0).blocks.get(4).text);
        assertEquals("mailto:user@example.com", slides.get(0).blocks.get(4).runs.get(1).href);
        assertEquals("FirstSecond", slides.get(0).blocks.get(5).text);
        assertEquals("ThirdFourth", slides.get(0).blocks.get(6).text);
    }

    @Test
    void preservesEscapedPipesInsideMarkdownTablesLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n| A \\| B | C |\n| --- | --- |\n| 1 | 2 |\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals(1, slides.get(0).blocks.size());
        assertEquals(SlideBlock.Kind.TABLE, slides.get(0).blocks.get(0).kind);
        assertEquals("A | B", slides.get(0).blocks.get(0).rows.get(0).get(0).text);
        assertEquals("C", slides.get(0).blocks.get(0).rows.get(0).get(1).text);
        assertEquals("1", slides.get(0).blocks.get(0).rows.get(1).get(0).text);
    }

    @Test
    void handlesGfmTableDelimiterWidthsLikeUpstream() {
        List<SlideModel> pipeTable = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n| A | B |\n| - | - |\n| 1 | 2 |\n", new Md2PptxOptions());
        List<SlideModel> noPipeShortDelimiter = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\nA | B\n- | -\n1 | 2\n", new Md2PptxOptions());

        assertEquals(SlideBlock.Kind.TABLE, pipeTable.get(0).blocks.get(0).kind);
        assertEquals("1", pipeTable.get(0).blocks.get(0).rows.get(1).get(0).text);
        assertEquals("A | B", noPipeShortDelimiter.get(0).blocks.get(0).text);
        assertEquals("- | - 1 | 2", noPipeShortDelimiter.get(0).blocks.get(1).text);
    }

    @Test
    void normalizesFootnotesLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\nText with footnote[^1].\n\n[^1]: Footnote text\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("Text with footnote.", slides.get(0).blocks.get(0).text);
        assertEquals("Footnote text", slides.get(0).blocks.get(1).text);
    }

    @Test
    void mergesFootnoteContinuationLinesLikeUpstream() {
        List<SlideModel> immediate = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n[^1]: first\nsecond\n\n- item\n", new Md2PptxOptions());
        List<SlideModel> indented = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n[^1]: first\n\n    second\n\n## Next\nBody\n", new Md2PptxOptions());

        assertEquals("first second", immediate.get(0).blocks.get(0).text);
        assertEquals("- item", immediate.get(0).blocks.get(1).text);
        assertEquals("first second", indented.get(0).blocks.get(0).text);
        assertEquals("Next", indented.get(1).title);
        assertEquals("Body", indented.get(1).blocks.get(0).text);
    }

    @Test
    void mergesIndentedListContinuationLinesLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "- First item\n  continued text\n  - Nested item\n- Next\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("- First item continued text", slides.get(0).blocks.get(0).text);
        assertEquals("  - Nested item", slides.get(0).blocks.get(1).text);
        assertEquals("- Next", slides.get(0).blocks.get(2).text);
    }

    @Test
    void mergesBlankSeparatedListParagraphsLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n- First paragraph\n\n  Second paragraph\n\n- Next\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("- First paragraphSecond paragraph", slides.get(0).blocks.get(0).text);
        assertEquals("- Next", slides.get(0).blocks.get(1).text);
    }

    @Test
    void mergesPlainParagraphLinesIntoListItemsLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n- Item\nNext paragraph\n\n> Quote\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("- Item Next paragraph", slides.get(0).blocks.get(0).text);
        assertEquals("> Quote", slides.get(0).blocks.get(1).text);
    }

    @Test
    void mergesListItemBlockquoteAndHeadingChildrenLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n- item\n  > quoted\n- next\n  ### detail\n", new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("- itemquoted", slides.get(0).blocks.get(0).text);
        assertEquals("- nextdetail", slides.get(0).blocks.get(1).text);
    }

    @Test
    void mergesListItemCodeTableAndThematicChildrenLikeUpstream() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides(
                "# Deck\n\n- code item\n\n      code\n\n"
                        + "- table item\n\n"
                        + "  | A | B |\n"
                        + "  | --- | --- |\n"
                        + "  | 1 | 2 |\n\n"
                        + "- thematic item\n\n"
                        + "  ---\n",
                new Md2PptxOptions());

        assertEquals(1, slides.size());
        assertEquals("- code itemcode", slides.get(0).blocks.get(0).text);
        assertEquals("- table itemA B 1 2", slides.get(0).blocks.get(1).text);
        assertEquals("- thematic item", slides.get(0).blocks.get(2).text);
    }

    @Test
    void normalizesInlineMarkersAndMultilineSpeakerNotesLikeUpstream() throws Exception {
        Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx(
                "# *Deck*\n\nBody with *emphasis*, _alt_, ~~delete~~, and `code`.\n\n"
                        + "Keep snake_case text.\n\n"
                        + "<!-- speaker-notes:\n"
                        + "First note\n"
                        + "Second note\n"
                        + "-->");
        Map<String, byte[]> entries = ZipTestSupport.unzip(result.getPptx());
        String slide = ZipTestSupport.text(entries, "ppt/slides/slide1.xml");
        String notes = ZipTestSupport.text(entries, "ppt/notesSlides/notesSlide1.xml");

        assertTrue(slide.contains("<a:t>Deck</a:t>"));
        assertTrue(slide.contains("<a:t>Body with emphasis, alt, delete, and code.</a:t>"));
        assertTrue(slide.contains("<a:t>Keep snake_case text.</a:t>"));
        assertTrue(notes.contains("<a:t>First note</a:t>"));
        assertTrue(notes.contains("<a:t>Second note</a:t>"));
    }

    @Test
    void preservesLinksAndImagesWithMarkdownTitleSyntax() throws Exception {
        Md2PptxOptions options = new Md2PptxOptions();
        options.setImageLoader(new Md2PptxOptions.ImageLoader() {
            @Override
            public ImageAsset load(String path) {
                if ("assets/chart.png".equals(path)) {
                    return new ImageAsset(ONE_PIXEL_PNG, "png");
                }
                return null;
            }
        });
        Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx(
                "# Deck\n\nSee [Example](https://example.com \"external\").\n\n![Chart](assets/chart.png \"logo\")",
                options);
        Map<String, byte[]> entries = ZipTestSupport.unzip(result.getPptx());

        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("<a:hlinkClick"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/_rels/slide1.xml.rels").contains("https://example.com"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("p:pic"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/_rels/slide1.xml.rels").contains("media/image1.png"));
    }

    @Test
    void stripsQuotedLinkAndImageUrlsBeforeProcessing() throws Exception {
        Md2PptxOptions options = new Md2PptxOptions();
        options.setImageLoader(new Md2PptxOptions.ImageLoader() {
            @Override
            public ImageAsset load(String path) {
                if ("assets/chart.png".equals(path)) {
                    return new ImageAsset(ONE_PIXEL_PNG, "png");
                }
                return null;
            }
        });
        Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx(
                "# Deck\n\nSee [Example](\"https://example.com\" \"external\").\n\n![Chart]('assets/chart.png' \"logo\")",
                options);
        Map<String, byte[]> entries = ZipTestSupport.unzip(result.getPptx());

        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("<a:hlinkClick"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/_rels/slide1.xml.rels").contains("https://example.com"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("p:pic"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/_rels/slide1.xml.rels").contains("media/image1.png"));
    }

    @Test
    void createsPptxPackageWithPresentationAndSlideParts() throws Exception {
        Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx("# Deck\n\n## Slide\n\nBody");
        Map<String, byte[]> entries = ZipTestSupport.unzip(result.getPptx());

        assertTrue(ZipTestSupport.text(entries, "[Content_Types].xml").contains("presentationml.presentation.main+xml"));
        assertTrue(ZipTestSupport.text(entries, "ppt/_rels/presentation.xml.rels").contains("slides/slide1.xml"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slideLayouts/slideLayout1.xml").contains("type=\"obj\""));
        assertFalse(ZipTestSupport.text(entries, "ppt/slideLayouts/slideLayout1.xml").contains("titleAndContent"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("Deck"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide2.xml").contains("Body"));
    }

    @Test
    void preservesSupplementaryUnicodeAndRemovesInvalidXmlCharacters() throws Exception {
        Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx("# Deck \uD83D\uDE00\n\nBody\u0001 text \uD800");
        String slide = ZipTestSupport.text(ZipTestSupport.unzip(result.getPptx()), "ppt/slides/slide1.xml");

        assertTrue(slide.contains("Deck \uD83D\uDE00"));
        assertTrue(slide.contains("Body text "));
        assertFalse(slide.contains("\u0001"));
        assertFalse(slide.contains("\uFFFD"));
    }

    @Test
    void usesTemplateLayoutWithoutCopyingTemplateSlides() throws Exception {
        byte[] template = new MikuMd2pptxCore().convertMarkdownToPptx("# Template cover\n\nTemplate-only body").getPptx();
        Md2PptxOptions options = new Md2PptxOptions();
        options.setTemplatePptx(template);
        Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx(
                "# Generated deck\n\nGenerated body", options);
        Map<String, byte[]> entries = ZipTestSupport.unzip(result.getPptx());
        String slide = ZipTestSupport.text(entries, "ppt/slides/slide1.xml");

        assertEquals("template-layout-selected", result.getDiagnostics().get(0).getCode());
        assertEquals("info", result.getDiagnostics().get(0).getSeverity());
        assertTrue(slide.contains("Generated deck"));
        assertTrue(slide.contains("Generated body"));
        assertFalse(slide.contains("Template-only body"));
        assertFalse(slide.contains(" sz=\"2400\""));
        assertFalse(slide.contains(" sz=\"1800\""));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/_rels/slide1.xml.rels")
                .contains("Target=\"../slideLayouts/slideLayout1.xml\""));
    }

    @Test
    void writesMarkdownTablesAsNativePowerPointTableParts() throws Exception {
        Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx("# Deck\n\n| A | B |\n| --- | --- |\n| 1 | 2 |");
        Map<String, byte[]> entries = ZipTestSupport.unzip(result.getPptx());
        String slide = ZipTestSupport.text(entries, "ppt/slides/slide1.xml");

        assertTrue(slide.contains("<p:graphicFrame>"));
        assertTrue(slide.contains("<a:tbl>"));
        assertTrue(slide.contains("<a:t>A</a:t>"));
    }

    @Test
    void writesMarkdownLinksAsPowerPointHyperlinkRelationships() throws Exception {
        Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx("# Deck\n\nSee [example](https://example.com/).");
        Map<String, byte[]> entries = ZipTestSupport.unzip(result.getPptx());

        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("<a:hlinkClick r:id=\"rId2\"/>"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/_rels/slide1.xml.rels").contains("Target=\"https://example.com/\""));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/_rels/slide1.xml.rels").contains("TargetMode=\"External\""));
    }

    @Test
    void embedsResolvedLocalMarkdownImagesAsPowerPointPictureParts() throws Exception {
        Md2PptxOptions options = new Md2PptxOptions();
        options.setImageLoader(new Md2PptxOptions.ImageLoader() {
            @Override
            public ImageAsset load(String path) {
                if ("assets/chart.png".equals(path)) {
                    return new ImageAsset(ONE_PIXEL_PNG, "png");
                }
                return null;
            }
        });
        Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx("# Deck\n\n![Chart](assets/chart.png)", options);
        Map<String, byte[]> entries = ZipTestSupport.unzip(result.getPptx());

        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("<p:pic>"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("descr=\"Chart\""));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/_rels/slide1.xml.rels").contains("Target=\"../media/image1.png\""));
        assertNotNull(entries.get("ppt/media/image1.png"));
    }

    @Test
    void writesSpeakerNotesCommentsAsPowerPointNotesSlides() throws Exception {
        Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx("# Deck\n\nBody\n\n<!-- speaker-notes: Presenter reminder -->");
        Map<String, byte[]> entries = ZipTestSupport.unzip(result.getPptx());

        assertTrue(ZipTestSupport.text(entries, "[Content_Types].xml").contains("presentationml.notesSlide+xml"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/_rels/slide1.xml.rels").contains("relationships/notesSlide"));
        assertTrue(ZipTestSupport.text(entries, "ppt/notesSlides/notesSlide1.xml").contains("Presenter reminder"));
        assertTrue(ZipTestSupport.text(entries, "ppt/notesSlides/notesSlide1.xml").contains("ph type=\"sldImg\""));
    }

    @Test
    void reportsDiagnosticsForSkippedImagesAndRawHtmlLikeText() {
        Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx("# Deck\n\n![Missing](assets/missing.png)\n\n<span>raw</span>");

        assertTrue(result.getPptx().length > 0);
        assertEquals(2, result.getDiagnostics().size());
        assertEquals("possible-raw-html-text", result.getDiagnostics().get(0).getCode());
        assertEquals("skipped-image", result.getDiagnostics().get(1).getCode());
    }

    private String readResource(String path) throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(input);
        byte[] buffer = new byte[8192];
        int read;
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        while ((read = input.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
        input.close();
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}
