package org.kapps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kapps.backup.VideoCompressor;
import org.kapps.utils.Directories;
import org.kapps.utils.FileUtils;
import org.kapps.utils.TestUtils;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

//@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(AppConfig.class)
public class BackupCLIExecutorTest {

    @Autowired
    ApplicationContext context;

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
    void testCLIRunWithValidArguments() throws Exception {
        Directories result = TestUtils.extractZipToTemp("src/test/resources/samples/simple.zip");
        Path source = result.sourceDir();
        Path target = result.targetDir();
        try {
            // Construct CLI arguments
            String[] args = {
                    "source=" + source.toString(),
                    "target=" + target.toString(),
                    "ffmpeg=D:\\apps\\ffmpeg\\bin\\ffmpeg.exe",
                    "ffprobe=D:\\apps\\ffmpeg\\bin\\ffprobe.exe",
                    "organize=NONE",
                    "--replace",
                    "--compress",
                    "--resume",
                    "--bitrate=500000"
            };

            // Execute
            new BackupCLIExecutor(context).run(args);

            // Verify results
            assertTrue(Files.exists(target.resolve("media/mix/pineapple.mp4")));
            assertTrue(Files.exists(target.resolve("media/pics/mr.serious.jpg")));
            assertTrue(Files.exists(target.resolve("lorem_doc.docx")));
            assertTrue(Files.exists(target.resolve("media/message.json")));
        } finally {
            FileUtils.deleteRecursively(result.tempBaseDir());
        }
    }
}
