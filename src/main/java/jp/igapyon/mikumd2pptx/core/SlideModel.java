package jp.igapyon.mikumd2pptx.core;

import java.util.ArrayList;
import java.util.List;

class SlideModel {
    String title;
    final List<SlideBlock> blocks = new ArrayList<SlideBlock>();
    final List<List<TextRun>> notes = new ArrayList<List<TextRun>>();

    SlideModel(String title) {
        this.title = title == null || title.trim().isEmpty() ? "Untitled slide" : title;
    }
}
