package hr.nipeta.cac.welcome;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.ant.LangtonAntSceneBuilder;
import hr.nipeta.cac.ca.CellularAutomataSceneBuilder;
import hr.nipeta.cac.gol.GolSceneBuilder;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class WelcomeSceneBuilder extends SceneBuilder {

    public WelcomeSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene build() {

        HBox box = horizontalMenu(
                gameOfLifeButton(),
                cellularAutomataButton(),
                langtonAntButton()
        );

        box.setPadding(new Insets(10));

        return new Scene(box);

    }

    private Button gameOfLifeButton() {
        return createButton(
                "Game of Life",
                e -> createScene(() -> new GolSceneBuilder(main)));
    }

    private Button cellularAutomataButton() {
        return createButton(
                "Cellular Automata",
                e -> createScene(() -> new CellularAutomataSceneBuilder(main)));
    }

    private Button langtonAntButton() {
        return createButton(
                "Langton's Ant",
                e -> createScene(() -> new LangtonAntSceneBuilder(main)));
    }

}
