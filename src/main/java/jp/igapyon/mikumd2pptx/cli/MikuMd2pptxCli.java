package jp.igapyon.mikumd2pptx.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jp.igapyon.mikumd2pptx.core.ImageAsset;
import jp.igapyon.mikumd2pptx.core.Md2PptxDiagnostic;
import jp.igapyon.mikumd2pptx.core.Md2PptxOptions;
import jp.igapyon.mikumd2pptx.core.Md2PptxResult;
import jp.igapyon.mikumd2pptx.core.MikuMd2pptxCore;

public class MikuMd2pptxCli {
    public static void main(String[] args) {
        int exitCode = new MikuMd2pptxCli().run(args, System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public int run(String[] args, PrintStream out, PrintStream err) {
        CliOptions options;
        try {
            options = CliOptions.parse(args);
        } catch (IllegalArgumentException ex) {
            err.println(ex.getMessage());
            return 2;
        }
        if (options.help) {
            out.print(helpText());
            return 0;
        }
        if (options.version) {
            out.println(MikuMd2pptxCore.VERSION);
            return 0;
        }
        if (options.inputPath == null || options.outPath == null) {
            err.print(helpText());
            return 2;
        }

        try {
            Path inputPath = Paths.get(options.inputPath).toAbsolutePath().normalize();
            Path outputPath = Paths.get(options.outPath).toAbsolutePath().normalize();
            String markdown = new String(Files.readAllBytes(inputPath), StandardCharsets.UTF_8);

            Md2PptxOptions convertOptions = new Md2PptxOptions();
            convertOptions.setTitle(options.title);
            convertOptions.setSourcePath(inputPath.toString());
            convertOptions.setImageLoader(createImageLoader(inputPath));

            Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx(markdown, convertOptions);
            Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(outputPath, result.getPptx());
            for (Md2PptxDiagnostic diagnostic : result.getDiagnostics()) {
                err.println(diagnostic.getSeverity() + ": " + diagnostic.getCode() + ": " + diagnostic.getMessage());
            }
            out.println("Wrote " + outputPath);
            return 0;
        } catch (IOException ex) {
            err.println(ex.getMessage());
            return 1;
        } catch (RuntimeException ex) {
            err.println(ex.getMessage());
            return 1;
        }
    }

    public static String helpText() {
        return "miku-md2pptx converts a Markdown file into a PowerPoint .pptx deck.\n"
                + "\n"
                + "Usage:\n"
                + "  java -jar target/miku-md2pptx-java-" + MikuMd2pptxCore.VERSION + ".jar <input.md> --out <output.pptx>\n"
                + "  java -jar target/miku-md2pptx-java-" + MikuMd2pptxCore.VERSION + ".jar --help\n"
                + "  java -jar target/miku-md2pptx-java-" + MikuMd2pptxCore.VERSION + ".jar --version\n"
                + "\n"
                + "Options:\n"
                + "  --out <path>       Output .pptx path.\n"
                + "  --title <text>     Override the generated presentation title.\n"
                + "  --help             Show this help.\n"
                + "  --version          Show the package version.\n"
                + "\n"
                + "Markdown handling notes:\n"
                + "  Heading level 1 and 2 blocks start new slides.\n"
                + "  Paragraphs, lists, code blocks, and tables become simple editable slide text.\n"
                + "  The first implementation prioritizes structure and local generation over\n"
                + "  pixel-perfect PowerPoint layout.\n"
                + "\n"
                + "Examples:\n"
                + "  java -jar target/miku-md2pptx-java-" + MikuMd2pptxCore.VERSION + ".jar sample.md --out sample.pptx\n"
                + "  java -jar target/miku-md2pptx-java-" + MikuMd2pptxCore.VERSION + ".jar sample.md --out sample.pptx --title \"Project brief\"\n";
    }

    private Md2PptxOptions.ImageLoader createImageLoader(final Path inputPath) {
        return new Md2PptxOptions.ImageLoader() {
            @Override
            public ImageAsset load(String path) {
                if (path == null || path.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*") || path.startsWith("/")) {
                    return null;
                }
                String cleanPath = path.split("#", 2)[0].split("\\?", 2)[0];
                String lower = cleanPath.toLowerCase();
                String extension;
                if (lower.endsWith(".png")) {
                    extension = "png";
                } else if (lower.endsWith(".jpg")) {
                    extension = "jpg";
                } else if (lower.endsWith(".jpeg")) {
                    extension = "jpeg";
                } else if (lower.endsWith(".gif")) {
                    extension = "gif";
                } else {
                    return null;
                }
                try {
                    Path base = inputPath.toAbsolutePath().getParent();
                    Path resolved = (base == null ? Paths.get(cleanPath) : base.resolve(cleanPath)).normalize();
                    if (base != null && !resolved.startsWith(base.normalize())) {
                        return null;
                    }
                    return new ImageAsset(Files.readAllBytes(resolved), extension);
                } catch (IOException ex) {
                    return null;
                }
            }
        };
    }
}
