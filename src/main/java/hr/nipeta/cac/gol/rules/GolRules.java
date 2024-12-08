package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;
import hr.nipeta.cac.gol.model.GolCellState;

import java.util.Arrays;
import java.util.List;

/**
 * Rules for classical Born/Survive rules i.e. 2 GolCellStates
 */
public abstract class GolRules {

    public abstract boolean becomeAlive(GolCell[] neighbours);
    public abstract boolean becomeAlive(int liveNeighbours);
    public abstract boolean stayAlive(GolCell[] neighbours);
    public abstract boolean stayAlive(int liveNeighbours);

    // TODO refactor this altough we'll probably end up deleting and use something optimal
    final int countLiveNeighbours(GolCell[] neighbours, Integer enoughLiveNeighbours) {

        int numberOfLiveNeighbours = 0;

        for (GolCell neighbour : neighbours) {
            if (numberOfLiveNeighbours >= enoughLiveNeighbours) {
                break;
            }
            if (neighbour.getCurrentState() == GolCellState.ALIVE) {
                numberOfLiveNeighbours++;
            }
        }

        return numberOfLiveNeighbours;

    }

    final int countLiveNeighbours(GolCell[] neighbours) {

        int numberOfLiveNeighbours = 0;

        for (GolCell neighbour : neighbours) {
            if (neighbour.getCurrentState() == GolCellState.ALIVE) {
                numberOfLiveNeighbours++;
            }
        }

        return numberOfLiveNeighbours;

    }

    /**
     *
     * @return pattern notation in Bx/Sy format (B = born, S = survive)
     */
    public abstract String getPatternNotation();

    /**
     * @return name of pattern (e.g. Conway, Annel, Day and Night)
     */
    public abstract String getName();

    public static final List<String> GAME_OF_LIFE_RULE_LABELS = Arrays.asList(
            "Conway (B3/S23)",
            "Anneal (B4678/S35678)",
            "DayAndNight (B3678/S34678)",
            "Diamoeba (B35678/S5678)",
            "Fredkin (B1357/S02468)",
            "HighLife (B36/S23)",
            "Maze (B3/S12345)",
            "Mazectric (B1234/S3)",
            "Morley (B368/S245)",
            "Replicator (B1357/S1357)",
            "Seeds (B2/S)"
    );

    public static GolRules fromLabel(String ruleLabel) {
        return switch (ruleLabel) {
            case "Conway (B3/S23)" -> new GolConwayRules();
            case "Anneal (B4678/S35678)" -> new GolAnnealRules();
            case "DayAndNight (B3678/S34678)" -> new GolDayAndNightRules();
            case "Diamoeba (B35678/S5678)" -> new GolDiamoebaRules();
            case "Fredkin (B1357/S02468)" -> new GolFredkinRules();
            case "HighLife (B36/S23)" -> new GolHighLifeRules();
            case "Maze (B3/S12345)" -> new GolMazeRules();
            case "Mazectric (B1234/S3)" -> new GolMazectric();
            case "Morley (B368/S245)" -> new GolMorleyRules();
            case "Replicator (B1357/S1357)" -> new GolReplicatorRules();
            case "Seeds (B2/S)" -> new GolSeedsRules();
            default -> throw new IllegalStateException(String.format("Unknown game of life label '%s'", ruleLabel));
        };
    }

}
