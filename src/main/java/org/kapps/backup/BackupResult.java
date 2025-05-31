package org.kapps.backup;

import org.kapps.index.IndexedFile;

public class BackupResult {
    private final IndexedFile indexedFile;
    private final BackupAction backupAction;
    private final Boolean status;
    private final String error;
    private final String agent;

    public BackupResult(IndexedFile indexedFile, String agent, BackupAction backupAction) {
        this.indexedFile = indexedFile;
        this.backupAction = backupAction;
        this.status = true;
        this.error = null;
        this.agent = agent;
    }

    public BackupResult(IndexedFile indexedFile, String agent, BackupAction backupAction, String error) {
        this.indexedFile = indexedFile;
        this.backupAction = backupAction;
        this.status = false;
        this.error = error;
        this.agent = agent;
    }

    public BackupAction getBackupAction() {
        return backupAction;
    }

    public Boolean getStatus() {
        return status;
    }

    public IndexedFile getIndexedFile() {
        return indexedFile;
    }

    public String getError() {
        return error;
    }

    public String getAgent() {
        return agent;
    }
}
