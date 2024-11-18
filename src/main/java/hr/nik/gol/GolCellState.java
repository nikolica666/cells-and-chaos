package hr.nik.gol;

public enum GolCellState {
    DEAD,
    ALIVE;

    public GolCellState next() {
        GolCellState[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

}
