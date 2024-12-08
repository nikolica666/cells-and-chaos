package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;

/**
 * Mazectric (B1234/S3)
 */
public class GolMazectric extends GolRules {

    @Override
    public boolean becomeAlive(GolCell[] neighbours) {
        return becomeAlive(countLiveNeighbours(neighbours, 5));
    }

    @Override
    public boolean becomeAlive(int liveNeighbours) {
        return liveNeighbours > 0 && liveNeighbours < 5;
    }

    @Override
    public boolean stayAlive(GolCell[] neighbours) {
        return stayAlive(countLiveNeighbours(neighbours, 4));
    }

    @Override
    public boolean stayAlive(int liveNeighbours) {
        return liveNeighbours == 3;
    }

    @Override
    public String getPatternNotation() {
        return "B1234/S3";
    }

    @Override
    public String getName() {
        return "Mazectric";
    }

}
