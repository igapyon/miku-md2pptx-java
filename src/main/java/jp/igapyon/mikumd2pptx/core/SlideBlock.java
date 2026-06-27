package jp.igapyon.mikumd2pptx.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SlideBlock {
    enum Kind {
        TEXT,
        TABLE,
        IMAGE
    }

    final Kind kind;
    final String text;
    final List<TextRun> runs;
    final List<List<TableCell>> rows;
    final String altText;
    final String url;

    private SlideBlock(Kind kind, String text, List<TextRun> runs, List<List<TableCell>> rows, String altText, String url) {
        this.kind = kind;
        this.text = text;
        this.runs = runs == null ? Collections.<TextRun>emptyList() : Collections.unmodifiableList(new ArrayList<TextRun>(runs));
        this.rows = rows == null ? Collections.<List<TableCell>>emptyList() : Collections.unmodifiableList(new ArrayList<List<TableCell>>(rows));
        this.altText = altText;
        this.url = url;
    }

    static SlideBlock text(List<TextRun> runs) {
        StringBuilder text = new StringBuilder();
        for (TextRun run : runs) {
            text.append(run.text);
        }
        return new SlideBlock(Kind.TEXT, text.toString(), runs, null, null, null);
    }

    static SlideBlock table(List<List<TableCell>> rows) {
        return new SlideBlock(Kind.TABLE, null, null, rows, null, null);
    }

    static SlideBlock image(String altText, String url) {
        return new SlideBlock(Kind.IMAGE, null, null, null, altText == null ? "" : altText, url == null ? "" : url);
    }
}
