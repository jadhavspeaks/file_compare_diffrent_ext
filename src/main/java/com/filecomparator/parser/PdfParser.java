package com.filecomparator.parser;

import com.filecomparator.model.FileContent;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
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
    public boolean canParse(String input) {
        return input.toLowerCase().endsWith(".pdf");
    }

    @Override
    public FileContent parse(String input) throws IOException {
        logger.info("Parsing PDF file: {}", input);
        FileContent fileContent = new FileContent();

        try (PDDocument document = Loader.loadPDF(new File(input))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            fileContent.setText(pdfStripper.getText(document));

            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
            ObjectExtractor oe = new ObjectExtractor(document);
            PageIterator pi = oe.extract();
            while (pi.hasNext()) {
                Page page = pi.next();
                for (Table table : sea.extract(page)) {
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

            for (PDPage page : document.getPages()) {
                if(page.getResources() == null) continue;
                for (var name : page.getResources().getXObjectNames()) {
                    if (page.getResources().isImageXObject(name)) {
                        fileContent.addImage(((PDImageXObject) page.getResources().getXObject(name)).getImage());
                    }
                }
            }
        }
        return fileContent;
    }
}
