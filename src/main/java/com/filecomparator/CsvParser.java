package com.filecomparator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CsvParser implements FileParser {

    @Override
    public FileContent parse(String filePath) throws IOException {
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
        }

        return fileContent;
    }
}
