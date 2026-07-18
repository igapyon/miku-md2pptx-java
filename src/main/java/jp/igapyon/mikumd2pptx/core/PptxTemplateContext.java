package jp.igapyon.mikumd2pptx.core;

import jp.igapyon.mikumsofficecore.DiagnosticSeverity;
import jp.igapyon.mikumsofficecore.OfficeDiagnostic;
import jp.igapyon.mikumsofficecore.OpcRelationship;
import jp.igapyon.mikumsofficecore.ZipEntry;
import jp.igapyon.mikumsofficecore.ZipPackage;
import jp.igapyon.mikumsofficecore.ZipReadResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PptxTemplateContext {
    final Map<String, byte[]> entries;
    final String layoutPath;
    final String layoutName;
    final String layoutTarget;
    final String titlePlaceholderXml;
    final String bodyPlaceholderXml;
    final Rect bodyPlaceholderRect;
    final String reason;
    final String presentationXml;
    final String presentationRelsXml;
    final int nextImageIndex;

    private PptxTemplateContext(Map<String, byte[]> entries, Layout layout,
            Rect bodyPlaceholderRect, String presentationXml, String presentationRelsXml,
            int nextImageIndex) {
        this.entries = entries;
        this.layoutPath = layout.path;
        this.layoutName = layout.name;
        this.layoutTarget = "../slideLayouts/" + layout.path.substring(layout.path.lastIndexOf('/') + 1);
        this.titlePlaceholderXml = layout.titlePlaceholderXml;
        this.bodyPlaceholderXml = layout.bodyPlaceholderXml;
        this.bodyPlaceholderRect = bodyPlaceholderRect;
        this.reason = layout.reason;
        this.presentationXml = presentationXml;
        this.presentationRelsXml = presentationRelsXml;
        this.nextImageIndex = nextImageIndex;
    }

    static PptxTemplateContext create(byte[] templatePptx) {
        ZipReadResult read = ZipPackage.readZipPackage(templatePptx);
        for (OfficeDiagnostic diagnostic : read.getDiagnostics()) {
            if (diagnostic.getSeverity() == DiagnosticSeverity.ERROR) {
                throw new IllegalArgumentException("Template PPTX is not a readable ZIP package: " + diagnostic.getMessage());
            }
        }
        Map<String, byte[]> entries = new LinkedHashMap<String, byte[]>();
        for (ZipEntry entry : read.getEntries()) {
            entries.put(entry.getPath(), entry.getData());
        }
        String presentationXml = readText(entries, "ppt/presentation.xml");
        String presentationRelsXml = readText(entries, "ppt/_rels/presentation.xml.rels");
        if (presentationXml == null || presentationRelsXml == null) {
            throw new IllegalArgumentException("Template PPTX does not contain required presentation parts.");
        }
        Layout layout = findLayout(entries);
        if (layout == null) {
            return null;
        }
        String layoutXml = readText(entries, layout.path);
        Rect rect = findPlaceholderRect(entries, layout.path, layoutXml == null ? "" : layoutXml,
                layout.bodyPlaceholderXml);
        return new PptxTemplateContext(entries, layout, rect, presentationXml,
                presentationRelsXml, nextImageIndex(entries));
    }

    static String readText(Map<String, byte[]> entries, String path) {
        byte[] data = entries.get(path);
        return data == null ? null : new String(data, StandardCharsets.UTF_8);
    }

    static String attribute(String tag, String localName) {
        Matcher matcher = Pattern.compile("(?:^|\\s)(?:[^\\s:=]+:)?" + Pattern.quote(localName) + "=\"([^\"]*)\"").matcher(tag);
        return matcher.find() ? matcher.group(1) : null;
    }

    static List<OpcRelationship> relationships(String xml) {
        List<OpcRelationship> result = new ArrayList<OpcRelationship>();
        Matcher matcher = Pattern.compile("<[^<\\s:]*:?Relationship\\b[^>]*>").matcher(xml);
        while (matcher.find()) {
            String tag = matcher.group();
            String id = attribute(tag, "Id");
            String type = attribute(tag, "Type");
            String target = attribute(tag, "Target");
            String targetMode = attribute(tag, "TargetMode");
            if (id != null && type != null && target != null) {
                result.add(new OpcRelationship(id, type, target, targetMode));
            }
        }
        return result;
    }

    static String extractXmlBlock(String xml, String localName) {
        Matcher matcher = Pattern.compile("<[^<\\s:]*:?" + Pattern.quote(localName)
                + "\\b[\\s\\S]*?</[^<\\s:]*:?" + Pattern.quote(localName) + ">").matcher(xml);
        return matcher.find() ? matcher.group() : null;
    }

    static String extractSelfClosing(String xml, String localName) {
        Matcher matcher = Pattern.compile("<[^<\\s:]*:?" + Pattern.quote(localName) + "\\b[^>]*/>").matcher(xml);
        return matcher.find() ? matcher.group() : null;
    }

    private static Layout findLayout(Map<String, byte[]> entries) {
        Layout titleOnly = null;
        for (String path : entries.keySet()) {
            if (!path.matches("^ppt/slideLayouts/slideLayout\\d+\\.xml$")) {
                continue;
            }
            String xml = readText(entries, path);
            if (xml == null) {
                continue;
            }
            String title = findPlaceholderTag(xml, new String[] { "title", "ctrTitle" });
            String body = findPlaceholderTag(xml, new String[] { "body", "obj" });
            Layout layout = new Layout(path, layoutName(xml, path),
                    title == null ? "<p:ph type=\"title\"/>" : title,
                    body == null ? "<p:ph type=\"body\"/>" : body,
                    body == null ? "found title placeholder only" : "found title and body placeholders");
            if (title != null && body != null) {
                return layout;
            }
            if (title != null && titleOnly == null) {
                titleOnly = layout;
            }
        }
        return titleOnly;
    }

    private static String layoutName(String xml, String path) {
        Matcher matcher = Pattern.compile("<[^<\\s:]*:?sldLayout\\b[^>]*>").matcher(xml);
        if (matcher.find()) {
            String name = attribute(matcher.group(), "name");
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private static List<String> tagBlocks(String xml, String localName) {
        List<String> result = new ArrayList<String>();
        Matcher matcher = Pattern.compile("<[^<\\s:]*:?" + Pattern.quote(localName)
                + "\\b[\\s\\S]*?</[^<\\s:]*:?" + Pattern.quote(localName) + ">").matcher(xml);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    private static String placeholderTag(String shapeXml) {
        Matcher matcher = Pattern.compile("<[^<\\s:]*:?ph\\b[^>]*/?>").matcher(shapeXml);
        return matcher.find() ? matcher.group() : null;
    }

    private static String findPlaceholderTag(String xml, String[] types) {
        for (String shape : tagBlocks(xml, "sp")) {
            String placeholder = placeholderTag(shape);
            if (placeholder == null) {
                continue;
            }
            String type = attribute(placeholder, "type");
            type = type == null ? "body" : type;
            for (String expected : types) {
                if (expected.equals(type)) {
                    return placeholder.endsWith("/>") ? placeholder
                            : placeholder.substring(0, placeholder.length() - 1) + "/>";
                }
            }
        }
        return null;
    }

    private static String findPlaceholderShape(String xml, String placeholderXml) {
        String targetIdx = attribute(placeholderXml, "idx");
        String targetType = attribute(placeholderXml, "type");
        targetType = targetType == null ? "body" : targetType;
        for (String shape : tagBlocks(xml, "sp")) {
            String candidate = placeholderTag(shape);
            if (candidate == null) {
                continue;
            }
            String candidateIdx = attribute(candidate, "idx");
            String candidateType = attribute(candidate, "type");
            candidateType = candidateType == null ? "body" : candidateType;
            if (targetIdx != null && targetIdx.equals(candidateIdx)) {
                return shape;
            }
            if (targetIdx == null && targetType.equals(candidateType)) {
                return shape;
            }
            if ("body".equals(targetType) && "obj".equals(candidateType)) {
                return shape;
            }
        }
        return null;
    }

    private static Rect extractRect(String shapeXml) {
        if (shapeXml == null) {
            return null;
        }
        String xfrm = extractXmlBlock(shapeXml, "xfrm");
        if (xfrm == null) {
            return null;
        }
        Matcher offMatcher = Pattern.compile("<[^<\\s:]*:?off\\b[^>]*>").matcher(xfrm);
        Matcher extMatcher = Pattern.compile("<[^<\\s:]*:?ext\\b[^>]*>").matcher(xfrm);
        if (!offMatcher.find() || !extMatcher.find()) {
            return null;
        }
        try {
            return new Rect(Long.parseLong(attribute(offMatcher.group(), "x")),
                    Long.parseLong(attribute(offMatcher.group(), "y")),
                    Long.parseLong(attribute(extMatcher.group(), "cx")),
                    Long.parseLong(attribute(extMatcher.group(), "cy")));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static Rect findPlaceholderRect(Map<String, byte[]> entries, String layoutPath,
            String layoutXml, String placeholderXml) {
        Rect layoutRect = extractRect(findPlaceholderShape(layoutXml, placeholderXml));
        if (layoutRect != null) {
            return layoutRect;
        }
        String relsPath = layoutPath.replace("ppt/slideLayouts/", "ppt/slideLayouts/_rels/") + ".rels";
        String relsXml = readText(entries, relsPath);
        if (relsXml == null) {
            return null;
        }
        OpcRelationship master = null;
        for (OpcRelationship rel : relationships(relsXml)) {
            if (rel.getType().endsWith("/slideMaster")) {
                master = rel;
                break;
            }
        }
        if (master == null) {
            return null;
        }
        String masterPath = normalizePackagePath("ppt/slideLayouts", master.getTarget());
        String masterXml = readText(entries, masterPath);
        return masterXml == null ? null : extractRect(findPlaceholderShape(masterXml, placeholderXml));
    }

    private static String normalizePackagePath(String baseDir, String target) {
        List<String> normalized = new ArrayList<String>();
        for (String part : (baseDir + "/" + target).split("/")) {
            if (part.isEmpty() || ".".equals(part)) {
                continue;
            }
            if ("..".equals(part)) {
                if (!normalized.isEmpty()) {
                    normalized.remove(normalized.size() - 1);
                }
            } else {
                normalized.add(part);
            }
        }
        StringBuilder result = new StringBuilder();
        for (String part : normalized) {
            if (result.length() > 0) {
                result.append('/');
            }
            result.append(part);
        }
        return result.toString();
    }

    private static int nextImageIndex(Map<String, byte[]> entries) {
        int max = 0;
        Pattern pattern = Pattern.compile("^ppt/media/image(\\d+)\\.(?:png|jpe?g|gif)$", Pattern.CASE_INSENSITIVE);
        for (String path : entries.keySet()) {
            Matcher matcher = pattern.matcher(path);
            if (matcher.matches()) {
                max = Math.max(max, Integer.parseInt(matcher.group(1)));
            }
        }
        return max + 1;
    }

    static final class Rect {
        final long x;
        final long y;
        final long cx;
        final long cy;

        Rect(long x, long y, long cx, long cy) {
            this.x = x;
            this.y = y;
            this.cx = cx;
            this.cy = cy;
        }
    }

    private static final class Layout {
        final String path;
        final String name;
        final String titlePlaceholderXml;
        final String bodyPlaceholderXml;
        final String reason;

        Layout(String path, String name, String titlePlaceholderXml, String bodyPlaceholderXml, String reason) {
            this.path = path;
            this.name = name;
            this.titlePlaceholderXml = titlePlaceholderXml;
            this.bodyPlaceholderXml = bodyPlaceholderXml;
            this.reason = reason;
        }
    }
}
