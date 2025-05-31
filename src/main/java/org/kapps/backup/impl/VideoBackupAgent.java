package org.kapps.backup.impl;

import org.kapps.backup.BackupAgent;
import org.kapps.backup.BackupOptions;
import org.kapps.backup.BackupResult;
import org.kapps.backup.VideoCompressor;
import org.kapps.index.IndexedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        /// //////////////////////////

        if (backupOptions.isOrganize()) {
            return BackupResult.builder()
                    .indexedFile(indexedFile)
                    .agent(name())
                    .backupAction(SKIP)
                    .status(true)
                    .message("Skipping for testing purpose")
                    .build();
        }

        /// ///////////////////////////


        Path sourcePath = indexedFile.getPath();
        File inputFile = sourcePath.toFile();
        Path targetDir = Paths.get(backupOptions.getTarget());

        // Calculate destination path
        Path targetPath;
        if (backupOptions.isOrganize()) {
            targetPath = Paths.get(
                    backupOptions.getTarget(),
                    indexedFile.getFileType().name(),
                    sourcePath.getFileName().toString()
            );
        } else {
            targetPath = targetDir.resolve(indexedFile.getRelativePath());
        }

        // create target directories
        File targetFile = targetPath.toFile();
        File parentDir = targetFile.getParentFile();
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            logger.error("Failed to create target directory: {}", parentDir.getAbsolutePath());
            return BackupResult.builder()
                    .indexedFile(indexedFile)
                    .agent(name())
                    .backupAction(BACKUP)
                    .status(false)
                    .message("Failed to create target directories")
                    .build();
        }

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

        // compress to destination if requested
        if (backupOptions.isCompressVideos() && !videoCompressor.isAlreadyCompressed(inputFile, metadata)) {
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