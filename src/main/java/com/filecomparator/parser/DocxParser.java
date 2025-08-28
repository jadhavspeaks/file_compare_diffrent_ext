package com.filecomparator.parser;

import com.filecomparator.model.FileContent;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DocxParser implements FileParser {

    private static final Logger logger = LoggerFactory.getLogger(DocxParser.class);

    @Override
    public boolean canParse(String input) {
        return input.toLowerCase().endsWith(".docx");
    }

    @Override
    public FileContent parse(String input) throws IOException {
        logger.info("Parsing DOCX file: {}", input);
        FileContent fileContent = new FileContent();
        StringBuilder textBuilder = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(new File(input));
             XWPFDocument document = new XWPFDocument(fis)) {

            // Extract text from paragraphs
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                textBuilder.append(paragraph.getText()).append("\n");
            }
            fileContent.setText(textBuilder.toString());
            logger.debug("DOCX text extraction complete.");

            // Extract tables
            for (XWPFTable table : document.getTables()) {
                List<List<String>> convertedTable = new ArrayList<>();
                for (XWPFTableRow row : table.getRows()) {
                    List<String> rowData = new ArrayList<>();
                    for (XWPFTableCell cell : row.getTableCells()) {
                        rowData.add(cell.getText());
                    }
                    convertedTable.add(rowData);
                }
                fileContent.addTable(convertedTable);
            }
            logger.info("DOCX table extraction complete. Found {} tables.", fileContent.getTables().size());

            // Extract images
            for (XWPFPictureData picture : document.getAllPictures()) {
                byte[] bytes = picture.getData();
                fileContent.addImage(ImageIO.read(new ByteArrayInputStream(bytes)));
            }
            logger.info("DOCX image extraction complete. Found {} images.", fileContent.getImages().size());
        }

        return fileContent;
    }
}
