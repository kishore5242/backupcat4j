package org.kapps.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class VideoCompressor {

    private static final Logger logger = LoggerFactory.getLogger(VideoCompressor.class);

    private static String ffmpeg = "D:\\apps\\ffmpeg-7.1.1-essentials_build\\bin\\ffmpeg.exe";
    private static String ffprobe = "D:\\apps\\ffmpeg-7.1.1-essentials_build\\bin\\ffprobe.exe";

    private static final Set<String> TARGET_CODECS = Set.of("h264", "hevc", "vp9");
    private static final long MAX_AVG_BITRATE = 1_500_000;


    public boolean isAlreadyCompressed(File inputFile, Map<String, String> metadata) {
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
            boolean bitrateOk = avgBitrate > 0 && avgBitrate <= MAX_AVG_BITRATE;

            return codecOk && bitrateOk;

        } catch (Exception e) {
            logger.error("Failed to analyze video: {}", e.getMessage(), e);
            return false; // fallback to re-encode
        }
    }

    public Map<String, String> probeVideo(File inputFile) {
        ProcessBuilder pb = new ProcessBuilder(
                ffprobe,
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

    public String compressVideo(File inputFile, File outputFile) {
        logger.info("compressing video...");
        String[] command = {
                ffmpeg,
                "-y",  // overwrite without prompt
                "-i", inputFile.getAbsolutePath(),
                "-vcodec", "libx264",
                "-crf", "28",             // reasonable compression
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
                while ((line = reader.readLine()) != null) {
                    logger.debug("ffmpeg: {}", line);
                }
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
}
