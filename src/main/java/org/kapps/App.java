package org.kapps;

import org.kapps.backup.BackupService;
import org.kapps.index.IndexedFile;
import org.kapps.index.FileIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("org.kapps");

            File source = new File("D:\\projects\\temp\\mix");
            File target = new File("D:\\projects\\temp\\destination");

            // Index
            List<IndexedFile> indexedFiles = FileIndexer.indexFiles(source.toPath());
            FileIndexer.logFileCountsByMime(indexedFiles);

            // Backup
            BackupService service = context.getBean(BackupService.class);
            service.backupFiles(indexedFiles, target.toPath());

        } catch (IOException e) {
            logger.error("Process failed", e);
        }
    }
}
