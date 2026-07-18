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
            Path workingDirectory = Paths.get("").toAbsolutePath().normalize();
            Path inputPath = workingDirectory.resolve(options.inputPath).normalize();
            Path outputPath = workingDirectory.resolve(options.outPath).normalize();
            String markdown = new String(Files.readAllBytes(inputPath), StandardCharsets.UTF_8);

            Md2PptxOptions convertOptions = new Md2PptxOptions();
            convertOptions.setTitle(options.title);
            convertOptions.setSourcePath(inputPath.toString());
            convertOptions.setImageLoader(createImageLoader(inputPath));
            if (options.templatePath != null) {
                Path templatePath = workingDirectory.resolve(options.templatePath).normalize();
                convertOptions.setTemplatePptx(Files.readAllBytes(templatePath));
            }

            Md2PptxResult result = new MikuMd2pptxCore().convertMarkdownToPptx(markdown, convertOptions);
            Path parent = outputPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(outputPath, result.getPptx());
            for (Md2PptxDiagnostic diagnostic : result.getDiagnostics()) {
                err.println(diagnostic.getSeverity() + ": " + diagnostic.getCode() + ": " + diagnostic.getMessage());
            }
            String writtenPath;
            try {
                writtenPath = workingDirectory.relativize(outputPath).toString();
            } catch (IllegalArgumentException ex) {
                writtenPath = outputPath.toString();
            }
            out.println("Wrote " + writtenPath);
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
                + "  java -jar target/miku-md2pptx-java-" + MikuMd2pptxCore.VERSION + ".jar <input.md> --out <output.pptx> [--template <template.pptx>]\n"
                + "  java -jar target/miku-md2pptx-java-" + MikuMd2pptxCore.VERSION + ".jar --help\n"
                + "  java -jar target/miku-md2pptx-java-" + MikuMd2pptxCore.VERSION + ".jar --version\n"
                + "\n"
                + "Options:\n"
                + "  --out <path>             Output .pptx path.\n"
                + "  --template <path>        Use a PowerPoint template's design information and\n"
                + "                           first title+body slide layout. Existing template\n"
                + "                           slides are not copied.\n"
                + "  --title <text>           Override the generated presentation title.\n"
                + "  --help, -h               Show this help.\n"
                + "  --version                Show the package version.\n"
                + "\n"
                + "Execution contract:\n"
                + "  Input, output, template, and local image paths are processed locally. Relative\n"
                + "  CLI paths are resolved from the current working directory.\n"
                + "  The output parent directory is created when needed. An existing output file\n"
                + "  is replaced without prompting.\n"
                + "  On success, the command exits 0 and prints \"Wrote <path>\" to stdout.\n"
                + "  Conversion diagnostics use \"<severity>: <code>: <message>\" on stderr. A\n"
                + "  warning does not by itself make the command fail. Fatal errors use stderr and\n"
                + "  a nonzero exit code.\n"
                + "\n"
                + "Template behavior:\n"
                + "  --template reads slide size, theme, slide masters, slide layouts, and related\n"
                + "  design parts from the template PPTX.\n"
                + "  Generated output contains only slides created from the Markdown input.\n"
                + "  Existing slides in the template are not copied, prepended, appended, or\n"
                + "  edited.\n"
                + "  Generated slides reference the first title+body/content layout found in the\n"
                + "  template. If no such layout is found, the converter tries a title-only layout,\n"
                + "  then falls back to the built-in generated layout with a diagnostic.\n"
                + "  If the template PPTX cannot be read, conversion fails instead of silently\n"
                + "  falling back.\n"
                + "  Template mode is structural, not pixel-perfect. Tables, images, and dense\n"
                + "  content may need final positioning in PowerPoint.\n"
                + "\n"
                + "Markdown handling notes:\n"
                + "  Heading level 1 and 2 blocks start new slides.\n"
                + "  Paragraphs, lists, fenced code blocks, and simple tables become editable\n"
                + "  PowerPoint content. Markdown links become external hyperlinks.\n"
                + "  Relative PNG, JPEG, and GIF images under the input file's directory can be\n"
                + "  embedded. Remote URLs, absolute paths, paths outside that directory, missing\n"
                + "  files, and unsupported formats are skipped with a warning.\n"
                + "  <!-- speaker-notes: text --> adds speaker notes to the current slide.\n"
                + "  The first implementation prioritizes structure and local generation over\n"
                + "  pixel-perfect PowerPoint layout.\n"
                + "\n"
                + "Examples:\n"
                + "  java -jar target/miku-md2pptx-java-" + MikuMd2pptxCore.VERSION + ".jar sample.md --out sample.pptx\n"
                + "  java -jar target/miku-md2pptx-java-" + MikuMd2pptxCore.VERSION + ".jar sample.md --out sample.pptx --template template.pptx\n"
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
