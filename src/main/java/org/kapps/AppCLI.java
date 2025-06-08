package org.kapps;

import org.kapps.utils.LogInitializer;
import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AppCLI {

    private static final Logger logger = LogInitializer.initLogger(AppCLI.class);

    public static void main(String[] args) {
        // Boot the Spring context
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("org.kapps")) {
            // Create the CLI executor with the context
            BackupCLIExecutor executor = new BackupCLIExecutor(context);
            // Run the actual logic
            executor.run(args);
        } catch (Exception e) {
            logger.error("Backup process failed", e);
            System.exit(1);
        }
    }
}
