package org.kapps.utils;

import org.kapps.backup.BackupOptions;
import org.kapps.backup.OrganizeMode;
import org.kapps.index.IndexedFile;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class BackupUtils {

    public static Path getDefaultTargetPath(IndexedFile indexedFile, BackupOptions backupOptions) {
        Path sourceDir = Paths.get(backupOptions.getSource()).normalize();
        Path targetDir = Paths.get(backupOptions.getTarget()).normalize();
        Path sourcePath = indexedFile.getPath();
        Path targetPath = targetDir.resolve(indexedFile.getRelativePath()); // default value

        if (backupOptions.getOrganize().equals(OrganizeMode.FULL)) {
            targetPath = Paths.get(
                    backupOptions.getTarget(),
                    indexedFile.getFileType().name(),
                    sourcePath.getFileName().toString()
            );
        } else if (backupOptions.getOrganize().equals(OrganizeMode.IGNORING_FIRST_FOLDER)) {
            Path relativePath = sourceDir.relativize(sourcePath);

            if (relativePath.getNameCount() > 1) {
                // Extract first folder
                String firstFolder = relativePath.getName(0).toString();
                targetPath = Paths.get(
                        targetDir.toString(),
                        firstFolder,
                        indexedFile.getFileType().name()
                ).resolve(sourcePath.getFileName());

            } else if (relativePath.getNameCount() == 1) {
                targetPath = Paths.get(
                        targetDir.toString(),
                        "others", // as first folder
                        indexedFile.getFileType().name()
                ).resolve(relativePath.getName(0).toString());
            }
        }

        // add suffix if present
        if (StringUtils.hasLength(indexedFile.getSuffix())) {
            targetPath = FileUtils.addSuffixToFile(targetPath, indexedFile.getSuffix());
        }

        return targetPath;
    }

    public static Path addSuffixToFileName(Path originalPath, String suffix) {
        Path parent = originalPath.getParent();
        String fileName = originalPath.getFileName().toString();

        int dotIndex = fileName.lastIndexOf('.');
        String name = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        String extension = (dotIndex == -1) ? "" : fileName.substring(dotIndex);

        return parent.resolve(name + suffix + extension);
    }
}
