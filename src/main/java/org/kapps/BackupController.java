package org.kapps;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import org.kapps.backup.BackupOptions;
import org.kapps.backup.BackupService;
import org.kapps.backup.OrganizeMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BackupController {

    @Autowired
    private BackupService backupService;

    @FXML
    void submit(MouseEvent event) {
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
        try {
            backupService.backupFiles(backupOptions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
