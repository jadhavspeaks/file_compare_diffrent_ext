package com.filecomparator.parser;

import com.filecomparator.model.FileContent;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class PdfParser implements FileParser {

    private static final Logger logger = LoggerFactory.getLogger(PdfParser.class);

    @Override
    public FileContent parse(String filePath) throws IOException {
        logger.info("Parsing PDF file: {}", filePath);
        FileContent fileContent = new FileContent();

        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            fileContent.setText(text);
            logger.debug("PDF parsing complete. Extracted {} characters.", text.length());
            // Table and image extraction from PDF is a complex task.
            // For now, we are focusing on text extraction.
        }

        return fileContent;
    }
}
