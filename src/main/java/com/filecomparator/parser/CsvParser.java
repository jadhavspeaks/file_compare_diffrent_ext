package com.filecomparator.parser;

import com.filecomparator.model.FileContent;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {

            List<List<String>> table = csvParser.getRecords().stream()
                    .map(CSVRecord::toList)
                    .collect(Collectors.toList());
            fileContent.addTable(table);
            logger.debug("CSV parsing complete. Found {} rows.", table.size());
        }

        return fileContent;
    }
}
