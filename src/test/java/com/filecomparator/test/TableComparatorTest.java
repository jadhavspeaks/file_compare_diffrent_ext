package com.filecomparator.test;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.filecomparator.model.diff.TableDifference;
import com.filecomparator.service.ComparatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

        assertTrue(report.getTableDifferences().isEmpty(), "Identical tables should have no differences.");
    }

    @Test
    void testDeletedColumn() {
        List<List<String>> table1 = Arrays.asList(
            Arrays.asList("ID", "Name", "Value"),
            Arrays.asList("1", "A", "100")
        );
        List<List<String>> table2 = Arrays.asList(
            Arrays.asList("ID", "Value"),
            Arrays.asList("1", "100")
        );
        FileContent content1 = createFileContentWithTable(table1);
        FileContent content2 = createFileContentWithTable(table2);

        ComparisonReport report = comparatorService.compare(content1, content2);

        assertEquals(1, report.getTableDifferences().size());
        TableDifference diff = report.getTableDifferences().get(0);
        assertEquals(TableDifference.DiffType.COLUMN_DELETED, diff.getType());
        assertEquals("Name", diff.getValue1());
    }

    @Test
    void testAddedColumn() {
        List<List<String>> table1 = Arrays.asList(
            Arrays.asList("ID", "Value"),
            Arrays.asList("1", "100")
        );
        List<List<String>> table2 = Arrays.asList(
            Arrays.asList("ID", "Name", "Value"),
            Arrays.asList("1", "A", "100")
        );
        FileContent content1 = createFileContentWithTable(table1);
        FileContent content2 = createFileContentWithTable(table2);

        ComparisonReport report = comparatorService.compare(content1, content2);

        assertEquals(1, report.getTableDifferences().size());
        TableDifference diff = report.getTableDifferences().get(0);
        assertEquals(TableDifference.DiffType.COLUMN_ADDED, diff.getType());
        assertEquals("Name", diff.getValue2());
    }

    @Test
    void testReorderedColumns() {
        List<List<String>> table1 = Arrays.asList(
            Arrays.asList("ID", "Name", "Value"),
            Arrays.asList("1", "A", "100")
        );
        List<List<String>> table2 = Arrays.asList(
            Arrays.asList("Value", "ID", "Name"),
            Arrays.asList("100", "1", "A")
        );
        FileContent content1 = createFileContentWithTable(table1);
        FileContent content2 = createFileContentWithTable(table2);

        ComparisonReport report = comparatorService.compare(content1, content2);

        if (!report.getTableDifferences().isEmpty()) {
            System.out.println("Unexpected differences found in testReorderedColumns:");
            for (TableDifference d : report.getTableDifferences()) {
                System.out.println("  - " + d.getType() + ": " + d.getValue1() + " vs " + d.getValue2());
            }
        }

        // No column differences should be reported, only cell differences if values were not moved correctly
        assertTrue(report.getTableDifferences().isEmpty(), "Reordered columns with same data should result in no differences.");
    }

    @Test
    void testCellDifferenceWithReorderedColumns() {
        List<List<String>> table1 = Arrays.asList(
            Arrays.asList("ID", "Name", "Value"),
            Arrays.asList("1", "A", "100")
        );
        List<List<String>> table2 = Arrays.asList(
            Arrays.asList("Value", "ID", "Name"),
            Arrays.asList("200", "1", "A") // Value changed
        );
        FileContent content1 = createFileContentWithTable(table1);
        FileContent content2 = createFileContentWithTable(table2);

        ComparisonReport report = comparatorService.compare(content1, content2);

        assertEquals(1, report.getTableDifferences().size());
        TableDifference diff = report.getTableDifferences().get(0);
        assertEquals(TableDifference.DiffType.CELL_DIFFERENCE, diff.getType());
        assertEquals("100", diff.getValue1());
        assertEquals("200", diff.getValue2());
        assertEquals(1, diff.getRowIndex()); // Data row index
        assertEquals(2, diff.getColIndex()); // Original column index of "Value" in table 1
    }
}
