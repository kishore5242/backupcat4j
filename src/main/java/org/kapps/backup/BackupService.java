package org.kapps.backup;

import com.google.common.base.Stopwatch;
import org.kapps.index.FileIndexer;
import org.kapps.index.IndexedFile;
import org.kapps.progress.ProgressTracker;
import org.kapps.utils.BackupUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.kapps.backup.BackupAction.BACKUP;
import static org.kapps.backup.FileType.OTHER;

@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);

    private final BackupAgentFactory agentFactory;

    @Autowired
    public BackupService(BackupAgentFactory agentFactory) {
        this.agentFactory = agentFactory;
    }

    public void backupFiles(BackupOptions backupOptions) throws IOException {
        Stopwatch sw = Stopwatch.createStarted();
        List<BackupResult> backupResults = new ArrayList<>();

        // Index folders
        List<IndexedFile> indexedFiles = index(backupOptions);

        ProgressTracker progressTracker = new ProgressTracker(indexedFiles);
        List<IndexedFile> pendingIndexedFiles = progressTracker.getPending(backupOptions);

        // Backup
        for (IndexedFile indexedFile : pendingIndexedFiles) {
            try {
                BackupAgent agent = agentFactory.getAgent(indexedFile.getMimeType());
                BackupResult backupResult = agent.backup(indexedFile, backupOptions);
                backupResults.add(backupResult);
                progressTracker.appendResult(backupResult);
            } catch (Exception e) {
                logger.error("Failed to back up: {}", indexedFile.getPath(), e);
                BackupResult errorResult = BackupResult.builder()
                        .indexedFile(indexedFile)
                        .agent("No agent")
                        .backupAction(BACKUP)
                        .status(false)
                        .message(e.getMessage())
                        .build();
                backupResults.add(errorResult);
                progressTracker.appendResult(errorResult);
            }
            progressTracker.logProgress(indexedFile);
        }

        // index target
        List<IndexedFile> targetIndexedFiles = FileIndexer.indexTargetFiles(backupOptions);

        // print results
        logBackupResults(pendingIndexedFiles, indexedFiles, targetIndexedFiles, backupResults, sw);
    }

    private List<IndexedFile> index(BackupOptions backupOptions) throws IOException {
        List<IndexedFile> indexedFiles = FileIndexer.indexFiles(backupOptions);
        FileIndexer.logFileCountsByFileType(indexedFiles);
        FileIndexer.logFileCountsByMime(indexedFiles);

        // Filter/Skip
        if (backupOptions.isSkipOthers()) {
            indexedFiles = indexedFiles.stream()
                    .filter(file -> !file.getFileType().equals(OTHER)).toList();
        }
        return indexedFiles;
    }


    private void logBackupResults(
            List<IndexedFile> pendingIndexedFiles,
            List<IndexedFile> indexedFiles,
            List<IndexedFile> targetIndexedFiles,
            List<BackupResult> backupResults,
            Stopwatch sw
    ) {
        logger.info("------------------------------------RESULT-----------------------------------------");

        // Errors
        Set<BackupResult> failed = backupResults.stream().filter(result -> !result.getStatus()).collect(Collectors.toSet());
        if (!failed.isEmpty()) {
            logger.info("Failures:");
            failed.forEach(f -> {
                logger.info("\tFailed to {} file {} with reason: {}",
                        f.getBackupAction(), f.getIndexedFile().getRelativePath(), f.getMessage());
            });
        }

        // actions
        logger.info("Actions performed:");
        Map<BackupAction, List<BackupResult>> groupedByAction = backupResults.stream().collect(Collectors.groupingBy(BackupResult::getBackupAction));
        groupedByAction.forEach((action, results) -> {
            long success = results.stream().filter(BackupResult::getStatus).count();
            logger.info(String.format("\t%s:\t%d/%d", fixedLength(action.toString(), 30), success, results.size()));
        });


        // agents
        logger.info("");
        logger.info("Processed by:");
        Map<String, List<BackupResult>> grouppedByAgent = backupResults.stream().collect(Collectors.groupingBy(BackupResult::getAgent));
        grouppedByAgent.forEach((agent, results) -> {
            long success = results.stream().filter(BackupResult::getStatus).count();
            logger.info(String.format("\t%s:\t%d/%d", fixedLength(agent, 30), success, results.size()));
        });

        logger.info("");
        logger.info("File counts:");
        logger.info("\t{}:\t{}", fixedLength("Indexed", 30), indexedFiles.size());
        int previous = indexedFiles.size() - pendingIndexedFiles.size();
        logger.info("\t{}:\t{}", fixedLength("Previously backed up", 30), previous);
        // clashes
        long clashes = pendingIndexedFiles.stream().filter(i -> StringUtils.hasLength(i.getSuffix())).count();
        logger.info("\t{}:\t{}", fixedLength("Clashes handled", 30), clashes);
        // total
        long backedUpCount = backupResults.stream().filter(BackupResult::getStatus).count();
        logger.info("\t{}:\t{}/{}", fixedLength("Backed up", 30), backedUpCount, pendingIndexedFiles.size());
        logger.info("\t{}:\t{}", fixedLength("Failed", 30), failed.size());

        // size
        long originalSize = indexedFiles.stream().mapToLong(IndexedFile::getSize).sum();
        long backupSize = targetIndexedFiles.stream().mapToLong(IndexedFile::getSize).sum();
        logger.info("");
        logger.info("Folder sizes:");
        logger.info("\t{}:\t{}", fixedLength("Original", 30), BackupUtils.humanReadableByteCount(originalSize));
        logger.info("\t{}:\t{}", fixedLength("Backup", 30), BackupUtils.humanReadableByteCount(backupSize));

        // time
        logger.info("");
        logger.info("Time taken:");
        logger.info("\t{}:\t{}", fixedLength("Total", 30), sw.stop());
        logger.info("-----------------------------------------------------------------------------------");
    }

    private static String fixedLength(String input, int length) {
        if (input == null) input = "";
        if (input.length() > length) {
            return input.substring(0, length);  // Truncate
        } else {
            return String.format("%-" + length + "s", input);  // Pad with spaces
        }
    }

}
