package org.kapps.utils;

import org.kapps.backup.FileType;

import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.kapps.backup.FileType.*;

public class OrganizerUtils {

    private static final Pattern DOCUMENT_MIME_PATTERN = Pattern.compile(
            "^application/.*(pdf|msword|spreadsheet|ms-excel|ms-powerpoint|officedocument|opendocument).*",
            Pattern.CASE_INSENSITIVE
    );

    public static FileType parse(String mimeType, Path path) {
        // Based on path
        String fileName = path.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == 0 || dotIndex == fileName.length() - 1) {
            return OTHER; // No extension or bad format like ".hidden" or "file."
        }

        // Based on MIME type
        if (mimeType.startsWith("image/")) {
            return IMAGE;
        } else if (mimeType.startsWith("video/")) {
            return VIDEO;
        } else if (DOCUMENT_MIME_PATTERN.matcher(mimeType).matches()) {
            return DOCUMENT;
        } else if (mimeType.startsWith("audio/")) {
            return AUDIO;
        } else {
            return OTHER;
        }
    }
}
