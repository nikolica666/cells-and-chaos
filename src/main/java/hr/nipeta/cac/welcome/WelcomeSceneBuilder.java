package hr.nipeta.cac.welcome;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class WelcomeSceneBuilder extends SceneBuilder {

    public WelcomeSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {

        HBox box = horizontalMenu(
                createSceneChangeButton("Game of Life", new GolSceneBuilder(main)),
                createSceneChangeButton("Cellular Automata", new CellularAutomataSceneBuilder(main)),
                createSceneChangeButton("Langton's Ant", new LangtonAntSceneBuilder(main)),
                createSceneChangeButton("Collatz conjecture",new CollatzSceneBuilder(main)),
                createSceneChangeButton("M", new MandlebrotSceneBuilder(main)),
                createSceneChangeButton("J", new JuliaSceneBuilder(main)),
                createSceneChangeButton("L-System", new LSystemSceneBuilder(main)),
                createSceneChangeButton("Logistic map", new LogisticSceneBuilder(main))
        );

        box.setPadding(new Insets(10));

        return new Scene(box);

    }

    private Button createSceneChangeButton(String label, SceneBuilder sceneBuilder) {
        return createButton(label, sceneEventHandler(sceneBuilder));
    }

    private EventHandler<ActionEvent> sceneEventHandler(SceneBuilder b) {
        return (e) -> createScene(() -> b);
    }

}
