package com.filecomparator;

import java.util.Optional;

public class ParserFactory {

    public static Optional<FileParser> getParser(String filePath) {
        String lowerCaseFilePath = filePath.toLowerCase();

        if (lowerCaseFilePath.endsWith(".txt")) {
            return Optional.of(new TxtParser());
        } else if (lowerCaseFilePath.endsWith(".csv")) {
            return Optional.of(new CsvParser());
        } else if (lowerCaseFilePath.endsWith(".xlsx") || lowerCaseFilePath.endsWith(".xls")) {
            return Optional.of(new ExcelParser());
        } else if (lowerCaseFilePath.endsWith(".pdf")) {
            return Optional.of(new PdfParser());
        } else if (lowerCaseFilePath.endsWith(".pptx")) {
            return Optional.of(new PptxParser());
        } else {
            return Optional.empty();
        }
    }
}
