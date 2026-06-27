package jp.igapyon.mikumd2pptx.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import jp.igapyon.mikumd2pptx.core.MikuMd2pptxCore;
import jp.igapyon.mikumd2pptx.core.ZipTestSupport;
import org.junit.jupiter.api.Test;

class MikuMd2pptxCliTest {
    @Test
    void printsVersion() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int exit = new MikuMd2pptxCli().run(new String[] { "--version" }, new PrintStream(out), System.err);

        assertEquals(0, exit);
        assertEquals(MikuMd2pptxCore.VERSION, new String(out.toByteArray(), StandardCharsets.UTF_8).trim());
    }

    @Test
    void printsAgentReadableHelp() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int exit = new MikuMd2pptxCli().run(new String[] { "--help" }, new PrintStream(out), System.err);
        String text = new String(out.toByteArray(), StandardCharsets.UTF_8);

        assertEquals(0, exit);
        assertTrue(text.contains("converts a Markdown file into a PowerPoint .pptx deck"));
        assertTrue(text.contains("Examples:"));
        assertTrue(text.contains("Markdown handling notes:"));
    }

    @Test
    void writesPptxFile() throws Exception {
        Path dir = Files.createTempDirectory("miku-md2pptx-java-");
        Path input = dir.resolve("input.md");
        Path out = dir.resolve("out.pptx");
        Files.write(input, "# Sales memo\n\n## Table\n\n| Fruit | Count |\n| --- | --- |\n| Apple | 3 |\n".getBytes(StandardCharsets.UTF_8));

        int exit = new MikuMd2pptxCli().run(new String[] { input.toString(), "--out", out.toString() }, System.out, System.err);
        Map<String, byte[]> entries = ZipTestSupport.unzip(Files.readAllBytes(out));

        assertEquals(0, exit);
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("Sales memo"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide2.xml").contains("<a:tbl>"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide2.xml").contains("<a:t>Apple</a:t>"));
    }

    @Test
    void printsConversionDiagnosticsToStderr() throws Exception {
        Path dir = Files.createTempDirectory("miku-md2pptx-java-");
        Path input = dir.resolve("input.md");
        Path out = dir.resolve("out.pptx");
        Files.write(input, "# Diagnostics\n\n![Missing](assets/missing.png)\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        int exit = new MikuMd2pptxCli().run(new String[] { input.toString(), "--out", out.toString() }, System.out, new PrintStream(err));

        assertEquals(0, exit);
        assertTrue(new String(err.toByteArray(), StandardCharsets.UTF_8).contains("warning: skipped-image: Markdown image was not embedded: assets/missing.png"));
    }
}
