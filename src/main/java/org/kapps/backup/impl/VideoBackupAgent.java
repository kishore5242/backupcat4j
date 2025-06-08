package org.kapps.backup.impl;

import org.kapps.backup.BackupAgent;
import org.kapps.backup.BackupOptions;
import org.kapps.backup.BackupResult;
import org.kapps.backup.VideoCompressor;
import org.kapps.index.IndexedFile;
import org.kapps.utils.BackupUtils;
import org.kapps.utils.FileUtils;
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

import static org.kapps.backup.BackupAction.*;

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
    public BackupResult backup(IndexedFile indexedFile, BackupOptions backupOptions) {
        logger.info("Backing up video: {}", indexedFile.getPath());

        BackupResult.Builder resultBuilder = BackupResult.builder()
                .indexedFile(indexedFile)
                .agent(name());

        // calculate the destination path
        Path targetPath = BackupUtils.getDefaultTargetPath(indexedFile, backupOptions);
        File sourceFile = indexedFile.getPath().toFile();

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

        if (Files.exists(targetPath) && !backupOptions.isReplace()) {
            logger.info("Skipping as the file already exists");
            return resultBuilder
                    .backupAction(SKIP)
                    .status(true)
                    .message("File already exists")
                    .build();
        } else if (backupOptions.isReplace()) {
            // First delete the target file
            FileUtils.silentDelete(targetPath);
        }

        if (backupOptions.isCompressVideos()) {
            // check if compression is required
            Map<String, String> metadata = videoCompressor.probeVideo(sourceFile, backupOptions);
            if (!videoCompressor.isAlreadyCompressed(sourceFile, metadata, backupOptions)) {
                // compress
                String error = videoCompressor.compressVideo(sourceFile, targetPath.toFile(), metadata, backupOptions);
                if (StringUtils.hasLength(error)) {
                    // delete file at the destination if was created
                    FileUtils.silentDelete(targetPath);
                    // so that copy will be attempted next
                } else {
                    return resultBuilder
                            .backupAction(COMPRESS)
                            .status(true)
                            .message("Video compressed successfully")
                            .build();
                }
            }
        }

        // copy as it is
        try {
            logger.info("Copying the video as it is..");
            Files.copy(indexedFile.getPath(), targetPath);
            return resultBuilder
                    .backupAction(COPY)
                    .status(true)
                    .message("Copied successfully")
                    .build();
        } catch (IOException e) {
            logger.error("Failed to copy video", e);
            return resultBuilder
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