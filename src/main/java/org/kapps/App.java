package org.kapps;

import org.kapps.backup.BackupOptions;
import org.kapps.backup.BackupService;
import org.kapps.index.FileIndexer;
import org.kapps.index.IndexedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.List;

import static org.kapps.backup.FileType.OTHER;

/**
 * Hello world!
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("org.kapps");

            BackupOptions backupOptions = BackupOptions.builder()
                    .source("D:\\backup\\oldphone")
                    .target("D:\\backup\\backup-testing")
                    .replace(false)
                    .organize(true)
                    .compressVideos(true)
                    .maxAvgBitRate(1_500_000)
                    .ffmpeg("D:\\apps\\ffmpeg-7.1.1-essentials_build\\bin\\ffmpeg.exe")
                    .ffprobe("D:\\apps\\ffmpeg-7.1.1-essentials_build\\bin\\ffprobe.exe")
                    .skipOthers(true)
                    .build();

            // Index
            List<IndexedFile> indexedFiles = FileIndexer.indexFiles(backupOptions.getSource());
            FileIndexer.logFileCountsByFileType(indexedFiles);
            FileIndexer.logFileCountsByMime(indexedFiles);

            // Filter/Skip
            if (backupOptions.isSkipOthers()) {
                indexedFiles = indexedFiles.stream()
                        .filter(file -> !file.getFileType().equals(OTHER)).toList();
            }

            // Backup
            BackupService service = context.getBean(BackupService.class);
            service.backupFiles(indexedFiles, backupOptions);

        } catch (IOException e) {
            logger.error("Process failed", e);
        }
    }
}
