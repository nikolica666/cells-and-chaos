package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 * Maze (B3/S12345)
 */
public class GolMazeRules extends GolRules {

    @Override
    public boolean becomeAlive(GolCell[] neighbours) {
        return becomeAlive(countLiveNeighbours(neighbours, 3));
    }

    @Override
    public boolean becomeAlive(int liveNeighbours) {
        return liveNeighbours == 3;
    }

    @Override
    public boolean stayAlive(GolCell[] neighbours) {
        return stayAlive(countLiveNeighbours(neighbours, 6));
    }

    @Override
    public boolean stayAlive(int liveNeighbours) {
        return liveNeighbours > 0 && liveNeighbours < 6;
    }

    @Override
    public String getPatternNotation() {
        return "B3/S12345";
    }

    @Override
    public String getName() {
        return "Maze";
    }

}
