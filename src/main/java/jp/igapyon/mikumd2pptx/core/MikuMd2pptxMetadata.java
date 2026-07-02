package jp.igapyon.mikumd2pptx.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MikuMd2pptxMetadata {
    public static final MikuMd2pptxMetadata INSTANCE = new MikuMd2pptxMetadata(
            "miku-md2pptx",
            "markdown-to-pptx-runtime",
            "markdown",
            "pptx",
            Arrays.asList("markdownToSlides", "markdownToPptx", "markdownToPptxResult"));

    private final String productName;
    private final String artifactRole;
    private final String primaryInput;
    private final String primaryOutput;
    private final List<String> coreApi;

    private MikuMd2pptxMetadata(String productName, String artifactRole, String primaryInput, String primaryOutput, List<String> coreApi) {
        this.productName = productName;
        this.artifactRole = artifactRole;
        this.primaryInput = primaryInput;
        this.primaryOutput = primaryOutput;
        this.coreApi = Collections.unmodifiableList(coreApi);
    }

    public String getProductName() {
        return productName;
    }

    public String getArtifactRole() {
        return artifactRole;
    }

    public String getPrimaryInput() {
        return primaryInput;
    }

    public String getPrimaryOutput() {
        return primaryOutput;
    }

    public List<String> getCoreApi() {
        return coreApi;
    }
}
