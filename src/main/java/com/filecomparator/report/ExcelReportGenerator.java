package com.filecomparator.report;

import com.filecomparator.model.ComparisonReport;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelReportGenerator {

    public void generateReport(ComparisonReport report, String outputPath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Summary Sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            Row summaryRow = summarySheet.createRow(0);
            Cell summaryCell = summaryRow.createCell(0);
            summaryCell.setCellValue(report.getSummary());

            // Text Differences Sheet
            Sheet textDiffSheet = workbook.createSheet("Text Differences");
            int rowNum = 0;
            for (String diff : report.getTextDifferences()) {
                Row row = textDiffSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(diff);
            }

            // Table Differences Sheet
            Sheet tableDiffSheet = workbook.createSheet("Table Differences");
            Row headerRow = tableDiffSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Table");
            headerRow.createCell(1).setCellValue("Row");
            headerRow.createCell(2).setCellValue("Column");
            headerRow.createCell(3).setCellValue("File 1 Value");
            headerRow.createCell(4).setCellValue("File 2 Value");

            rowNum = 1; // Reset for the new table
            for (String diff : report.getTableDifferences()) {
                Row row = tableDiffSheet.createRow(rowNum++);
                // Basic parsing of the diff string. A more robust solution would use a structured object.
                if (diff.startsWith("Mismatch at")) {
                    try {
                        String[] parts = diff.split(":");
                        String[] location = parts[0].replaceAll("[^0-9,]", "").split(",");
                        String[] values = parts[1].split("' vs '");
                        row.createCell(0).setCellValue(location[0]);
                        row.createCell(1).setCellValue(location[1]);
                        row.createCell(2).setCellValue(location[2]);
                        row.createCell(3).setCellValue(values[0].substring(2));
                        row.createCell(4).setCellValue(values[1].substring(0, values[1].length() - 1));
                    } catch (Exception e) {
                        row.createCell(0).setCellValue(diff); // Fallback
                    }
                } else {
                    row.createCell(0).setCellValue(diff);
                }
            }

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
        }
    }
}
