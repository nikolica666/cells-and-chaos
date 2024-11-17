package hr.nik.gol;

import hr.nik.model.Coordinates;

import java.util.HashMap;
import java.util.Map;

public class GolLogic {

    private final int GRID_SIZE_X;
    private final int GRID_SIZE_Y;

    private final Map<Integer, Map<Integer, GolCell>> golCells = new HashMap<>();

    public GolLogic(int gridSizeX, int gridSizeY) {
        GRID_SIZE_X = gridSizeX;
        GRID_SIZE_Y = gridSizeY;
    }

    public void calculateNextState(int row, int col) {

        GolCell thisCell = findGolCell(row, col);
        int liveNeighbours = countLiveNeighbours(row, col);

        thisCell.setAliveNext(isAliveNext(thisCell, liveNeighbours));

    }

    private boolean isAliveNext(GolCell thisCell, int liveNeighbours) {
        if (thisCell.isAlive()) {
            return liveNeighbours == 2 || liveNeighbours == 3;
        } else {
            return liveNeighbours == 3;
        }
    }

    public boolean setAndReturnNextState(int row, int col) {
        GolCell thisCell = golCells.get(row).get(col);
        return thisCell.setAndReturnNextState();
    }

    public int countLiveNeighbours(int thisRowNumber, int thisColNumber) {

        boolean thisRowIsFirst = thisRowNumber == 0;
        boolean thisRowIsLast = thisRowNumber == GRID_SIZE_Y - 1;

        boolean thisColIsFirst = thisColNumber == 0;
        boolean thisColIsLast = thisColNumber == GRID_SIZE_X - 1;

        Map<Integer, GolCell> prevRow = golCells.get(thisRowIsFirst ? GRID_SIZE_Y - 1 : thisRowNumber - 1);
        Map<Integer, GolCell> thisRow = golCells.get(thisRowNumber);
        Map<Integer, GolCell> nextRow = golCells.get(thisRowIsLast ? 0 : thisRowNumber + 1);

        int leftNeighboursIndex = thisColIsFirst ? GRID_SIZE_X - 1 : thisColNumber - 1;
        int rightNeighboursIndex = thisColIsLast ? 0 : thisColNumber + 1;

        int liveNeighbours = 0;

        if (prevRow.get(leftNeighboursIndex).isAlive()) {
            liveNeighbours++;
        }
        if (thisRow.get(leftNeighboursIndex).isAlive()) {
            liveNeighbours++;
        }
        if (nextRow.get(leftNeighboursIndex).isAlive()) {
            liveNeighbours++;
        }
        if (prevRow.get(rightNeighboursIndex).isAlive()) {
            liveNeighbours++;
        }
        if (thisRow.get(rightNeighboursIndex).isAlive()) {
            liveNeighbours++;
        }
        if (nextRow.get(rightNeighboursIndex).isAlive()) {
            liveNeighbours++;
        }
        if (prevRow.get(thisColNumber).isAlive()) {
            liveNeighbours++;
        }
        if (nextRow.get(thisColNumber).isAlive()) {
            liveNeighbours++;
        }

        return liveNeighbours;

    }

    public void clearGolCell(Coordinates<Integer> coordinates) {
        findGolCell(coordinates).clear();
    }

    public void clearGolCell(int row, int col) {
        findGolCell(row,col).clear();
    }

    public boolean toggleCell(Coordinates<Integer> coordinates) {
        GolCell thisCell = findGolCell(coordinates);
        thisCell.toggleAlive();
        return thisCell.isAlive();
    }

    public GolCell findGolCell(Coordinates<Integer> coordinates) {
        return findGolCell(coordinates.getY(), coordinates.getX());
    }

    public GolCell findGolCell(int row, int col) {
        return golCells.get(row).get(col);
    }

    public void setRowCells(int rowNumber, Map<Integer, GolCell> columns) {
        golCells.put(rowNumber, columns);
    }

    public void setCell(int row, int col, boolean isAlive) {

        Map<Integer, GolCell> cellsInRow = golCells.get(row);
        if (cellsInRow == null) {
            golCells.put(row, new HashMap<>());
        }

        if (golCells.get(row).get(col) == null) {
            Coordinates<Integer> coordinates = new Coordinates<>(col, row);
            golCells.get(row).put(col, GolCell.create(coordinates, isAlive));
        } else {
            golCells.get(row).get(col).setAlive(isAlive);
        }

    }
}
