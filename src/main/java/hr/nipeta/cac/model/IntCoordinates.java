package hr.nipeta.cac.model;

public class IntCoordinates extends Coordinates<Integer> {

    public static IntCoordinates of(int x, int y) {
        return new IntCoordinates(x,y);
    }

    protected IntCoordinates(Integer x, Integer y) {
        super(x, y);
    }
}
