package org.kapps;

import org.kapps.backup.BackupOptions;
import org.kapps.backup.BackupService;
import org.kapps.backup.OrganizeMode;
import org.kapps.utils.LogInitializer;
import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

public class App {

    private static final Logger logger = LogInitializer.initLogger(App.class);

    public static void main(String[] args) {
        try {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("org.kapps");

            BackupOptions backupOptions = BackupOptions.builder()
                    .source("D:\\projects\\temp\\mix")
                    .target("D:\\projects\\temp\\destination")
                    .replace(true)
                    .organize(OrganizeMode.FULL)
                    .compressVideos(true)
                    .maxAvgBitRate(3_000_000)
                    .ffmpeg("D:\\apps\\ffmpeg-7.1.1-essentials_build\\bin\\ffmpeg.exe")
                    .ffprobe("D:\\apps\\ffmpeg-7.1.1-essentials_build\\bin\\ffprobe.exe")
                    .skipOthers(false)
                    .resume(true)
                    .build();

            // Backup
            BackupService service = context.getBean(BackupService.class);
            service.backupFiles(backupOptions);

        } catch (IOException e) {
            logger.error("Process failed", e);
        }
    }
}
