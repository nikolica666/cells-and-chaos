package hr.nipeta.cac.gol.file.parser;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GolFileParserResult {
    private int sizeX, sizeY;
    private boolean startRelative;
    private int startX, startY;
    private String rule;
    private List<int[]> liveCells;
    private List<String> comments = new ArrayList<>();
    private String name;
    private String createdBy;
}
