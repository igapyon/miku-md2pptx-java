package jp.igapyon.mikumd2pptx.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Md2PptxResult {
    private final byte[] pptx;
    private final List<Md2PptxDiagnostic> diagnostics;

    Md2PptxResult(byte[] pptx, List<Md2PptxDiagnostic> diagnostics) {
        this.pptx = pptx == null ? new byte[0] : pptx.clone();
        this.diagnostics = Collections.unmodifiableList(new ArrayList<Md2PptxDiagnostic>(diagnostics));
    }

    public byte[] getPptx() {
        return pptx.clone();
    }

    public List<Md2PptxDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
