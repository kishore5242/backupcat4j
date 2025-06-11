package org.kapps.progress;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.kapps.backup.BackupOptions;
import org.kapps.backup.BackupResult;
import org.kapps.index.FileIndexer;
import org.kapps.index.IndexedFile;
import org.kapps.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProgressService {

    private static final Logger logger = LoggerFactory.getLogger(ProgressService.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private Path progressFile;
    private List<IndexedFile> indexedFiles = new ArrayList<>();
    private List<IndexedFile> resumedFiles = new ArrayList<>();
    private List<BackupResult> backupResults = new ArrayList<>();
    private long totalSize = 1L;
    private long completedSize = 1L;
    private long startTime;
    private double subPercent = 0.0;

    public List<IndexedFile> start(List<IndexedFile> indexedFiles, BackupOptions backupOptions) throws IOException {
        this.startTime = System.nanoTime();
        this.indexedFiles = indexedFiles;
        this.backupResults = new ArrayList<>();

        this.progressFile = Paths.get(System.getProperty("user.dir")).resolve("logs/backupcat4j-progress.jsonl");
        FileUtils.createIfNotExists(this.progressFile);

        this.resumedFiles = getPendingFiles(backupOptions);

        logger.info("Progress service started.");
        return this.resumedFiles;
    }

    public void addResult(BackupResult result) throws IOException {
        backupResults.add(result);
        writeResultToFile(result);
        this.totalSize = resumedFiles.stream().mapToLong(IndexedFile::getSize).sum();
        this.completedSize = backupResults.stream().mapToLong(br -> br.getIndexedFile().getSize()).sum();
    }

    public double getProgressPercent() {
        if (backupResults.size() <= 2) return 0; // at least 2 files are required to calculate
        return totalSize > 0 ? (completedSize * 100.0) / totalSize : 0;
    }

    public void setSubPercent(double percent) {
        this.subPercent = percent;
    }

    public double getSubPercent() {
        return this.subPercent;
    }

    public String getRemainingTime() {
        long elapsed = (long) ((System.nanoTime() - startTime) / 1_000_000.0);
        long speed = elapsed > 0 ? completedSize / elapsed : 1;
        long remainingBytes = totalSize - completedSize;
        long remainingTime = (speed > 0) ? remainingBytes / speed : 0;
        return DurationFormatUtils.formatDuration(remainingTime, "H'h' m'm' s's'");
    }

    private List<IndexedFile> getPendingFiles(BackupOptions backupOptions) throws IOException {
        if (!backupOptions.isResume()) {
            // start from beginning
            return this.indexedFiles;
        } else {
            // check for last result
            BackupResult lastResult = readLastResult();
            if (lastResult == null) {
                // start from beginning
                return this.indexedFiles;
            } else {
                // check if it was completed previously
                List<IndexedFile> slice = FileIndexer.slice(indexedFiles, lastResult.getIndexedFile().getPath());
                if (slice.size() == indexedFiles.size()) {
                    // start from beginning
                    return this.indexedFiles;
                } else {
                    // resume
                    return slice;
                }
            }
        }
    }

    private void writeResultToFile(BackupResult result) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(
                this.progressFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            writer.write(mapper.writeValueAsString(result));
            writer.newLine();
        }
    }

    private BackupResult readLastResult() throws IOException {
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
}
