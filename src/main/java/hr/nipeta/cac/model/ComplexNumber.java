package hr.nipeta.cac.model;

public class ComplexNumber extends Coordinates<Double> {

    public static final ComplexNumber MINUS_ONE = new ComplexNumber(-1,0);
    public static final ComplexNumber ZERO = new ComplexNumber(0,0);
    public static final ComplexNumber ONE = new ComplexNumber(1,0);

    public ComplexNumber(double real, double imaginary) {
        super(real, imaginary);
    }

    public static ComplexNumber xy(double real, double imaginary) {
        return new ComplexNumber(real, imaginary);
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
        return Math.sqrt(x * x + y * y);
    }

    public ComplexNumber conjugate() {
        return new ComplexNumber(x, -y);
    }

    @Override
    public String toString() {
        return x + " + " + y + "i";
    }

}
