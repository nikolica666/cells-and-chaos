package hr.nik.ca;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CellularAutomataLogic {

    private String ruleAsBinaryString;

    private final int NUMBER_OF_COLUMNS;
    private final int CENTRAL_COLUMN_INDEX;
    private final int NUMBER_OF_ROWS;

    private final Map<Integer, Map<Integer, Boolean>> cells = new HashMap<>();

    public boolean isAlive(int row, int col) {
        return cells.get(row).get(col);
    }

    public CellularAutomataLogic(int gridSizeX, int gridSizeY) {
        NUMBER_OF_COLUMNS = gridSizeX % 2 == 0 ? gridSizeX + 1 : gridSizeX;
        CENTRAL_COLUMN_INDEX = (NUMBER_OF_COLUMNS - 1) / 2;
        NUMBER_OF_ROWS = gridSizeY;
    }

    public void init(int rule) {

        ruleAsBinaryString = String.format("%8s", Integer.toBinaryString(rule)).replace(' ', '0');

        log.debug("Init rule {} ({})", ruleAsBinaryString, rule);

        cells.put(0, initRow0());

        for (int rowIndex = 1; rowIndex < NUMBER_OF_ROWS; rowIndex++) {
            Map<Integer, Boolean> previousRow = cells.get(rowIndex - 1);
            cells.put(rowIndex, calculateRow(previousRow));
        }

    }

    private Map<Integer, Boolean> initRow0() {
        Map<Integer, Boolean> row0 = new HashMap<>();
        for (int columnIndex = 0 ; columnIndex < NUMBER_OF_COLUMNS; columnIndex++) {
            row0.put(columnIndex, columnIndex == CENTRAL_COLUMN_INDEX);
        }
        return row0;
    }

    private Map<Integer, Boolean> calculateRow(Map<Integer, Boolean> previousRow) {

        Map<Integer, Boolean> row = new HashMap<>();

        CellularAutomataState stateFirst = CellularAutomataState.of(
                previousRow.get(NUMBER_OF_COLUMNS - 1), 
                previousRow.get(0), 
                previousRow.get(1));
        
        row.put(0, ruleAsBinaryString.charAt(stateFirst.ordinal()) == '1');

        for (int columnIndex = 1 ; columnIndex < NUMBER_OF_COLUMNS - 1; columnIndex++) {

            CellularAutomataState state = CellularAutomataState.of(
                    previousRow.get(columnIndex - 1),
                    previousRow.get(columnIndex),
                    previousRow.get(columnIndex + 1)
            );
            
            row.put(columnIndex, ruleAsBinaryString.charAt(state.ordinal()) == '1');

        }

        CellularAutomataState stateLast = CellularAutomataState.of(
                previousRow.get(NUMBER_OF_COLUMNS - 2),
                previousRow.get(NUMBER_OF_COLUMNS - 1),
                previousRow.get(0));
        
        row.put(NUMBER_OF_COLUMNS - 1, ruleAsBinaryString.charAt(stateLast.ordinal()) == '1');

        return row;

    }

    public void evolveRow() {

        Map<Integer, Boolean> lastRow = cells.get(NUMBER_OF_ROWS - 1);
        Map<Integer, Boolean> newRow = calculateRow(lastRow);

        // Shift all one index down
        for (int i = 0; i < NUMBER_OF_ROWS; i++) {
            cells.put(i, cells.get(i + 1));
        }

        // Add new row as last
        cells.put(NUMBER_OF_ROWS - 1, newRow);

    }

}
