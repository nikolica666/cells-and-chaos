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

    public LangtonAntDirection nextDirection(LangtonAntTile tile) {
        double newDegrees = (this.degrees + tile.getTurnDegrees() + 360) % 360;
        return fromDegrees(newDegrees);
    }

    public static LangtonAntDirection fromDegrees(double degrees) {
        switch ((int) degrees) {
            case 0 -> {
                return NORTH;
            }
            case 90 -> {
                return EAST;
            }
            case 180 -> {
                return SOUTH;
            }
            case 270 -> {
                return WEST;
            }
            default -> throw new RuntimeException(String.format("Unexpected %s degrees", degrees));
        }
    }

}
