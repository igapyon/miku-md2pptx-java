package jp.igapyon.mikumd2pptx.core;

import java.util.List;

class Ooxml {
    static final String PRESENTATION_NS = "http://schemas.openxmlformats.org/presentationml/2006/main";
    static final String REL_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    static final String DRAWING_NS = "http://schemas.openxmlformats.org/drawingml/2006/main";

    static String xmlEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    static String relsXml(List<SlideRelationship> relationships) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        xml.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n");
        for (SlideRelationship rel : relationships) {
            xml.append("  <Relationship Id=\"").append(rel.id).append("\" Type=\"").append(rel.type)
                    .append("\" Target=\"").append(xmlEscape(rel.target)).append("\"");
            if (rel.targetMode != null) {
                xml.append(" TargetMode=\"").append(rel.targetMode).append("\"");
            }
            xml.append("/>\n");
        }
        xml.append("</Relationships>");
        return xml.toString();
    }
}
