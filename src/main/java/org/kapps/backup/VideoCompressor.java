package org.kapps.backup;

import org.kapps.utils.BackupUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VideoCompressor {

    private static final Logger logger = LoggerFactory.getLogger(VideoCompressor.class);

    private static final Set<String> TARGET_CODECS = Set.of("h264", "hevc", "vp9");

    public boolean isAlreadyCompressed(File inputFile, Map<String, String> metadata, BackupOptions backupOptions) {
        try {
            String codec = metadata.get("codec_name");
            String durationStr = metadata.get("duration");
            long durationSeconds = (durationStr != null) ? Math.round(Double.parseDouble(durationStr)) : 0;
            long fileSizeBytes = inputFile.length();
            long avgBitrate = (durationSeconds > 0)
                    ? (fileSizeBytes * 8 / durationSeconds) // bits/sec
                    : 0;

            logger.info("Codec: {}, Duration: {}s, File Size: {} bytes, Avg Bitrate: {} bps",
                    codec, durationSeconds, fileSizeBytes, avgBitrate);

            boolean codecOk = TARGET_CODECS.contains(codec);
            boolean bitrateOk = avgBitrate > 0 && avgBitrate <= backupOptions.getMaxAvgBitRate();

            return codecOk && bitrateOk;

        } catch (Exception e) {
            logger.error("Failed to analyze video: {}", e.getMessage(), e);
            return false; // fallback to re-encode
        }
    }

    public Map<String, String> probeVideo(File inputFile, BackupOptions backupOptions) {

        String ffprobePath = resolveFfprobePath(backupOptions);

        ProcessBuilder pb = new ProcessBuilder(
                ffprobePath,
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=codec_name",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=0",
                inputFile.getAbsolutePath()
        );

        Map<String, String> result = new HashMap<>();
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));


            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    result.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            logger.error("Video probing failed", e);
        }
        return result;
    }

    public String compressVideo(File inputFile, File outputFile, Map<String, String> metadata, BackupOptions backupOptions) {
        logger.info("compressing video...");

        String durationStr = metadata.get("duration");
        long totalDuration = (durationStr != null) ? Math.round(Double.parseDouble(durationStr)) : 0;

        String ffmpegPath = resolveFfmpegPath(backupOptions);

        String[] command = {
                ffmpegPath,
                "-y",  // overwrite without prompt
                "-i", inputFile.getAbsolutePath(),
                "-vcodec", "libx264",
                "-crf", "30",             // reasonable compression
                "-preset", "ultrafast",   // fast
                "-acodec", "copy",        // skip audio encoding
                outputFile.getAbsolutePath()
        };

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true); // merge stdout and stderr

        try {
            Process process = builder.start();

            // Important: Read the output stream to avoid blocking
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;

                long startTimeMillis = System.currentTimeMillis();
                Pattern timePattern = Pattern.compile("time=([\\d:.]+)");
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = timePattern.matcher(line);
                    if (matcher.find()) {
                        String timeStr = matcher.group(1);
                        double processedSeconds = parseTimeToSeconds(timeStr);
                        double percent = (processedSeconds / totalDuration) * 100;

                        long now = System.currentTimeMillis();
                        double elapsedSeconds = (now - startTimeMillis) / 1000.0;

                        // Avoid division by zero
                        if (processedSeconds > 0) {
                            double estimatedTotalTime = (elapsedSeconds / processedSeconds) * totalDuration;
                            double remainingTime = estimatedTotalTime - elapsedSeconds;

                            String remainingStr = BackupUtils.formatSecondsToHHMMSS(remainingTime);
                            System.out.printf("\r[ ETA: %s ] [ %3.0f%% ]", remainingStr, percent);
                            System.out.flush();
                        }
                    }
                }
                System.out.println(" ✓");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Video compression successful");
                return "";
            } else {
                String error = String.format("Video compression failed with exit code: %s", exitCode);
                logger.error(error);
                return error;
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Error during video compression", e);
            Thread.currentThread().interrupt();
            return String.format("Error during video compression - %s", e.getMessage());
        }
    }

    private String resolveFfmpegPath(BackupOptions options) {
        if (options.getFfmpeg() != null && !options.getFfmpeg().isBlank()) {
            return options.getFfmpeg();
        }

        String ffmpegBinary = isWindows() ? "ffmpeg.exe" : "ffmpeg";
        String ffmpegPath = Paths.get("ffmpeg", ffmpegBinary).toString();

        File ffmpegFile = new File(ffmpegPath);
        if (!ffmpegFile.exists() || !ffmpegFile.canExecute()) {
            throw new IllegalStateException("FFmpeg not found at " + ffmpegPath);
        }
        return ffmpegPath;
    }

    private String resolveFfprobePath(BackupOptions options) {
        if (options.getFfprobe() != null && !options.getFfprobe().isBlank()) {
            return options.getFfprobe();
        }

        String ffprobeBinary = isWindows() ? "ffprobe.exe" : "ffprobe";
        String ffprobePath = Paths.get("ffprobe", ffprobeBinary).toString();

        File ffprobeFile = new File(ffprobePath);
        if (!ffprobeFile.exists() || !ffprobeFile.canExecute()) {
            throw new IllegalStateException("FFprobe not found at " + ffprobePath);
        }
        return ffprobePath;
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private double parseTimeToSeconds(String timeStr) {
        String[] parts = timeStr.split(":");
        double hours = Double.parseDouble(parts[0]);
        double minutes = Double.parseDouble(parts[1]);
        double seconds = Double.parseDouble(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }
}
