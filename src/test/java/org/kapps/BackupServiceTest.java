package org.kapps;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kapps.backup.*;
import org.kapps.index.FileIndexer;
import org.kapps.index.IndexedFile;
import org.kapps.utils.Directories;
import org.kapps.utils.FileUtils;
import org.kapps.utils.TestUtils;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
public class BackupServiceTest {

    @Autowired
    BackupService backupService;

    @MockitoBean
    VideoCompressor videoCompressor;

    @BeforeEach
    void beforeEach() {
        Mockito.when(videoCompressor.isAlreadyCompressed(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);
        Mockito.when(videoCompressor.probeVideo(Mockito.any(), Mockito.any()))
                .thenReturn(Map.of("codec", "video/mp4"));
        Mockito.when(videoCompressor.compressVideo(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn("");
    }

    @Test
    void testSimpleBackup() throws IOException {
        Directories result = TestUtils.extractZipToTemp("src/test/resources/samples/simple.zip");
        Path source = result.sourceDir();
        Path target = result.targetDir();
        try {
            BackupOptions backupOptions = BackupOptions.builder()
                    .source(source.toString())
                    .target(target.toString())
                    .replace(true)
                    .organize(OrganizeMode.NONE)
                    .compressVideos(true)
                    .maxAvgBitRate(1_000_000)
                    .skipOthers(false)
                    .build();

            backupService.backupFiles(backupOptions);

            // Asserts
            assertTrue(Files.exists(Paths.get(target.toString(), "media/mix/pineapple.mp4")));
            assertTrue(Files.exists(Paths.get(target.toString(), "media/pics/mr.serious.jpg")));
            assertTrue(Files.exists(Paths.get(target.toString(), "lorem_doc.docx")));
            assertTrue(Files.exists(Paths.get(target.toString(), "media/message.json")));

        } finally {
            FileUtils.deleteRecursively(result.tempBaseDir());
        }

    }

    @Test
    void testSkipOther() throws IOException {
        Directories result = TestUtils.extractZipToTemp("src/test/resources/samples/simple.zip");
        Path source = result.sourceDir();
        Path target = result.targetDir();
        try {
            BackupOptions backupOptions = BackupOptions.builder()
                    .source(source.toString())
                    .target(target.toString())
                    .replace(true)
                    .organize(OrganizeMode.NONE)
                    .compressVideos(true)
                    .maxAvgBitRate(1_000_000)
                    .skipOthers(true)
                    .build();

            backupService.backupFiles(backupOptions);

            // Asserts
            assertTrue(Files.exists(Paths.get(target.toString(), "media/mix/pineapple.mp4")));
            assertTrue(Files.exists(Paths.get(target.toString(), "media/pics/mr.serious.jpg")));
            assertTrue(Files.exists(Paths.get(target.toString(), "lorem_doc.docx")));
            assertFalse(Files.exists(Paths.get(target.toString(), "media/message.json")));

        } finally {
            FileUtils.deleteRecursively(result.tempBaseDir());
        }
    }

    @Test
    void testOrganizeFull() throws IOException {
        Directories result = TestUtils.extractZipToTemp("src/test/resources/samples/simple.zip");
        Path source = result.sourceDir();
        Path target = result.targetDir();
        try {
            BackupOptions backupOptions = BackupOptions.builder()
                    .source(source.toString())
                    .target(target.toString())
                    .replace(true)
                    .organize(OrganizeMode.FULL)
                    .compressVideos(true)
                    .maxAvgBitRate(1_000_000)
                    .skipOthers(false)
                    .build();

            backupService.backupFiles(backupOptions);

            // Asserts
            assertTrue(Files.exists(Paths.get(target.toString(), "Videos/pineapple.mp4")));
            assertTrue(Files.exists(Paths.get(target.toString(), "Images/mr.serious.jpg")));
            assertTrue(Files.exists(Paths.get(target.toString(), "Documents/lorem_doc.docx")));
            assertTrue(Files.exists(Paths.get(target.toString(), "Others/message.json")));

        } finally {
            FileUtils.deleteRecursively(result.tempBaseDir());
        }
    }

    @Test
    void testOrganizeClashes() throws IOException {
        Directories result = TestUtils.extractZipToTemp("src/test/resources/samples/clash.zip");
        Path source = result.sourceDir();
        Path target = result.targetDir();
        try {
            BackupOptions backupOptions = BackupOptions.builder()
                    .source(source.toString())
                    .target(target.toString())
                    .replace(true)
                    .organize(OrganizeMode.FULL)
                    .compressVideos(true)
                    .maxAvgBitRate(1_000_000)
                    .skipOthers(false)
                    .build();

            backupService.backupFiles(backupOptions);

            // Asserts
            assertTrue(Files.exists(Paths.get(target.toString(), "Videos/space.mp4")));
            assertTrue(Files.exists(Paths.get(target.toString(), "Videos/space_1.mp4")));
            assertTrue(Files.exists(Paths.get(target.toString(), "Videos/space_2.mp4")));
            assertTrue(Files.exists(Paths.get(target.toString(), "Images/kitty.jpg")));
            assertTrue(Files.exists(Paths.get(target.toString(), "Images/kitty_1.jpg")));
            assertTrue(Files.exists(Paths.get(target.toString(), "Images/kitty_2.jpg")));

        } finally {
            FileUtils.deleteRecursively(result.tempBaseDir());
        }
    }

    @Test
    void testOrganizeFirstFolders() throws IOException {
        Directories result = TestUtils.extractZipToTemp("src/test/resources/samples/simple.zip");
        Path source = result.sourceDir();
        Path target = result.targetDir();
        try {
            BackupOptions backupOptions = BackupOptions.builder()
                    .source(source.toString())
                    .target(target.toString())
                    .replace(true)
                    .organize(OrganizeMode.IGNORING_FIRST_FOLDER)
                    .compressVideos(true)
                    .maxAvgBitRate(1_000_000)
                    .skipOthers(false)
                    .build();

            backupService.backupFiles(backupOptions);

            // Asserts
            assertTrue(Files.exists(Paths.get(target.toString(), "media/VIDEO/pineapple.mp4")));
            assertTrue(Files.exists(Paths.get(target.toString(), "media/IMAGE/mr.serious.jpg")));
            assertTrue(Files.exists(Paths.get(target.toString(), "media/OTHER/message.json")));
            assertTrue(Files.exists(Paths.get(target.toString(), "others/DOCUMENT/lorem_doc.docx")));

        } finally {
            FileUtils.deleteRecursively(result.tempBaseDir());
        }
    }

    @Test
    void testResume() throws IOException {
        Directories result = TestUtils.extractZipToTemp("src/test/resources/samples/simple.zip");
        Path source = result.sourceDir();
        Path target = result.targetDir();
        try {
            BackupOptions backupOptions = BackupOptions.builder()
                    .source(source.toString())
                    .target(target.toString())
                    .replace(true)
                    .organize(OrganizeMode.FULL)
                    .compressVideos(true)
                    .maxAvgBitRate(1_000_000)
                    .skipOthers(false)
                    .resume(true)
                    .build();

            // Create a last run file
            List<IndexedFile> indexedFiles = FileIndexer.indexFiles(backupOptions);
            ProgressTracker progressTracker = new ProgressTracker(indexedFiles);
            progressTracker.reset();

            // complete 4 files
            int lastIndex = 4;
            for (int i = 0; i < lastIndex; i++) {
                progressTracker.appendResult(BackupResult.builder()
                        .indexedFile(indexedFiles.get(i))
                        .status(true)
                        .backupAction(BackupAction.BACKUP)
                        .agent("junit")
                        .build());
            }

            backupService.backupFiles(backupOptions);

            // previously processed
            assertFalse(Files.exists(Paths.get(target.toString(), "Others/message.json")));
            assertFalse(Files.exists(Paths.get(target.toString(), "Others/lorem_doc.docx")));
            // resumed
            assertTrue(Files.exists(Paths.get(target.toString(), "Videos/pineapple.mp4")));
            assertTrue(Files.exists(Paths.get(target.toString(), "Images/mr.serious.jpg")));
            assertTrue(Files.exists(Paths.get(target.toString(), "Others/hello.txt")));

        } finally {
            FileUtils.deleteRecursively(result.tempBaseDir());
        }
    }
}
