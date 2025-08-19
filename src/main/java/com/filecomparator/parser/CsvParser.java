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
import java.util.List;
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
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String header = reader.readLine();
            if (header == null) {
                return ','; // Default to comma for empty files
            }
            if (header.contains(";")) return ';';
            if (header.contains("\t")) return '\t';
            return ','; // Default
        }
    }
}
