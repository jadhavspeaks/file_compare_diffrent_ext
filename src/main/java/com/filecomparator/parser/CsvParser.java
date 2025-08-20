package com.filecomparator.parser;

import com.filecomparator.model.FileContent;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvParser implements FileParser {

    private static final Logger logger = LoggerFactory.getLogger(CsvParser.class);

    @Override
    public FileContent parse(String filePath) throws IOException {
        logger.info("Parsing CSV file: {}", filePath);
        FileContent fileContent = new FileContent();

        // Extract raw text
        String text = new String(Files.readAllBytes(Paths.get(filePath)));
        fileContent.setText(text);

        // Extract table data
        try (Reader reader = new FileReader(filePath)) {
            char delimiter = detectDelimiter(filePath);
            CSVFormat csvFormat = CSVFormat.DEFAULT.withDelimiter(delimiter).withHeader();
            CSVParser csvParser = new CSVParser(reader, csvFormat);

            List<List<String>> table = csvParser.getRecords().stream()
                    .map(CSVRecord::toList)
                    .collect(Collectors.toList());
            fileContent.addTable(table);
            logger.debug("CSV parsing complete. Found {} rows.", table.size());
        }

        return fileContent;
    }

    private char detectDelimiter(String filePath) throws IOException {
        List<Character> delimiters = Arrays.asList(',', ';', '\t', '|');
        Map<Character, Integer> delimiterCounts = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            int lineCount = 0;
            String line;
            while ((line = reader.readLine()) != null && lineCount < 5) { // Check first 5 lines
                for (char delim : delimiters) {
                    int count = (int) line.chars().filter(c -> c == delim).count();
                    if (count > 0) {
                        delimiterCounts.put(delim, delimiterCounts.getOrDefault(delim, 0) + count);
                    }
                }
                lineCount++;
            }
        }

        if (delimiterCounts.isEmpty()) {
            return ','; // Default
        }

        // Return the delimiter with the highest count
        return Collections.max(delimiterCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
}
