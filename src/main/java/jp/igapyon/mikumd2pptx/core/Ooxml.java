package jp.igapyon.mikumd2pptx.core;

import jp.igapyon.mikumsofficecore.OpcRelationship;
import jp.igapyon.mikumsofficecore.OpcRelationships;
import jp.igapyon.mikumsofficecore.XmlHelper;

import java.util.List;

class Ooxml {
    static final String PRESENTATION_NS = "http://schemas.openxmlformats.org/presentationml/2006/main";
    static final String REL_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    static final String DRAWING_NS = "http://schemas.openxmlformats.org/drawingml/2006/main";

    static String xmlEscape(String value) {
        if (value == null) {
            return "";
        }
        return XmlHelper.escapeXmlAttribute(value);
    }

    static String relsXml(List<OpcRelationship> relationships) {
        return OpcRelationships.buildOpcRelationshipsXml(relationships);
    }
}
