package org.kapps.backup.impl;

import org.kapps.backup.BackupAgent;
import org.kapps.backup.BackupResult;
import org.kapps.backup.VideoCompressor;
import org.kapps.index.IndexedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.kapps.backup.BackupAction.COMPRESS;
import static org.kapps.backup.BackupAction.COPY;

@Component
@Order(10)
public class VideoBackupAgent implements BackupAgent {

    private static final Logger logger = LoggerFactory.getLogger(VideoBackupAgent.class);

    @Autowired
    private VideoCompressor videoCompressor;

    @Override
    public String name() {
        return "Video backup agent";
    }

    @Override
    public BackupResult backup(IndexedFile indexedFile, Path targetRoot) {
        logger.info("Backing up video: {}", indexedFile.getPath());
        Path targetPath = targetRoot.resolve(indexedFile.getRelativePath());
        File inputFile = indexedFile.getPath().toFile();

        Map<String, String> metadata = videoCompressor.probeVideo(inputFile);

        // compress to destination
        if (!videoCompressor.isAlreadyCompressed(inputFile, metadata)) {
            String error = videoCompressor.compressVideo(inputFile, targetPath.toFile());
            if (StringUtils.hasLength(error)) {
                return new BackupResult(indexedFile, name(), COMPRESS, error);
            }
            return new BackupResult(indexedFile, name(), COMPRESS);
        }

        // copy as it is
        try {
            logger.info("Copying the video as it is..");
            Files.copy(indexedFile.getPath(), targetRoot.resolve(indexedFile.getRelativePath()));
            return new BackupResult(indexedFile, name(), COPY);
        } catch (IOException e) {
            logger.error("Failed to copy video", e);
            return new BackupResult(indexedFile, name(), COPY, e.getMessage());
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return mimeType != null && mimeType.startsWith("video/");
    }
}