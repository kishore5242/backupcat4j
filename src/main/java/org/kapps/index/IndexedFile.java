package org.kapps.index;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.kapps.backup.FileType;
import org.kapps.utils.OrganizerUtils;

import java.nio.file.Path;
import java.util.Objects;

@JsonDeserialize(builder = IndexedFile.Builder.class)
public class IndexedFile implements Comparable<IndexedFile> {
    private final Path path;
    private final long size;
    private final long lastModified;
    private final String mimeType;
    private final String relativePath;
    private final String suffix;

    // calculated
    @JsonIgnore
    private final FileType fileType;

    private IndexedFile(Builder builder) {
        this.path = builder.path;
        this.size = builder.size;
        this.lastModified = builder.lastModified;
        this.mimeType = builder.mimeType;
        this.relativePath = builder.relativePath;
        this.fileType = OrganizerUtils.parse(builder.mimeType, builder.path);
        this.suffix = builder.suffix;
    }

    // Getters
    public Path getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public FileType getFileType() {
        return fileType;
    }

    public String getSuffix() {
        return suffix;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IndexedFile that = (IndexedFile) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }

    @Override
    public int compareTo(IndexedFile o) {
        return this.path.compareTo(o.path);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private Path path;
        private long size;
        private long lastModified;
        private String mimeType;
        private String relativePath;
        private String suffix = "";

        public Builder path(Path path) {
            this.path = path;
            return this;
        }

        public Builder size(long size) {
            this.size = size;
            return this;
        }

        public Builder lastModified(long lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder relativePath(String relativePath) {
            this.relativePath = relativePath;
            return this;
        }

        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public IndexedFile build() {
            return new IndexedFile(this);
        }
    }
}
