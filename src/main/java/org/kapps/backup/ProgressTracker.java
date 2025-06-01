package org.kapps.backup;

import org.kapps.index.IndexedFile;
import org.kapps.utils.BackupUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProgressTracker {

    private static final Logger logger = LoggerFactory.getLogger(ProgressTracker.class);

    private final List<IndexedFile> indexedFiles;
    private final long totalSize;

    private long completedSize = 0;
    private int completedFiles = 0;

    private final long startTimeNanos;

    public ProgressTracker(List<IndexedFile> indexedFiles) {
        this.indexedFiles = indexedFiles;
        this.totalSize = indexedFiles.stream().mapToLong(IndexedFile::getSize).sum();
        this.startTimeNanos = System.nanoTime();
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

        logger.info("---------------------------------------------------------------------------------- [ Files: {}/{} ] [ {}% ] [ ETA: {} ]",
                completedFiles, indexedFiles.size(),
                String.format("%.2f", percentage),
                timeStr
        );
    }
}

