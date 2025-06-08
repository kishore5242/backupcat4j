package org.kapps.utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogInitializer {

    public static Logger initLogger(Class<?> clazz) {
        // Set log file with timestamp once per run
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String logFilePath = "logs/backupcat4j-" + timestamp + ".log";
        System.setProperty("LOG_FILE", logFilePath);

        // Reset and reconfigure Logback context
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        try {
            new ContextInitializer(context).autoConfig();
        } catch (Exception e) {
            System.err.println("Failed to initialize logs - " + e.getMessage());
        }

        return LoggerFactory.getLogger(clazz);
    }
}
