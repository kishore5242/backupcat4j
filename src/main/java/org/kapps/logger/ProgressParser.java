package org.kapps.logger;

public class ProgressParser {

    public static Integer parseLogLine(String logLine) {
        System.out.println("+++++++" + logLine);
        if (logLine != null && logLine.contains("-Progress-")) {
            String[] split = logLine.split("\\|");
            System.out.println("Progress ? " + split[1]);
            return (int) Double.parseDouble(split[1]);
        }
        return null;
    }
}
