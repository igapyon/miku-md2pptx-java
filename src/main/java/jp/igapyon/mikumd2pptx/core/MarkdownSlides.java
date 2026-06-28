package jp.igapyon.mikumd2pptx.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MarkdownSlides {
    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.*)$");
    private static final Pattern SETEXT_HEADING = Pattern.compile("^\\s*(=+|-+)\\s*$");
    private static final String LINK_TARGET_PATTERN = "((?:\\\\.|\\((?:\\\\.|\\([^()]*\\)|[^()])*\\)|[^)])+)";
    private static final String LABEL_PATTERN = "((?:\\\\.|\\[[^\\[\\]]*\\]|[^\\\\\\]])*)";
    private static final Pattern IMAGE_ONLY = Pattern.compile("^!\\[" + LABEL_PATTERN + "\\]\\(" + LINK_TARGET_PATTERN + "\\)\\s*$");
    private static final Pattern NOTES = Pattern.compile("^\\s*<!--\\s*speaker-notes(?::|\\s)(.*?)-->\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern NOTES_START = Pattern.compile("^\\s*<!--\\s*speaker-notes(?::|\\s)(.*)$", Pattern.CASE_INSENSITIVE);
    private static final String EMAIL_PATTERN = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}";
    private static final Pattern INLINE_LINK = Pattern.compile("(?<!!)\\[" + LABEL_PATTERN + "\\]\\(" + LINK_TARGET_PATTERN + "\\)|<((?:https?|mailto):[^>\\s]+)>|<(" + EMAIL_PATTERN + ")>|(?<![\\w/@])((?:https?://|www\\.)[^\\s<]+[^\\s<.,;:!?])|(?<![\\w.+-])(" + EMAIL_PATTERN + ")(?=$|[^A-Za-z0-9_%+-])");
    private static final Pattern LIST_ITEM = Pattern.compile("^(\\s*)(?:[-+*]|\\d+[.)])\\s+(.*)$");
    private static final Pattern BLOCKQUOTE = Pattern.compile("^\\s*>.*$");
    private static final Pattern THEMATIC_BREAK = Pattern.compile("^\\s{0,3}([-*_])(?:\\s*\\1){2,}\\s*$");
    private static final Pattern FENCED_CODE = Pattern.compile("^\\s{0,3}(```+|~~~+).*$");
    private static final Pattern FOOTNOTE_DEFINITION = Pattern.compile("^\\s{0,3}\\[\\^[^\\]]+\\]:\\s*(.*)$");
    private static final Pattern DEFINITION = Pattern.compile("^\\s{0,3}\\[[^\\]]+\\]:\\s+.+$");
    private static final Pattern DEFINITION_LABEL = Pattern.compile("^\\s{0,3}\\[([^\\]]+)\\]:\\s+.+$");
    private static final Pattern SHORTCUT_REFERENCE = Pattern.compile("(?<!!)(?<!\\\\)\\[([^\\]^][^\\]]*)\\](?![\\[(])");
    private static final Pattern IMAGE_REFERENCE_ONLY = Pattern.compile("^!\\[" + LABEL_PATTERN + "\\]\\[[^\\]]*\\]\\s*$");
    private static final Pattern IMAGE_SHORTCUT_ONLY = Pattern.compile("^!\\[" + LABEL_PATTERN + "\\]\\s*$");
    private static final Pattern IMAGE_SHORTCUT_REFERENCE = Pattern.compile("!\\[" + LABEL_PATTERN + "\\](?![\\[(])");
    private static final Pattern TASK_MARKER = Pattern.compile("^\\[[ xX]\\]\\s+");
    private static final Pattern NUMERIC_ENTITY = Pattern.compile("&#(x[0-9A-Fa-f]+|[0-9]+);");
    private Set<String> referenceLabels = new HashSet<String>();

    List<SlideModel> parse(String markdown, Md2PptxOptions options) {
        List<SlideModel> slides = new ArrayList<SlideModel>();
        SlideModel current = null;
        String[] lines = markdown == null ? new String[0] : markdown.split("\\r?\\n", -1);
        referenceLabels = referenceLabels(lines);
        int index = 0;
        while (index < lines.length) {
            String line = lines[index];
            Matcher heading = HEADING.matcher(line);
            if (heading.matches() && heading.group(1).length() <= 2) {
                current = new SlideModel(headingText(heading.group(2), true));
                slides.add(current);
                index++;
                continue;
            }
            if (heading.matches()) {
                current = ensureSlide(slides, current, options);
                String text = headingText(heading.group(2), false);
                if (!text.isEmpty()) {
                    current.blocks.add(SlideBlock.text(Arrays.asList(new TextRun(text))));
                }
                index++;
                continue;
            }

            if (isSetextHeading(lines, index)) {
                current = new SlideModel(headingText(lines[index], true));
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

            Matcher footnoteDefinition = FOOTNOTE_DEFINITION.matcher(line);
            if (footnoteDefinition.matches()) {
                current = ensureSlide(slides, current, options);
                StringBuilder footnoteText = new StringBuilder(footnoteDefinition.group(1));
                index++;
                while (index < lines.length && isFootnoteContinuation(lines, index)) {
                    if (lines[index].trim().isEmpty()) {
                        index = skipBlankLines(lines, index);
                        continue;
                    }
                    footnoteText.append(' ').append(lines[index].trim());
                    index++;
                }
                current.blocks.add(SlideBlock.text(textRuns(footnoteText.toString())));
                continue;
            }

            if (DEFINITION.matcher(line).matches()) {
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

            if (IMAGE_REFERENCE_ONLY.matcher(line.trim()).matches()) {
                current = ensureSlide(slides, current, options);
                index++;
                continue;
            }

            Matcher imageShortcut = IMAGE_SHORTCUT_ONLY.matcher(line.trim());
            if (imageShortcut.matches() && referenceLabels.contains(normalizeReferenceLabel(imageShortcut.group(1)))) {
                current = ensureSlide(slides, current, options);
                index++;
                continue;
            }

            Matcher image = IMAGE_ONLY.matcher(line.trim());
            if (image.matches()) {
                current = ensureSlide(slides, current, options);
                String imageUrl = extractLinkTarget(image.group(2));
                current.blocks.add(SlideBlock.image(stripInlineMarkdown(image.group(1)), imageUrl));
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
                    StringBuilder itemText = new StringBuilder(stripTaskMarker(item.group(2)));
                    int currentIndent = listIndent(item.group(1));
                    index++;
                    while (index < lines.length && isListContinuation(lines[index], currentIndent)) {
                        appendListContinuation(itemText, lines[index].trim(), true);
                        index++;
                    }
                    while (index < lines.length && isListParagraphContinuation(lines, index)) {
                        itemText.append(' ').append(lines[index].trim());
                        index++;
                    }
                    while (index < lines.length && isBlankSeparatedListContinuation(lines, index, currentIndent)) {
                        index = skipBlankLines(lines, index);
                        boolean first = true;
                        while (index < lines.length && isListContinuation(lines[index], currentIndent)) {
                            if (first) {
                                appendListContinuation(itemText, lines[index].trim(), false);
                                first = false;
                            } else {
                                appendListContinuation(itemText, lines[index].trim(), true);
                            }
                            index++;
                        }
                    }
                    current.blocks.add(SlideBlock.text(prefixedRuns(listPrefix(item.group(1)), itemText.toString())));
                }
                continue;
            }

            Matcher blockquote = BLOCKQUOTE.matcher(line);
            if (blockquote.matches()) {
                current = ensureSlide(slides, current, options);
                index = addBlockquoteBlocks(lines, index, current);
                continue;
            }

            Matcher fencedCode = FENCED_CODE.matcher(line);
            if (fencedCode.matches()) {
                current = ensureSlide(slides, current, options);
                String fence = fencedCode.group(1);
                index++;
                while (index < lines.length && !isClosingFence(lines[index], fence)) {
                    current.blocks.add(SlideBlock.text(Arrays.asList(new TextRun("    " + lines[index]))));
                    index++;
                }
                if (index < lines.length) {
                    index++;
                }
                continue;
            }

            if (isIndentedCodeLine(line)) {
                current = ensureSlide(slides, current, options);
                index = addIndentedCodeBlocks(lines, index, current);
                continue;
            }

            current = ensureSlide(slides, current, options);
            StringBuilder paragraph = new StringBuilder(paragraphSegment(line));
            boolean previousHardBreak = isHardBreakLine(line);
            index++;
            while (index < lines.length && !lines[index].trim().isEmpty()
                    && !HEADING.matcher(lines[index]).matches()
                    && !isTableStart(lines, index)
                    && !LIST_ITEM.matcher(lines[index]).matches()
                    && !BLOCKQUOTE.matcher(lines[index]).matches()
                    && !THEMATIC_BREAK.matcher(lines[index]).matches()
                    && !DEFINITION.matcher(lines[index]).matches()
                    && !FENCED_CODE.matcher(lines[index]).matches()
                    && !isIndentedCodeLine(lines[index])
                    && !NOTES.matcher(lines[index]).matches()
                    && !NOTES_START.matcher(lines[index]).matches()) {
                if (!previousHardBreak) {
                    paragraph.append(' ');
                }
                paragraph.append(paragraphSegment(lines[index]));
                previousHardBreak = isHardBreakLine(lines[index]);
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
        return index + 1 < lines.length && isTableLine(lines[index]) && isTableDelimiterLine(lines[index + 1]);
    }

    private boolean isTableLine(String line) {
        return line != null && line.contains("|") && line.trim().length() > 0;
    }

    private boolean isTableDelimiterLine(String line) {
        if (line == null || !line.contains("|")) {
            return false;
        }
        String trimmed = line.trim();
        boolean hasOuterPipe = trimmed.startsWith("|") || trimmed.endsWith("|");
        String value = trimmed;
        if (value.startsWith("|")) {
            value = value.substring(1);
        }
        if (value.endsWith("|")) {
            value = value.substring(0, value.length() - 1);
        }
        String[] cells = value.split("\\|", -1);
        if (cells.length < 2) {
            return false;
        }
        String dashPattern = hasOuterPipe ? ":?-+:?" : ":?-{3,}:?";
        for (String cell : cells) {
            if (!cell.trim().matches(dashPattern)) {
                return false;
            }
        }
        return true;
    }

    private List<TableCell> tableRow(String line) {
        String value = line.trim();
        if (value.startsWith("|")) {
            value = value.substring(1);
        }
        if (value.endsWith("|")) {
            value = value.substring(0, value.length() - 1);
        }
        List<TableCell> row = new ArrayList<TableCell>();
        for (String cell : splitTableCells(value)) {
            row.add(new TableCell(textRuns(cell.trim())));
        }
        return row;
    }

    private List<TextRun> textRuns(String markdownText) {
        List<TextRun> runs = new ArrayList<TextRun>();
        Matcher matcher = INLINE_LINK.matcher(markdownText);
        int offset = 0;
        while (matcher.find()) {
            if (matcher.start() > offset) {
                runs.add(new TextRun(stripInlineMarkdown(markdownText.substring(offset, matcher.start()))));
            }
            if (matcher.group(1) != null) {
                String target = extractLinkTarget(matcher.group(2));
                if (!target.isEmpty()) {
                    runs.add(new TextRun(stripInlineMarkdown(matcher.group(1)), target));
                } else {
                    runs.add(new TextRun(stripInlineMarkdown(matcher.group(1))));
                }
            } else if (matcher.group(3) != null) {
                runs.add(new TextRun(matcher.group(3), matcher.group(3)));
            } else if (matcher.group(4) != null) {
                runs.add(new TextRun(matcher.group(4), "mailto:" + matcher.group(4)));
            } else if (matcher.group(5) != null) {
                String rawUrl = matcher.group(5);
                String url = trimBareAutolink(rawUrl);
                String href = url.startsWith("www.") ? "http://" + url : url;
                runs.add(new TextRun(url, href));
                if (url.length() < rawUrl.length()) {
                    runs.add(new TextRun(stripInlineMarkdown(rawUrl.substring(url.length()))));
                }
            } else {
                runs.add(new TextRun(matcher.group(6), "mailto:" + matcher.group(6)));
            }
            offset = matcher.end();
        }
        if (offset < markdownText.length()) {
            runs.add(new TextRun(stripInlineMarkdown(markdownText.substring(offset))));
        }
        List<TextRun> normalized = new ArrayList<TextRun>();
        for (int index = 0; index < runs.size(); index++) {
            TextRun run = runs.get(index);
            String text = normalizeRunText(run.text, index == 0, index == runs.size() - 1);
            if (!text.isEmpty()) {
                normalized.add(new TextRun(text, run.href));
            }
        }
        return normalized;
    }

    private String normalizeRunText(String value, boolean first, boolean last) {
        String text = value == null ? "" : removeDanglingInlineMarkers(value).replaceAll("\\s+", " ");
        if (first) {
            text = text.replaceFirst("^\\s+", "");
        }
        if (last) {
            text = text.replaceFirst("\\s+$", "");
        }
        return text;
    }

    private List<String> splitTableCells(String value) {
        List<String> cells = new ArrayList<String>();
        StringBuilder cell = new StringBuilder();
        boolean escaped = false;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (escaped) {
                cell.append(current);
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                continue;
            }
            if (current == '|') {
                cells.add(cell.toString());
                cell.setLength(0);
                continue;
            }
            cell.append(current);
        }
        if (escaped) {
            cell.append('\\');
        }
        cells.add(cell.toString());
        return cells;
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

    private boolean isListParagraphContinuation(String[] lines, int index) {
        if (index >= lines.length) {
            return false;
        }
        String line = lines[index];
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        if (HEADING.matcher(line).matches()
                || isTableStart(lines, index)
                || LIST_ITEM.matcher(line).matches()
                || BLOCKQUOTE.matcher(line).matches()
                || THEMATIC_BREAK.matcher(line).matches()
                || FOOTNOTE_DEFINITION.matcher(line).matches()
                || DEFINITION.matcher(line).matches()
                || FENCED_CODE.matcher(line).matches()
                || isIndentedCodeLine(line)
                || NOTES.matcher(line).matches()
                || NOTES_START.matcher(line).matches()) {
            return false;
        }
        return true;
    }

    private void appendListContinuation(StringBuilder itemText, String continuation, boolean ordinarySpace) {
        String normalized = listContinuationText(continuation);
        if (normalized.isEmpty()) {
            return;
        }
        if (ordinarySpace && shouldSeparateListContinuation(continuation)) {
            itemText.append(' ');
        }
        itemText.append(normalized);
    }

    private String listContinuationText(String continuation) {
        String value = continuation == null ? "" : continuation.trim();
        if (THEMATIC_BREAK.matcher(value).matches() || isTableDelimiterLine(value)) {
            return "";
        }
        if (isTableLine(value)) {
            return tableLineText(value);
        }
        if (value.startsWith(">")) {
            return parseBlockquoteLine(value).text.trim();
        }
        Matcher heading = HEADING.matcher(value);
        if (heading.matches()) {
            return headingText(heading.group(2), false);
        }
        return value;
    }

    private boolean shouldSeparateListContinuation(String continuation) {
        String value = continuation == null ? "" : continuation.trim();
        return !value.startsWith(">") && !HEADING.matcher(value).matches();
    }

    private String tableLineText(String value) {
        List<String> texts = new ArrayList<String>();
        for (String cell : splitTableCells(trimTableOuterPipes(value))) {
            String text = normalizeLine(stripInlineMarkdown(cell.trim()));
            if (!text.isEmpty()) {
                texts.add(text);
            }
        }
        StringBuilder joined = new StringBuilder();
        for (int index = 0; index < texts.size(); index++) {
            if (index > 0) {
                joined.append(' ');
            }
            joined.append(texts.get(index));
        }
        return joined.toString();
    }

    private String trimTableOuterPipes(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.startsWith("|")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("|")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private boolean isFootnoteContinuation(String[] lines, int index) {
        if (index >= lines.length) {
            return false;
        }
        String line = lines[index];
        if (line == null) {
            return false;
        }
        if (line.trim().isEmpty()) {
            int next = skipBlankLines(lines, index);
            return next < lines.length && isIndentedCodeLine(lines[next]);
        }
        if (HEADING.matcher(line).matches()
                || isTableStart(lines, index)
                || LIST_ITEM.matcher(line).matches()
                || BLOCKQUOTE.matcher(line).matches()
                || THEMATIC_BREAK.matcher(line).matches()
                || FOOTNOTE_DEFINITION.matcher(line).matches()
                || DEFINITION.matcher(line).matches()
                || FENCED_CODE.matcher(line).matches()
                || NOTES.matcher(line).matches()
                || NOTES_START.matcher(line).matches()) {
            return false;
        }
        return true;
    }

    private boolean isBlankSeparatedListContinuation(String[] lines, int index, int listIndent) {
        if (index >= lines.length || !lines[index].trim().isEmpty()) {
            return false;
        }
        int next = skipBlankLines(lines, index);
        return next < lines.length && isListContinuation(lines[next], listIndent);
    }

    private int skipBlankLines(String[] lines, int index) {
        while (index < lines.length && lines[index].trim().isEmpty()) {
            index++;
        }
        return index;
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

    private boolean isHardBreakLine(String line) {
        return line != null && (line.endsWith("\\") || line.endsWith("  "));
    }

    private String paragraphSegment(String line) {
        if (line == null) {
            return "";
        }
        String value = line;
        if (value.endsWith("\\")) {
            value = value.substring(0, value.length() - 1);
        }
        return value.trim();
    }

    private boolean isClosingFence(String line, String openingFence) {
        if (line == null || openingFence == null || openingFence.isEmpty()) {
            return false;
        }
        char marker = openingFence.charAt(0);
        int required = openingFence.length();
        int index = 0;
        while (index < line.length() && index < 3 && line.charAt(index) == ' ') {
            index++;
        }
        int count = 0;
        while (index < line.length() && line.charAt(index) == marker) {
            count++;
            index++;
        }
        return count >= required && line.substring(index).trim().isEmpty();
    }

    private boolean isIndentedCodeLine(String line) {
        return line != null && (line.startsWith("    ") || line.startsWith("\t"));
    }

    private String unindentCodeLine(String line) {
        if (line == null) {
            return "";
        }
        if (line.startsWith("\t")) {
            return line.substring(1);
        }
        return line.length() >= 4 ? line.substring(4) : line;
    }

    private int addIndentedCodeBlocks(String[] lines, int index, SlideModel current) {
        while (index < lines.length) {
            if (isIndentedCodeLine(lines[index])) {
                current.blocks.add(SlideBlock.text(Arrays.asList(new TextRun("    " + unindentCodeLine(lines[index])))));
                index++;
                continue;
            }
            if (lines[index].trim().isEmpty() && hasFollowingIndentedCodeLine(lines, index + 1)) {
                current.blocks.add(SlideBlock.text(Arrays.asList(new TextRun("    "))));
                index++;
                continue;
            }
            break;
        }
        return index;
    }

    private boolean hasFollowingIndentedCodeLine(String[] lines, int index) {
        while (index < lines.length) {
            if (lines[index].trim().isEmpty()) {
                index++;
                continue;
            }
            return isIndentedCodeLine(lines[index]);
        }
        return false;
    }

    private String headingText(String value, boolean title) {
        String text = value == null ? "" : value;
        text = text.replaceFirst("\\s+#+\\s*$", "");
        text = normalizeLine(stripInlineMarkdown(text));
        if (title && text.isEmpty()) {
            return "Untitled slide";
        }
        return text;
    }

    private String stripTaskMarker(String value) {
        return value == null ? "" : TASK_MARKER.matcher(value).replaceFirst("");
    }

    private int addBlockquoteBlocks(String[] lines, int index, SlideModel current) {
        String currentPrefix = null;
        StringBuilder currentText = new StringBuilder();
        while (index < lines.length && BLOCKQUOTE.matcher(lines[index]).matches()) {
            BlockquoteLine quoted = parseBlockquoteLine(lines[index]);
            if (quoted.text.trim().isEmpty()) {
                addPendingBlockquote(current, currentPrefix, currentText);
                currentPrefix = null;
                currentText.setLength(0);
                index++;
                continue;
            }
            if (isBlockquoteTableStart(lines, index)) {
                addPendingBlockquote(current, currentPrefix, currentText);
                currentPrefix = null;
                currentText.setLength(0);
                List<List<TableCell>> rows = new ArrayList<List<TableCell>>();
                rows.add(tableRow(quoted.text));
                index += 2;
                while (index < lines.length && BLOCKQUOTE.matcher(lines[index]).matches()) {
                    BlockquoteLine tableLine = parseBlockquoteLine(lines[index]);
                    if (!isTableLine(tableLine.text)) {
                        break;
                    }
                    rows.add(tableRow(tableLine.text));
                    index++;
                }
                current.blocks.add(SlideBlock.table(rows));
                continue;
            }
            Matcher fenced = FENCED_CODE.matcher(quoted.text);
            if (fenced.matches()) {
                addPendingBlockquote(current, currentPrefix, currentText);
                currentPrefix = null;
                currentText.setLength(0);
                String fence = fenced.group(1);
                index++;
                while (index < lines.length && BLOCKQUOTE.matcher(lines[index]).matches()) {
                    BlockquoteLine codeLine = parseBlockquoteLine(lines[index]);
                    if (isClosingFence(codeLine.text, fence)) {
                        index++;
                        break;
                    }
                    current.blocks.add(SlideBlock.text(Arrays.asList(new TextRun(codeLine.prefix), new TextRun("    " + codeLine.text))));
                    index++;
                }
                continue;
            }

            Matcher heading = HEADING.matcher(quoted.text);
            if (heading.matches()) {
                addPendingBlockquote(current, currentPrefix, currentText);
                currentPrefix = quoted.prefix;
                currentText.setLength(0);
                currentText.append(headingText(heading.group(2), false));
                index++;
                continue;
            }

            Matcher listItem = LIST_ITEM.matcher(quoted.text);
            if (listItem.matches()) {
                addPendingBlockquote(current, currentPrefix, currentText);
                currentPrefix = quoted.prefix + listPrefix(listItem.group(1));
                currentText.setLength(0);
                currentText.append(stripTaskMarker(listItem.group(2)));
                index++;
                continue;
            }

            if (currentPrefix != null && isBlockquoteContinuation(currentPrefix, quoted)) {
                currentText.append(' ').append(quoted.text.trim());
            } else {
                addPendingBlockquote(current, currentPrefix, currentText);
                currentPrefix = quoted.prefix;
                currentText.setLength(0);
                currentText.append(quoted.text.trim());
            }
            index++;
        }
        addPendingBlockquote(current, currentPrefix, currentText);
        return index;
    }

    private boolean isBlockquoteTableStart(String[] lines, int index) {
        if (index + 1 >= lines.length
                || !BLOCKQUOTE.matcher(lines[index]).matches()
                || !BLOCKQUOTE.matcher(lines[index + 1]).matches()) {
            return false;
        }
        BlockquoteLine header = parseBlockquoteLine(lines[index]);
        BlockquoteLine delimiter = parseBlockquoteLine(lines[index + 1]);
        return isTableLine(header.text) && isTableDelimiterLine(delimiter.text);
    }

    private void addPendingBlockquote(SlideModel current, String prefix, StringBuilder text) {
        if (prefix != null && text.length() > 0) {
            current.blocks.add(SlideBlock.text(prefixedRuns(prefix, text.toString())));
        }
    }

    private boolean isBlockquoteContinuation(String currentPrefix, BlockquoteLine quoted) {
        if (currentPrefix.endsWith("- ")) {
            return lineIndent(quoted.text) > 0;
        }
        return currentPrefix.equals(quoted.prefix);
    }

    private BlockquoteLine parseBlockquoteLine(String line) {
        String value = line == null ? "" : line.trim();
        StringBuilder prefix = new StringBuilder();
        while (value.startsWith(">")) {
            prefix.append("> ");
            value = value.substring(1);
            if (value.startsWith(" ")) {
                value = value.substring(1);
            }
        }
        return new BlockquoteLine(prefix.toString(), value);
    }

    private static final class BlockquoteLine {
        final String prefix;
        final String text;

        BlockquoteLine(String prefix, String text) {
            this.prefix = prefix;
            this.text = text;
        }
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
        String stripped = value.replaceAll("(?<!\\\\)\\*\\*([^*]+)(?<!\\\\)\\*\\*", "$1")
                .replaceAll("(?<!\\\\)__([^_]+)(?<!\\\\)__", "$1")
                .replaceAll("(?<!\\\\)~~([^~]+)(?<!\\\\)~~", "$1")
                .replaceAll("(?<!\\\\)(`+)(.+?)(?<!\\\\)\\1", "$2")
                .replaceAll("!\\[" + LABEL_PATTERN + "\\]\\(" + LINK_TARGET_PATTERN + "\\)", "")
                .replaceAll("!\\[" + LABEL_PATTERN + "\\]\\[[^\\]]*\\]", "")
                .replaceAll("\\[\\^[^\\]]+\\]", "")
                .replaceAll("\\[([^\\]]+)\\]\\[[^\\]]*\\]", "$1")
                .replaceAll("(?<![\\\\\\w])\\*([^*]+)(?<!\\\\)\\*(?!\\w)", "$1")
                .replaceAll("(?<![\\\\\\w])_([^_]+)(?<!\\\\)_(?!\\w)", "$1");
        return decodeHtmlEntities(unescapeMarkdownPunctuation(replaceShortcutReferences(removeShortcutImages(stripped))));
    }

    private Set<String> referenceLabels(String[] lines) {
        Set<String> labels = new HashSet<String>();
        for (String line : lines) {
            Matcher definition = DEFINITION_LABEL.matcher(line);
            if (definition.matches()) {
                labels.add(normalizeReferenceLabel(definition.group(1)));
            }
        }
        return labels;
    }

    private String replaceShortcutReferences(String value) {
        Matcher matcher = SHORTCUT_REFERENCE.matcher(value);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String label = matcher.group(1);
            if (referenceLabels.contains(normalizeReferenceLabel(label))) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(label));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String removeShortcutImages(String value) {
        Matcher matcher = IMAGE_SHORTCUT_REFERENCE.matcher(value);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            if (referenceLabels.contains(normalizeReferenceLabel(matcher.group(1)))) {
                matcher.appendReplacement(result, "");
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String normalizeReferenceLabel(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private String unescapeMarkdownPunctuation(String value) {
        return value.replaceAll("\\\\([\\\\`*{}\\[\\]()#+\\-.!_>~|])", "$1");
    }

    private String decodeHtmlEntities(String value) {
        String decoded = value.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&copy;", "\u00a9")
                .replace("&nbsp;", " ")
                .replace("&reg;", "\u00ae")
                .replace("&mdash;", "\u2014")
                .replace("&hellip;", "\u2026");
        Matcher matcher = NUMERIC_ENTITY.matcher(decoded);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String raw = matcher.group(1);
            try {
                int codePoint = raw.startsWith("x") || raw.startsWith("X")
                        ? Integer.parseInt(raw.substring(1), 16)
                        : Integer.parseInt(raw, 10);
                if (Character.isValidCodePoint(codePoint)) {
                    matcher.appendReplacement(result, Matcher.quoteReplacement(new String(Character.toChars(codePoint))));
                }
            } catch (NumberFormatException ignored) {
                // Keep malformed numeric entities as written.
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String removeDanglingInlineMarkers(String value) {
        String text = value == null ? "" : value;
        if (text.matches("\\s*(?:\\*\\*|__|~~|\\*|_)\\s*")) {
            return "";
        }
        return text.replaceAll("(?<!\\\\)(\\*\\*|__|~~)", "")
                .replaceAll("(?<!\\\\)(?<!\\w)[*_](?!\\w)", "");
    }

    private String normalizeLine(String value) {
        return value == null ? "" : removeDanglingInlineMarkers(value).replaceAll("\\s+", " ").trim();
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
        return unescapeMarkdownPunctuation(target.trim());
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

    private String trimBareAutolink(String rawUrl) {
        String url = rawUrl == null ? "" : rawUrl;
        while (url.endsWith(")") && count(url, ')') > count(url, '(')) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private int count(String value, char character) {
        int count = 0;
        for (int index = 0; index < value.length(); index++) {
            if (value.charAt(index) == character) {
                count++;
            }
        }
        return count;
    }
}
