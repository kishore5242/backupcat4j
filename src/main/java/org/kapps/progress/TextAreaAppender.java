package org.kapps.progress;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class TextAreaAppender extends AppenderBase<ILoggingEvent> {
    private static TextArea console;

    public static void setTextArea(TextArea ta) {
        console = ta;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (console != null) {
            Platform.runLater(() -> {
                String line = eventObject.getFormattedMessage();
                console.appendText(line + "\n");
            });
        }
    }
}
