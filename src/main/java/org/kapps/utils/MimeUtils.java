package org.kapps.utils;

import org.apache.tika.Tika;

import java.io.IOException;
import java.nio.file.Path;

public class MimeUtils {
    private static final Tika tika = new Tika();

    public static String detectMimeType(Path path) throws IOException {
        return tika.detect(path.toFile());
    }
}
