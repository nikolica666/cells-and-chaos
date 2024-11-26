package hr.nipeta.cac.fract.model;

import hr.nipeta.cac.model.ComplexNumber;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class FractalResult {

    private ComplexNumber c;
    private int iterations;
    private boolean diverged;
    private String reasonNotDiverged;

    public static FractalResult diverged(ComplexNumber c, int iterations) {
        return new FractalResult(c, iterations, true, null);
    }

    public static FractalResult converged(ComplexNumber c, int iterations, String reasonNotDiverged) {
        return new FractalResult(c, iterations, false, reasonNotDiverged);
    }

}