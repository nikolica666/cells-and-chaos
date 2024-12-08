package hr.nipeta.cac.gol.file.parser;

import java.io.File;
import java.io.IOException;

public interface GolFileParser {
    GolFileParserResult parse(File file) throws IOException;
}
