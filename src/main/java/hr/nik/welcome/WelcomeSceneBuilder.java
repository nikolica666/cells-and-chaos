package hr.nik.welcome;

import hr.nik.Main;
import hr.nik.SceneBuilder;
import hr.nik.ant.LangtonAntSceneBuilder;
import hr.nik.ca.CellularAutomataSceneBuilder;
import hr.nik.gol.GolSceneBuilder;
import hr.nik.gol.GolOldSceneBuilder;
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
                oldGameOfLifeButton(),
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

    private Button oldGameOfLifeButton() {
        return createButton(
                "Game of Life (old)",
                e -> createScene(() -> new GolOldSceneBuilder(main)));
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
