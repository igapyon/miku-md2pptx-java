package jp.igapyon.mikumd2pptx.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MarkdownSlides {
    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*)$");
    private static final Pattern SETEXT_HEADING = Pattern.compile("^\\s*(=+|-+)\\s*$");
    private static final Pattern IMAGE_ONLY = Pattern.compile("^!\\[([^\\]]*)\\]\\((.+)\\)\\s*$");
    private static final Pattern NOTES = Pattern.compile("^\\s*<!--\\s*speaker-notes(?::|\\s)(.*?)-->\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NOTES_START = Pattern.compile("^\\s*<!--\\s*speaker-notes(?::|\\s)(.*)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern LINK = Pattern.compile("\\[([^\\]]+)\\]\\((.+?)\\)");
    private static final Pattern LIST_ITEM = Pattern.compile("^(\\s*)(?:[-+*]|\\d+[.)])\\s+(.*)$");
    private static final Pattern BLOCKQUOTE = Pattern.compile("^\\s*>\\s?(.*)$");
    private static final Pattern THEMATIC_BREAK = Pattern.compile("^\\s{0,3}([-*_])(?:\\s*\\1){2,}\\s*$");

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

            if (isSetextHeading(lines, index)) {
                current = new SlideModel(normalizeLine(stripInlineMarkdown(lines[index])));
                slides.add(current);
                index += 2;
                continue;
            }

            Matcher notes = NOTES.matcher(line);
            if (notes.matches()) {
                current = ensureSlide(slides, current, options);
                addNotes(current, notes.group(1));
                index++;
                continue;
            }

            Matcher notesStart = NOTES_START.matcher(line);
            if (notesStart.matches()) {
                current = ensureSlide(slides, current, options);
                StringBuilder noteText = new StringBuilder();
                String firstLine = notesStart.group(1);
                int end = firstLine.indexOf("-->");
                if (end >= 0) {
                    noteText.append(firstLine.substring(0, end));
                    index++;
                } else {
                    noteText.append(firstLine);
                    index++;
                    while (index < lines.length) {
                        String noteLine = lines[index];
                        int close = noteLine.indexOf("-->");
                        if (close >= 0) {
                            noteText.append('\n').append(noteLine.substring(0, close));
                            index++;
                            break;
                        }
                        noteText.append('\n').append(noteLine);
                        index++;
                    }
                }
                addNotes(current, noteText.toString());
                continue;
            }

            if (line.trim().isEmpty()) {
                index++;
                continue;
            }

            Matcher thematicBreak = THEMATIC_BREAK.matcher(line);
            if (thematicBreak.matches()) {
                current = ensureSlide(slides, current, options);
                current.blocks.add(SlideBlock.text(Arrays.asList(new TextRun("---"))));
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
                String imageUrl = extractLinkTarget(image.group(2));
                current.blocks.add(SlideBlock.image(image.group(1), imageUrl));
                index++;
                continue;
            }

            Matcher listItem = LIST_ITEM.matcher(line);
            if (listItem.matches()) {
                current = ensureSlide(slides, current, options);
                while (index < lines.length) {
                    Matcher item = LIST_ITEM.matcher(lines[index]);
                    if (!item.matches()) {
                        break;
                    }
                    StringBuilder itemText = new StringBuilder(item.group(2));
                    int currentIndent = listIndent(item.group(1));
                    index++;
                    while (index < lines.length && isListContinuation(lines[index], currentIndent)) {
                        itemText.append(' ').append(lines[index].trim());
                        index++;
                    }
                    current.blocks.add(SlideBlock.text(prefixedRuns(listPrefix(item.group(1)), itemText.toString())));
                }
                continue;
            }

            Matcher blockquote = BLOCKQUOTE.matcher(line);
            if (blockquote.matches()) {
                current = ensureSlide(slides, current, options);
                while (index < lines.length) {
                    Matcher quote = BLOCKQUOTE.matcher(lines[index]);
                    if (!quote.matches()) {
                        break;
                    }
                    current.blocks.add(SlideBlock.text(prefixedRuns("> ", quote.group(1))));
                    index++;
                }
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
                    && !LIST_ITEM.matcher(lines[index]).matches()
                    && !BLOCKQUOTE.matcher(lines[index]).matches()
                    && !THEMATIC_BREAK.matcher(lines[index]).matches()
                    && !NOTES.matcher(lines[index]).matches()
                    && !NOTES_START.matcher(lines[index]).matches()) {
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
            String target = extractLinkTarget(matcher.group(2));
            if (!target.isEmpty()) {
                runs.add(new TextRun(stripInlineMarkdown(matcher.group(1)), target));
            } else {
                runs.add(new TextRun(stripInlineMarkdown(matcher.group(1))));
            }
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

    private List<TextRun> prefixedRuns(String prefix, String markdownText) {
        List<TextRun> runs = new ArrayList<TextRun>();
        runs.add(new TextRun(prefix));
        runs.addAll(textRuns(markdownText));
        return runs;
    }

    private int listIndent(String indent) {
        if (indent == null) {
            return 0;
        }
        return indent.replace("\t", "  ").length();
    }

    private int lineIndent(String line) {
        int spaces = 0;
        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (current == ' ') {
                spaces++;
            } else if (current == '\t') {
                spaces += 2;
            } else {
                break;
            }
        }
        return spaces;
    }

    private boolean isListContinuation(String line, int listIndent) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        Matcher nested = LIST_ITEM.matcher(line);
        if (nested.matches()) {
            return false;
        }
        return lineIndent(line) > listIndent;
    }

    private boolean isSetextHeading(String[] lines, int index) {
        if (index + 1 >= lines.length) {
            return false;
        }
        if (lines[index].trim().isEmpty() || HEADING.matcher(lines[index]).matches()) {
            return false;
        }
        return SETEXT_HEADING.matcher(lines[index + 1]).matches();
    }

    private String listPrefix(String indent) {
        int depth = listIndent(indent) / 2;
        StringBuilder prefix = new StringBuilder();
        for (int index = 0; index < depth; index++) {
            prefix.append("  ");
        }
        prefix.append("- ");
        return prefix.toString();
    }

    private String stripInlineMarkdown(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\*\\*([^*]+)\\*\\*", "$1")
                .replaceAll("__([^_]+)__", "$1")
                .replaceAll("~~([^~]+)~~", "$1")
                .replaceAll("`([^`]+)`", "$1")
                .replaceAll("(?<!\\w)\\*([^*]+)\\*(?!\\w)", "$1")
                .replaceAll("(?<!\\w)_([^_]+)_(?!\\w)", "$1");
    }

    private String normalizeLine(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private void addNotes(SlideModel slide, String value) {
        if (value == null) {
            return;
        }
        String[] noteLines = value.split("\\r?\\n", -1);
        for (String noteLine : noteLines) {
            String text = normalizeLine(noteLine);
            if (!text.isEmpty()) {
                slide.notes.add(Arrays.asList(new TextRun(text)));
            }
        }
    }

    private String extractLinkTarget(String rawTarget) {
        if (rawTarget == null) {
            return "";
        }
        String target = rawTarget.trim();
        if (target.startsWith("<") && target.indexOf(">") >= 0) {
            target = target.substring(1, target.indexOf(">"));
        }
        int separator = firstUnquotedWhitespace(target);
        if (separator >= 0) {
            target = target.substring(0, separator);
        }
        if (target.length() >= 2) {
            char start = target.charAt(0);
            char end = target.charAt(target.length() - 1);
            if ((start == '"' && end == '"') || (start == '\'' && end == '\'')) {
                target = target.substring(1, target.length() - 1);
            }
        }
        return target.trim();
    }

    private int firstUnquotedWhitespace(String value) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current == '\'') {
                inSingleQuote = !inSingleQuote;
            } else if (current == '"') {
                inDoubleQuote = !inDoubleQuote;
            } else if (Character.isWhitespace(current) && !inSingleQuote && !inDoubleQuote) {
                return index;
            }
        }
        return -1;
    }
}
