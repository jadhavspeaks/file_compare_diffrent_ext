package com.filecomparator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class ReportGenerator {

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
            rowNum = 0;
            for (String diff : report.getTableDifferences()) {
                Row row = tableDiffSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(diff);
            }

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
        }
    }
}
