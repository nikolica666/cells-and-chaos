package hr.nipeta.cac.gol;

import hr.nipeta.cac.gol.model.GolCellState;
import hr.nipeta.cac.gol.rules.GolRules;
import hr.nipeta.cac.gol.count.NeighbourCount;
import hr.nipeta.cac.model.IntCoordinates;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class GolLogic {

    private final int GRID_SIZE_X;
    private final int GRID_SIZE_Y;
    private final int GRID_SIZE;

    // We allow Born/Survive rules to be changed dynamically
    @Setter private GolRules rules;
    // We allow count rules to be changed dynamically
    @Setter private NeighbourCount neighbourCount;

    @Getter private Set<IntCoordinates> liveCells;
    @Getter private Set<IntCoordinates> deadCells;

    public GolLogic(int gridSizeX, int gridSizeY, GolRules golRules, NeighbourCount neighbourCount) {

        log.info("Initializing X={},Y={},rules={},neighbourCount={}", gridSizeX, gridSizeY, golRules.getPatternNotation(), neighbourCount.getName());

        this.GRID_SIZE_X = gridSizeX;
        this.GRID_SIZE_Y = gridSizeY;
        this.GRID_SIZE = gridSizeX * gridSizeY;
        this.rules = golRules;
        this.neighbourCount = neighbourCount;
        this.liveCells = new HashSet<>();
        this.deadCells = new HashSet<>();
    }

    public void randomize() {

        this.liveCells = new HashSet<>();
        this.deadCells = new HashSet<>();

        for (int row = 0; row < GRID_SIZE_Y; row++) {
            for (int col = 0; col < GRID_SIZE_X; col++) {
                IntCoordinates coordinates = IntCoordinates.of(col, row);
                // TODO this will not work OK if we introduce more then 2 states
                if (new Random().nextBoolean()) {
                    liveCells.add(coordinates);
                } else {
                    deadCells.add(coordinates);
                }
            }
        }
    }

    public void evolve() {

        Map<IntCoordinates, Integer> neighborCounts = neighbourCount.count(liveCells, GRID_SIZE_X, GRID_SIZE_Y);

        neighborCounts.forEach((n,c) -> {
            log.trace("{} has {} live neighbors", n, c);
        });

        Set<IntCoordinates> newLiveCells = new HashSet<>();
        Set<IntCoordinates> newDeadCells = new HashSet<>();

        for (Map.Entry<IntCoordinates, Integer> entry : neighborCounts.entrySet()) {
            IntCoordinates cell = entry.getKey();
            int liveNeighbours = entry.getValue();
            boolean cellCurrentlyAlive = liveCells.contains(cell);
            if (cellCurrentlyAlive) {
                if (rules.stayAlive(liveNeighbours)) {
                    newLiveCells.add(cell);
                    log.trace("{} added to newLiveCells (was already alive)", cell);
                } else {
                    newDeadCells.add(cell);
                    log.trace("{} added to newDeadCells", cell);
                }
            } else {
                if (rules.becomeAlive(liveNeighbours)) {
                    newLiveCells.add(cell); // Becomes alive
                    log.trace("{} added to newLiveCells (was not already alive)", cell);
                }

            }
        }

        Set<IntCoordinates> liveCellsThatBecameDead = new HashSet<>(liveCells);
        liveCellsThatBecameDead.removeAll(newLiveCells);

        liveCellsThatBecameDead.forEach(c -> {
            log.trace("live cell {} became dead", c);
        });

        // Live cells are just new live cells
        liveCells = newLiveCells;

        liveCells.forEach(c -> {
            log.trace("live cell is {}", c);
        });

        // Dead cells are new dead cell?
        deadCells = newDeadCells;
        deadCells.addAll(liveCellsThatBecameDead);
        deadCells.forEach(c -> {
            log.trace("dead cell is {}", c);
        });

        log.debug("number of live cells is {}/{}", liveCells.size(), GRID_SIZE);
        log.debug("number of dead cells is {}/{}", deadCells.size(), GRID_SIZE);

    }

    public GolCellState toggle(int row, int col) {

        // TODO Needs refactoring if we gonna introduce more states, horrible logic
        IntCoordinates coordinates = IntCoordinates.of(col, row);

        final GolCellState newState;
        if (liveCells.contains(coordinates)) {
            log.debug("{} is live cell, removing, and adding to dead", coordinates);
            newState = GolCellState.DEAD;
            liveCells.remove(coordinates);
            // Need to add to dead cells so canvas knows what to redraw
            deadCells.add(coordinates);
        } else if (deadCells.contains(coordinates)) {
            log.debug("{} is dead cell, removing, and adding to live", coordinates);
            newState = GolCellState.ALIVE;
            deadCells.remove(coordinates);
            // Need to add to live cells so canvas knows what to redraw
            liveCells.add(coordinates);
        } else {
            log.debug("{} is empty cell, adding to live", coordinates);
            newState = GolCellState.ALIVE;
            // Set ALIVE, as it was "dead" i.e. didn't even exist
            liveCells.add(coordinates);
        }

        return newState;

    }

    public void setAllDead() {

        this.liveCells = new HashSet<>();
        this.deadCells = new HashSet<>();


    }
}
