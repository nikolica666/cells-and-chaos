package hr.nipeta.cac.gol.file.parser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GolFileParserFactory {

    private static final Map<String, Supplier<? extends GolFileParser>> PARSER_REGISTRY = new HashMap<>();

    static {
        registerParser("rle", GolFileParserRle::new);
        registerParser("cells", GolFileParserPlain::new);
    }

    public static void registerParser(String extension, Supplier<? extends GolFileParser> parserSupplier) {
        PARSER_REGISTRY.put(extension.toLowerCase(), parserSupplier);
    }

    public static GolFileParserResult parse(File file) throws IOException {
        return getParser(file).parse(file);
    }

    public static GolFileParser getParser(File file) {
        String extension = getFileExtension(file.getName());
        Supplier<? extends GolFileParser> parserSupplier = PARSER_REGISTRY.get(extension);

        if (parserSupplier == null) {
            throw new UnsupportedOperationException(
                    String.format("No Game of Life parser available for file extension '%s'", extension));
        }

        return parserSupplier.get();
    }

    private static String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return (lastIndex > 0) ? fileName.substring(lastIndex + 1).toLowerCase() : "";
    }

}
