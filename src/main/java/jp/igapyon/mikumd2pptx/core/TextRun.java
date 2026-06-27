package jp.igapyon.mikumd2pptx.core;

class TextRun {
    final String text;
    final String href;

    TextRun(String text) {
        this(text, null);
    }

    TextRun(String text, String href) {
        this.text = text == null ? "" : text;
        this.href = href;
    }
}
