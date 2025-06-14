package org.kapps.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static void silentDelete(Path path) {
        try {
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            logger.error("Failed to delete file", e);
        }
    }

    public static void createIfNotExists(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
        } catch (IOException e) {
            logger.error("Failed to create file", e);
        }
    }

    public static void recreate(Path path) {
        try {
            if (Files.exists(path)) {
                Files.delete(path);
            }
            Files.createFile(path);
        } catch (IOException e) {
            logger.error("Failed to recreate file", e);
        }
    }

    public static Path addSuffixToFile(Path originalPath, String suffix) {
        Path parent = originalPath.getParent();
        String fileName = originalPath.getFileName().toString();

        int dotIndex = fileName.lastIndexOf('.');
        String name = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        String extension = (dotIndex == -1) ? "" : fileName.substring(dotIndex);

        return parent.resolve(name + "_" + suffix + extension);
    }

    public static void deleteRecursively(Path path) throws IOException {
        if (Files.notExists(path)) return;
        try (Stream<Path> stream = Files.walk(path)) {
            stream.sorted((a, b) -> b.compareTo(a)) // delete children before parents
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            System.err.println("Failed to delete: " + p + " — " + e.getMessage());
                        }
                    });
        }
    }
}
