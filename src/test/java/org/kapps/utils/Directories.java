package org.kapps.utils;

import java.nio.file.Path;

public record Directories(Path tempBaseDir, Path sourceDir, Path targetDir) {
}