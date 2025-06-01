package org.kapps.backup;

import org.kapps.index.IndexedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProgressTracker {

    private static final Logger logger = LoggerFactory.getLogger(ProgressTracker.class);

    private final List<IndexedFile> indexedFiles;
    private int completedFiles = 0;

    private final long totalSize;
    private long completedSize = 0;

    public ProgressTracker(List<IndexedFile> indexedFiles) {
        this.indexedFiles = indexedFiles;
        this.totalSize = indexedFiles.stream().mapToLong(IndexedFile::getSize).sum();
    }

    public void logProgress(IndexedFile indexedFile) {
        this.completedSize += indexedFile.getSize();
        this.completedFiles++;
        if (totalSize > 0) {
            double percentage = (completedSize * 100.0) / totalSize;
            String formatted = String.format("%.2f", percentage);
            logger.info("---------------------------------------------------------------------------------------------- Files: [ {}/{} ] [ {}% ]",
                    completedFiles, indexedFiles.size(), formatted);
        } else {
            logger.warn("Total size is zero, cannot compute progress.");
        }
    }
}
