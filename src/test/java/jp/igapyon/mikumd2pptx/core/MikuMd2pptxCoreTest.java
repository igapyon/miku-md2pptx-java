package jp.igapyon.mikumd2pptx.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void splitsMarkdownIntoSlidesAtLevelOneAndTwoHeadings() {
        List<SlideModel> slides = new MikuMd2pptxCore().markdownToSlides("# One\n\nBody\n\n## Two\n\n- Item\n", new Md2PptxOptions());

        assertEquals(2, slides.size());
        assertEquals("One", slides.get(0).title);
        assertEquals("Body", slides.get(0).blocks.get(0).text);
        assertEquals("Two", slides.get(1).title);
        assertEquals("- Item", slides.get(1).blocks.get(0).text);
    }

    @Test
    void createsPptxPackageWithPresentationAndSlideParts() throws Exception {
        Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx("# Deck\n\n## Slide\n\nBody");
        Map<String, byte[]> entries = ZipTestSupport.unzip(result.getPptx());

        assertTrue(ZipTestSupport.text(entries, "[Content_Types].xml").contains("presentationml.presentation.main+xml"));
        assertTrue(ZipTestSupport.text(entries, "ppt/_rels/presentation.xml.rels").contains("slides/slide1.xml"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("Deck"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide2.xml").contains("Body"));
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
}
