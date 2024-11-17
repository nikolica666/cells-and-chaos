package hr.nik.gol;

import hr.nik.model.Coordinates;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(of = {"coordinates"})
public class GolCell {

    @Setter private boolean alive;
    @Setter private boolean aliveNext;

    private final Coordinates<Integer> coordinates;

    public GolCell(Coordinates<Integer> coordinates, boolean alive) {
        this.coordinates = coordinates;
        this.alive = alive;
    }

    public static GolCell createDead(Coordinates<Integer> coordinates) {
        return create(coordinates, false);
    }

    public static GolCell create(Coordinates<Integer> coordinates, boolean isAlive) {
        return new GolCell(coordinates, isAlive);
    }

    public void toggleAlive() {
        this.alive = !this.alive;
    }

    public boolean setAndReturnNextState() {
        this.alive = this.aliveNext;
        return this.alive;
    }

    public void clear() {
        this.alive = false;
    }

}
