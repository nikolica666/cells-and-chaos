package hr.nipeta.cac;

import hr.nipeta.cac.welcome.WelcomeSceneBuilder;
import javafx.application.Application;
import javafx.scene.text.Font;
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

        loadFonts();

        this.primaryStage = primaryStage;

        primaryStage.setScene(new WelcomeSceneBuilder(this).build());
        primaryStage.setTitle("Cells and Chaos");
        primaryStage.show();

    }

    public static Font lightFont;
    public static Font regularFont;
    public static Font semiBoldFont;
    public static Font boldFont;

    private void loadFonts() {
        try {
            lightFont = Font.loadFont(getClass().getResourceAsStream("/fonts/lemon-milk/LEMONMILK-Light.otf"), 16);
            regularFont = Font.loadFont(getClass().getResourceAsStream("/fonts/lemon-milk/LEMONMILK-Regular.otf"), 16);
            semiBoldFont = Font.loadFont(getClass().getResourceAsStream("/fonts/lemon-milk/LEMONMILK-Semibold.otf"), 16);
            boldFont = Font.loadFont(getClass().getResourceAsStream("/fonts/lemon-milk/LEMONMILK-Bold.otf"), 16);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}