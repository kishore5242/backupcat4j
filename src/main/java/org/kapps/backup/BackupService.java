package org.kapps.backup;

import com.google.common.base.Stopwatch;
import org.kapps.index.IndexedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.kapps.backup.BackupAction.BACKUP;

@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);


    private final BackupAgentFactory agentFactory;

    @Autowired
    public BackupService(BackupAgentFactory agentFactory) {
        this.agentFactory = agentFactory;
    }

    public void backupFiles(List<IndexedFile> indexedFiles, Path targetRoot) {
        Stopwatch sw = Stopwatch.createStarted();
        List<BackupResult> backupResults = new ArrayList<>();
        for (IndexedFile indexedFile : indexedFiles) {
            try {
                // Ensure target directories exist
                Path relativePath = Path.of(indexedFile.getRelativePath());
                Path targetPath = targetRoot.resolve(relativePath);
                File targetFile = targetPath.toFile();
                File parentDir = targetFile.getParentFile();
                if (!parentDir.exists() && !parentDir.mkdirs()) {
                    logger.error("Failed to create target directory: {}", parentDir.getAbsolutePath());
                    return;
                }
                BackupAgent agent = agentFactory.getAgent(indexedFile.getMimeType());
                BackupResult backupResult = agent.backup(indexedFile, targetRoot);
                backupResults.add(backupResult);
            } catch (Exception e) {
                logger.error("Failed to back up: {}", indexedFile.getPath(), e);
                backupResults.add(new BackupResult(indexedFile, "No agent", BACKUP, e.getMessage()));
            }
        }
        // print results
        logBackupResults(indexedFiles, backupResults, sw);
    }

    private void logBackupResults(List<IndexedFile> indexedFiles, List<BackupResult> backupResults, Stopwatch sw) {
        logger.info("-------------------------------------------RESULT-----------------------------------------------");

        // actions
        logger.info("Actions performed:");
        Map<BackupAction, List<BackupResult>> groupedByAction = backupResults.stream().collect(Collectors.groupingBy(BackupResult::getBackupAction));
        groupedByAction.forEach((action, results) -> {
            long success = results.stream().filter(BackupResult::getStatus).count();
            logger.info("{}: {}/{}", action, success, results.size());
        });

        // agents
        logger.info("");
        logger.info("Processed by:");
        Map<String, List<BackupResult>> grouppedByAgent = backupResults.stream().collect(Collectors.groupingBy(BackupResult::getAgent));
        grouppedByAgent.forEach((agent, results) -> {
            long success = results.stream().filter(BackupResult::getStatus).count();
            logger.info("{}: {}/{} files", agent, success, results.size());
        });

        logger.info("");
        logger.info("Failures:");
        // Errors
        backupResults.forEach(result -> {
            if (!result.getStatus()) {
                logger.info("Failed to {} file {} with reason: {}",
                        result.getBackupAction(), result.getIndexedFile().getRelativePath(), result.getError());
            }
        });

        // total
        logger.info("");
        long backedUpCount = backupResults.stream().filter(BackupResult::getStatus).count();
        logger.info("Backed up: {}/{} files", backedUpCount, indexedFiles.size());

        // time
        logger.info("");
        logger.info("Time taken: {}", sw.stop());
        logger.info("----------------------------------------COMPLETE------------------------------------------------");
    }
}
