package hr.nik.ant;

import hr.nik.model.Coordinates;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LangtonAntLogic {

    private final int NUMBER_OF_COLUMNS;
    private final int NUMBER_OF_ROWS;

    private final Map<Integer, Map<Integer, Boolean>> cells = new HashMap<>();

    @Getter
    private LangtonAntState antState;

    public LangtonAntLogic(int gridSizeX, int gridSizeY) {
        NUMBER_OF_COLUMNS = gridSizeX % 2 == 0 ? gridSizeX + 1 : gridSizeX;
        NUMBER_OF_ROWS = gridSizeY;
    }

    public void init() {

        for (int rowIndex = 0; rowIndex < NUMBER_OF_ROWS; rowIndex++) {
            cells.put(rowIndex, new HashMap<>());
            for (int colIndex = 0; colIndex < NUMBER_OF_COLUMNS; colIndex++) {
                cells.get(rowIndex).put(colIndex, false);
            }
        }

        // We'll put him mor to the top left if it's clear grid and he's facing west
        antState = LangtonAntState.of(
                LangtonAntDirection.WEST,
                new Coordinates<>(40,40));

    }

    public void evolve() {

        int currentRow = antState.getCoordinates().getY();
        int currentCol = antState.getCoordinates().getX();

        // Get current cell
        boolean activeCell = cells.get(currentRow).get(currentCol);

        // Reposition direction based on current cell state
        LangtonAntDirection newDirection = antState.getDirection().nextDirection(activeCell);

        // Toggle current cell state
        cells.get(currentRow).put(currentCol, !activeCell);


        // Take a step with an ant
        // TODO refaktoriraj ovo...
        switch (newDirection) {
            case NORTH -> antState = LangtonAntState.of(LangtonAntDirection.NORTH, new Coordinates<>(currentCol, currentRow - 1));
            case EAST -> antState = LangtonAntState.of(LangtonAntDirection.EAST, new Coordinates<>(currentCol + 1, currentRow));
            case SOUTH -> antState = LangtonAntState.of(LangtonAntDirection.SOUTH, new Coordinates<>(currentCol, currentRow + 1));
            case WEST -> antState = LangtonAntState.of(LangtonAntDirection.WEST, new Coordinates<>(currentCol - 1, currentRow));
            default -> throw new RuntimeException();
        }

    }

    public boolean isAlive(int row, int col) {
        return cells.get(row).get(col);
    }

}
