package hr.nik.gol;

import hr.nik.gol.model.GolCell;
import hr.nik.gol.model.GolCellState;
import hr.nik.gol.rules.GolRules;
import hr.nik.model.Coordinates;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GolLogic {

    private final int GRID_SIZE_X;
    private final int GRID_SIZE_Y;

    private final GolCell[][] cells;

    private GolRules rules;

    public GolLogic(int gridSizeX, int gridSizeY, GolRules golRules) {
        GRID_SIZE_X = gridSizeX;
        GRID_SIZE_Y = gridSizeY;
        this.cells = new GolCell[GRID_SIZE_Y][GRID_SIZE_X];
        this.rules = golRules;
    }

    public GolCellState calculateAndSetNextState(int row, int col) {
        NextStateCalculationResult calculationResult = calculateNewCurrentState(row, col);
        GolCellState calculatedNextState = calculationResult.getCalculatedNextState();
        calculationResult.getCell().setNextState(calculatedNextState);
        return calculatedNextState;
    }

    private NextStateCalculationResult calculateNewCurrentState(int row, int col) {

        GolCell thisCell = findCell(row, col);
        GolCell[] neighbours = findNeighbours(row, col);

        GolCellState nextState = rules.nextState(thisCell.getCurrentState(), neighbours);

        return new NextStateCalculationResult(thisCell, nextState);

    }

    @Data
    @AllArgsConstructor
    private static class NextStateCalculationResult {
        private GolCell cell;
        private GolCellState calculatedNextState;
    }

    public GolCell[] findNeighbours(int thisRowNumber, int thisColNumber) {

        boolean thisRowIsFirst = thisRowNumber == 0;
        boolean thisRowIsLast = thisRowNumber == GRID_SIZE_Y - 1;

        boolean thisColIsFirst = thisColNumber == 0;
        boolean thisColIsLast = thisColNumber == GRID_SIZE_X - 1;

        GolCell[] topRow = cells[thisRowIsLast ? 0 : thisRowNumber + 1];
        GolCell[] thisRow = cells[thisRowNumber];
        GolCell[] botRow = cells[thisRowIsFirst ? GRID_SIZE_Y - 1 : thisRowNumber - 1];

        int leftNeighboursIndex = thisColIsFirst ? GRID_SIZE_X - 1 : thisColNumber - 1;
        int rightNeighboursIndex = thisColIsLast ? 0 : thisColNumber + 1;

        return new GolCell[]{
                topRow[leftNeighboursIndex],
                topRow[rightNeighboursIndex],
                topRow[thisColNumber],
                thisRow[leftNeighboursIndex],
                thisRow[rightNeighboursIndex],
                botRow[leftNeighboursIndex],
                botRow[rightNeighboursIndex],
                botRow[thisColNumber]
        };

    }

    public GolCell findCell(int row, int col) {
        return cells[row][col];
    }

    public GolCell findCellSetDeadIfAbsent(int row, int col) {
        if (cells[row][col] == null) {
            cells[row][col] = GolCell.create(new Coordinates<>(col, row), GolCellState.DEAD);
        }
        return cells[row][col];
    }

    public void setCellCurrentState(int row, int col, GolCellState state) {

        GolCell cell = cells[row][col];
        if (cell == null) {
            cells[row][col] = GolCell.create(new Coordinates<>(col, row), state);
        } else {
            cell.setCurrentState(state);
        }

    }

}
