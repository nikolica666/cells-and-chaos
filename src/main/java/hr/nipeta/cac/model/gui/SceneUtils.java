package hr.nipeta.cac.model.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import java.util.function.Consumer;

public class SceneUtils {

    /**
     * @param text Button text
     * @param actionEventHandler Action after button is clicked
     */
    public static Button createButton(String text, EventHandler<ActionEvent> actionEventHandler) {
        Button button = new Button(text);
        button.setOnAction(actionEventHandler);
        return button;
    }

    /**
     * Creates simple tooltip for given text
     * <p>ShowDelay and HideDelay are both set to 0</p>
     *
     * @param text Tooltip text
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

    public static TextField createInput(Object prompt, double prefWidth, Tooltip tooltip, Consumer<TextField> onEnterPressed) {
        TextField input = createInput(prompt, prefWidth, tooltip);
        input.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (!input.getText().isEmpty()) {
                    onEnterPressed.accept(input);
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
        alert.initStyle(javafx.stage.StageStyle.UNDECORATED);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().addAll(
                SceneUtils.class.getResource("/css/font-lemonmilk.css").toExternalForm(),
                SceneUtils.class.getResource("/css/main.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add(".dialog-pane");
        alert.showAndWait();
    }

}
