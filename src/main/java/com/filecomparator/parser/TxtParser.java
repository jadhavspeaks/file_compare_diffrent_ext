package com.filecomparator.parser;

import com.filecomparator.model.FileContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class TxtParser implements FileParser {

    private static final Logger logger = LoggerFactory.getLogger(TxtParser.class);
    private static final int MIN_TABLE_ROWS = 3; // Minimum number of consecutive rows to be considered a table

    @Override
    public FileContent parse(String filePath) throws IOException {
        logger.info("Parsing text file: {}", filePath);
        FileContent fileContent = new FileContent();
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        StringBuilder textBuilder = new StringBuilder();
        List<String> potentialTableLines = new ArrayList<>();
        String currentDelimiter = null;
        int lastColumnCount = -1;

        for (String line : lines) {
            String delimiter = guessDelimiter(line);
            int columnCount = (delimiter == null) ? 1 : line.split(Pattern.quote(delimiter)).length;

            if (delimiter != null && (delimiter.equals(currentDelimiter) || currentDelimiter == null) && (columnCount == lastColumnCount || lastColumnCount == -1) && columnCount > 1) {
                potentialTableLines.add(line);
                currentDelimiter = delimiter;
                lastColumnCount = columnCount;
            } else {
                flushTable(potentialTableLines, currentDelimiter, fileContent, textBuilder);
                textBuilder.append(line).append("\n");
                potentialTableLines.clear();
                currentDelimiter = null;
                lastColumnCount = -1;
            }
        }
        flushTable(potentialTableLines, currentDelimiter, fileContent, textBuilder);

        fileContent.setText(textBuilder.toString());
        logger.debug("Text file parsing complete.");
        return fileContent;
    }

    private String guessDelimiter(String line) {
        if (line.split("\\s{2,}").length > 1) return "\\s{2,}"; // Two or more spaces
        if (line.split("\t").length > 1) return "\t"; // Tab
        if (line.split("\\|").length > 1) return "|"; // Pipe
        return null;
    }

    private void flushTable(List<String> potentialTableLines, String delimiter, FileContent fileContent, StringBuilder textBuilder) {
        if (potentialTableLines.size() >= MIN_TABLE_ROWS) {
            logger.info("Detected a potential table with {} rows.", potentialTableLines.size());
            List<List<String>> table = new ArrayList<>();
            for (String tableLine : potentialTableLines) {
                table.add(Arrays.asList(tableLine.split(Pattern.quote(delimiter))));
            }
            fileContent.addTable(table);
        } else {
            for (String line : potentialTableLines) {
                textBuilder.append(line).append("\n");
            }
        }
        potentialTableLines.clear();
    }
}
