package jp.igapyon.mikumd2pptx.core;

public class ImageAsset {
    private final byte[] bytes;
    private final String extension;

    public ImageAsset(byte[] bytes, String extension) {
        this.bytes = bytes == null ? new byte[0] : bytes.clone();
        this.extension = extension;
    }

    public byte[] getBytes() {
        return bytes.clone();
    }

    public String getExtension() {
        return extension;
    }
}
