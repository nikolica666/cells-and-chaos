package hr.nik.ant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LangtonAntDirection {

    NORTH(0),
    EAST(90),
    SOUTH(180),
    WEST(270);

    private final double degrees;

    public LangtonAntDirection nextDirection(boolean activeCell) {
        switch (this) {
            case NORTH -> {
                return activeCell ? WEST : EAST;
            }
            case EAST -> {
                return activeCell ? NORTH : SOUTH;
            }
            case SOUTH -> {
                return activeCell ? EAST : WEST;
            }
            case WEST -> {
                return activeCell ? SOUTH : NORTH;
            }
            default -> throw new RuntimeException();
        }
    }
}
