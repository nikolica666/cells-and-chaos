package hr.nipeta.cac.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RectangularGrid {

    private int rows;
    private int cols;
    private int numberOfCells;

    private double cellSize;
    private double cellBorder;
    private double cellSizeWithBorder;

    public static RectangularGrid of(int rows, int columns, double cellSize, double cellBorder) {
        return new RectangularGrid(rows, columns, rows * columns, cellSize, cellBorder, cellSize + cellBorder);
    }

    public void setCellSize(double newCellSize) {
        this.cellSize = newCellSize;
        this.cellSizeWithBorder = newCellSize + this.cellBorder;
    }

    public void setCellBorder(double newCellBorder) {
        this.cellBorder = newCellBorder;
        this.cellSizeWithBorder = this.cellSize + newCellBorder;
    }

}
