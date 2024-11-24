package hr.nipeta.cac.fract.mandlebrot;

import hr.nipeta.cac.fract.model.FractalResult;
import hr.nipeta.cac.model.ComplexNumber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MandlebrotLogic {

    public static final double MAX_ITERATIONS = 255;
    public static final double MAX_MAGNITUDE = 2;
    public static final double EPSILON = 1e-9;

    public FractalResult[][] calculateGrid(double fromTopLeftX, double fromTopLeftY, double step, int stepsX, int stepsY) {

        long milli = System.currentTimeMillis();
log.debug("from {} + {}i step {} stepsX={} stepsY={}", fromTopLeftX, fromTopLeftY, step, stepsX, stepsY);
        FractalResult[][] result = new FractalResult[stepsX][stepsY];

        for (int i = 0; i < stepsX; i++) {
            for (int j = 0; j < stepsY; j++) {
                result[i][j] = calculate(ComplexNumber.xy(fromTopLeftX + i * step, fromTopLeftY - j * step));
            }
        }

        log.debug("calculated grid in {}ms", (System.currentTimeMillis() - milli));
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

}
