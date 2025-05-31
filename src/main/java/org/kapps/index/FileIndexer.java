package org.kapps.index;

import org.kapps.utils.MimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileIndexer {

    private static final Logger logger = LoggerFactory.getLogger(FileIndexer.class);

    public static List<IndexedFile> indexFiles(Path sourceDir) throws IOException {
        logger.info("Indexing folder {}", sourceDir);
        List<IndexedFile> indexedFiles = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(sourceDir)) {
            stream.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                            long size = attr.size();
                            long lastModified = attr.lastModifiedTime().toMillis();
                            String key = sourceDir.relativize(path).toString(); // relative path as key
                            String mimeType = MimeUtils.detectMimeType(path);
                            IndexedFile indexedFile = new IndexedFile.Builder()
                                    .path(path)
                                    .mimeType(mimeType)
                                    .relativePath(key)
                                    .size(size)
                                    .lastModified(lastModified)
                                    .build();
                            indexedFiles.add(indexedFile);
                        } catch (IOException e) {
                            logger.error("Failed to index {}", path, e);
                        }
                    });
            logger.info("Indexing completed for {}", sourceDir);
        }
        Collections.sort(indexedFiles);
        return indexedFiles;
    }

    public static void logFileCountsByMime(List<IndexedFile> indexedFiles) {
        Map<String, List<IndexedFile>> groupedFiles = indexedFiles.stream().collect(Collectors.groupingBy(IndexedFile::getMimeType));
        groupedFiles.forEach((mimeType, files) -> {
            logger.info("Found {} {} files", files.size(), mimeType);
        });
        logger.info("Total: {} files", indexedFiles.size());
    }
}
