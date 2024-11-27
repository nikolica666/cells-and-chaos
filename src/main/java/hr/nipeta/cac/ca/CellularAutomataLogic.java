package hr.nipeta.cac.ca;

import lombok.extern.slf4j.Slf4j;

import java.util.stream.IntStream;

@Slf4j
public class CellularAutomataLogic {

    private int rule;
    private String ruleAsBinaryString;

    private final int NUMBER_OF_COLUMNS;
    private final int CENTRAL_COLUMN_INDEX;
    private final int NUMBER_OF_ROWS;

    private final boolean[][] cells;

    public boolean isAlive(int row, int col) {
        return cells[row][col];
    }

    public CellularAutomataLogic(int gridSizeX, int gridSizeY) {
        NUMBER_OF_COLUMNS = gridSizeX % 2 == 0 ? gridSizeX + 1 : gridSizeX;
        CENTRAL_COLUMN_INDEX = (NUMBER_OF_COLUMNS - 1) / 2;
        NUMBER_OF_ROWS = gridSizeY;
        cells = new boolean[NUMBER_OF_ROWS][NUMBER_OF_COLUMNS];
    }

    public void init(int rule) {

        this.rule = rule;
        ruleAsBinaryString = String.format("%8s", Integer.toBinaryString(rule)).replace(' ', '0');

        log.debug("Init rule {} ({})", ruleAsBinaryString, rule);
long milli = System.currentTimeMillis();
        cells[0] = initRow0();

        for (int rowIndex = 1; rowIndex < NUMBER_OF_ROWS; rowIndex++) {
            boolean[] previousRow = cells[rowIndex - 1];
            cells[rowIndex] = calculateRow(previousRow);
        }
log.debug("Evolved in {}ms", (System.currentTimeMillis()-milli));
    }

    private boolean[] initRow0() {
        boolean[] row0 = new boolean[NUMBER_OF_COLUMNS];
        row0[CENTRAL_COLUMN_INDEX] = true;
        return row0;
    }

    /***
     *
     * TODO this can be optimised with<br/>
     * {@code
     * int neighborhood =
     *  (previousRow[columnIndex - 1] ? 4 : 0) +
     *  (previousRow[columnIndex] ? 2 : 0) +
     *  (previousRow[columnIndex + 1] ? 1 : 0);}<br/>
     * {@code row[columnIndex] = (rule & (1 << neighborhood)) != 0;}<br/>
     *
     * but we're gonna leave more readable at this moment (as it allows us to generalise to more then 2 states)
     */
    private boolean[] calculateRow(boolean[] previousRow) {

        boolean[] row = new boolean[NUMBER_OF_COLUMNS];

        CellularAutomataState stateFirst = CellularAutomataState.of(
                previousRow[NUMBER_OF_COLUMNS - 1],
                previousRow[0],
                previousRow[1]);

        row[0] = ruleAsBinaryString.charAt(stateFirst.ordinal()) == '1';

        for (int columnIndex = 1 ; columnIndex < NUMBER_OF_COLUMNS - 1; columnIndex++) {

            CellularAutomataState state = CellularAutomataState.of(
                    previousRow[columnIndex - 1],
                    previousRow[columnIndex],
                    previousRow[columnIndex + 1]
            );

            row[columnIndex] = ruleAsBinaryString.charAt(state.ordinal()) == '1';

        }

        CellularAutomataState stateLast = CellularAutomataState.of(
                previousRow[NUMBER_OF_COLUMNS - 2],
                previousRow[NUMBER_OF_COLUMNS - 1],
                previousRow[0]);

        row[NUMBER_OF_COLUMNS - 1] = ruleAsBinaryString.charAt(stateLast.ordinal()) == '1';

        return row;

    }

    public void evolveRow() {

        boolean[] lastRow = cells[NUMBER_OF_ROWS - 1];
        boolean[] newRow = calculateRow(lastRow);

        // Shift all one index down
        for (int i = 0; i < NUMBER_OF_ROWS - 1; i++) {
            cells[i] = cells[i + 1];
        }

        // Add new row as last
        cells[NUMBER_OF_ROWS - 1] = newRow;

    }

}
