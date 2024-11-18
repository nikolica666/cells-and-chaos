package hr.nik.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@EqualsAndHashCode
public class Coordinates<T extends Number> {

    private final T x;
    private final T y;

    public Coordinates(T x, T y) {
        this.x = x;
        this.y = y;
    }

}
