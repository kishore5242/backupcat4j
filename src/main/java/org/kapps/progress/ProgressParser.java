package org.kapps.progress;

public class ProgressParser {

    public static Integer parseLogLine(String logLine) {
        if (logLine != null && logLine.contains("-Progress-")) {
            String[] split = logLine.split("\\|");
            return (int) Double.parseDouble(split[1]);
        }
        return null;
    }
}
