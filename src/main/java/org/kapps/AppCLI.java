package org.kapps;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AppCLI {

    public static void main(String[] args) {
        // Boot the Spring context
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("org.kapps")) {
            // Create the CLI executor with the context
            BackupCLIExecutor executor = new BackupCLIExecutor(context);
            // Run the actual logic
            executor.run(args);
        } catch (Exception e) {
            System.err.println("Backup process failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
