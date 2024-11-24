package hr.nipeta.cac.fract.model;

import hr.nipeta.cac.model.ComplexNumber;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public  class FractalResult {
    private ComplexNumber start;
    private int iterations;
    private boolean diverged;
    private String reasonNotDiverged;
}