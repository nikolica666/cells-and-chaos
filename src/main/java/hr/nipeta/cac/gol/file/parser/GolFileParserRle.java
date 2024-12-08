package hr.nipeta.cac.gol;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Parsing 'Run Length Encoded' (.rle) files.
 * @see <a href="https://conwaylife.com/wiki/Run_Length_Encoded">Game of Life wiki</a>
 */
@Slf4j
public class GolFileParserRle {

    private final GolFileParserResult result = new GolFileParserResult();

    public static GolFileParserResult parse(File file) throws IOException {
        return new GolFileParserRle().parseFile(file);
    }

    private GolFileParserResult parseFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return parseLineByLine(reader);
        }
    }

    private GolFileParserResult parseLineByLine(BufferedReader reader) throws IOException {

        String line;
        String golRowsFromFileInSingleRow = ""; // We concatenate all data rows into single String, it's simpler

        boolean headerLineRead = false;

        while ((line = reader.readLine()) != null) {

            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            // Read comments
            if (line.startsWith("#")) {
                if (headerLineRead) {
                    throw new IllegalStateException("Lines with # must come before header line");
                }
                parseHashtagLine(line);
            } else {

                // We can't replace hashtag lines, because then we can't know starting coordinates
                line = line.replaceAll("\\s+", "");

                // Read header
                if (line.startsWith("x")) {
                    // Not gonna allow more then 1 header row
                    if (headerLineRead) {
                        throw new IllegalStateException("Header line is already read! There can only be one!");
                    }
                    parseHeaderLine(line);
                    headerLineRead = true;
                }
                // Read data
                else {
                    if (!headerLineRead) {
                        throw new IllegalStateException("Header line must be read before data lines!");
                    }
                    // Line with ! means it is last line, as everything beyond ! should be ignored
                    if (line.contains("!")) {
                        golRowsFromFileInSingleRow += line.split("!")[0];
                        break;
                    } else {
                        golRowsFromFileInSingleRow += line;
                    }

                }
            }

        }

        if (!headerLineRead) {
            throw new IllegalStateException("No header line found!");
        }

        if (golRowsFromFileInSingleRow.isEmpty()) {
            throw new IllegalStateException("No data found!");
        }

        // Rows are separated by $ sign
        String[] golRowsFromFile = golRowsFromFileInSingleRow.split("\\$");

        // Number of rows should be less or equal compared to header y value (if it's less, missing rows are all 0s)
        if (golRowsFromFile.length > result.sizeY) {
            throw new IllegalStateException(String.format(
                    "Header declared %s rows, data has %s!",
                    result.sizeY,
                    golRowsFromFile.length));
        }

        List<int[]> liveCells = new ArrayList<>(golRowsFromFile.length);
        for (String golRowFromFile : golRowsFromFile) {

            // TODO Number of cols should be less or equal compared to header x value
            int[] liveCellsInCurrentRow = new int[result.sizeX];

            char[] golRowFromFileCharArray = golRowFromFile.toCharArray();
            int colIndex = 0;
            String currentNumberStr = "";

            for (char c : golRowFromFileCharArray) {
                // Accumulate digits
                if (Character.isDigit(c)) {
                    currentNumberStr += Character.toString(c);
                }
                // If we're done with digits, we're on 'c' (dead) or 'o' (live) cells
                else if (c == 'o' || c == 'b') {
                    // Parse accumulated number so we know how much there is in a row
                    int numberOfSameCells = currentNumberStr.isEmpty() ? 1 : Integer.parseInt(currentNumberStr);
                    // If they're live cells, set value to 1 (everything is at 0 when array is initialized)
                    if (c == 'o') {
                        for (int i = colIndex; i < colIndex + numberOfSameCells; i++) {
                            liveCellsInCurrentRow[i] = 1;
                        }
                    }
                    // Increment column index so we know where start of next 'block' is, and reset accumulated Str
                    colIndex += numberOfSameCells;
                    currentNumberStr = "";
                } else {
                    throw new IllegalStateException("Unexpected character '" + c + "' in data row (expecting digit or 'o' or 'b')");
                }
            }

            liveCells.add(liveCellsInCurrentRow);
        }

        result.liveCells = liveCells;

        return result;

    }

    /**
     * Explanation of hashtag lines (they start with #):
     *
     * <ul>
     *     <li>#C = Indicates that a line of comment follows</li>
     *     <li>#c = Same as C, but not recommended</li>
     *     <li>#N = The name of the pattern (e.g. 'My awesome pattern'))</li>
     *     <li>#O = When and by whom the file was created (e.g. 'John Smith js@mail.com Fri Apr 30 19:38:52 1999')</li>
     *     <li>#P = Where to put pattern, absolute coordinates as viewed from top-left corner</li>
     *     <li>#R = Where to put pattern, relative coordinates as viewed from center of the grid (may be negative)</li>
     *     <li>#r = Rules in survival/birth pattern (e.g. '23/3' for Conway's life)</li>
     * </ul>
     **/
    private void parseHashtagLine(String line) {
        String lineChar = line.substring(1,2);
        String lineContent = line.substring(2).trim();
        switch (lineChar) {
            case "C", "c" -> this.result.comments.add(lineContent);
            case "N" -> {
                if (this.result.name == null) {
                    this.result.name = lineContent;
                } else {
                    this.result.name += "\r\n" + lineContent;
                }
            }
            case "O" -> {
                if (this.result.createdBy == null) {
                    this.result.createdBy = lineContent;
                } else {
                    this.result.createdBy += "\r\n" + lineContent;
                }
            }
            case "P" -> throw new IllegalStateException();
            case "R" -> throw new IllegalStateException();
            case "r" -> {
                String[] lineContentSplit = lineContent.split("/");
                this.result.rule = String.format("B%s/S%s", lineContentSplit[1], lineContentSplit[2]);
            }
        }
    }

    /**
     * Line should be in format <pre>{@code x=2,y=3,rule=B3/S23}</pre>
     * x and y are required, rule is optional, if omitted, default Conway's rule is set
     */
    private void parseHeaderLine(String line) {
        String[] headerLineSplit = line.split(",");
        this.result.sizeX = Integer.parseInt(headerLineSplit[0].split("=")[1]);
        this.result.sizeY = Integer.parseInt(headerLineSplit[1].split("=")[1]);

        // Header can be also set with hashtag line r e.g. '#r 23/3' for Conway's rules
        // If rule is in this header line, set it; if not, and rule is not in '#r' line, set default Conway's
        if (headerLineSplit.length == 3) {
            if (this.result.rule != null) {
                log.debug("I'm overwriting already set rule, because there is a rule in RLE header line");
            }
            this.result.rule = headerLineSplit[2];
        } else {
            if (this.result.rule == null) {
                this.result.rule = "B3/S23"; // Set default Conway's rules
            } else {
                log.debug("I'm ignoring setting default Conway's rule B3/S23 because some rule is already set");
            }
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
