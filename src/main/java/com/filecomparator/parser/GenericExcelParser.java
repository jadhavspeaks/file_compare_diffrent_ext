package com.filecomparator.parser;

import com.filecomparator.dto.ExcelMapping;
import com.filecomparator.dto.Mappings;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class GenericExcelParser {

    private static final Logger logger = LoggerFactory.getLogger(GenericExcelParser.class);

    public static class ReportProductPair {
        private final String report;
        private final String product;

        public ReportProductPair(String report, String product) {
            this.report = report;
            this.product = product;
        }

        public String getReport() {
            return report;
        }

        public String getProduct() {
            return product;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReportProductPair that = (ReportProductPair) o;
            return Objects.equals(report, that.report) && Objects.equals(product, that.product);
        }

        @Override
        public int hashCode() {
            return Objects.hash(report, product);
        }
    }

    public Map<String, Map<String, List<String>>> parseExcel1(String filePath, Mappings mapping) throws IOException {
        Map<String, Map<String, List<String>>> data = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int headerRowIndex = findHeaderRow(sheet);
            if (headerRowIndex == -1) {
                throw new IOException("Header row not found in Excel file: " + filePath);
            }
            Row headerRow = sheet.getRow(headerRowIndex);

            String attributeColumnName = mapping.getAttributeField().getExcel1();
            int attributeColumnIndex = findColumnIndex(headerRow, attributeColumnName);
            if (attributeColumnIndex == -1) {
                throw new IOException("Attribute column '" + attributeColumnName + "' not found in " + filePath);
            }

            Map<String, Integer> reportColumnIndexes = new HashMap<>();
            for (String reportName : mapping.getReportFields().getExcel1()) {
                reportColumnIndexes.put(reportName, findColumnIndex(headerRow, reportName));
            }

            Map<String, Integer> productColumnIndexes = new HashMap<>();
            for (String productName : mapping.getProductFields().getExcel1()) {
                productColumnIndexes.put(productName, findColumnIndex(headerRow, productName));
            }

            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String attribute = getCellValueAsString(row.getCell(attributeColumnIndex));
                if (attribute.isEmpty()) continue;

                data.putIfAbsent(attribute, new HashMap<>());
                data.get(attribute).putIfAbsent("Reports", new ArrayList<>());
                data.get(attribute).putIfAbsent("Products", new ArrayList<>());

                for (Map.Entry<String, Integer> entry : reportColumnIndexes.entrySet()) {
                    if (entry.getValue() != -1) {
                        String cellValue = getCellValueAsString(row.getCell(entry.getValue()));
                        if (!cellValue.isEmpty()) { // Assuming any non-empty value means it's selected
                            data.get(attribute).get("Reports").add(entry.getKey());
                        }
                    }
                }

                for (Map.Entry<String, Integer> entry : productColumnIndexes.entrySet()) {
                    if (entry.getValue() != -1) {
                        String cellValue = getCellValueAsString(row.getCell(entry.getValue()));
                        if (!cellValue.isEmpty()) {
                            data.get(attribute).get("Products").add(entry.getKey());
                        }
                    }
                }
            }
        }
        return data;
    }

    public Map<String, List<ReportProductPair>> parseExcel2(String filePath, Mappings mapping) throws IOException {
        Map<String, List<ReportProductPair>> data = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int headerRowIndex = findHeaderRow(sheet);
            if (headerRowIndex == -1) {
                throw new IOException("Header row not found in Excel file: " + filePath);
            }
            Row headerRow = sheet.getRow(headerRowIndex);

            int attributeCol = findColumnIndex(headerRow, mapping.getAttributeField().getExcel2());
            int reportCol = findColumnIndex(headerRow, mapping.getReportFields().getExcel2());
            int productCol = findColumnIndex(headerRow, mapping.getProductFields().getExcel2());

            if (attributeCol == -1 || reportCol == -1 || productCol == -1) {
                throw new IOException("One or more mapped columns not found in " + filePath);
            }

            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String attribute = getCellValueAsString(row.getCell(attributeCol));
                String report = getCellValueAsString(row.getCell(reportCol));
                String product = getCellValueAsString(row.getCell(productCol));

                if (attribute.isEmpty()) continue;

                data.putIfAbsent(attribute, new ArrayList<>());
                data.get(attribute).add(new ReportProductPair(report, product));
            }
        }
        return data;
    }


    public static List<String> getHeaders(String filePath) throws IOException {
        List<String> headers = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            // We need an instance to call non-static methods, or we make them static.
            // Let's make them static as they are utility methods.
            int headerRowIndex = findHeaderRow(sheet);
            if (headerRowIndex != -1) {
                Row headerRow = sheet.getRow(headerRowIndex);
                for (Cell cell : headerRow) {
                    headers.add(getCellValueAsString(cell));
                }
            }
        }
        return headers;
    }

    public static int findHeaderRow(Sheet sheet) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell != null && cell.getCellType() == CellType.STRING && !cell.getStringCellValue().isEmpty()) {
                    return row.getRowNum();
                }
            }
        }
        return -1;
    }

    private int findColumnIndex(Row headerRow, String columnName) {
        for (Cell cell : headerRow) {
            if (columnName.equalsIgnoreCase(getCellValueAsString(cell))) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    public static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return Double.toString(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
