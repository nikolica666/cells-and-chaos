package hr.nipeta.cac.gol.count;

import hr.nipeta.cac.model.IntCoordinates;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NeighbourCountOpen implements NeighbourCount {

    @Override
    public ConcurrentHashMap<IntCoordinates, Integer> count(Collection<IntCoordinates> liveCells, int GRID_SIZE_X, int GRID_SIZE_Y) {

        ConcurrentHashMap<IntCoordinates, Integer> neighborCounts = new ConcurrentHashMap<>();

        // Count live neighbors for all live cells and their neighbors
        liveCells.parallelStream().forEach(cell -> {
            for (int dx = -1; dx <= 1; dx++) {
                int neighbourX = cell.getX() + dx;
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) {
                        continue;
                    }
                    int neighbourY = cell.getY() + dy;
                    IntCoordinates neighbor = IntCoordinates.of(neighbourX, neighbourY);
                    neighborCounts.compute(neighbor, (key, value) -> (value == null) ? 1 : value + 1);
                }
            }
        });

        return neighborCounts;

    }

    @Override
    public String getName() {
        return "Border rule open";
    }

}
