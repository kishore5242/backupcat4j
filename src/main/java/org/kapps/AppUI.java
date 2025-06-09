package org.kapps;

import org.kapps.utils.LogInitializer;
import org.slf4j.Logger;

public class AppUI {
    private static final Logger logger = LogInitializer.initLogger(AppUI.class);

    public static void main(String[] args) {
        logger.info("Starting UI...");
        AppFX.main(args);
    }
}
