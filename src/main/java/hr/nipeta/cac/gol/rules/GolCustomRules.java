package hr.nipeta.cac.gol.rules;

import hr.nipeta.cac.gol.model.GolCell;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.joining;

@Slf4j
public class GolCustomRules implements GolRules {

    private final Set<Integer> toBecomeAlive;
    private final Set<Integer> toStayAlive;
    private final String patternNotation;

    public static boolean convertAndValidatePattern(String pattern) {

        if (pattern == null) {
            return false;
        }

        pattern = pattern.replaceAll("\\s+", "");

        if (pattern.isEmpty()) {
            return false;
        }

        if (!pattern.matches("B[0-8]*/S[0-8]*")) {
            return false;
        }

        return true;

    }

    public GolCustomRules(String patternNotation) {

        patternNotation = patternNotation.replaceAll("\\s+", "");

        if (!patternNotation.matches("B[0-8]*/S[0-8]*")) {
            throw new RuntimeException("Bad pattern, expecting Bx/Sy (born/survive) and numbers 0-8 (or no number)");
        }

        this.patternNotation = patternNotation;

        this.toBecomeAlive = new HashSet<>();
        this.toStayAlive = new HashSet<>();

        String[] splitBS = patternNotation.split("/");

        for (char c : splitBS[0].toCharArray()) {
            if (Character.isDigit(c)) { // Check if the character is a digit
                toBecomeAlive.add(Character.getNumericValue(c));
            }
        }

        for (char c : splitBS[1].toCharArray()) {
            if (Character.isDigit(c)) { // Check if the character is a digit
                toStayAlive.add(Character.getNumericValue(c));
            }
        }

        log.info("Created custom rules {}", patternNotation);

    }

    public GolCustomRules(Set<Integer> toBecomeAlive, Set<Integer> toStayAlive) {
        this.toBecomeAlive = toBecomeAlive;
        this.toStayAlive = toStayAlive;
        this.patternNotation = "B" + joinNumbers(toBecomeAlive) + "/" + "S" + joinNumbers(toStayAlive);
        System.out.println("custom pattern " + patternNotation);
    }

    private String joinNumbers(Collection<Integer> numbers) {
        return numbers.stream().map(String::valueOf).collect(joining());
    }

    @Override
    public boolean becomeAlive(GolCell[] neighbours) {
        return becomeAlive(countLiveNeighbours(neighbours));
    }

    @Override
    public boolean becomeAlive(int liveNeighbours) {
        return toBecomeAlive.contains(liveNeighbours);
    }

    @Override
    public boolean stayAlive(GolCell[] neighbours) {
        return stayAlive(countLiveNeighbours(neighbours));
    }

    @Override
    public boolean stayAlive(int liveNeighbours) {
        return toStayAlive.contains(liveNeighbours);
    }

    @Override
    public String getPatternNotation() {
        return patternNotation;
    }
}