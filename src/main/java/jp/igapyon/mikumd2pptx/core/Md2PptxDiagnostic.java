package jp.igapyon.mikumd2pptx.core;

public class Md2PptxDiagnostic {
    private final String severity;
    private final String code;
    private final String message;
    private final String source;

    public Md2PptxDiagnostic(String severity, String code, String message, String source) {
        this.severity = severity;
        this.code = code;
        this.message = message;
        this.source = source;
    }

    public String getSeverity() {
        return severity;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }
}
