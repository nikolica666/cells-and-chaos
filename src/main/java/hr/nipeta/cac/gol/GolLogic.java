package hr.nipeta.cac.gol;

import hr.nipeta.cac.gol.model.GolCell;
import hr.nipeta.cac.gol.model.GolCellState;
import hr.nipeta.cac.gol.rules.GolRules;
import hr.nipeta.cac.model.Coordinates;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class GolLogic {

    private final int GRID_SIZE_X;
    private final int GRID_SIZE_Y;
    private final GolCell[][] cells;
    private final GolRules rules;

    public GolLogic(int gridSizeX, int gridSizeY, GolRules golRules) {
        this.GRID_SIZE_X = gridSizeX;
        this.GRID_SIZE_Y = gridSizeY;
        this.cells = new GolCell[GRID_SIZE_Y][GRID_SIZE_X];
        this.rules = golRules;
    }

    public void randomize() {
        for (int row = 0; row < GRID_SIZE_Y; row++) {
            for (int col = 0; col < GRID_SIZE_X; col++) {
                GolCell cell = cells[row][col];
                // TODO this will not work OK if we introduce more then 2 states
                cell.setNextState(new Random().nextBoolean() ? GolCellState.ALIVE : GolCellState.DEAD);
                cell.setCurrentStateToNextStateValue();
            }
        }
    }

    public void evolve() {
        for (int row = 0; row < GRID_SIZE_Y; row++) {
            for (int col = 0; col < GRID_SIZE_X; col++) {
                calculateAndSetNextState(cells[row][col]);
            }
        }
        for (int row = 0; row < GRID_SIZE_Y; row++) {
            for (int col = 0; col < GRID_SIZE_X; col++) {
                cells[row][col].setCurrentStateToNextStateValue();
            }
        }
    }

    public void calculateAndSetNextState(GolCell cell) {
        GolCell[] neighbours = findNeighbours(cell.getCoordinates().getY(), cell.getCoordinates().getX());
        GolCellState calculatedNextState = rules.nextState(cell.getCurrentState(), neighbours);
        cell.setNextState(calculatedNextState);
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
