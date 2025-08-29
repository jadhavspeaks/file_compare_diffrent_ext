package com.filecomparator.parser;

import com.filecomparator.model.FileContent;
import org.apache.poi.xslf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PptxParser implements FileParser {

    private static final Logger logger = LoggerFactory.getLogger(PptxParser.class);

    @Override
    public boolean canParse(String input) {
        return input.toLowerCase().endsWith(".pptx");
    }

    @Override
    public FileContent parse(String input) throws IOException {
        logger.info("Parsing PowerPoint file: {}", input);
        FileContent fileContent = new FileContent();
        StringBuilder textBuilder = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(new File(input));
             XMLSlideShow ppt = new XMLSlideShow(fis)) {

            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        textBuilder.append(((XSLFTextShape) shape).getText()).append("\n");
                    } else if (shape instanceof XSLFTable) {
                        XSLFTable pptxTable = (XSLFTable) shape;
                        List<List<String>> table = new ArrayList<>();
                        for (XSLFTableRow row : pptxTable.getRows()) {
                            List<String> tableRow = new ArrayList<>();
                            for (XSLFTableCell cell : row.getCells()) {
                                tableRow.add(cell.getText());
                            }
                            table.add(tableRow);
                        }
                        fileContent.addTable(table);
                    }
                }
            }
        }
        fileContent.setText(textBuilder.toString());
        return fileContent;
    }
}
