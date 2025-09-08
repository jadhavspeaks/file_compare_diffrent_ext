package com.filecomparator.test;

import com.filecomparator.dto.*;
import com.filecomparator.parser.GenericExcelParser;
import com.filecomparator.parser.GenericExcelParser.ReportProductPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// NOTE: This test requires the sample files 'test1.xlsx' and 'test2.xlsx' to be present in 'src/test/resources'.
// The test is disabled by default to prevent failure in environments where these files are not available.
@Disabled("Requires sample Excel files in src/test/resources")
public class GenericExcelParserTest {

    private Mappings mappings;
    private GenericExcelParser parser;
    private String testFile1Path;
    private String testFile2Path;

    @BeforeEach
    void setUp() {
        parser = new GenericExcelParser();

        // Setup mapping configuration
        mappings = new Mappings();
        AttributeField attrField = new AttributeField();
        attrField.setExcel1("Reporting Physical Data Attribute Name");
        attrField.setExcel2("Attribute Name");
        mappings.setAttributeField(attrField);

        ReportFields rptFields = new ReportFields();
        rptFields.setExcel1(Arrays.asList("PRA110", "LCR", "NSFR"));
        rptFields.setExcel2("Report Name");
        mappings.setReportFields(rptFields);

        ProductFields prodFields = new ProductFields();
        prodFields.setExcel1(Arrays.asList("Commodities", "FX Derivatives", "Equities Derivatives"));
        prodFields.setExcel2("Product");
        mappings.setProductFields(prodFields);

        // Setup file paths
        File file1 = new File("src/test/resources/test1.xlsx");
        File file2 = new File("src/test/resources/test2.xlsx");
        testFile1Path = file1.getAbsolutePath();
        testFile2Path = file2.getAbsolutePath();
    }

    @Test
    void testParseExcel1() throws IOException {
        Map<String, Map<String, List<String>>> data = parser.parseExcel1(testFile1Path, mappings);

        assertNotNull(data);
        assertEquals(4, data.size());

        // Assertions for "Allocated SLR PFE Leverage"
        assertTrue(data.containsKey("Allocated SLR PFE Leverage"));
        Map<String, List<String>> attr1 = data.get("Allocated SLR PFE Leverage");
        assertEquals(Arrays.asList("PRA110", "LCR"), attr1.get("Reports"));
        assertEquals(Arrays.asList("Commodities", "Equities Derivatives"), attr1.get("Products"));
    }

    @Test
    void testParseExcel2() throws IOException {
        Map<String, List<ReportProductPair>> data = parser.parseExcel2(testFile2Path, mappings);

        assertNotNull(data);
        assertEquals(4, data.size()); // 3 unique attributes from our sample data + 1 extra

        // Assertions for "Allocated SLR PFE Multiplier 1"
        assertTrue(data.containsKey("Allocated SLR PFE Multiplier 1"));
        List<ReportProductPair> pairs = data.get("Allocated SLR PFE Multiplier 1");
        assertEquals(3, pairs.size());
        assertTrue(pairs.contains(new ReportProductPair("PRA110", "FX Derivatives")));
        assertTrue(pairs.contains(new ReportProductPair("LCR", "FX Derivatives")));
        assertTrue(pairs.contains(new ReportProductPair("NSFR", "FX Derivatives")));
    }
}
