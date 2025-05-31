package org.kapps.backup.impl;

import org.kapps.backup.BackupAgent;
import org.kapps.backup.BackupOptions;
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
import java.nio.file.Paths;
import java.util.Map;

import static org.kapps.backup.BackupAction.*;

@Component
@Order(10)
public class VideoBackupAgent implements BackupAgent {

    private static final Logger logger = LoggerFactory.getLogger(VideoBackupAgent.class);

    @Override
    public String name() {
        return "Video backup agent";
    }

    @Override
    public BackupResult backup(IndexedFile indexedFile, BackupOptions backupOptions) {
        logger.info("Backing up video: {}", indexedFile.getPath());

        Path sourcePath = indexedFile.getPath();
        File inputFile = sourcePath.toFile();
        Path targetDir = Paths.get(backupOptions.getTarget());
        Path targetPath = targetDir.resolve(indexedFile.getRelativePath());

        if (Files.exists(targetPath)) {
            logger.info("Skipping as the file already exists");
            return BackupResult.builder()
                    .indexedFile(indexedFile)
                    .agent(name())
                    .backupAction(SKIP)
                    .status(true)
                    .message("File already exists")
                    .build();
        }

        VideoCompressor videoCompressor = new VideoCompressor(backupOptions);

        Map<String, String> metadata = videoCompressor.probeVideo(inputFile);

        // compress to destination
        if (!videoCompressor.isAlreadyCompressed(inputFile, metadata)) {
            String error = videoCompressor.compressVideo(inputFile, targetPath.toFile());
            if (StringUtils.hasLength(error)) {
                return BackupResult.builder()
                        .indexedFile(indexedFile)
                        .agent(name())
                        .backupAction(COMPRESS)
                        .status(false)
                        .message(error)
                        .build();
            }
            return BackupResult.builder()
                    .indexedFile(indexedFile)
                    .agent(name())
                    .backupAction(COMPRESS)
                    .status(true)
                    .message("Video compressed successfully")
                    .build();
        }

        // copy as it is
        try {
            logger.info("Copying the video as it is..");
            Files.copy(indexedFile.getPath(), targetDir.resolve(indexedFile.getRelativePath()));

            return BackupResult.builder()
                    .indexedFile(indexedFile)
                    .agent(name())
                    .backupAction(COPY)
                    .status(true)
                    .message("Copied successfully")
                    .build();
        } catch (IOException e) {
            logger.error("Failed to copy video", e);
            return BackupResult.builder()
                    .indexedFile(indexedFile)
                    .agent(name())
                    .backupAction(COPY)
                    .status(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean supports(String mimeType) {
        return mimeType != null && mimeType.startsWith("video/");
    }
}