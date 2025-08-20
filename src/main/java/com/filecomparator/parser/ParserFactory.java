package com.filecomparator.parser;

import java.util.Optional;

public class ParserFactory {

    public static Optional<FileParser> getParser(String input) {
        String lowerCaseInput = input.toLowerCase();

        if (lowerCaseInput.startsWith("http://") || lowerCaseInput.startsWith("https://")) {
            return Optional.of(new UrlParser());
        } else if (lowerCaseInput.endsWith(".txt")) {
            return Optional.of(new TxtParser());
        } else if (lowerCaseInput.endsWith(".csv")) {
            return Optional.of(new CsvParser());
        } else if (lowerCaseInput.endsWith(".xlsx") || lowerCaseInput.endsWith(".xls")) {
            return Optional.of(new ExcelParser());
        } else if (lowerCaseInput.endsWith(".pdf")) {
            return Optional.of(new PdfParser());
        } else if (lowerCaseInput.endsWith(".pptx")) {
            return Optional.of(new PptxParser());
        } else {
            return Optional.empty();
        }
    }
}
