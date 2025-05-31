package org.kapps.backup;

public class BackupOptions {
    private final String source;
    private final String target;
    private final boolean replace;
    private final boolean organize;
    private final boolean compressVideos;
    private final long maxAvgBitRate;
    private final String ffmpeg;
    private final String ffprobe;
    private final boolean skipOthers;

    private BackupOptions(Builder builder) {
        this.source = builder.source;
        this.target = builder.target;
        this.replace = builder.replace;
        this.compressVideos = builder.compressVideos;
        this.maxAvgBitRate = builder.maxAvgBitRate;
        this.ffmpeg = builder.ffmpeg;
        this.ffprobe = builder.ffprobe;
        this.organize = builder.organize;
        this.skipOthers = builder.skipOthers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String source;
        private String target;
        private boolean replace;
        private boolean compressVideos;
        private long maxAvgBitRate;
        private String ffmpeg;
        private String ffprobe;
        private boolean organize;
        private boolean skipOthers;

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder target(String target) {
            this.target = target;
            return this;
        }

        public Builder replace(boolean replace) {
            this.replace = replace;
            return this;
        }

        public Builder compressVideos(boolean compressVideos) {
            this.compressVideos = compressVideos;
            return this;
        }

        public Builder organize(boolean organize) {
            this.organize = organize;
            return this;
        }

        public Builder maxAvgBitRate(long maxAvgBitRate) {
            this.maxAvgBitRate = maxAvgBitRate;
            return this;
        }

        public Builder ffmpeg(String ffmpeg) {
            this.ffmpeg = ffmpeg;
            return this;
        }

        public Builder ffprobe(String ffprobe) {
            this.ffprobe = ffprobe;
            return this;
        }

        public Builder skipOthers(boolean skipOthers) {
            this.skipOthers = skipOthers;
            return this;
        }

        public BackupOptions build() {
            return new BackupOptions(this);
        }
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public boolean isOrganize() {
        return organize;
    }

    public boolean isReplace() {
        return replace;
    }

    public boolean isCompressVideos() {
        return compressVideos;
    }

    public long getMaxAvgBitRate() {
        return maxAvgBitRate;
    }

    public String getFfmpeg() {
        return ffmpeg;
    }

    public String getFfprobe() {
        return ffprobe;
    }

    public boolean isSkipOthers() {
        return skipOthers;
    }
}

