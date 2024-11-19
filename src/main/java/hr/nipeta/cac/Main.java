package hr.nipeta.cac;

import hr.nipeta.cac.welcome.WelcomeSceneBuilder;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@Getter
public class Main extends Application {

    private Stage primaryStage;

    public static void main(String[] args) {

        if (args != null && args.length > 0) {
            log.info("Launching app with command line args: {}", Arrays.toString(args));
        } else {
            log.info("Launching app without command line args");
        }

        launch(args);

    }

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;

        primaryStage.setScene(new WelcomeSceneBuilder(this).build());
        primaryStage.setTitle("Cellular automata");
        primaryStage.show();

    }

}