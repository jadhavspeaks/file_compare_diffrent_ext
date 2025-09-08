package com.filecomparator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filecomparator.dto.ComparisonResult;
import com.filecomparator.dto.ExcelMapping;
import com.filecomparator.engine.ExcelComparisonEngine;
import com.filecomparator.parser.GenericExcelParser;
import com.filecomparator.parser.GenericExcelParser.ReportProductPair;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ComparisonController {

    private final GenericExcelParser parser;
    private final ExcelComparisonEngine engine;
    private final ObjectMapper objectMapper;

    public ComparisonController() {
        this.parser = new GenericExcelParser();
        this.engine = new ExcelComparisonEngine();
        this.objectMapper = new ObjectMapper();
    }

    public ComparisonResult compare(String mappingJson) throws IOException {
        ExcelMapping mapping = objectMapper.readValue(mappingJson, ExcelMapping.class);

        String file1 = mapping.getFiles().getExcel1();
        String file2 = mapping.getFiles().getExcel2();

        Map<String, Map<String, List<String>>> data1 = parser.parseExcel1(file1, mapping.getMappings());
        Map<String, List<ReportProductPair>> data2 = parser.parseExcel2(file2, mapping.getMappings());

        return engine.compare(data1, data2);
    }
}
