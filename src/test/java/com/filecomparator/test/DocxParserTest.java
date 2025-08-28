package com.filecomparator.test;

import com.filecomparator.model.FileContent;
import com.filecomparator.parser.DocxParser;
import org.apache.poi.xwpf.usermodel.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DocxParserTest {

    private static final String TEST_FILE_PATH = "test_document.docx";
    private DocxParser parser;

    @BeforeEach
    void setUp() throws IOException {
        parser = new DocxParser();
        createTestDocument();
    }

    @AfterEach
    void tearDown() {
        new File(TEST_FILE_PATH).delete();
    }

    @Test
    void testParseDocxFile() throws IOException {
        // When
        FileContent fileContent = parser.parse(TEST_FILE_PATH);

        // Then
        assertNotNull(fileContent);

        // Verify text
        String text = fileContent.getText();
        assertTrue(text.contains("This is a test paragraph."));
        assertTrue(text.contains("This is another test paragraph."));

        // Verify table
        List<List<List<String>>> tables = fileContent.getTables();
        assertFalse(tables.isEmpty());
        List<List<String>> firstTable = tables.get(0);
        assertEquals(2, firstTable.size()); // 2 rows
        assertEquals("Header 1", firstTable.get(0).get(0));
        assertEquals("Header 2", firstTable.get(0).get(1));
        assertEquals("Row 1, Cell 1", firstTable.get(1).get(0));
        assertEquals("Row 1, Cell 2", firstTable.get(1).get(1));

        // Verify no images, since we didn't add any
        assertTrue(fileContent.getImages().isEmpty());
    }

    private void createTestDocument() throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(TEST_FILE_PATH)) {

            // Add paragraphs
            document.createParagraph().createRun().setText("This is a test paragraph.");
            document.createParagraph().createRun().setText("This is another test paragraph.");

            // Add a table
            XWPFTable table = document.createTable(2, 2);
            table.getRow(0).getCell(0).setText("Header 1");
            table.getRow(0).getCell(1).setText("Header 2");
            table.getRow(1).getCell(0).setText("Row 1, Cell 1");
            table.getRow(1).getCell(1).setText("Row 1, Cell 2");

            document.write(out);
        }
    }
}
