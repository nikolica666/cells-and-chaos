package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 * Morley (B368/S245)
 */
public class GolMorleyRules extends GolRules {

    @Override
    public boolean becomeAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours);
        return becomeAlive(liveNeighbours);
    }

    @Override
    public boolean becomeAlive(int liveNeighbours) {
        return liveNeighbours == 3 || liveNeighbours == 6 || liveNeighbours == 8;
    }

    @Override
    public boolean stayAlive(GolCell[] neighbours) {
        int liveNeighbours = countLiveNeighbours(neighbours, 5);
        return stayAlive(liveNeighbours);
    }

    @Override
    public boolean stayAlive(int liveNeighbours) {
        return liveNeighbours == 2 || liveNeighbours == 4 || liveNeighbours == 5;
    }

    @Override
    public String getPatternNotation() {
        return "B368/S245";
    }

    @Override
    public String getName() {
        return "Morley";
    }

}
