package hr.nipeta.cac.ant;

import hr.nipeta.cac.model.Coordinates;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class LangtonAntLogic {

    private final int NUMBER_OF_COLUMNS;
    private final int NUMBER_OF_ROWS;

    private List<LangtonAntTile> tiles;

    // row, column, tileIndex
    private final Map<Integer, Map<Integer, Integer>> cells = new HashMap<>();

    private LangtonAntState antState;

    public LangtonAntLogic(int gridSizeX, int gridSizeY) {
        NUMBER_OF_COLUMNS = gridSizeX % 2 == 0 ? gridSizeX + 1 : gridSizeX;
        NUMBER_OF_ROWS = gridSizeY;
    }

    public void init(String rules) {

        tiles = new ArrayList<>();

        char[] patternCharArray = rules.trim().toUpperCase().toCharArray();
        for (int i = 0; i < patternCharArray.length; i++) {
            char c = patternCharArray[i];
            switch (c) {
                case 'L' -> tiles.add(new LangtonAntTile("L" + i, -90));
                case 'R' -> tiles.add(new LangtonAntTile("R" + i, 90));
                case 'C' -> tiles.add(new LangtonAntTile("C" + i, 180));
                case 'U' -> tiles.add(new LangtonAntTile("U" + i, 0));
                default -> throw new RuntimeException("Pattern letters must be L,R,C or U");
            }
        }

        for (int rowIndex = 0; rowIndex < NUMBER_OF_ROWS; rowIndex++) {
            cells.put(rowIndex, new HashMap<>());
            for (int colIndex = 0; colIndex < NUMBER_OF_COLUMNS; colIndex++) {
                cells.get(rowIndex).put(colIndex, 0);
            }
        }

        // We'll put him mor to the top left if it's clear grid and he's facing west
        antState = LangtonAntState.of(
                LangtonAntDirection.WEST,
                new Coordinates<>(NUMBER_OF_COLUMNS / 3,NUMBER_OF_ROWS / 2));

    }

    public void evolve() {

        int currentRow = antState.getCoordinates().getY();
        int currentCol = antState.getCoordinates().getX();

        // Get current cell
        int currentTileIndex = cells.get(currentRow).get(currentCol);

        // Reposition direction based on current cell state
        LangtonAntDirection newDirection = antState.getDirection().nextDirection(tiles.get(currentTileIndex));

        // Toggle current cell state
        int nextTileIndex = (currentTileIndex + 1) % tiles.size();
        cells.get(currentRow).put(currentCol, nextTileIndex);

        // Take a step with an ant
        // TODO refactor...
        switch (newDirection) {
            case NORTH -> antState = LangtonAntState.of(LangtonAntDirection.NORTH, new Coordinates<>(currentCol, currentRow - 1));
            case EAST -> antState = LangtonAntState.of(LangtonAntDirection.EAST, new Coordinates<>(currentCol + 1, currentRow));
            case SOUTH -> antState = LangtonAntState.of(LangtonAntDirection.SOUTH, new Coordinates<>(currentCol, currentRow + 1));
            case WEST -> antState = LangtonAntState.of(LangtonAntDirection.WEST, new Coordinates<>(currentCol - 1, currentRow));
            default -> throw new RuntimeException();
        }

    }

    public int getTileIndex(int row, int col) {
        return cells.get(row).get(col);
    }

    public Coordinates<Integer> getCurrentStateCoordinates() {
        return antState.getCoordinates();
    }

    public double getCurrentStateDirectionDegrees() {
        return antState.getDirection().getDegrees();
    }
}
