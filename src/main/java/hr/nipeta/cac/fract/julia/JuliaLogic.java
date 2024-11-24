package hr.nipeta.cac.fract.julia;

import hr.nipeta.cac.fract.model.FractalResult;
import hr.nipeta.cac.model.ComplexNumber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class JuliaLogic {

    private final ComplexNumber pivot;

    public static final double MAX_ITERATIONS = 127;
    public static final double MAX_MAGNITUDE = 1_000;
    public static final double EPSILON = 1e-6;

    public FractalResult[][] calculateGrid(ComplexNumber center, double step, int stepsX, int stepsY) {

        double relativeX = center.getX();
        double relativeY = center.getY();

        FractalResult[][] result = new FractalResult[2 * stepsX + 1][2 * stepsY + 1];

        for (int i = -stepsX; i <= stepsX; i++) {
            for (int j = -stepsY; j <= stepsY; j++) {
                ComplexNumber c = new ComplexNumber(step * i + relativeX,step * j + relativeY);
                result[i + stepsX][j + stepsY] = calculate(c);
            }
        }

        return result;

    }

    public FractalResult calculate(ComplexNumber c) {
        return calculate(c, pivot, 0);
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
