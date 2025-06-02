package org.kapps.backup;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.kapps.index.IndexedFile;

@JsonDeserialize(builder = BackupResult.Builder.class)
public class BackupResult {
    private final IndexedFile indexedFile;
    private final BackupAction backupAction;
    private final Boolean status;
    private final String message;
    private final String agent;

    private BackupResult(Builder builder) {
        this.indexedFile = builder.indexedFile;
        this.backupAction = builder.backupAction;
        this.status = builder.status;
        this.message = builder.message;
        this.agent = builder.agent;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private IndexedFile indexedFile;
        private BackupAction backupAction;
        private Boolean status;
        private String message;
        private String agent;

        public Builder indexedFile(IndexedFile indexedFile) {
            this.indexedFile = indexedFile;
            return this;
        }

        public Builder backupAction(BackupAction backupAction) {
            this.backupAction = backupAction;
            return this;
        }

        public Builder status(Boolean status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder agent(String agent) {
            this.agent = agent;
            return this;
        }

        public BackupResult build() {
            return new BackupResult(this);
        }
    }

    public IndexedFile getIndexedFile() {
        return indexedFile;
    }

    public BackupAction getBackupAction() {
        return backupAction;
    }

    public Boolean getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getAgent() {
        return agent;
    }
}

