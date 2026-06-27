package jp.igapyon.mikumd2pptx.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MarkdownSlides {
    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*)$");
    private static final Pattern IMAGE_ONLY = Pattern.compile("^!\\[([^\\]]*)\\]\\(([^)]+)\\)\\s*$");
    private static final Pattern NOTES = Pattern.compile("^\\s*<!--\\s*speaker-notes(?::|\\s)(.*?)-->\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LINK = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");

    List<SlideModel> parse(String markdown, Md2PptxOptions options) {
        List<SlideModel> slides = new ArrayList<SlideModel>();
        SlideModel current = null;
        String[] lines = markdown == null ? new String[0] : markdown.split("\\r?\\n", -1);
        int index = 0;
        while (index < lines.length) {
            String line = lines[index];
            Matcher heading = HEADING.matcher(line);
            if (heading.matches() && heading.group(1).length() <= 2) {
                current = new SlideModel(normalizeLine(stripInlineMarkdown(heading.group(2))));
                slides.add(current);
                index++;
                continue;
            }

            Matcher notes = NOTES.matcher(line);
            if (notes.matches()) {
                current = ensureSlide(slides, current, options);
                String value = notes.group(1).trim();
                if (!value.isEmpty()) {
                    current.notes.add(Arrays.asList(new TextRun(value)));
                }
                index++;
                continue;
            }

            if (line.trim().isEmpty()) {
                index++;
                continue;
            }

            if (isTableStart(lines, index)) {
                current = ensureSlide(slides, current, options);
                List<List<TableCell>> rows = new ArrayList<List<TableCell>>();
                rows.add(tableRow(lines[index]));
                index += 2;
                while (index < lines.length && isTableLine(lines[index])) {
                    rows.add(tableRow(lines[index]));
                    index++;
                }
                current.blocks.add(SlideBlock.table(rows));
                continue;
            }

            Matcher image = IMAGE_ONLY.matcher(line.trim());
            if (image.matches()) {
                current = ensureSlide(slides, current, options);
                current.blocks.add(SlideBlock.image(image.group(1), image.group(2)));
                index++;
                continue;
            }

            if (line.startsWith("```")) {
                current = ensureSlide(slides, current, options);
                index++;
                while (index < lines.length && !lines[index].startsWith("```")) {
                    current.blocks.add(SlideBlock.text(Arrays.asList(new TextRun("    " + lines[index]))));
                    index++;
                }
                if (index < lines.length) {
                    index++;
                }
                continue;
            }

            current = ensureSlide(slides, current, options);
            StringBuilder paragraph = new StringBuilder(line.trim());
            index++;
            while (index < lines.length && !lines[index].trim().isEmpty()
                    && !HEADING.matcher(lines[index]).matches()
                    && !isTableStart(lines, index)
                    && !NOTES.matcher(lines[index]).matches()) {
                paragraph.append(' ').append(lines[index].trim());
                index++;
            }
            current.blocks.add(SlideBlock.text(textRuns(paragraph.toString())));
        }

        if (slides.isEmpty()) {
            slides.add(new SlideModel(options != null && options.getTitle() != null ? options.getTitle() : "Markdown deck"));
        } else if (options != null && options.getTitle() != null && !options.getTitle().trim().isEmpty()) {
            slides.get(0).title = options.getTitle();
        }
        return slides;
    }

    private SlideModel ensureSlide(List<SlideModel> slides, SlideModel current, Md2PptxOptions options) {
        if (current != null) {
            return current;
        }
        String title = options != null && options.getTitle() != null ? options.getTitle() : "Markdown deck";
        SlideModel created = new SlideModel(title);
        slides.add(created);
        return created;
    }

    private boolean isTableStart(String[] lines, int index) {
        return index + 1 < lines.length && isTableLine(lines[index]) && lines[index + 1].matches("^\\s*\\|?\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)+\\|?\\s*$");
    }

    private boolean isTableLine(String line) {
        return line != null && line.contains("|") && line.trim().length() > 0;
    }

    private List<TableCell> tableRow(String line) {
        String value = line.trim();
        if (value.startsWith("|")) {
            value = value.substring(1);
        }
        if (value.endsWith("|")) {
            value = value.substring(0, value.length() - 1);
        }
        String[] cells = value.split("\\|", -1);
        List<TableCell> row = new ArrayList<TableCell>();
        for (String cell : cells) {
            row.add(new TableCell(textRuns(cell.trim())));
        }
        return row;
    }

    private List<TextRun> textRuns(String markdownText) {
        List<TextRun> runs = new ArrayList<TextRun>();
        Matcher matcher = LINK.matcher(markdownText);
        int offset = 0;
        while (matcher.find()) {
            if (matcher.start() > offset) {
                runs.add(new TextRun(stripInlineMarkdown(markdownText.substring(offset, matcher.start()))));
            }
            runs.add(new TextRun(stripInlineMarkdown(matcher.group(1)), matcher.group(2)));
            offset = matcher.end();
        }
        if (offset < markdownText.length()) {
            runs.add(new TextRun(stripInlineMarkdown(markdownText.substring(offset))));
        }
        List<TextRun> normalized = new ArrayList<TextRun>();
        for (TextRun run : runs) {
            String text = normalizeLine(run.text);
            if (!text.isEmpty()) {
                normalized.add(new TextRun(text, run.href));
            }
        }
        return normalized;
    }

    private String stripInlineMarkdown(String value) {
        return value == null ? "" : value.replace("**", "").replace("__", "").replace("`", "");
    }

    private String normalizeLine(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }
}
