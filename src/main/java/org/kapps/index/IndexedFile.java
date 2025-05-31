package org.kapps.index;

import java.nio.file.Path;
import java.util.Objects;

public class IndexedFile implements Comparable<IndexedFile> {
    private final long size;
    private final long lastModified;
    private final Path path;
    private final String mimeType;
    private final String relativePath;

    private IndexedFile(Builder builder) {
        this.size = builder.size;
        this.lastModified = builder.lastModified;
        this.path = builder.path;
        this.mimeType = builder.mimeType;
        this.relativePath = builder.relativePath;
    }

    // Getters
    public long getSize() {
        return size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public Path getPath() {
        return path;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getRelativePath() {
        return relativePath;
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

    // Builder
    public static class Builder {
        private long size;
        private long lastModified;
        private Path path;
        private String mimeType;
        private String relativePath;

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

        public IndexedFile build() {
            return new IndexedFile(this);
        }
    }
}
