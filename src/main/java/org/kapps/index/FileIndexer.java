package org.kapps.index;

import org.kapps.backup.BackupOptions;
import org.kapps.backup.FileType;
import org.kapps.backup.OrganizeMode;
import org.kapps.utils.MimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FileIndexer {

    private static final Logger logger = LoggerFactory.getLogger(FileIndexer.class);

    public static List<IndexedFile> indexFiles(BackupOptions backupOptions) throws IOException {
        List<IndexedFile> indexedFiles = new ArrayList<>();

        String source = backupOptions.getSource();
        Path sourceDir = Paths.get(source);
        logger.info("Indexing folder {}", source);

        // get all file paths
        List<Path> files = new ArrayList<>();
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile()) {
                    files.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Log and skip access-denied files
                logger.error("Skipping inaccessible file/folder: {}", file);
                return FileVisitResult.CONTINUE;
            }
        });
        int total = files.size();

        logger.info("Found {} files", total);
        AtomicInteger i = new AtomicInteger();
        Map<Path, Integer> suffixes = new HashMap<>(); // path and their suffix - to handle clashes
        boolean skipSuffix = backupOptions.getOrganize().equals(OrganizeMode.NONE);
        for (Path path : files) {
            int percentage = (i.get() * 100) / total;
            System.out.print("\rIndexing... [ " + percentage + "% ]");
            try {
                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                long size = attr.size();
                long lastModified = attr.lastModifiedTime().toMillis();
                String key = sourceDir.relativize(path).toString();
                String mimeType = MimeUtils.detectMimeType(path);

                // Handle clashes and suffix
                Integer thisFileSuffix = 0;
                if (!skipSuffix) {
                    Integer existing = suffixes.get(path.getFileName());
                    // handle clash
                    if (existing != null) {
                        thisFileSuffix = existing + 1;
                        suffixes.put(path.getFileName(), thisFileSuffix);
                    } else {
                        // non-null so that clash can be detected
                        suffixes.put(path.getFileName(), thisFileSuffix);
                    }
                }

                IndexedFile indexedFile = new IndexedFile.Builder()
                        .path(path)
                        .mimeType(mimeType)
                        .relativePath(key)
                        .size(size)
                        .lastModified(lastModified)
                        .suffix(thisFileSuffix == 0 ? "" : String.valueOf(thisFileSuffix))
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

    public static List<IndexedFile> indexTargetFiles(BackupOptions backupOptions) throws IOException {
        List<IndexedFile> indexedFiles = new ArrayList<>();

        String target = backupOptions.getTarget();
        Path targetDir = Paths.get(target);
        logger.info("Indexing target folder {}", target);

        // get all file paths
        List<Path> files = new ArrayList<>();
        Files.walkFileTree(targetDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile()) {
                    files.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Log and skip access-denied files
                logger.error("Skipping inaccessible file/folder: {}", file);
                return FileVisitResult.CONTINUE;
            }
        });
        int total = files.size();

        logger.info("Found {} files", total);
        AtomicInteger i = new AtomicInteger();
        for (Path path : files) {
            int percentage = (i.get() * 100) / total;
            System.out.print("\rIndexing... [ " + percentage + "% ]");
            try {
                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
                long size = attr.size();
                long lastModified = attr.lastModifiedTime().toMillis();
                String key = targetDir.relativize(path).toString();
                String mimeType = MimeUtils.detectMimeType(path);

                IndexedFile indexedFile = new IndexedFile.Builder()
                        .path(path)
                        .mimeType(mimeType)
                        .relativePath(key)
                        .size(size)
                        .lastModified(lastModified)
                        .suffix("")
                        .build();

                indexedFiles.add(indexedFile);
            } catch (IOException e) {
                logger.error("Failed to index {}", path, e);
            } finally {
                i.getAndIncrement();
            }
        }

        System.out.println(); // move to new line after progress
        logger.info("Indexing completed for {}", targetDir);
        Collections.sort(indexedFiles);
        return indexedFiles;
    }

    public static List<IndexedFile> slice(List<IndexedFile> indexedFiles, Path path) {
        int index = -1;
        for (int i = 0; i < indexedFiles.size(); i++) {
            if (path.equals(indexedFiles.get(i).getPath())) {
                index = i;
            }
        }
        if (index == -1 || index == indexedFiles.size() - 1) {
            return indexedFiles;
        }
        return indexedFiles.subList(index + 1, indexedFiles.size());
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
