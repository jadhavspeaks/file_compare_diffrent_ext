package com.filecomparator.exporter;

import com.filecomparator.dto.ComparisonResult;
import com.filecomparator.dto.Result;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ResultExporter {

    private static final String[] HEADERS = {"Attribute", "Status", "Missing Reports", "Missing Products"};

    public static void exportToCsv(ComparisonResult comparisonResult, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.append(String.join(",", HEADERS));
            writer.append("\n");

            // Write data
            for (Result result : comparisonResult.getResults()) {
                writer.append(escapeCsv(result.getAttribute()));
                writer.append(",");
                writer.append(escapeCsv(result.getStatus()));
                writer.append(",");
                writer.append(escapeCsv(String.join(";", result.getMissingReports())));
                writer.append(",");
                writer.append(escapeCsv(String.join(";", result.getMissingProducts())));
                writer.append("\n");
            }
        }
    }

    public static void exportToExcel(ComparisonResult comparisonResult, File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Summary Sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            summarySheet.createRow(0).createCell(0).setCellValue("Metric");
            summarySheet.getRow(0).createCell(1).setCellValue("Value");
            summarySheet.createRow(1).createCell(0).setCellValue("Total Attributes");
            summarySheet.getRow(1).createCell(1).setCellValue(comparisonResult.getSummary().getTotalAttributes());
            summarySheet.createRow(2).createCell(0).setCellValue("Matched");
            summarySheet.getRow(2).createCell(1).setCellValue(comparisonResult.getSummary().getMatched());
            summarySheet.createRow(3).createCell(0).setCellValue("Mismatched");
            summarySheet.getRow(3).createCell(1).setCellValue(comparisonResult.getSummary().getMismatched());

            // Details Sheet
            Sheet detailsSheet = workbook.createSheet("Details");
            Row headerRow = detailsSheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
            }

            int rowNum = 1;
            for (Result result : comparisonResult.getResults()) {
                Row row = detailsSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(result.getAttribute());
                row.createCell(1).setCellValue(result.getStatus());
                row.createCell(2).setCellValue(String.join(", ", result.getMissingReports()));
                row.createCell(3).setCellValue(String.join(", ", result.getMissingProducts()));
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
        }
    }

    private static String escapeCsv(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (escapedData.contains(",") || escapedData.contains("\"") || escapedData.contains("'")) {
            escapedData = escapedData.replace("\"", "\"\"");
            return "\"" + escapedData + "\"";
        }
        return escapedData;
    }
}
