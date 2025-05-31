package org.kapps.backup;

public enum BackupAction {
    BACKUP, // Generic unknown action
    COMPRESS,
    COPY,
    SKIP,
}