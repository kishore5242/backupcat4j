package org.kapps;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class FileManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileManager.class);

    /**
     * Copies one directory into another, preserving all subfolders and files.
     *
     * @param sourceDir source directory
     * @param targetDir target directory
     * @throws IOException if something goes wrong
     */
    public static void copyFolder(File sourceDir, File targetDir) throws IOException {
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            LOGGER.error("Source directory does not exist or is not a directory: {}", sourceDir);
            throw new IllegalArgumentException("Invalid source directory");
        }

        LOGGER.info("Starting to copy from {} to {}", sourceDir, targetDir);
        FileUtils.copyDirectory(sourceDir, targetDir);
        LOGGER.info("Copy completed successfully.");
    }
}
