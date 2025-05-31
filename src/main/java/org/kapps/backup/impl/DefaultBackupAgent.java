package org.kapps.backup.impl;

import org.kapps.backup.BackupAgent;
import org.kapps.backup.BackupResult;
import org.kapps.index.IndexedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.kapps.backup.BackupAction.COPY;

@Component
@Order(100)
public class DefaultBackupAgent implements BackupAgent {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBackupAgent.class);

    @Override
    public String name() {
        return "Default backup agent";
    }

    @Override
    public BackupResult backup(IndexedFile indexedFile, Path targetRoot) {
        try {
            logger.info("Backing up file: {}", indexedFile.getPath());
            Files.copy(indexedFile.getPath(), targetRoot.resolve(indexedFile.getRelativePath()));
            return new BackupResult(indexedFile, name(), COPY);
        } catch (IOException e) {
            logger.error("Failed to copy file", e);
            return new BackupResult(indexedFile, name(), COPY, e.getMessage());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return true; // fallback for any MIME
    }
}