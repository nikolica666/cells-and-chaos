package hr.nik.gol;

import hr.nik.model.Coordinates;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class GolLogic {

    private final int GRID_SIZE_X;
    private final int GRID_SIZE_Y;

    private final Map<Integer, Map<Integer, GolCell>> golCells = new HashMap<>();

    private GolRules rules;

    public GolLogic(int gridSizeX, int gridSizeY, GolRules golRules) {
        GRID_SIZE_X = gridSizeX;
        GRID_SIZE_Y = gridSizeY;
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
        Map<Integer, GolCell> neighbours = findNeighbours(row, col);

        log.trace("Calculating for {}", thisCell);
        neighbours.forEach((k,v) -> log.trace("{}", v));

        GolCellState nextState = rules.nextState(thisCell.getCurrentState(), neighbours.values().stream().map(GolCell::getCurrentState).collect(Collectors.toList()));

        return new NextStateCalculationResult(thisCell, nextState);

    }

    @Data
    @AllArgsConstructor
    private static class NextStateCalculationResult {
        private GolCell cell;
        private GolCellState calculatedNextState;
    }

    public Map<Integer, GolCell> findNeighbours(int thisRowNumber, int thisColNumber) {

        boolean thisRowIsFirst = thisRowNumber == 0;
        boolean thisRowIsLast = thisRowNumber == GRID_SIZE_Y - 1;

        boolean thisColIsFirst = thisColNumber == 0;
        boolean thisColIsLast = thisColNumber == GRID_SIZE_X - 1;

        Map<Integer, GolCell> topRow = golCells.get(thisRowIsLast ? 0 : thisRowNumber + 1);
        Map<Integer, GolCell> thisRow = golCells.get(thisRowNumber);
        Map<Integer, GolCell> botRow = golCells.get(thisRowIsFirst ? GRID_SIZE_Y - 1 : thisRowNumber - 1);

        int leftNeighboursIndex = thisColIsFirst ? GRID_SIZE_X - 1 : thisColNumber - 1;
        int rightNeighboursIndex = thisColIsLast ? 0 : thisColNumber + 1;

        Map<Integer, GolCell> neighbours = new HashMap<>();

        neighbours.put(1, topRow.get(leftNeighboursIndex));
        neighbours.put(2, topRow.get(rightNeighboursIndex));
        neighbours.put(3, topRow.get(thisColNumber));

        neighbours.put(4, thisRow.get(leftNeighboursIndex));
        neighbours.put(5, thisRow.get(rightNeighboursIndex));

        neighbours.put(6, botRow.get(leftNeighboursIndex));
        neighbours.put(7, botRow.get(rightNeighboursIndex));
        neighbours.put(8, botRow.get(thisColNumber));

        return neighbours;

    }

    public GolCell findCell(int row, int col) {
        return golCells.get(row).get(col);
    }

    public GolCell findCellSetDeadIfAbsent(int row, int col) {
        return golCells
                .computeIfAbsent(row, k -> new HashMap<>())
                .computeIfAbsent(col, k -> GolCell.create(new Coordinates<>(col, row), GolCellState.DEAD));
    }

    public boolean setCellCurrentState(int row, int col, GolCellState state) {

        Map<Integer, GolCell> golCellsRow = golCells.computeIfAbsent(row, k -> new HashMap<>());

        final boolean stateChanged;

        if (golCellsRow.get(col) == null) {
            Coordinates<Integer> coordinates = new Coordinates<>(col, row);
            golCells.get(row).put(col, GolCell.create(coordinates, state));

            // Cell didn't exist, so we consider that to be same as DEAD
            stateChanged = state != GolCellState.DEAD;

        } else {

            stateChanged = golCellsRow.get(col).getCurrentState() != state;

            golCellsRow.get(col).setCurrentState(state);
        }

        return stateChanged;

    }

}
