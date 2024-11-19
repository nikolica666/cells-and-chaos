package hr.nipeta.cac.gol.model;

public enum GolCellState {
    DEAD,
    ALIVE;

    public GolCellState next() {
        GolCellState[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

}
