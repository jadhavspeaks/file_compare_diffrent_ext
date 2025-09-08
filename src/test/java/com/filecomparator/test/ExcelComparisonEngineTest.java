package com.filecomparator.test;

import com.filecomparator.engine.ExcelComparisonEngine;
import com.filecomparator.parser.GenericExcelParser.ReportProductPair;
import com.filecomparator.dto.ComparisonResult;
import com.filecomparator.dto.Result;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelComparisonEngineTest {

    @Test
    public void testCompare() {
        // 1. Setup mock data
        Map<String, Map<String, List<String>>> excel1Data = new HashMap<>();

        Map<String, List<String>> attr1Map = new HashMap<>();
        attr1Map.put("Reports", Arrays.asList("PRA110", "LCR"));
        attr1Map.put("Products", Arrays.asList("Commodities", "FX Derivatives", "Equities Derivatives"));
        excel1Data.put("Allocated SLR PFE Leverage", attr1Map);

        Map<String, List<String>> attr2Map = new HashMap<>();
        attr2Map.put("Reports", Arrays.asList("PRA110", "LCR", "NSFR"));
        attr2Map.put("Products", Arrays.asList("FX Derivatives"));
        excel1Data.put("Allocated SLR PFE Multiplier 1", attr2Map);

        Map<String, List<String>> attr3Map = new HashMap<>();
        attr3Map.put("Reports", Arrays.asList("LCR", "NSFR"));
        attr3Map.put("Products", Arrays.asList("Commodities", "FX Derivatives"));
        excel1Data.put("Unallocated SLR PFE", attr3Map);

        Map<String, List<String>> attr4Map = new HashMap<>();
        attr4Map.put("Reports", Arrays.asList("PRA110"));
        attr4Map.put("Products", Arrays.asList());
        excel1Data.put("Mismatch Attribute", attr4Map);


        Map<String, List<ReportProductPair>> excel2Data = new HashMap<>();
        excel2Data.put("Allocated SLR PFE Leverage", Arrays.asList(
            new ReportProductPair("PRA110", "Commodities"),
            new ReportProductPair("LCR", "Equities Derivatives")
        ));
        excel2Data.put("Allocated SLR PFE Multiplier 1", Arrays.asList(
            new ReportProductPair("PRA110", "FX Derivatives"),
            new ReportProductPair("LCR", "FX Derivatives"),
            new ReportProductPair("NSFR", "FX Derivatives")
        ));
         excel2Data.put("Unallocated SLR PFE", Arrays.asList(
            new ReportProductPair("LCR", "Commodities"),
            new ReportProductPair("NSFR", "FX Derivatives")
        ));
        excel2Data.put("Extra Attribute", Arrays.asList(
            new ReportProductPair("FOO", "BAR")
        ));

        // 2. Execute
        ExcelComparisonEngine engine = new ExcelComparisonEngine();
        ComparisonResult result = engine.compare(excel1Data, excel2Data);

        // 3. Assert
        assertNotNull(result);
        assertEquals(4, result.getSummary().getTotalAttributes());
        assertEquals(2, result.getSummary().getMatched());
        assertEquals(2, result.getSummary().getMismatched());

        for (Result res : result.getResults()) {
            switch (res.getAttribute()) {
                case "Allocated SLR PFE Leverage":
                    assertEquals("FAIL", res.getStatus());
                    assertTrue(res.getMissingReports().isEmpty());
                    assertEquals(Arrays.asList("FX Derivatives"), res.getMissingProducts());
                    break;
                case "Allocated SLR PFE Multiplier 1":
                    assertEquals("PASS", res.getStatus());
                    assertTrue(res.getMissingReports().isEmpty());
                    assertTrue(res.getMissingProducts().isEmpty());
                    break;
                case "Unallocated SLR PFE":
                    assertEquals("PASS", res.getStatus());
                    assertTrue(res.getMissingReports().isEmpty());
                    assertTrue(res.getMissingProducts().isEmpty());
                    break;
                case "Mismatch Attribute":
                    assertEquals("FAIL", res.getStatus());
                    assertEquals(Arrays.asList("PRA110"), res.getMissingReports());
                    assertTrue(res.getMissingProducts().isEmpty());
                    break;
                default:
                    fail("Unexpected attribute in results: " + res.getAttribute());
            }
        }
    }
}
