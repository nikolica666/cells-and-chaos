package hr.nipeta.cac;

import hr.nipeta.cac.ant.LangtonAntSceneBuilder;
import hr.nipeta.cac.ca.CellularAutomataSceneBuilder;
import hr.nipeta.cac.collatz.CollatzSceneBuilder;
import hr.nipeta.cac.fract.julia.JuliaSceneBuilder;
import hr.nipeta.cac.fract.logistic.LogisticSceneBuilder;
import hr.nipeta.cac.fract.mandlebrot.MandlebrotSceneBuilder;
import hr.nipeta.cac.gol.GolSceneBuilder;
import hr.nipeta.cac.lsystem.LSystemSceneBuilder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.function.Supplier;

import static hr.nipeta.cac.model.gui.SceneUtils.createButton;

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


    protected Pane createSceneChangePane() {

        VBox box = verticalMenu(
                createSceneChangeButton("Game of Life", new GolSceneBuilder(main)),
                createSceneChangeButton("Cellular Automata", new CellularAutomataSceneBuilder(main)),
                createSceneChangeButton("Langton's Ant", new LangtonAntSceneBuilder(main)),
                createSceneChangeButton("Collatz conjecture",new CollatzSceneBuilder(main)),
                createSceneChangeButton("Mandelbrot set", new MandlebrotSceneBuilder(main)),
                createSceneChangeButton("Julia set", new JuliaSceneBuilder(main)),
                createSceneChangeButton("L-System", new LSystemSceneBuilder(main)),
                createSceneChangeButton("Logistic map", new LogisticSceneBuilder(main))
        );

        box.setPadding(new Insets(10));
        box.setSpacing(5);

        return box;

    }

    private Button createSceneChangeButton(String label, SceneBuilder sceneBuilder) {
        return createButton(label, sceneEventHandler(sceneBuilder));
    }

    private EventHandler<ActionEvent> sceneEventHandler(SceneBuilder b) {
        return (e) -> createScene(() -> b);
    }

    protected Button createSceneChangePopupButton() {

        Popup popup = new Popup();
        popup.setAutoHide(true);

        Pane sceneChangePane = createSceneChangePane();
        sceneChangePane.setStyle("""
                -fx-background-color: #ffffff;
                -fx-border-color: #cccccc;
                -fx-border-radius: 5px;
                -fx-background-radius: 5px;""");
        popup.getContent().add(sceneChangePane);

        Button showPopupButton = new Button("Main menu");
        showPopupButton.setOnAction(e -> popup.show(main.getPrimaryStage()));

        return showPopupButton;

    }

    public static void center(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX() + (screenBounds.getWidth() - stage.getScene().getWidth()) / 2);
        stage.setY(screenBounds.getMinY() + (screenBounds.getHeight() - stage.getScene().getHeight()) / 2);
    }

    protected HBox horizontalMenu(Node... nodes) {
        return new HBox(nodes);
    }

    protected VBox verticalMenu(Node... nodes) {
        return new VBox(nodes);
    }

}
