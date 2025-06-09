package org.kapps;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kapps.utils.LogInitializer;
import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

public class AppFX extends Application {

    private static final Logger logger = LogInitializer.initLogger(AppFX.class);

    private AnnotationConfigApplicationContext context;

    @Override
    public void init() {
        // Create Spring context using your config class or component scan
        context = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/backup.fxml"));

        // Let Spring manage controllers
        loader.setControllerFactory(context::getBean);

        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.setTitle("BackupCat4j");
        stage.show();
    }

    @Override
    public void stop() {
        context.close();
    }

    public static void main(String[] args) {
        logger.info("Launching JavaFX application...");
        launch();
    }

}
