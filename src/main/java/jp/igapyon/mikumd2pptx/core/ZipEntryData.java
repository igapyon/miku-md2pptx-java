package jp.igapyon.mikumd2pptx.core;

class ZipEntryData {
    final String path;
    final byte[] bytes;

    ZipEntryData(String path, byte[] bytes) {
        this.path = path;
        this.bytes = bytes;
    }
}
