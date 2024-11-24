package hr.nipeta.cac.fract.mandlebrot;

import hr.nipeta.cac.Main;
import hr.nipeta.cac.SceneBuilder;
import hr.nipeta.cac.model.ComplexNumber;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MandlebrotSceneBuilder extends SceneBuilder {

    private int canvasPixels = 1001;
    private ComplexNumber currentCenter = ComplexNumber.ZERO;
    private double step = 0.008;

    public MandlebrotSceneBuilder(Main main) {
        super(main);
    }

    @Override
    public Scene build() {

        MandlebrotLogic m = new MandlebrotLogic();


        Canvas canvas = new Canvas(canvasPixels, canvasPixels);
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> handleMousePressed(e, canvas.getGraphicsContext2D()));
        GraphicsContext gc = canvas.getGraphicsContext2D();

        calculateAndDraw(gc, currentCenter,step,(canvasPixels - 1) / 2);

        return new Scene(new Pane(canvas));

    }

    private void handleMousePressed(MouseEvent e, GraphicsContext gc) {

        log.debug("clicked on x={}, y={}", e.getX(),e.getY());

        double pixelsToCenterX = (canvasPixels - 1) / 2 - e.getX();
        double pixelsToCenterY = (canvasPixels - 1) / 2 - e.getY();

        log.debug("pixelsToCenterX={}, pixelsToCenterY={}", pixelsToCenterX,pixelsToCenterY);

        double realPart = currentCenter.getX() - step * pixelsToCenterX;
        double imagPart = currentCenter.getY() + step * pixelsToCenterY;

        log.debug("coordX={}, coordY={}", realPart,imagPart);

        ComplexNumber newCurrentCenter = ComplexNumber.xy(realPart,imagPart);

        log.debug("oldCenter={}", currentCenter);
        log.debug("newCenter={}", newCurrentCenter);

        currentCenter = newCurrentCenter;

        switch (e.getButton()) {
            case PRIMARY -> {
                step = step / 2;
            }
            case SECONDARY -> {
                step = step * 2;
            }
        }

        calculateAndDraw(gc, currentCenter,step,(canvasPixels - 1) / 2);


    }

    private void calculateAndDraw(GraphicsContext gc, ComplexNumber center, double step, int numberOfSteps) {

        MandlebrotLogic.FractalResult[][] fractalResults = new MandlebrotLogic()
                .calculateGrid(center, step, numberOfSteps);

        var pixelWriter = gc.getPixelWriter();

        // Loop through each pixel (x, y)
        for (int x = 0; x < canvasPixels; x++) {
            for (int y = 0; y < canvasPixels; y++) {

                MandlebrotLogic.FractalResult f = fractalResults[x][y];
                Color color = fractalResults[x][y].isDiverged() ?
                        Color.rgb(f.getIterations(), f.getIterations(), f.getIterations()) :
                        Color.BLACK;

                pixelWriter.setColor(x, y, color);

            }
        }

    }

}

