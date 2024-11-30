package hr.nipeta.cac.model.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

public class SceneUtils {

    public static Button createButton(String text, EventHandler<ActionEvent> actionEventHandler) {
        Button button = new Button(text);
        button.setOnAction(actionEventHandler);
        return button;
    }

    /**
     * Creates simple tooltip for given text
     * <p>ShowDelay and HideDelay are both set to 0</p>
     */
    public static Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(0));
        tooltip.setHideDelay(Duration.millis(0));
        return tooltip;
    }

    public static TextField createInput(Object prompt, double prefWidth, Tooltip tooltip, Runnable onEnterPressed) {
        TextField input = createInput(prompt, prefWidth, tooltip);
        input.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (!input.getText().isEmpty()) {
                    onEnterPressed.run();
                }
            }
        });
        return input;
    }

    public static TextField createInput(Object prompt, double prefWidth, Tooltip tooltip) {
        TextField input = new TextField();
        input.setPrefWidth(prefWidth);
        if (prompt != null) {
            input.setPromptText(prompt.toString());
        }
        if (tooltip != null) {
            input.setTooltip(tooltip);
        }
        return input;
    }

    public static void showAlertError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
