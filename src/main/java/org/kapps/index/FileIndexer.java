package org.kapps.index;

import org.kapps.backup.FileType;
import org.kapps.utils.MimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileIndexer {

    private static final Logger logger = LoggerFactory.getLogger(FileIndexer.class);

    public static List<IndexedFile> indexFiles(String source) throws IOException {
        logger.info("Indexing folder {}", source);
        Path sourceDir = Paths.get(source);
        List<IndexedFile> indexedFiles = new ArrayList<>();

        List<Path> files;
        try (Stream<Path> stream = Files.walk(sourceDir)) {
            files = stream.filter(Files::isRegularFile).toList();
        }
        int total = files.size();
        logger.info("Found {} files", total);
        AtomicInteger i = new AtomicInteger();
        for (Path path : files) {
            int percentage = (i.get() * 100) / total;
            System.out.print("\rIndexing... " + percentage + "%");
            try {
                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                long size = attr.size();
                long lastModified = attr.lastModifiedTime().toMillis();
                String key = sourceDir.relativize(path).toString();
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
            } finally {
                i.getAndIncrement();
            }
        }

        System.out.println(); // move to new line after progress
        logger.info("Indexing completed for {}", sourceDir);
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

    public static void logFileCountsByFileType(List<IndexedFile> indexedFiles) {
        Map<FileType, List<IndexedFile>> groupedFiles = indexedFiles.stream().collect(Collectors.groupingBy(IndexedFile::getFileType));
        groupedFiles.forEach((mimeType, files) -> {
            logger.info("Found {} {} files", files.size(), mimeType);
        });
        logger.info("Total: {} files", indexedFiles.size());
    }
}
