package jp.igapyon.mikumd2pptx.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipTestSupport {
    public static Map<String, byte[]> unzip(byte[] zipBytes) throws IOException {
        Map<String, byte[]> entries = new HashMap<String, byte[]>();
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(zipBytes));
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = zip.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
            entries.put(entry.getName(), out.toByteArray());
        }
        zip.close();
        return entries;
    }

    public static String text(Map<String, byte[]> entries, String path) {
        byte[] bytes = entries.get(path);
        return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
    }
}
