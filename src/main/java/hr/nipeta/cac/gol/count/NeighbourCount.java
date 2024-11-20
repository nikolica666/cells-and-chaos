package hr.nipeta.cac.gol.count;

import hr.nipeta.cac.model.IntCoordinates;

import java.util.Collection;
import java.util.Map;

public interface NeighbourCount {
    Map<IntCoordinates, Integer> count(Collection<IntCoordinates> liveCells, int GRID_SIZE_X, int GRID_SIZE_Y);
    String getName();
}
