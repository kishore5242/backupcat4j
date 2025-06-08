package org.kapps;

import org.kapps.backup.BackupOptions;
import org.kapps.backup.BackupService;
import org.kapps.backup.OrganizeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BackupCLIExecutor {

    private static final Logger logger = LoggerFactory.getLogger(BackupCLIExecutor.class);

    private final ApplicationContext context;

    public BackupCLIExecutor(ApplicationContext context) {
        this.context = context;
    }

    public void run(String[] args) throws IOException {
        if (args.length == 0 || Arrays.asList(args).contains("--help")) {
            printUsage();
            return;
        }

        Map<String, String> options = parseKeyValueArgs(args);
        boolean replace = hasFlag(args, "--replace");
        boolean compress = hasFlag(args, "--compress");
        boolean resume = hasFlag(args, "--resume");
        boolean skipOthers = hasFlag(args, "--skip-others");

        String source = options.get("source");
        String target = options.get("target");
        String ffmpeg = options.get("ffmpeg");
        String ffprobe = options.get("ffprobe");

        if (source == null || target == null || ffmpeg == null || ffprobe == null) {
            throw new IllegalArgumentException("source, target, ffmpeg, and ffprobe must be specified.");
        }

        int bitrate = parseBitRate(args);
        OrganizeMode organize = parseOrganizeMode(options.get("organize"));

        BackupOptions backupOptions = BackupOptions.builder()
                .source(source)
                .target(target)
                .ffmpeg(ffmpeg)
                .ffprobe(ffprobe)
                .replace(replace)
                .compressVideos(compress)
                .resume(resume)
                .skipOthers(skipOthers)
                .organize(organize)
                .maxAvgBitRate(bitrate)
                .build();

        BackupService service = context.getBean(BackupService.class);
        service.backupFiles(backupOptions);
    }


    private void printUsage() {
        System.out.println("Usage: java -jar backupcat4j.jar <source> <target> <ffmpeg_path> <ffprobe_path> [options]");
        System.out.println("Options:");
        System.out.println("  --replace            Replace existing files in target");
        System.out.println("  --compress           Compress video files");
        System.out.println("  --bitrate=<bps>      Max average video bitrate in bits per second (default: 3000000)");
        System.out.println("  --resume             Resume backup from last point if interrupted");
        System.out.println("  --skip-others        Skip files not classified as video, image, document, or audio");
        System.out.println("  --help               Show this help message");
    }

    private Map<String, String> parseKeyValueArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (String arg : args) {
            if (arg.contains("=") && !arg.startsWith("--")) {
                String[] kv = arg.split("=", 2);
                if (kv.length == 2) {
                    map.put(kv[0].trim().toLowerCase(), kv[1].trim());
                }
            }
        }
        return map;
    }

    private boolean hasFlag(String[] args, String flag) {
        return Arrays.asList(args).contains(flag);
    }

    private int parseBitRate(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--bitrate=")) {
                try {
                    return Integer.parseInt(arg.substring("--bitrate=".length()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid bitrate value.");
                }
            }
        }
        return 3_000_000; // default
    }

    private OrganizeMode parseOrganizeMode(String mode) {
        if (mode == null) return OrganizeMode.NONE;
        try {
            return OrganizeMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid organize mode: " + mode);
        }
    }
}