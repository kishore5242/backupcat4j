package org.kapps;

import ch.qos.logback.classic.LoggerContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.kapps.backup.BackupOptions;
import org.kapps.backup.BackupService;
import org.kapps.backup.OrganizeMode;
import org.kapps.progress.TextAreaAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class BackupController {

    private static final Logger logger = LoggerFactory.getLogger(BackupController.class);

    @Autowired
    private BackupService backupService;

    @FXML
    private Button backupButton;

    @FXML
    private Button destinationButton;

    @FXML
    private TextField destinationInput;

    @FXML
    private Button sourceButton;

    @FXML
    private TextField sourceInput;

    @FXML
    private CheckBox compressCheckBox;

    @FXML
    private TextField maxBitRate;

    @FXML
    private ChoiceBox<String> organizeChoiceBox;

    @FXML
    private CheckBox skipOthers;

    @FXML
    private TextArea consoleArea;

    @FXML
    public void initialize() {
        organizeChoiceBox.getItems().addAll("File type", "First folder and File type", "None");
        organizeChoiceBox.setValue("File type");
        compressCheckBox.setSelected(true);
        maxBitRate.setDisable(false);
        maxBitRate.setText("3000000");

        addErrorClearingListener(sourceInput);
        addErrorClearingListener(destinationInput);
        addErrorClearingListener(maxBitRate);

        // Initiate console log
        TextAreaAppender appender = new TextAreaAppender();
        appender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        appender.start();
        ch.qos.logback.classic.Logger rootLogger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(appender);
        TextAreaAppender.setTextArea(consoleArea, progressBar);
    }

    @FXML
    void browseDestination(MouseEvent event) {
        Stage stage = getStage(event);
        // Browse
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Destination Directory");
        File selectedDirectory = directoryChooser.showDialog(stage);
        // set
        destinationInput.setText(selectedDirectory.getAbsolutePath());
    }

    @FXML
    void browseSource(MouseEvent event) {
        Stage stage = getStage(event);
        // Browse
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Source Directory");
        File selectedDirectory = directoryChooser.showDialog(stage);
        // set
        sourceInput.setText(selectedDirectory.getAbsolutePath());
    }

    @FXML
    void onCompressAction(ActionEvent event) {
        maxBitRate.setDisable(!compressCheckBox.isSelected());
    }

    @FXML
    private ProgressBar progressBar;

    @FXML
    void submit(MouseEvent event) {
        String source = sourceInput.getText();
        String destination = destinationInput.getText();
        if (!StringUtils.hasLength(source)) {
            consoleArea.setText("ERROR: Source not selected");
            return;
        } else if (!StringUtils.hasLength(destination)) {
            consoleArea.setText("ERROR: Destination not selected");
            return;
        }

        boolean compressVideos = compressCheckBox.isSelected();
        long maxAvgBitRate;
        try {
            maxAvgBitRate = Long.parseLong(maxBitRate.getText());
        } catch (Exception e) {
            consoleArea.setText("ERROR: Maximum bitrate is not a number - " + maxBitRate.getText());
            return;
        }

        OrganizeMode organizeMode;
        switch (organizeChoiceBox.getValue()) {
            case "File type" -> organizeMode = OrganizeMode.FULL;
            case "First folder and File type" -> organizeMode = OrganizeMode.IGNORING_FIRST_FOLDER;
            default -> organizeMode = OrganizeMode.NONE;
        }
        boolean skipOtherFileTypes = skipOthers.isSelected();

        Path ffmpegPath;
        Path ffprobePath;
        Path currentPath = Paths.get("/").toAbsolutePath();
        if(currentPath.endsWith("bin")) {
            ffmpegPath = Paths.get("ffmpeg", "ffmpeg.exe").toAbsolutePath();
            ffprobePath = Paths.get("ffmpeg", "ffprobe.exe").toAbsolutePath();
        } else {
            ffmpegPath = Paths.get("bin/ffmpeg", "ffmpeg.exe").toAbsolutePath();
            ffprobePath = Paths.get("bin/ffmpeg", "ffprobe.exe").toAbsolutePath();
        }

        if (!Files.exists(ffmpegPath)) {
            consoleArea.setText("ERROR: ffmpeg missing at " + ffmpegPath);
            return;
        } else if (!Files.exists(ffprobePath)) {
            consoleArea.setText("ERROR: ffprobe missing at " + ffprobePath);
            return;
        }
        logger.info("ffmpeg: {}", ffmpegPath);
        logger.info("ffprobe: {}", ffprobePath);

        BackupOptions backupOptions = BackupOptions.builder()
                .source(source)
                .target(destination)
                .replace(true)
                .ffmpeg(ffmpegPath.toString())
                .ffprobe(ffprobePath.toString())
                .compressVideos(compressVideos)
                .maxAvgBitRate(maxAvgBitRate)
                .organize(organizeMode)
                .skipOthers(skipOtherFileTypes)
                .resume(true)
                .build();

        consoleArea.clear();
        backupButton.setDisable(true);
        consoleArea.requestFocus();
        // Backup
        new Thread(() -> {
            try {
                backupService.backupFiles(backupOptions);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                backupButton.setDisable(false);
            }
        }).start();
    }

    private void addErrorClearingListener(TextField field) {
        field.textProperty().addListener((obs, oldText, newText) -> {
            consoleArea.setText("");
            field.setStyle(""); // optional: clear red border
        });
    }

    private static Stage getStage(MouseEvent event) {
        // Get the current stage from any UI element (replace with your actual Node if needed)
        return (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
    }

    private void disableInputs() {
        backupButton.setDisable(true);
    }
}
