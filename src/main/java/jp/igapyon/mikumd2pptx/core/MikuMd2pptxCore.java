package jp.igapyon.mikumd2pptx.core;

import java.util.ArrayList;
import java.util.List;

public class MikuMd2pptxCore {
    public static final String VERSION = "0.2.3";
    public static final MikuMd2pptxMetadata METADATA = MikuMd2pptxMetadata.INSTANCE;

    public Md2PptxResult convertMarkdownToPptx(String markdown) {
        return convertMarkdownToPptx(markdown, new Md2PptxOptions());
    }

    public Md2PptxResult convertMarkdownToPptx(String markdown, Md2PptxOptions options) {
        Md2PptxOptions effectiveOptions = options == null ? new Md2PptxOptions() : options;
        List<SlideModel> slides = new MarkdownSlides().parse(markdown, effectiveOptions);
        List<Md2PptxDiagnostic> diagnostics = collectDiagnostics(slides, effectiveOptions);
        byte[] pptx = new PptxPackageBuilder(diagnostics, effectiveOptions).build(slides);
        return new Md2PptxResult(pptx, diagnostics);
    }

    List<SlideModel> markdownToSlides(String markdown, Md2PptxOptions options) {
        return new MarkdownSlides().parse(markdown, options == null ? new Md2PptxOptions() : options);
    }

    private List<Md2PptxDiagnostic> collectDiagnostics(List<SlideModel> slides, Md2PptxOptions options) {
        List<Md2PptxDiagnostic> diagnostics = new ArrayList<Md2PptxDiagnostic>();
        for (SlideModel slide : slides) {
            for (SlideBlock block : slide.blocks) {
                if (block.kind == SlideBlock.Kind.TEXT && block.text != null && block.text.contains("<") && block.text.contains(">")) {
                    diagnostics.add(new Md2PptxDiagnostic("warning", "possible-raw-html-text", "Raw HTML-like text was emitted as plain slide text.", options.getSourcePath()));
                }
            }
        }
        return diagnostics;
    }
}
