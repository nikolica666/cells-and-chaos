package hr.nipeta.cac;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
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
        scene.getStylesheets().addAll(
                getClass().getResource("/css/font-lemonmilk.css").toExternalForm(),
                getClass().getResource("/css/main.css").toExternalForm()
        );
        return scene;
    }

    protected abstract Scene createContent();

    protected <T extends SceneBuilder> void createScene(Supplier<T> constructor) {
        Scene scene = constructor.get().build();
        main.getPrimaryStage().setScene(scene);
        center(main.getPrimaryStage());
    }

    public static void center(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - stage.getScene().getWidth()) / 2);
        stage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - stage.getScene().getHeight()) / 2);
    }

    protected HBox horizontalMenu(Node... nodes) {
        return new HBox(nodes);
    }

}
