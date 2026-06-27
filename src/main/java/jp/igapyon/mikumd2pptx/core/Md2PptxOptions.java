package jp.igapyon.mikumd2pptx.core;

public class Md2PptxOptions {
    public interface ImageLoader {
        ImageAsset load(String path);
    }

    private String title;
    private String sourcePath;
    private ImageLoader imageLoader;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public ImageLoader getImageLoader() {
        return imageLoader;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }
}
