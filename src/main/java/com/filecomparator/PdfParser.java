package com.filecomparator;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PdfParser implements FileParser {

    @Override
    public FileContent parse(String filePath) throws IOException {
        FileContent fileContent = new FileContent();

        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            fileContent.setText(text);
            // Table and image extraction from PDF is a complex task.
            // For now, we are focusing on text extraction.
        }

        return fileContent;
    }
}
