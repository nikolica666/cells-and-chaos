package hr.nipeta.cac.gol;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parsing 'Plaintext' (.cells) files.
 * @see <a href="https://conwaylife.com/wiki/plaintext">Game of Life wiki</a>
 */
@Slf4j
public class GolFileParserPlain {

    private final GolFileParserResult result = new GolFileParserResult();

    public static GolFileParserResult parse(File file) throws IOException {
        return new GolFileParserPlain().parseFile(file);
    }

    private GolFileParserResult parseFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return parseLineByLine(reader);
        }
    }

    private GolFileParserResult parseLineByLine(BufferedReader reader) throws IOException {

        String line;
        List<int[]> liveCells = new ArrayList<>();

        boolean headerLineRead = false;

        while ((line = reader.readLine()) != null) {

            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            // Read comments and line with 'Name:'
            if (line.startsWith("!")) {
                parseExclamationLine(line);
            } else {

                // Lines contain dots (dead) and capital O (live) cells info
                line = line.replaceAll("\\s+", "");
                char[] lineCharArray = line.toCharArray();

                int numberOfColumns = lineCharArray.length;
                int[] row = new int[numberOfColumns];

                for (int i = 0; i < numberOfColumns; i++) {
                    char c = lineCharArray[i];
                    if (c == 'O') {
                        row[i] = 1;
                    } else {
                        if (c != '.') {
                            throw new IllegalStateException("Unexpected character '" + c + "' (expecting 'O' or '.')");
                        }
                    }
                }

                liveCells.add(row);

            }

        }

        result.liveCells = liveCells;

        return result;

    }

    /**
     * Explanation of exclamation lines (they start with !):
     *
     * <ul>
     *    <li>!Name: = The name of the pattern (e.g. '!Name: My awesome pattern'))</li>
     *    <li>! = Comment (e.g. '!This is my comment')</li>
     * </ul>
     **/
    private void parseExclamationLine(String line) {
        if (line.startsWith("!Name:")) {
            result.name = line.substring(line.indexOf("!Name:"));
        } else {
            result.comments.add(line.substring(1));
        }
    }

    @Data
    public static class GolFileParserResult {
        private int sizeX, sizeY;
        private boolean startRelative;
        private int startX, startY;
        private String rule;
        private List<int[]> liveCells;
        private List<String> comments = new ArrayList<>();
        private String name;
        private String createdBy;
    }
}
