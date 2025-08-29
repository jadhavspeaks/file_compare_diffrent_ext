package com.filecomparator.service;

import com.filecomparator.model.FileContent;
import com.filecomparator.parser.FileParser;
import java.io.IOException;
import java.util.Optional;

public class ParserUtils {

    public static FileContent parseAndValidate(String source) throws IOException {
        Optional<FileParser> parserOpt = ParserFactory.getParser(source);
        if (parserOpt.isEmpty()) {
            throw new IOException("Unsupported input type for: " + source);
        }

        FileContent content = parserOpt.get().parse(source);

        if (isTableExpected(source) && !hasMeaningfulTableData(content)) {
            throw new IOException("No meaningful table data was found in the source file: " + source);
        }

        return content;
    }

    private static boolean isTableExpected(String source) {
        String lower = source.toLowerCase();
        return lower.endsWith(".csv") || lower.endsWith(".tsv") || lower.endsWith(".psv") ||
               lower.endsWith(".xls") || lower.endsWith(".xlsx");
    }

    private static boolean hasMeaningfulTableData(FileContent content) {
        if (content.getTables().isEmpty()) {
            return false;
        }
        // Check if at least one table has more than one row (i.e., more than just a header)
        return content.getTables().stream().anyMatch(table -> table.size() > 1);
    }
}
