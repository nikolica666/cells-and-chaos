package hr.nik.ant;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Getter
public class LangtonAntTile {

    private String id;
    private double turnDegrees;

}
