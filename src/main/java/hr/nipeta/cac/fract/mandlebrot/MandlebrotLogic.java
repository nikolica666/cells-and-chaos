package hr.nipeta.cac.fract.mandlebrot;

import hr.nipeta.cac.model.ComplexNumber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MandlebrotLogic {

    private static final double MAX_ITERATIONS = 64;
    private static final double MAX_MAGNITUDE = 1_000_000;
    private static final double EPSILON = 1e-6;
public static void main(String[] args){
    MandlebrotLogic m = new MandlebrotLogic();
    FractalResult result = m.calculate(new ComplexNumber(0.1,0.1));
    log.debug("Finished {}", result);

    FractalResult[][] fractalResults = m.calculateGrid(ComplexNumber.xy(-0.70176, -0.3842), 0.001, 100);
    log.debug("Finished calculateGrid");
    for (int i = 0; i < 2 * 10 + 1; i++) {
        for (int j = 0; j < 2 * 10 + 1; j++) {
            log.debug("{}", fractalResults[i][j]);
        }
    }

}

    public FractalResult[][] calculateGrid(ComplexNumber center, double step, int numberOfSteps) {

        double relativeX = center.getX();
        double relativeY = center.getY();

        FractalResult[][] result = new FractalResult[2*numberOfSteps+1][2*numberOfSteps+1];

        for (int i = -numberOfSteps; i <= numberOfSteps; i++) {
            for (int j = -numberOfSteps; j <= numberOfSteps; j++) {
                result[i + numberOfSteps][j + numberOfSteps] = calculate(new ComplexNumber(step * i + relativeX,step * j + relativeY));
            }
        }

        return result;

    }

    public FractalResult calculate(ComplexNumber c) {
        return calculate(ComplexNumber.ZERO, c, 0);
    }

    public FractalResult calculate(ComplexNumber zCurr, ComplexNumber c, int iteration) {

        ComplexNumber zNext = zCurr.sq().add(c);
        double zNextMagnitude = zNext.magnitude();

        if (zNext.magnitude() > MAX_MAGNITUDE) {
            return new FractalResult(c, iteration, true, null);
        }

        iteration++;

        if (Math.abs(zCurr.magnitude() - zNextMagnitude) < EPSILON) {
            return new FractalResult(c, iteration, false, "magnitude less then " + EPSILON);
        }

        if (iteration >= MAX_ITERATIONS) {
            return new FractalResult(c, iteration, false, "iteration greater then " + MAX_ITERATIONS);
        }

        return calculate(zNext, c, iteration);

    }

    @Data
    @AllArgsConstructor
    public static class FractalResult {
        private ComplexNumber start;
        private int iterations;
        private boolean diverged;
        private String reasonNotDiverged;
    }

}
