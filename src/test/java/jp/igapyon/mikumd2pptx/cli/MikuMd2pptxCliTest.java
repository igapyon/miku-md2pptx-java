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
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

class MikuMd2pptxCliTest {
    @TempDir
    Path tempDir;

    @Test
    void printsVersion() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int exit = new MikuMd2pptxCli().run(new String[] { "--version" }, new PrintStream(out), System.err);

        assertEquals(0, exit);
        assertEquals(MikuMd2pptxCore.VERSION, new String(out.toByteArray(), StandardCharsets.UTF_8).trim());
    }

    @Test
    void printsAgentReadableHelp() {
        CliRun run = runCli("--help");

        assertEquals(0, run.exitCode);
        assertTrue(run.out.startsWith("miku-md2pptx converts a Markdown file into a PowerPoint .pptx deck.\n"));
        assertTrue(run.out.contains("Examples:"));
        assertTrue(run.out.contains("Execution contract:"));
        assertTrue(run.out.contains("replaced without prompting"));
        assertTrue(run.out.contains("Conversion diagnostics"));
        assertTrue(run.out.contains("Tables, images, and dense"));
        assertTrue(run.out.contains("Markdown handling notes:"));
        assertTrue(run.out.contains("  --version                Show the package version."));
        assertTrue(run.out.contains("java -jar miku-md2pptx-java-" + MikuMd2pptxCore.VERSION + ".jar"));
        assertTrue(!run.out.contains("java -jar target/"));
        assertTrue(run.out.contains("Inputs:"));
        assertTrue(run.out.contains("Outputs:"));
        assertTrue(run.out.contains("Generated artifacts:"));
        assertTrue(run.out.contains("Machine-readable output contract:"));
        assertTrue(run.out.contains("not a stable machine-readable data format"));
        assertTrue(run.out.contains("Exit codes:"));
        assertTrue(run.out.contains("  1  Input, output, template, or conversion processing failed."));
        assertTrue(run.out.contains("  2  CLI usage error"));
        assertTrue(run.out.contains("<!-- speaker-notes: text -->"));
        assertTrue(run.out.contains("  pixel-perfect PowerPoint layout."));
        assertEquals("", run.err);
    }

    @Test
    void printsHelpWithShortAlias() {
        CliRun run = runCli("-h");

        assertEquals(0, run.exitCode);
        assertTrue(run.out.contains("Usage:"));
        assertEquals("", run.err);
    }

    @Test
    void rejectsUnknownOption() {
        CliRun run = runCli("--unknown");

        assertEquals(2, run.exitCode);
        assertEquals("Unknown option: --unknown\n", run.err);
        assertEquals("", run.out);
    }

    @Test
    void rejectsUnknownShortOption() {
        CliRun run = runCli("-x");

        assertEquals(2, run.exitCode);
        assertEquals("Unknown option: -x\n", run.err);
        assertEquals("", run.out);
    }

    @Test
    void rejectsNoArgumentsAsUsageError() {
        CliRun run = runCli();

        assertEquals(2, run.exitCode);
        assertEquals("Input Markdown path is required. Use --help for usage.\n", run.err);
        assertEquals("", run.out);
    }

    @Test
    void rejectsUnexpectedArgument() {
        CliRun run = runCli("one.md", "two.md", "--out", tempDir.resolve("out.pptx").toString());

        assertEquals(2, run.exitCode);
        assertEquals("Unexpected argument: two.md\n", run.err);
        assertEquals("", run.out);
    }

    @Test
    void rejectsMissingOutArgument() {
        CliRun run = runCli(tempDir.resolve("input.md").toString());

        assertEquals(2, run.exitCode);
        assertEquals("--out is required. Use --help for usage.\n", run.err);
        assertEquals("", run.out);
    }

    @Test
    void rejectsMissingInputArgument() {
        CliRun run = runCli("--out", tempDir.resolve("out.pptx").toString());

        assertEquals(2, run.exitCode);
        assertEquals("Input Markdown path is required. Use --help for usage.\n", run.err);
        assertEquals("", run.out);
    }

    @Test
    void rejectsMissingValueForOptions() {
        CliRun run = runCli(tempDir.resolve("input.md").toString(), "--out");

        assertEquals(2, run.exitCode);
        assertEquals("--out requires a path.\n", run.err);
        assertEquals("", run.out);
    }

    @Test
    void rejectsOptionTokenAsOutValue() {
        CliRun run = runCli(tempDir.resolve("input.md").toString(), "--out", "-x");

        assertEquals(2, run.exitCode);
        assertEquals("--out requires a path.\n", run.err);
        assertEquals("", run.out);
    }

    @Test
    void rejectsOptionTokenAsTemplateValue() {
        CliRun run = runCli(tempDir.resolve("input.md").toString(), "--out",
                tempDir.resolve("out.pptx").toString(), "--template", "-x");

        assertEquals(2, run.exitCode);
        assertEquals("--template requires a .pptx path.\n", run.err);
        assertEquals("", run.out);
    }

    @Test
    void rejectsOptionTokenAsTitleValue() {
        CliRun run = runCli(tempDir.resolve("input.md").toString(), "--out",
                tempDir.resolve("out.pptx").toString(), "--title", "-x");

        assertEquals(2, run.exitCode);
        assertEquals("--title requires text.\n", run.err);
        assertEquals("", run.out);
    }

    @Test
    void writesPptxFile() throws Exception {
        Path input = tempDir.resolve("input.md");
        Path out = tempDir.resolve("out.pptx");
        Files.write(input, "# Sales memo\n\n## Table\n\n| Fruit | Count |\n| --- | --- |\n| Apple | 3 |\n".getBytes(StandardCharsets.UTF_8));

        CliRun run = runCli(input.toString(), "--out", out.toString());
        Map<String, byte[]> entries = ZipTestSupport.unzip(Files.readAllBytes(out));

        assertEquals(0, run.exitCode);
        assertEquals("", run.err);
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("Sales memo"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide2.xml").contains("<a:tbl>"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide2.xml").contains("<a:t>Apple</a:t>"));
    }

    @Test
    void createsOutputParentDirectoriesAndReplacesExistingFile() throws Exception {
        Path input = tempDir.resolve("input.md");
        Path out = tempDir.resolve("nested/output/out.pptx");
        Files.write(input, "# First\n\nBody\n".getBytes(StandardCharsets.UTF_8));

        CliRun first = runCli(input.toString(), "--out", out.toString());
        Files.write(input, "# Replacement\n\nUpdated\n".getBytes(StandardCharsets.UTF_8));
        CliRun second = runCli(input.toString(), "--out", out.toString());
        Map<String, byte[]> entries = ZipTestSupport.unzip(Files.readAllBytes(out));

        assertEquals(0, first.exitCode);
        assertEquals(0, second.exitCode);
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("Replacement"));
        assertTrue(!ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("First"));
    }

    @Test
    void reportsMissingInputFileAsProcessingFailure() {
        Path missing = tempDir.resolve("missing.md");
        Path out = tempDir.resolve("out.pptx");

        CliRun run = runCli(missing.toString(), "--out", out.toString());

        assertEquals(1, run.exitCode);
        assertTrue(!run.err.isEmpty());
        assertEquals("", run.out);
    }

    @Test
    void printsConversionDiagnosticsToStderr() throws Exception {
        Path input = tempDir.resolve("input.md");
        Path out = tempDir.resolve("out.pptx");
        Files.write(input, "# Diagnostics\n\n![Missing](assets/missing.png)\n".getBytes(StandardCharsets.UTF_8));
        CliRun run = runCli(input.toString(), "--out", out.toString());

        assertEquals(0, run.exitCode);
        assertTrue(run.out.contains("Wrote "));
        assertTrue(run.err.contains("warning: skipped-image: Markdown image was not embedded: assets/missing.png"));
    }

    @Test
    void writesPptxUsingTemplateWithoutCopyingTemplateSlides() throws Exception {
        Path input = tempDir.resolve("input.md");
        Path template = tempDir.resolve("template.pptx");
        Path out = tempDir.resolve("out.pptx");
        Files.write(input, "# Generated\n\nGenerated body\n".getBytes(StandardCharsets.UTF_8));
        Files.write(template, new MikuMd2pptxCore().convertMarkdownToPptx(
                "# Template\n\nTemplate-only body").getPptx());

        CliRun run = runCli(input.toString(), "--out", out.toString(), "--template", template.toString());
        Map<String, byte[]> entries = ZipTestSupport.unzip(Files.readAllBytes(out));

        assertEquals(0, run.exitCode);
        assertTrue(run.err.contains("info: template-layout-selected:"));
        assertTrue(ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("Generated body"));
        assertTrue(!ZipTestSupport.text(entries, "ppt/slides/slide1.xml").contains("Template-only body"));
    }

    private CliRun runCli(String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int exitCode = new MikuMd2pptxCli().run(args, new PrintStream(out), new PrintStream(err));
        return new CliRun(exitCode, new String(out.toByteArray(), StandardCharsets.UTF_8), new String(err.toByteArray(), StandardCharsets.UTF_8));
    }

    private static final class CliRun {
        private final int exitCode;
        private final String out;
        private final String err;

        private CliRun(int exitCode, String out, String err) {
            this.exitCode = exitCode;
            this.out = out;
            this.err = err;
        }
    }
}
