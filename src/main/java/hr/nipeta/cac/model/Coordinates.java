package hr.nipeta.cac.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class Coordinates<T extends Number> {

    protected final T x;
    protected final T y;

    public Coordinates(T x, T y) {
        this.x = x;
        this.y = y;
    }

}
