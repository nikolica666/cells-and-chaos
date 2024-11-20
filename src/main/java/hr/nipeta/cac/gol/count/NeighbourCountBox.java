package hr.nipeta.cac.gol.count;

import hr.nipeta.cac.model.IntCoordinates;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NeighbourCountBox implements NeighbourCount {

    @Override
    public Map<IntCoordinates, Integer> count(Collection<IntCoordinates> liveCells, int GRID_SIZE_X, int GRID_SIZE_Y) {

        Map<IntCoordinates, Integer> neighborCounts = new HashMap<>();

        // Count live neighbors for all live cells and their neighbors
        for (IntCoordinates cell : liveCells) {
            for (int dx = -1; dx <= 1; dx++) {
                int neighbourX = cell.getX() + dx;
                if (neighbourX < 0 || neighbourX > GRID_SIZE_X - 1) {
                    continue;
                }
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }
                    int neighbourY = cell.getY() + dy;
                    if (neighbourY < 0 || neighbourY > GRID_SIZE_Y - 1) {
                        continue;
                    }

                    IntCoordinates neighbor = IntCoordinates.of(neighbourX, neighbourY);
                    neighborCounts.put(neighbor, neighborCounts.getOrDefault(neighbor, 0) + 1);
                }
            }
        }

        return neighborCounts;

    }

    @Override
    public String getName() {
        return "Border rule box";
    }

}
