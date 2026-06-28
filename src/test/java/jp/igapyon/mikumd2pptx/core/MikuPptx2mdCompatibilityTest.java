package jp.igapyon.mikumd2pptx.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MikuPptx2mdCompatibilityTest {
    @TempDir
    Path tempDir;

    @Test
    void roundTripsRepresentativeDeckThroughLocalMikuPptx2mdWhenAvailable() throws Exception {
        Path reverseRoot = java.nio.file.Paths.get("..", "miku-pptx2md").toAbsolutePath().normalize();
        Path reverseCli = reverseRoot.resolve("scripts/miku-pptx2md-cli.mjs");
        Path reverseCore = reverseRoot.resolve("dist/js/core.js");
        assumeTrue(Files.isRegularFile(reverseCli), "../miku-pptx2md CLI is not available");
        assumeTrue(Files.isRegularFile(reverseCore), "../miku-pptx2md dist output is not built");
        assumeTrue(isNodeAvailable(), "node is not available");

        Path pptx = tempDir.resolve("roundtrip.pptx");
        Path markdown = tempDir.resolve("roundtrip.md");
        Path summary = tempDir.resolve("summary.json");
        String source = "# Sales memo\n\n"
                + "Intro paragraph for the deck.\n\n"
                + "See [project site](https://example.com/project).\n\n"
                + "<!-- speaker-notes: Mention the project site during the talk. -->\n\n"
                + "## Numbers\n\n"
                + "| Item | Quantity |\n"
                + "| --- | --- |\n"
                + "| Apple | 3 |\n"
                + "| Orange | 5 |\n\n"
                + "- Confirm quantities\n";
        Files.write(pptx, new MikuMd2pptxCore().convertMarkdownToPptx(source).getPptx());

        ProcessResult result = runProcess(reverseRoot, "node", reverseCli.toString(), pptx.toString(),
                "--out", markdown.toString(), "--summary-json-out", summary.toString());

        assertEquals(0, result.exitCode, result.stderr);
        String converted = new String(Files.readAllBytes(markdown), StandardCharsets.UTF_8);
        String summaryJson = new String(Files.readAllBytes(summary), StandardCharsets.UTF_8);

        assertTrue(converted.contains("# Sales memo"));
        assertTrue(converted.contains("Intro paragraph for the deck."));
        assertTrue(converted.contains("[project site](https://example.com/project)"));
        assertTrue(converted.contains("### Speaker Notes"));
        assertTrue(converted.contains("Mention the project site during the talk."));
        assertTrue(converted.contains("## Slide 2: Numbers"));
        assertTrue(converted.contains("| Item | Quantity |"));
        assertTrue(converted.contains("| Apple | 3 |"));
        assertTrue(converted.contains("- Confirm quantities"));
        assertTrue(summaryJson.contains("\"slides\": 2"));
        assertTrue(summaryJson.contains("\"tables\": 1"));
        assertTrue(summaryJson.contains("\"hyperlinks\": 1"));
        assertTrue(summaryJson.contains("\"notesSlides\": 1"));
    }

    private boolean isNodeAvailable() {
        try {
            return runProcess(null, "node", "--version").exitCode == 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private ProcessResult runProcess(Path workingDirectory, String... command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command);
        if (workingDirectory != null) {
            builder.directory(workingDirectory.toFile());
        }
        Process process = builder.start();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        copy(process.getInputStream(), stdout);
        copy(process.getErrorStream(), stderr);
        int exitCode = process.waitFor();
        return new ProcessResult(exitCode, new String(stdout.toByteArray(), StandardCharsets.UTF_8),
                new String(stderr.toByteArray(), StandardCharsets.UTF_8));
    }

    private void copy(InputStream input, ByteArrayOutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
    }

    private static final class ProcessResult {
        final int exitCode;
        final String stdout;
        final String stderr;

        ProcessResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
