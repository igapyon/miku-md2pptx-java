package jp.igapyon.mikumd2pptx.core;

import jp.igapyon.mikumsofficecore.OpcContentTypeDefault;
import jp.igapyon.mikumsofficecore.OpcContentTypeOverride;
import jp.igapyon.mikumsofficecore.OpcContentTypes;
import jp.igapyon.mikumsofficecore.OpcRelationship;
import jp.igapyon.mikumsofficecore.ZipEntryInput;
import jp.igapyon.mikumsofficecore.ZipPackage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PptxPackageBuilder {
    private final List<Md2PptxDiagnostic> diagnostics;
    private final Md2PptxOptions options;
    private final List<ZipEntryInput> mediaEntries = new ArrayList<ZipEntryInput>();
    private int nextImageIndex = 1;

    PptxPackageBuilder(List<Md2PptxDiagnostic> diagnostics, Md2PptxOptions options) {
        this.diagnostics = diagnostics;
        this.options = options == null ? new Md2PptxOptions() : options;
    }

    byte[] build(List<SlideModel> slides) {
        boolean hasNotes = hasNotes(slides);
        byte[] templateBytes = options.getTemplatePptx();
        PptxTemplateContext template = templateBytes == null ? null : PptxTemplateContext.create(templateBytes);
        if (templateBytes != null && template != null) {
            diagnostics.add(new Md2PptxDiagnostic("info", "template-layout-selected",
                    "Template layout selected: " + template.layoutName + " (" + template.layoutPath + "); " + template.reason + ".",
                    options.getSourcePath()));
            nextImageIndex = template.nextImageIndex;
        } else if (templateBytes != null) {
            diagnostics.add(new Md2PptxDiagnostic("warning", "template-layout-fallback",
                    "No template slide layout with a title placeholder was found; used the default generated layout.",
                    options.getSourcePath()));
        }
        List<ZipEntryInput> entries = template == null
                ? defaultBaseEntries(slides, hasNotes)
                : templateBaseEntries(slides, hasNotes, template);

        for (int i = 0; i < slides.size(); i++) {
            SlideXmlResult slide = slideXml(slides.get(i), i + 1, template);
            entries.add(text("ppt/slides/slide" + (i + 1) + ".xml", slide.xml));
            entries.add(text("ppt/slides/_rels/slide" + (i + 1) + ".xml.rels", Ooxml.relsXml(slide.relationships)));
            if (!slides.get(i).notes.isEmpty()) {
                entries.add(text("ppt/notesSlides/notesSlide" + (i + 1) + ".xml", notesXml(slides.get(i))));
                entries.add(text("ppt/notesSlides/_rels/notesSlide" + (i + 1) + ".xml.rels", PptxStaticParts.notesSlideRelsXml(i + 1)));
            }
        }

        if (hasNotes) {
            addIfMissing(entries, text("ppt/notesMasters/notesMaster1.xml", PptxStaticParts.notesMasterXml()));
            addIfMissing(entries, text("ppt/notesMasters/_rels/notesMaster1.xml.rels", Ooxml.relsXml(notesMasterRelationships())));
            addIfMissing(entries, text("ppt/theme/theme2.xml", PptxStaticParts.themeXml()));
            addIfMissing(entries, text("ppt/presProps.xml", PptxStaticParts.presPropsXml()));
            addIfMissing(entries, text("ppt/viewProps.xml", PptxStaticParts.viewPropsXml()));
            addIfMissing(entries, text("ppt/tableStyles.xml", PptxStaticParts.tableStylesXml()));
        }
        for (ZipEntryInput entry : mediaEntries) {
            addIfMissing(entries, entry);
        }
        return ZipPackage.writeZipPackage(entries);
    }

    private List<ZipEntryInput> defaultBaseEntries(List<SlideModel> slides, boolean hasNotes) {
        List<ZipEntryInput> entries = new ArrayList<ZipEntryInput>();
        entries.add(text("[Content_Types].xml", contentTypes(slides, hasNotes)));
        entries.add(text("_rels/.rels", Ooxml.relsXml(rootRelationships())));
        entries.add(text("docProps/app.xml", appXml(slides)));
        entries.add(text("docProps/core.xml", corePropsXml(slides)));
        entries.add(text("ppt/presentation.xml", presentationXml(slides, hasNotes)));
        entries.add(text("ppt/_rels/presentation.xml.rels", presentationRelsXml(slides, hasNotes)));
        entries.add(text("ppt/slideMasters/slideMaster1.xml", PptxStaticParts.slideMasterXml()));
        entries.add(text("ppt/slideMasters/_rels/slideMaster1.xml.rels", Ooxml.relsXml(slideMasterRelationships())));
        entries.add(text("ppt/slideLayouts/slideLayout1.xml", PptxStaticParts.slideLayoutXml()));
        entries.add(text("ppt/slideLayouts/_rels/slideLayout1.xml.rels", Ooxml.relsXml(slideLayoutRelationships())));
        entries.add(text("ppt/theme/theme1.xml", PptxStaticParts.themeXml()));
        return entries;
    }

    private void addIfMissing(List<ZipEntryInput> entries, ZipEntryInput candidate) {
        for (ZipEntryInput entry : entries) {
            if (entry.getPath().equals(candidate.getPath())) {
                return;
            }
        }
        entries.add(candidate);
    }

    private boolean hasNotes(List<SlideModel> slides) {
        for (SlideModel slide : slides) {
            if (!slide.notes.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private ZipEntryInput text(String path, String value) {
        return new ZipEntryInput(path, value.getBytes(StandardCharsets.UTF_8));
    }

    private List<OpcRelationship> rootRelationships() {
        List<OpcRelationship> rels = new ArrayList<OpcRelationship>();
        rels.add(new OpcRelationship("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument", "ppt/presentation.xml"));
        rels.add(new OpcRelationship("rId2", "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties", "docProps/core.xml"));
        rels.add(new OpcRelationship("rId3", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties", "docProps/app.xml"));
        return rels;
    }

    private List<OpcRelationship> slideMasterRelationships() {
        List<OpcRelationship> rels = new ArrayList<OpcRelationship>();
        rels.add(new OpcRelationship("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout", "../slideLayouts/slideLayout1.xml"));
        rels.add(new OpcRelationship("rId2", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme", "../theme/theme1.xml"));
        return rels;
    }

    private List<OpcRelationship> slideLayoutRelationships() {
        List<OpcRelationship> rels = new ArrayList<OpcRelationship>();
        rels.add(new OpcRelationship("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster", "../slideMasters/slideMaster1.xml"));
        return rels;
    }

    private List<OpcRelationship> notesMasterRelationships() {
        List<OpcRelationship> rels = new ArrayList<OpcRelationship>();
        rels.add(new OpcRelationship("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme", "../theme/theme2.xml"));
        return rels;
    }

    private String appXml(List<SlideModel> slides) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\">"
                + "<Application>miku-md2pptx-java</Application><Slides>" + slides.size() + "</Slides></Properties>";
    }

    private String corePropsXml(List<SlideModel> slides) {
        String title = options.getTitle() != null ? options.getTitle() : (slides.isEmpty() ? "Markdown deck" : slides.get(0).title);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">"
                + "<dc:title>" + Ooxml.xmlEscape(title) + "</dc:title><dc:creator>miku-md2pptx-java</dc:creator></cp:coreProperties>";
    }

    private String presentationXml(List<SlideModel> slides, boolean hasNotes) {
        StringBuilder slideIds = new StringBuilder();
        for (int i = 0; i < slides.size(); i++) {
            slideIds.append("    <p:sldId id=\"").append(256 + i).append("\" r:id=\"rId").append(i + 2).append("\"/>\n");
        }
        String notes = hasNotes ? "<p:notesMasterIdLst><p:notesMasterId r:id=\"rId" + (slides.size() + 2) + "\"/></p:notesMasterIdLst>" : "";
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<p:presentation xmlns:a=\"" + Ooxml.DRAWING_NS + "\" xmlns:r=\"" + Ooxml.REL_NS + "\" xmlns:p=\"" + Ooxml.PRESENTATION_NS + "\">"
                + "<p:sldMasterIdLst><p:sldMasterId id=\"2147483648\" r:id=\"rId1\"/></p:sldMasterIdLst>"
                + notes + "<p:sldIdLst>\n" + slideIds + "  </p:sldIdLst>"
                + "<p:sldSz cx=\"9144000\" cy=\"6858000\" type=\"screen4x3\"/><p:notesSz cx=\"6858000\" cy=\"9144000\"/>"
                + "<p:defaultTextStyle><a:defPPr><a:defRPr lang=\"en-US\"/></a:defPPr></p:defaultTextStyle></p:presentation>";
    }

    private String presentationRelsXml(List<SlideModel> slides, boolean hasNotes) {
        List<OpcRelationship> rels = new ArrayList<OpcRelationship>();
        rels.add(new OpcRelationship("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster", "slideMasters/slideMaster1.xml"));
        for (int i = 0; i < slides.size(); i++) {
            rels.add(new OpcRelationship("rId" + (i + 2), "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide", "slides/slide" + (i + 1) + ".xml"));
        }
        if (hasNotes) {
            int base = slides.size() + 2;
            rels.add(new OpcRelationship("rId" + base, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesMaster", "notesMasters/notesMaster1.xml"));
            rels.add(new OpcRelationship("rId" + (base + 1), "http://schemas.openxmlformats.org/officeDocument/2006/relationships/presProps", "presProps.xml"));
            rels.add(new OpcRelationship("rId" + (base + 2), "http://schemas.openxmlformats.org/officeDocument/2006/relationships/viewProps", "viewProps.xml"));
            rels.add(new OpcRelationship("rId" + (base + 3), "http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme", "theme/theme1.xml"));
            rels.add(new OpcRelationship("rId" + (base + 4), "http://schemas.openxmlformats.org/officeDocument/2006/relationships/tableStyles", "tableStyles.xml"));
        }
        return Ooxml.relsXml(rels);
    }

    private String contentTypes(List<SlideModel> slides, boolean hasNotes) {
        List<OpcContentTypeDefault> defaults = new ArrayList<OpcContentTypeDefault>();
        defaults.add(new OpcContentTypeDefault("rels", "application/vnd.openxmlformats-package.relationships+xml"));
        defaults.add(new OpcContentTypeDefault("xml", "application/xml"));
        defaults.add(new OpcContentTypeDefault("png", "image/png"));
        defaults.add(new OpcContentTypeDefault("jpg", "image/jpeg"));
        defaults.add(new OpcContentTypeDefault("jpeg", "image/jpeg"));
        defaults.add(new OpcContentTypeDefault("gif", "image/gif"));

        List<OpcContentTypeOverride> overrides = new ArrayList<OpcContentTypeOverride>();
        overrides.add(new OpcContentTypeOverride("docProps/app.xml", "application/vnd.openxmlformats-officedocument.extended-properties+xml"));
        overrides.add(new OpcContentTypeOverride("docProps/core.xml", "application/vnd.openxmlformats-package.core-properties+xml"));
        overrides.add(new OpcContentTypeOverride("ppt/presentation.xml", "application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"));
        overrides.add(new OpcContentTypeOverride("ppt/slideMasters/slideMaster1.xml", "application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"));
        overrides.add(new OpcContentTypeOverride("ppt/slideLayouts/slideLayout1.xml", "application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"));
        overrides.add(new OpcContentTypeOverride("ppt/theme/theme1.xml", "application/vnd.openxmlformats-officedocument.theme+xml"));
        for (int i = 0; i < slides.size(); i++) {
            overrides.add(new OpcContentTypeOverride("ppt/slides/slide" + (i + 1) + ".xml", "application/vnd.openxmlformats-officedocument.presentationml.slide+xml"));
            if (!slides.get(i).notes.isEmpty()) {
                overrides.add(new OpcContentTypeOverride("ppt/notesSlides/notesSlide" + (i + 1) + ".xml", "application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml"));
            }
        }
        if (hasNotes) {
            overrides.add(new OpcContentTypeOverride("ppt/notesMasters/notesMaster1.xml", "application/vnd.openxmlformats-officedocument.presentationml.notesMaster+xml"));
            overrides.add(new OpcContentTypeOverride("ppt/theme/theme2.xml", "application/vnd.openxmlformats-officedocument.theme+xml"));
            overrides.add(new OpcContentTypeOverride("ppt/presProps.xml", "application/vnd.openxmlformats-officedocument.presentationml.presProps+xml"));
            overrides.add(new OpcContentTypeOverride("ppt/viewProps.xml", "application/vnd.openxmlformats-officedocument.presentationml.viewProps+xml"));
            overrides.add(new OpcContentTypeOverride("ppt/tableStyles.xml", "application/vnd.openxmlformats-officedocument.presentationml.tableStyles+xml"));
        }
        return OpcContentTypes.buildOpcContentTypesXml(new OpcContentTypes(defaults, overrides));
    }

    private List<ZipEntryInput> templateBaseEntries(List<SlideModel> slides, boolean hasNotes,
            PptxTemplateContext template) {
        List<ZipEntryInput> entries = new ArrayList<ZipEntryInput>();
        entries.add(text("[Content_Types].xml", templateContentTypesXml(slides, hasNotes, template)));
        for (Map.Entry<String, byte[]> entry : template.entries.entrySet()) {
            if (shouldCopyTemplateEntry(entry.getKey())) {
                entries.add(new ZipEntryInput(entry.getKey(), entry.getValue()));
            }
        }
        entries.add(text("docProps/app.xml", appXml(slides)));
        entries.add(text("docProps/core.xml", corePropsXml(slides)));
        entries.add(text("ppt/presentation.xml", templatePresentationXml(slides, hasNotes, template)));
        entries.add(text("ppt/_rels/presentation.xml.rels", templatePresentationRelsXml(slides, hasNotes, template)));
        return entries;
    }

    private boolean shouldCopyTemplateEntry(String path) {
        if ("[Content_Types].xml".equals(path) || "ppt/presentation.xml".equals(path)
                || "ppt/_rels/presentation.xml.rels".equals(path)
                || "docProps/app.xml".equals(path) || "docProps/core.xml".equals(path)) {
            return false;
        }
        return !path.matches("^ppt/slides(?:/|$).*") && !path.matches("^ppt/notesSlides(?:/|$).*");
    }

    private String templatePresentationXml(List<SlideModel> slides, boolean hasNotes,
            PptxTemplateContext template) {
        String masterIds = PptxTemplateContext.extractXmlBlock(template.presentationXml, "sldMasterIdLst");
        String slideSize = PptxTemplateContext.extractSelfClosing(template.presentationXml, "sldSz");
        String notesSize = PptxTemplateContext.extractSelfClosing(template.presentationXml, "notesSz");
        String defaultTextStyle = PptxTemplateContext.extractXmlBlock(template.presentationXml, "defaultTextStyle");
        String notesMasterIds = hasNotes
                ? PptxTemplateContext.extractXmlBlock(template.presentationXml, "notesMasterIdLst") : "";
        if (masterIds == null) {
            masterIds = "<p:sldMasterIdLst><p:sldMasterId id=\"2147483648\" r:id=\"rId1\"/></p:sldMasterIdLst>";
        }
        if (slideSize == null) {
            slideSize = "<p:sldSz cx=\"9144000\" cy=\"6858000\" type=\"screen4x3\"/>";
        }
        if (notesSize == null) {
            notesSize = "<p:notesSz cx=\"6858000\" cy=\"9144000\"/>";
        }
        if (defaultTextStyle == null) {
            defaultTextStyle = "";
        }
        if (hasNotes && notesMasterIds == null) {
            notesMasterIds = "<p:notesMasterIdLst><p:notesMasterId r:id=\"rIdGeneratedNotesMaster\"/></p:notesMasterIdLst>";
        }
        StringBuilder slideIds = new StringBuilder();
        for (int i = 0; i < slides.size(); i++) {
            slideIds.append("<p:sldId id=\"").append(256 + i)
                    .append("\" r:id=\"rIdGeneratedSlide").append(i + 1).append("\"/>");
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<p:presentation xmlns:a=\"" + Ooxml.DRAWING_NS + "\" xmlns:r=\"" + Ooxml.REL_NS
                + "\" xmlns:p=\"" + Ooxml.PRESENTATION_NS + "\">" + masterIds
                + (notesMasterIds == null ? "" : notesMasterIds) + "<p:sldIdLst>" + slideIds
                + "</p:sldIdLst>" + slideSize + notesSize + defaultTextStyle + "</p:presentation>";
    }

    private String templatePresentationRelsXml(List<SlideModel> slides, boolean hasNotes,
            PptxTemplateContext template) {
        List<OpcRelationship> rels = new ArrayList<OpcRelationship>();
        boolean hasNotesMaster = false;
        for (OpcRelationship rel : PptxTemplateContext.relationships(template.presentationRelsXml)) {
            if (!rel.getType().endsWith("/slide") && !rel.getType().endsWith("/notesSlide")) {
                rels.add(rel);
                hasNotesMaster = hasNotesMaster || rel.getType().endsWith("/notesMaster");
            }
        }
        if (hasNotes && !hasNotesMaster) {
            rels.add(new OpcRelationship("rIdGeneratedNotesMaster",
                    "http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesMaster",
                    "notesMasters/notesMaster1.xml"));
        }
        for (int i = 0; i < slides.size(); i++) {
            rels.add(new OpcRelationship("rIdGeneratedSlide" + (i + 1),
                    "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide",
                    "slides/slide" + (i + 1) + ".xml"));
        }
        return Ooxml.relsXml(rels);
    }

    private String templateContentTypesXml(List<SlideModel> slides, boolean hasNotes,
            PptxTemplateContext template) {
        String existing = PptxTemplateContext.readText(template.entries, "[Content_Types].xml");
        if (existing == null) {
            return contentTypes(slides, hasNotes);
        }
        String updated = existing
                .replaceAll("\\s*<Override\\b[^>]*PartName=\"/ppt/slides/slide\\d+\\.xml\"[^>]*/>", "")
                .replaceAll("\\s*<Override\\b[^>]*PartName=\"/ppt/notesSlides/notesSlide\\d+\\.xml\"[^>]*/>", "")
                .replaceAll("\\s*<Override\\b[^>]*PartName=\"/docProps/app\\.xml\"[^>]*/>", "")
                .replaceAll("\\s*<Override\\b[^>]*PartName=\"/docProps/core\\.xml\"[^>]*/>", "");
        StringBuilder additions = new StringBuilder();
        additions.append("<Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>");
        additions.append("<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>");
        for (int i = 0; i < slides.size(); i++) {
            additions.append("<Override PartName=\"/ppt/slides/slide").append(i + 1)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.slide+xml\"/>");
            if (!slides.get(i).notes.isEmpty()) {
                additions.append("<Override PartName=\"/ppt/notesSlides/notesSlide").append(i + 1)
                        .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml\"/>");
            }
        }
        if (hasNotes && !updated.contains("PartName=\"/ppt/notesMasters/notesMaster1.xml\"")) {
            additions.append("<Override PartName=\"/ppt/notesMasters/notesMaster1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.presentationml.notesMaster+xml\"/>");
        }
        return updated.replaceFirst("</Types>\\s*$", additions.toString() + "</Types>");
    }

    private SlideXmlResult slideXml(SlideModel slide, int index, PptxTemplateContext template) {
        List<OpcRelationship> rels = new ArrayList<OpcRelationship>();
        rels.add(new OpcRelationship("rId1", "http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout",
                template == null ? "../slideLayouts/slideLayout1.xml" : template.layoutTarget));
        if (!slide.notes.isEmpty()) {
            rels.add(new OpcRelationship("rId" + (rels.size() + 1), "http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesSlide", "../notesSlides/notesSlide" + index + ".xml"));
        }
        StringBuilder body = new StringBuilder();
        StringBuilder extras = new StringBuilder();
        int textIndex = 1;
        int tableIndex = 0;
        int imageIndex = 0;
        int textBlockCount = 0;
        for (SlideBlock block : slide.blocks) {
            if (block.kind == SlideBlock.Kind.TEXT) {
                body.append(textParagraphFromRuns(block.runs, textIndex++, rels, template == null));
                textBlockCount++;
            } else if (block.kind == SlideBlock.Kind.TABLE) {
                extras.append(tableXml(block.rows, 4 + tableIndex, rels, template == null,
                        template == null ? null : template.bodyPlaceholderRect, textBlockCount + tableIndex));
                tableIndex++;
            } else if (block.kind == SlideBlock.Kind.IMAGE) {
                String target = addImage(block);
                if (target != null) {
                    String relId = addRelationship(rels, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image", target, null);
                    extras.append(pictureXml(block, 40 + imageIndex++, relId));
                }
            }
        }
        if (body.length() == 0) {
            body.append(textParagraph(" ", 1, rels, template == null));
        }
        String titlePlaceholder = template == null ? "<p:ph type=\"title\"/>" : template.titlePlaceholderXml;
        String bodyPlaceholder = template == null ? "<p:ph type=\"body\"/>" : template.bodyPlaceholderXml;
        String titleShape = template == null
                ? "<p:spPr><a:xfrm><a:off x=\"685800\" y=\"457200\"/><a:ext cx=\"7772400\" cy=\"914400\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></p:spPr>"
                : "<p:spPr/>";
        String bodyShape = template == null
                ? "<p:spPr><a:xfrm><a:off x=\"685800\" y=\"1600200\"/><a:ext cx=\"7772400\" cy=\"4572000\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></p:spPr>"
                : "<p:spPr/>";
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<p:sld xmlns:a=\"" + Ooxml.DRAWING_NS + "\" xmlns:r=\"" + Ooxml.REL_NS + "\" xmlns:p=\"" + Ooxml.PRESENTATION_NS + "\">"
                + "<p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id=\"1\" name=\"\"/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"0\" cy=\"0\"/><a:chOff x=\"0\" y=\"0\"/><a:chExt cx=\"0\" cy=\"0\"/></a:xfrm></p:grpSpPr>"
                + "<p:sp><p:nvSpPr><p:cNvPr id=\"2\" name=\"Title " + index + "\"/><p:cNvSpPr><a:spLocks noGrp=\"1\"/></p:cNvSpPr><p:nvPr>" + titlePlaceholder + "</p:nvPr></p:nvSpPr>" + titleShape + "<p:txBody><a:bodyPr/><a:lstStyle/>" + textParagraph(slide.title, 0, rels, template == null) + "</p:txBody></p:sp>"
                + "<p:sp><p:nvSpPr><p:cNvPr id=\"3\" name=\"Body " + index + "\"/><p:cNvSpPr><a:spLocks noGrp=\"1\"/></p:cNvSpPr><p:nvPr>" + bodyPlaceholder + "</p:nvPr></p:nvSpPr>" + bodyShape + "<p:txBody><a:bodyPr wrap=\"square\"/><a:lstStyle/>" + body + "</p:txBody></p:sp>"
                + extras + "</p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sld>";
        return new SlideXmlResult(xml, rels);
    }

    private String textParagraph(String text, int index, List<OpcRelationship> relationships) {
        return textParagraph(text, index, relationships, true);
    }

    private String textParagraph(String text, int index, List<OpcRelationship> relationships, boolean explicitFontSize) {
        List<TextRun> runs = new ArrayList<TextRun>();
        runs.add(new TextRun(text));
        return textParagraphFromRuns(runs, index, relationships, explicitFontSize);
    }

    private String textParagraphFromRuns(List<TextRun> runs, int index, List<OpcRelationship> relationships) {
        return textParagraphFromRuns(runs, index, relationships, true);
    }

    private String textParagraphFromRuns(List<TextRun> runs, int index, List<OpcRelationship> relationships,
            boolean explicitFontSize) {
        ParsedRuns parsed = parseListRuns(runs);
        String pPr = parsed.bullet ? "<a:pPr" + (parsed.level > 0 ? " lvl=\"" + parsed.level + "\"" : "") + "><a:buChar char=\"&#8226;\"/></a:pPr>" : "";
        StringBuilder body = new StringBuilder();
        List<TextRun> source = parsed.runs.isEmpty() ? singletonRun(" ") : parsed.runs;
        for (TextRun run : source) {
            body.append(textRunXml(run, index, relationships, explicitFontSize));
        }
        return "<a:p>" + pPr + body + "<a:endParaRPr lang=\"en-US\"/></a:p>";
    }

    private List<TextRun> singletonRun(String text) {
        List<TextRun> runs = new ArrayList<TextRun>();
        runs.add(new TextRun(text));
        return runs;
    }

    private ParsedRuns parseListRuns(List<TextRun> runs) {
        StringBuilder text = new StringBuilder();
        for (TextRun run : runs) {
            text.append(run.text);
        }
        String value = text.toString();
        if (!value.matches("^\\s*-\\s+.*$")) {
            return new ParsedRuns(runs, false, 0);
        }
        int spaces = 0;
        while (spaces < value.length() && value.charAt(spaces) == ' ') {
            spaces++;
        }
        int prefixLength = spaces + 2;
        List<TextRun> trimmed = new ArrayList<TextRun>();
        int remaining = prefixLength;
        for (TextRun run : runs) {
            if (remaining >= run.text.length()) {
                remaining -= run.text.length();
                continue;
            }
            String textPart = remaining > 0 ? run.text.substring(remaining) : run.text;
            remaining = 0;
            trimmed.add(new TextRun(textPart, run.href));
        }
        return new ParsedRuns(trimmed, true, Math.min(8, spaces / 2));
    }

    private String textRunXml(TextRun run, int index, List<OpcRelationship> relationships) {
        return textRunXml(run, index, relationships, true);
    }

    private String textRunXml(TextRun run, int index, List<OpcRelationship> relationships,
            boolean explicitFontSize) {
        String relId = null;
        if (run.href != null) {
            relId = findOrAddHyperlink(relationships, run.href);
        }
        String hyperlink = relId == null ? "" : "<a:hlinkClick r:id=\"" + relId + "\"/>";
        String fontSize = explicitFontSize ? " sz=\"" + (index == 0 ? 2400 : 1800) + "\"" : "";
        return "<a:r><a:rPr lang=\"en-US\"" + fontSize + ">" + hyperlink + "</a:rPr><a:t>" + Ooxml.xmlEscape(run.text) + "</a:t></a:r>";
    }

    private String findOrAddHyperlink(List<OpcRelationship> relationships, String href) {
        for (OpcRelationship rel : relationships) {
            if (href.equals(rel.getTarget()) && rel.getType().endsWith("/hyperlink")) {
                return rel.getId();
            }
        }
        return addRelationship(relationships, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink", href, "External");
    }

    private String addRelationship(List<OpcRelationship> relationships, String type, String target, String targetMode) {
        String id = "rId" + (relationships.size() + 1);
        relationships.add(new OpcRelationship(id, type, target, targetMode));
        return id;
    }

    private String tableXml(List<List<TableCell>> rows, int id, List<OpcRelationship> relationships,
            boolean explicitFontSize, PptxTemplateContext.Rect bodyRect, int precedingTextBlockCount) {
        int columnCount = 1;
        for (List<TableCell> row : rows) {
            columnCount = Math.max(columnCount, row.size());
        }
        StringBuilder grid = new StringBuilder();
        for (int i = 0; i < columnCount; i++) {
            grid.append("<a:gridCol w=\"1828800\"/>");
        }
        StringBuilder rowXml = new StringBuilder();
        List<List<TableCell>> sourceRows = rows.isEmpty() ? emptyTableRows() : rows;
        for (List<TableCell> row : sourceRows) {
            rowXml.append("<a:tr h=\"370840\">");
            for (int i = 0; i < columnCount; i++) {
                TableCell cell = i < row.size() ? row.get(i) : new TableCell(singletonRun(""));
                rowXml.append("<a:tc><a:txBody><a:bodyPr/><a:lstStyle/>")
                        .append(textParagraphFromRuns(cell.runs.isEmpty() ? singletonRun(cell.text) : cell.runs,
                                1, relationships, explicitFontSize))
                        .append("</a:txBody><a:tcPr/></a:tc>");
            }
            rowXml.append("</a:tr>");
        }
        long tableHeight = Math.max(740000L, sourceRows.size() * 370840L);
        long tableWidth = bodyRect == null ? 7772400L
                : Math.min(bodyRect.cx, Math.max(3600000L, (long) Math.floor(bodyRect.cx * 0.55d)));
        long tableX = bodyRect == null ? 685800L : bodyRect.x;
        long textOffset = precedingTextBlockCount > 0
                ? Math.min(bodyRect == null ? 2000000L : Math.max(0L, bodyRect.cy - tableHeight),
                        300000L + precedingTextBlockCount * 520000L)
                : 0L;
        long tableY = (bodyRect == null ? 2743200L : bodyRect.y) + textOffset;
        return "<p:graphicFrame><p:nvGraphicFramePr><p:cNvPr id=\"" + id + "\" name=\"Table " + id + "\"/><p:cNvGraphicFramePr/><p:nvPr/></p:nvGraphicFramePr>"
                + "<p:xfrm><a:off x=\"" + tableX + "\" y=\"" + tableY + "\"/><a:ext cx=\"" + tableWidth + "\" cy=\"" + tableHeight + "\"/></p:xfrm>"
                + "<a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/table\"><a:tbl><a:tblPr firstRow=\"1\" bandRow=\"1\"/><a:tblGrid>"
                + grid + "</a:tblGrid>" + rowXml + "</a:tbl></a:graphicData></a:graphic></p:graphicFrame>";
    }

    private List<List<TableCell>> emptyTableRows() {
        List<List<TableCell>> rows = new ArrayList<List<TableCell>>();
        List<TableCell> row = new ArrayList<TableCell>();
        row.add(new TableCell(singletonRun("")));
        rows.add(row);
        return rows;
    }

    private String addImage(SlideBlock image) {
        Md2PptxOptions.ImageLoader loader = options.getImageLoader();
        ImageAsset asset = loader == null ? null : loader.load(image.url);
        if (asset == null) {
            diagnostics.add(new Md2PptxDiagnostic("warning", "skipped-image", "Markdown image was not embedded: " + image.url, options.getSourcePath()));
            return null;
        }
        String extension = normalizeImageExtension(asset.getExtension());
        String fileName = "image" + nextImageIndex++ + "." + extension;
        mediaEntries.add(new ZipEntryInput("ppt/media/" + fileName, asset.getBytes()));
        return "../media/" + fileName;
    }

    private String normalizeImageExtension(String extension) {
        String value = extension == null ? "png" : extension.toLowerCase();
        if ("jpeg".equals(value) || "jpg".equals(value)) {
            return "jpg";
        }
        if ("gif".equals(value)) {
            return "gif";
        }
        return "png";
    }

    private String pictureXml(SlideBlock image, int id, String relId) {
        String name = image.altText == null || image.altText.isEmpty() ? image.url : image.altText;
        return "<p:pic><p:nvPicPr><p:cNvPr id=\"" + id + "\" name=\"" + Ooxml.xmlEscape(name) + "\" descr=\"" + Ooxml.xmlEscape(name) + "\"/><p:cNvPicPr/><p:nvPr/></p:nvPicPr>"
                + "<p:blipFill><a:blip r:embed=\"" + relId + "\"/><a:stretch><a:fillRect/></a:stretch></p:blipFill>"
                + "<p:spPr><a:xfrm><a:off x=\"685800\" y=\"3886200\"/><a:ext cx=\"2743200\" cy=\"1828800\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></p:spPr></p:pic>";
    }

    private String notesXml(SlideModel slide) {
        StringBuilder paragraphs = new StringBuilder();
        List<OpcRelationship> relationships = new ArrayList<OpcRelationship>();
        int index = 1;
        for (List<TextRun> runs : slide.notes) {
            paragraphs.append(textParagraphFromRuns(runs, index++, relationships));
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<p:notes xmlns:a=\"" + Ooxml.DRAWING_NS + "\" xmlns:r=\"" + Ooxml.REL_NS + "\" xmlns:p=\"" + Ooxml.PRESENTATION_NS + "\"><p:cSld><p:spTree>"
                + "<p:nvGrpSpPr><p:cNvPr id=\"1\" name=\"\"/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"0\" cy=\"0\"/><a:chOff x=\"0\" y=\"0\"/><a:chExt cx=\"0\" cy=\"0\"/></a:xfrm></p:grpSpPr>"
                + "<p:sp><p:nvSpPr><p:cNvPr id=\"2\" name=\"Slide image placeholder\"/><p:cNvSpPr><a:spLocks noGrp=\"1\" noRot=\"1\" noChangeAspect=\"1\"/></p:cNvSpPr><p:nvPr><p:ph type=\"sldImg\"/></p:nvPr></p:nvSpPr><p:spPr/></p:sp>"
                + "<p:sp><p:nvSpPr><p:cNvPr id=\"3\" name=\"Notes placeholder\"/><p:cNvSpPr><a:spLocks noGrp=\"1\"/></p:cNvSpPr><p:nvPr><p:ph type=\"body\" idx=\"1\"/></p:nvPr></p:nvSpPr><p:spPr/><p:txBody><a:bodyPr/><a:lstStyle/>"
                + paragraphs + "</p:txBody></p:sp></p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:notes>";
    }

    private static class ParsedRuns {
        final List<TextRun> runs;
        final boolean bullet;
        final int level;

        ParsedRuns(List<TextRun> runs, boolean bullet, int level) {
            this.runs = runs;
            this.bullet = bullet;
            this.level = level;
        }
    }

    private static class SlideXmlResult {
        final String xml;
        final List<OpcRelationship> relationships;

        SlideXmlResult(String xml, List<OpcRelationship> relationships) {
            this.xml = xml;
            this.relationships = relationships;
        }
    }
}
