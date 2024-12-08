package hr.nipeta.cac.gol.count;

public class NeighbourCountFactory {
    public static NeighbourCount from(String name) {
        return switch (name) {
            case "Box" -> new NeighbourCountBox();
            case "Open" -> new NeighbourCountOpen();
            case "Wrap" -> new NeighbourCountWrap();
            default -> throw new IllegalStateException(String.format("Unknown NeighbourCount '%s'", name));
        };
    }
}
