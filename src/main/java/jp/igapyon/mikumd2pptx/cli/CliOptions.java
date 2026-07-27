package jp.igapyon.mikumd2pptx.cli;

class CliOptions {
    String inputPath;
    String outPath;
    String templatePath;
    String title;
    boolean help;
    boolean version;

    static CliOptions parse(String[] args) {
        CliOptions options = new CliOptions();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--help".equals(arg) || "-h".equals(arg)) {
                options.help = true;
                return options;
            } else if ("--version".equals(arg)) {
                options.version = true;
                return options;
            } else if ("--out".equals(arg)) {
                options.outPath = requireValue(args, ++i, "--out requires a path.");
            } else if ("--title".equals(arg)) {
                options.title = requireValue(args, ++i, "--title requires text.");
            } else if ("--template".equals(arg)) {
                options.templatePath = requireValue(args, ++i, "--template requires a .pptx path.");
            } else if (arg.startsWith("-")) {
                throw new IllegalArgumentException("Unknown option: " + arg);
            } else if (options.inputPath == null) {
                options.inputPath = arg;
            } else {
                throw new IllegalArgumentException("Unexpected argument: " + arg);
            }
        }
        if (options.inputPath == null) {
            throw new IllegalArgumentException("Input Markdown path is required. Use --help for usage.");
        }
        if (options.outPath == null) {
            throw new IllegalArgumentException("--out is required. Use --help for usage.");
        }
        return options;
    }

    private static String requireValue(String[] args, int index, String message) {
        if (index >= args.length || args[index].startsWith("-")) {
            throw new IllegalArgumentException(message);
        }
        return args[index];
    }
}
