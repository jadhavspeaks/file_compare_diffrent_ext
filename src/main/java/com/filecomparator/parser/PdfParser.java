package com.filecomparator.parser;

import com.filecomparator.model.FileContent;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import technology.tabula.PageIterator;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfParser implements FileParser {

    private static final Logger logger = LoggerFactory.getLogger(PdfParser.class);

    @Override
    public FileContent parse(String filePath) throws IOException {
        logger.info("Parsing PDF file: {}", filePath);
        FileContent fileContent = new FileContent();

        try (PDDocument document = PDDocument.load(new File(filePath))) {
            // Extract text
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            fileContent.setText(text);
            logger.debug("PDF text extraction complete. Extracted {} characters.", text.length());

            // Extract tables with Tabula
            logger.info("Extracting tables from PDF file: {}", filePath);
            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
            ObjectExtractor oe = new ObjectExtractor(document);
            PageIterator pi = oe.extract();

            while (pi.hasNext()) {
                Page page = pi.next();
                List<Table> tables = sea.extract(page);
                for (Table table : tables) {
                    List<List<String>> convertedTable = new ArrayList<>();
                    for (List<RectangularTextContainer> row : table.getRows()) {
                        List<String> rowData = new ArrayList<>();
                        for (RectangularTextContainer cell : row) {
                            rowData.add(cell.getText().replace("\r", " "));
                        }
                        convertedTable.add(rowData);
                    }
                    fileContent.addTable(convertedTable);
                }
            }
            logger.info("PDF table extraction complete. Found {} tables.", fileContent.getTables().size());

            // Extract images with PDFBox
            logger.info("Extracting images from PDF file: {}", filePath);
            for (PDPage page : document.getPages()) {
                for (COSName name : page.getResources().getXObjectNames()) {
                    if (page.getResources().isImageXObject(name)) {
                        fileContent.addImage(((org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) page.getResources().getXObject(name)).getImage());
                    }
                }
            }
            logger.info("PDF image extraction complete. Found {} images.", fileContent.getImages().size());
        }

        return fileContent;
    }
}
