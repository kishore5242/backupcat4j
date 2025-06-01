package org.kapps.backup.impl;

import org.kapps.backup.BackupAgent;
import org.kapps.backup.BackupOptions;
import org.kapps.backup.BackupResult;
import org.kapps.index.IndexedFile;
import org.kapps.utils.BackupUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.kapps.backup.BackupAction.*;

@Component
@Order(100)
public class DefaultBackupAgent implements BackupAgent {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBackupAgent.class);

    @Override
    public String name() {
        return "Default backup agent";
    }

    @Override
    public BackupResult backup(IndexedFile indexedFile, BackupOptions backupOptions) {
        BackupResult.Builder resultBuilder = BackupResult.builder()
                .indexedFile(indexedFile)
                .agent(name());
        try {

            // calculate the destination path
            Path targetPath = BackupUtils.getDefaultTargetPath(indexedFile, backupOptions);

            if (Files.exists(targetPath)) {
                return resultBuilder
                        .backupAction(SKIP)
                        .status(true)
                        .message("File already exists")
                        .build();
            }

            // create target directories
            File targetFile = targetPath.toFile();
            File parentDir = targetFile.getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                logger.error("Failed to create target directory: {}", parentDir.getAbsolutePath());
                return resultBuilder
                        .backupAction(BACKUP)
                        .status(false)
                        .message("Failed to create target directories")
                        .build();
            }

            logger.info("Backing up file: {}", indexedFile.getPath());
            Files.copy(indexedFile.getPath(), targetPath);
            return resultBuilder
                    .backupAction(COPY)
                    .status(true)
                    .message("File copied successfully")
                    .build();

        } catch (IOException e) {
            logger.error("Failed to copy file", e);
            return resultBuilder
                    .backupAction(COPY)
                    .status(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return true; // fallback for any MIME
    }
}