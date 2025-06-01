package org.kapps.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TestUtils {

    public static Directories extractZipToTemp(String path) throws IOException {
        // 1. Epoch timestamp
        long epoch = System.currentTimeMillis();

        // 2. Create temp base dir (optional but clean)
        Path tempBaseDir = Files.createTempDirectory("test-run-");

        // 3. Define source and target folders
        Path sourceDir = tempBaseDir.resolve("source-" + epoch);
        Path targetDir = tempBaseDir.resolve("target-" + epoch);

        Files.createDirectories(sourceDir);
        Files.createDirectories(targetDir);

        // 4. Load zip file from resources
        Path zipPath = Paths.get(path);
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path outPath = sourceDir.resolve(entry.getName()).normalize();
                if (!outPath.startsWith(sourceDir)) {
                    throw new IOException("Zip entry is outside target dir: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    Files.copy(zis, outPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        return new Directories(tempBaseDir, sourceDir, targetDir);
    }
}
