package jp.igapyon.mikumd2pptx.core;

import java.util.List;

class TableCell {
    final String text;
    final List<TextRun> runs;

    TableCell(List<TextRun> runs) {
        StringBuilder value = new StringBuilder();
        for (TextRun run : runs) {
            value.append(run.text);
        }
        this.text = value.toString();
        this.runs = runs;
    }
}
