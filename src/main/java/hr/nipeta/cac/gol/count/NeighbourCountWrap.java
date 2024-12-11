package hr.nipeta.cac.gol.count;

import hr.nipeta.cac.model.IntCoordinates;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NeighbourCountWrap implements NeighbourCount {

    @Override
    public ConcurrentHashMap<IntCoordinates, Integer> count(Collection<IntCoordinates> liveCells, int GRID_SIZE_X, int GRID_SIZE_Y) {

        ConcurrentHashMap<IntCoordinates, Integer> neighborCounts = new ConcurrentHashMap<>();

        // Count live neighbors for all live cells and their neighbors
        liveCells.parallelStream().forEach(cell -> {
            for (int dx = -1; dx <= 1; dx++) {
                int neighbourX = wrap(cell.getX() + dx, GRID_SIZE_X);
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }
                    int neighbourY = wrap(cell.getY() + dy, GRID_SIZE_Y);
                    IntCoordinates neighbor = IntCoordinates.of(neighbourX, neighbourY);
                    neighborCounts.compute(neighbor, (key, value) -> (value == null) ? 1 : value + 1);
                }
            }
        });

        return neighborCounts;

    }

    private int wrap(int coordinate, int maxSize) {
        if (coordinate < 0) {
            return maxSize - 1;
        } else if (coordinate > maxSize - 1) {
            return 0;
        } else {
            return coordinate;
        }
    }

    @Override
    public String getName() {
        return "Border rule wrap";
    }

}
