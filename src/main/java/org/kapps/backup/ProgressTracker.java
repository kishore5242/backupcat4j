package org.kapps.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.kapps.index.FileIndexer;
import org.kapps.index.IndexedFile;
import org.kapps.utils.BackupUtils;
import org.kapps.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ProgressTracker {

    private static final Logger logger = LoggerFactory.getLogger(ProgressTracker.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final List<IndexedFile> indexedFiles;
    private final long totalSize;
    private long completedSize = 0;
    private int completedFiles = 0;
    private long startTimeNanos;
    private final Path progressFile;

    public ProgressTracker(List<IndexedFile> indexedFiles) {
        this.indexedFiles = indexedFiles;
        this.totalSize = indexedFiles.stream().mapToLong(IndexedFile::getSize).sum();
        this.startTimeNanos = System.nanoTime();
        this.progressFile = Paths.get(System.getProperty("user.dir")).resolve("logs/backupcat4j-progress.jsonl");
        FileUtils.createIfNotExists(this.progressFile);
        logger.info("Progress tracker {}", this.progressFile);
    }

    public List<IndexedFile> getPending(BackupOptions backupOptions) throws IOException {
        if (!backupOptions.isResume()) {
            // start from beginning
            reset();
        } else {
            // check for last result
            BackupResult lastResult = readLastResult();
            if (lastResult == null) {
                // start from beginning
                reset();
            } else {
                // check if it was completed previously
                List<IndexedFile> slice = FileIndexer.slice(indexedFiles, lastResult.getIndexedFile().getPath());
                if (slice.size() == indexedFiles.size()) {
                    // start from beginning
                    reset();
                } else {
                    // resume
                    return slice;
                }
            }
        }
        return indexedFiles;
    }

    public void reset() {
        FileUtils.recreate(progressFile);
        this.startTimeNanos = System.nanoTime();
        this.completedSize = 0;
        this.completedFiles = 0;
    }

    public void appendResult(BackupResult entry) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(
                this.progressFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(mapper.writeValueAsString(entry));
            writer.newLine();
        }
    }

    public BackupResult readLastResult() throws IOException {
        try (ReversedLinesFileReader reversedReader = new ReversedLinesFileReader(
                this.progressFile.toFile(), 4096, "UTF-8"
        )) {
            String lastLine;
            while ((lastLine = reversedReader.readLine()) != null) {
                lastLine = lastLine.trim();
                if (!lastLine.isEmpty()) {
                    return mapper.readValue(lastLine, BackupResult.class);
                }
            }
        }
        return null;
    }

    public void logProgress(IndexedFile indexedFile) {
        completedSize += indexedFile.getSize();
        completedFiles++;

        if (totalSize <= 0) {
            logger.warn("Total size is zero, cannot compute progress.");
            return;
        }

        double percentage = (completedSize * 100.0) / totalSize;

        // Time calculations
        long now = System.nanoTime();
        double elapsedSeconds = (now - startTimeNanos) / 1_000_000_000.0;
        long remainingBytes = totalSize - completedSize;
        double speed = completedSize / elapsedSeconds;  // bytes per second

        double remainingSeconds = (speed > 0) ? remainingBytes / speed : -1;

        String timeStr = (remainingSeconds >= 0)
                ? BackupUtils.formatSecondsToHHMMSS(remainingSeconds)
                : "Unknown";

        logger.info("--Progress------- [ Files: {}/{} ] |{}|% [ ETA: {} ]",
                completedFiles, indexedFiles.size(),
                String.format("%.2f", percentage),
                timeStr
        );
    }


}

