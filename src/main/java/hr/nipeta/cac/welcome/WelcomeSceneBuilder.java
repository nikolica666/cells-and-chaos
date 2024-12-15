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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import static hr.nipeta.cac.model.gui.SceneUtils.createButton;

public class WelcomeSceneBuilder extends SceneBuilder {

    public WelcomeSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene createContent() {
        return new Scene(createSceneChangePane());
    }

}
