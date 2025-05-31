package org.kapps.backup;

import org.kapps.index.IndexedFile;

import java.nio.file.Path;

public interface BackupAgent {
    String name();

    BackupResult backup(IndexedFile indexedFile, Path targetRoot);

    boolean supports(String mimeType);
}