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
                options.outPath = requireValue(args, ++i, "--out");
            } else if ("--title".equals(arg)) {
                options.title = requireValue(args, ++i, "--title");
            } else if ("--template".equals(arg)) {
                options.templatePath = requireValue(args, ++i, "--template");
            } else if (arg.startsWith("--")) {
                throw new IllegalArgumentException("Unknown option: " + arg);
            } else if (options.inputPath == null) {
                options.inputPath = arg;
            } else {
                throw new IllegalArgumentException("Unexpected argument: " + arg);
            }
        }
        return options;
    }

    private static String requireValue(String[] args, int index, String option) {
        if (index >= args.length || args[index].startsWith("--")) {
            throw new IllegalArgumentException(option + " requires a value.");
        }
        return args[index];
    }
}
