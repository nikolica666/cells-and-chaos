package hr.nipeta.cac.model;

import java.util.Arrays;

public class ComplexNumber extends Coordinates<Double> {

    public static final ComplexNumber MINUS_ONE = new ComplexNumber(-1,0);
    public static final ComplexNumber ZERO = new ComplexNumber(0,0);
    public static final ComplexNumber ONE = new ComplexNumber(0,1);

    public static ComplexNumber parse(String realPlusImaginaryI) {
        return new ComplexNumber(realPlusImaginaryI);
    }

    public static ComplexNumber xy(double real, double imaginary) {
        return new ComplexNumber(real, imaginary);
    }

    private ComplexNumber(String realPlusImaginaryI) {
        this(realPlusImaginaryISplit(realPlusImaginaryI));
    }

    private static double[] realPlusImaginaryISplit(String realPlusImaginaryI) {

        realPlusImaginaryI = realPlusImaginaryI
                .replaceAll("\\s+", "")
                .replace("−", "-")
                .toLowerCase()
                .replace("j", "i");

        // if '+3+3i' simplify to '3+3i'
        if (realPlusImaginaryI.startsWith("+")) {
            realPlusImaginaryI = realPlusImaginaryI.substring(1);
        }

        final String[] splitReal0Imaginary1;

        // If there is plus it's simple, just split (we check later if there are 2 or more)
        if (realPlusImaginaryI.contains("+")) {
            splitReal0Imaginary1 = realPlusImaginaryI.split("\\+");
        }
        // If there's at least one minus
        else if (realPlusImaginaryI.contains("-")) {
            // If real part is negative
            if (realPlusImaginaryI.startsWith("-")) {
                // Split (if it's leading '-' 0th item will be ""
                String[] tempSplit = realPlusImaginaryI.split("-");
                // Transfer 1st and 2nd item to new array, ignore 0th
                splitReal0Imaginary1 = Arrays.copyOfRange(tempSplit, 1, tempSplit.length);
                // Remember, String was starting with '-' so add it back to real part...
                splitReal0Imaginary1[0] = "-" + splitReal0Imaginary1[0];
            } else {
                // Real part is not negative, so just split by '-'
                splitReal0Imaginary1 = realPlusImaginaryI.split("-");
            }
        } else {

            // TODO cover case when number is pure real or pure imaginary

            throw new IllegalArgumentException("Expected + or - sign");
        }

        if (splitReal0Imaginary1.length != 1 && splitReal0Imaginary1.length != 2) {
            throw new IllegalArgumentException("Expected exactly one + or - sign");
        }

        final String realPart;
        final String imaginaryPart;

        // Cover case when number is on X-axis or Y-axis
        if (splitReal0Imaginary1.length == 1) {
            if (splitReal0Imaginary1[0].contains("i")) {
                realPart = "0";
                imaginaryPart = splitReal0Imaginary1[0].replace("i", "");
            } else {
                realPart = splitReal0Imaginary1[0];
                imaginaryPart = "0";
            }
        } else {
            realPart = splitReal0Imaginary1[0];
            imaginaryPart = splitReal0Imaginary1[1].replace("i", "");
        }

        return new double[] {
                Double.parseDouble(realPart),
                Double.parseDouble(imaginaryPart)};
    }

    private ComplexNumber(double[] real0imaginary1) {
        this(real0imaginary1[0], real0imaginary1[1]);
    }

    private ComplexNumber(double real, double imaginary) {
        super(real, imaginary);
    }

    public ComplexNumber sq() {
        return new ComplexNumber(x*x - y*y, 2*x*y);
    }

    public ComplexNumber add(ComplexNumber other) {
        return new ComplexNumber(this.x + other.x, this.y + other.y);
    }

    public ComplexNumber sub(ComplexNumber other) {
        return new ComplexNumber(this.x - other.x, this.y - other.y);
    }

    public ComplexNumber mult(ComplexNumber other) {
        return new ComplexNumber(this.x * other.x - this.y * other.y, this.x * other.y + this.y + other.x);
    }

    public double magnitude() {
        return Math.sqrt(magnitudeSq());
    }

    public double magnitudeSq() {
        return x * x + y * y;
    }

    public ComplexNumber conjugate() {
        return new ComplexNumber(x, -y);
    }

    @Override
    public String toString() {
        return x + " + " + y + "i";
    }

}
