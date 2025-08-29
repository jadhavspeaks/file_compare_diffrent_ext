package com.filecomparator.test;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.filecomparator.model.diff.TableDifference;
import com.filecomparator.parser.CsvParser;
import com.filecomparator.service.ComparatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class TableComparatorTest {

    private ComparatorService comparatorService;

    @BeforeEach
    void setUp() {
        comparatorService = new ComparatorService();
    }

    private FileContent createFileContentWithTable(List<List<String>> table) {
        FileContent content = new FileContent();
        content.addTable(table);
        return content;
    }

    @Test
    void testIdenticalTables() {
        List<List<String>> table = Arrays.asList(
            Arrays.asList("ID", "Name", "Value"),
            Arrays.asList("1", "A", "100")
        );
        FileContent content1 = createFileContentWithTable(table);
        FileContent content2 = createFileContentWithTable(table);
        ComparisonReport report = comparatorService.compare(content1, content2);
        assertTrue(report.getTableDifferences().isEmpty());
    }

    @Test
    void testDeletedColumn() {
        List<List<String>> table1 = Arrays.asList(Arrays.asList("ID", "Name", "Value"), Arrays.asList("1", "A", "100"));
        List<List<String>> table2 = Arrays.asList(Arrays.asList("ID", "Value"), Arrays.asList("1", "100"));
        ComparisonReport report = comparatorService.compare(createFileContentWithTable(table1), createFileContentWithTable(table2));
        assertEquals(1, report.getTableDifferences().size());
        assertEquals(TableDifference.DiffType.COLUMN_DELETED, report.getTableDifferences().get(0).getType());
    }

    @Test
    void testAddedColumn() {
        List<List<String>> table1 = Arrays.asList(Arrays.asList("ID", "Value"), Arrays.asList("1", "100"));
        List<List<String>> table2 = Arrays.asList(Arrays.asList("ID", "Name", "Value"), Arrays.asList("1", "A", "100"));
        ComparisonReport report = comparatorService.compare(createFileContentWithTable(table1), createFileContentWithTable(table2));
        assertEquals(1, report.getTableDifferences().size());
        assertEquals(TableDifference.DiffType.COLUMN_ADDED, report.getTableDifferences().get(0).getType());
    }

    @Test
    void testReorderedColumns() {
        List<List<String>> table1 = Arrays.asList(Arrays.asList("ID", "Name", "Value"), Arrays.asList("1", "A", "100"));
        List<List<String>> table2 = Arrays.asList(Arrays.asList("Value", "ID", "Name"), Arrays.asList("100", "1", "A"));
        ComparisonReport report = comparatorService.compare(createFileContentWithTable(table1), createFileContentWithTable(table2));
        assertTrue(report.getTableDifferences().isEmpty());
    }

    @Test
    void testCellDifferenceWithReorderedColumns() {
        List<List<String>> table1 = Arrays.asList(Arrays.asList("ID", "Name", "Value"), Arrays.asList("1", "A", "100"));
        List<List<String>> table2 = Arrays.asList(Arrays.asList("Value", "ID", "Name"), Arrays.asList("200", "1", "A"));
        ComparisonReport report = comparatorService.compare(createFileContentWithTable(table1), createFileContentWithTable(table2));
        assertEquals(1, report.getTableDifferences().size());
        assertEquals(TableDifference.DiffType.CELL_DIFFERENCE, report.getTableDifferences().get(0).getType());
    }

    @Test
    void testCsvComparisonIsTableOnly() throws IOException {
        File csv1 = new File("test1.csv");
        File csv2 = new File("test2.csv");
        try (FileWriter writer1 = new FileWriter(csv1); FileWriter writer2 = new FileWriter(csv2)) {
            writer1.write("ID,Name\n1,A");
            writer2.write("ID,Name\n1,B");
        }

        CsvParser csvParser = new CsvParser();
        FileContent content1 = csvParser.parse(csv1.getPath());
        FileContent content2 = csvParser.parse(csv2.getPath());

        ComparisonReport report = comparatorService.compare(content1, content2);

        assertEquals(0, report.getTextDifferences().size());
        assertEquals(1, report.getTableDifferences().size());

        csv1.delete();
        csv2.delete();
    }

    @Test
    void testPipeDelimitedFile() throws IOException {
        File psvFile = new File("test.psv");
        try (FileWriter writer = new FileWriter(psvFile)) {
            writer.write("ID|Name|Value\n1|Pipe|100");
        }

        CsvParser csvParser = new CsvParser();
        FileContent content = csvParser.parse(psvFile.getPath());

        assertFalse(content.getTables().isEmpty());
        assertEquals(2, content.getTables().get(0).size()); // Header + 1 data row
        assertEquals("Pipe", content.getTables().get(0).get(1).get(1));

        psvFile.delete();
    }

    @Test
    void testEmptyTableValidationError() throws IOException {
        File emptyCsv = new File("empty.csv");
        try (FileWriter writer = new FileWriter(emptyCsv)) {
            writer.write(""); // Empty file
        }

        IOException exception = assertThrows(IOException.class, () -> {
            com.filecomparator.service.ParserUtils.parseAndValidate(emptyCsv.getPath());
        });

        assertTrue(exception.getMessage().contains("No meaningful table data was found"));

        emptyCsv.delete();
    }
}
