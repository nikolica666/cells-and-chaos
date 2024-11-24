package hr.nipeta.cac;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.function.Supplier;

public abstract class SceneBuilder {

    protected Main main;  // Main instance

    public SceneBuilder(Main main) {
        this.main = main;  // Assign the Main instance
    }

    public Scene build() {
        Scene scene = createContent();
        scene.getStylesheets().add(getClass().getResource("/css/main.css").toExternalForm());
        return scene;
    }

    protected abstract Scene createContent();

    protected <T extends SceneBuilder> void createScene(Supplier<T> constructor) {
        Scene scene = constructor.get().build();
        main.getPrimaryStage().setScene(scene);
        center(main.getPrimaryStage());
    }

    protected Button createButton(String text, EventHandler<ActionEvent> actionEventHandler) {
        Button button = new Button(text);
        button.setOnAction(actionEventHandler);
        return button;
    }

    protected void showAlertError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void center(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - stage.getScene().getWidth()) / 2);
        stage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - stage.getScene().getHeight()) / 2);
    }

    protected TextInputControl onTextInputEnter(TextInputControl input, Runnable action) {
        input.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (!input.getText().isEmpty()) {
                    action.run();
                }
            }
        });
        return input;
    }

    protected HBox horizontalMenu(Node... nodes) {
        return new HBox(nodes);
    }

}
