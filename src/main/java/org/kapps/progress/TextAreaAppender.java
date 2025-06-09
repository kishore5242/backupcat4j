package org.kapps.progress;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

public class TextAreaAppender extends AppenderBase<ILoggingEvent> {
    private static TextArea console;
    private static ProgressBar progressBar;

    public static void setTextArea(TextArea ta, ProgressBar pb) {
        console = ta;
        progressBar = pb;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (console != null) {
            Platform.runLater(() -> {
                String line = eventObject.getFormattedMessage();
                Integer percent = ProgressParser.parseLogLine(line);
                if (percent != null) {
                    double barValue = percent / 100.0;
                    progressBar.setProgress(barValue);
                }
                console.appendText(line + "\n");
            });
        }
    }
}
