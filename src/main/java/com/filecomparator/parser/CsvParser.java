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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvParser implements FileParser {

    private static final Logger logger = LoggerFactory.getLogger(CsvParser.class);

    @Override
    public boolean canParse(String input) {
        return input.toLowerCase().endsWith(".csv");
    }

    @Override
    public FileContent parse(String input) throws IOException {
        logger.info("Parsing CSV file: {}", input);
        FileContent fileContent = new FileContent();

        // Only extract table data, not raw text, to avoid redundant comparisons.
        try (Reader reader = new FileReader(input)) {
            char delimiter = detectDelimiter(input);
            // Use withFirstRecordAsHeader() to correctly handle the header row
            CSVFormat csvFormat = CSVFormat.DEFAULT.withDelimiter(delimiter).withFirstRecordAsHeader();
            CSVParser csvParser = new CSVParser(reader, csvFormat);

            // Add header to the table data
            List<String> header = csvParser.getHeaderNames();
            List<List<String>> tableData = new ArrayList<>();
            tableData.add(header);

            // Add records
            for (CSVRecord record : csvParser) {
                tableData.add(record.toList());
            }

            fileContent.addTable(tableData);
            logger.debug("CSV parsing complete. Found {} data rows.", tableData.size() - 1);
        }

        return fileContent;
    }

    private char detectDelimiter(String input) throws IOException {
        List<Character> delimiters = Arrays.asList(',', ';', '\t', '|');
        Map<Character, Integer> delimiterCounts = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(input))) {
            String line = reader.readLine(); // Only check the first line (usually the header)
            if (line != null) {
                for (char delim : delimiters) {
                    int count = (int) line.chars().filter(c -> c == delim).count();
                    if (count > 0) {
                        delimiterCounts.put(delim, count);
                    }
                }
            }
        }

        if (delimiterCounts.isEmpty()) {
            return ','; // Default
        }

        return Collections.max(delimiterCounts.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
}
