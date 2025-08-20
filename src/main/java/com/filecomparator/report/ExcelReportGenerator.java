package com.filecomparator.report;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.diff.ImageDifference;
import com.filecomparator.model.diff.TableDifference;
import com.filecomparator.model.diff.TextDifference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.util.IOUtils;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ExcelReportGenerator {

    public void generateReport(ComparisonReport report, String outputPath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create styles
            CellStyle greenStyle = createStyle(workbook, IndexedColors.LIGHT_GREEN);
            CellStyle redStyle = createStyle(workbook, IndexedColors.ROSE);
            CellStyle yellowStyle = createStyle(workbook, IndexedColors.LIGHT_YELLOW);

            // Summary Sheet
            createSummarySheet(workbook, report);

            // Text Differences Sheet
            createTextDiffSheet(workbook, report, greenStyle, redStyle, yellowStyle);

            // Table Differences Sheet
            createTableDiffSheet(workbook, report);

            // Image Differences Sheet
            createImageDiffSheet(workbook, report);


            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
        }
    }

    private CellStyle createStyle(Workbook workbook, IndexedColors color) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void createSummarySheet(Workbook workbook, ComparisonReport report) {
        Sheet summarySheet = workbook.createSheet("Summary");
        summarySheet.createRow(0).createCell(0).setCellValue("Comparison Summary");
        String summary = String.format("Found %d text differences, %d table cell differences, and %d image differences.",
                report.getTextDifferences().size(),
                report.getTableDifferences().size(),
                report.getImageDifferences().size());
        summarySheet.createRow(1).createCell(0).setCellValue(summary);
    }

    private void createTextDiffSheet(Workbook workbook, ComparisonReport report, CellStyle green, CellStyle red, CellStyle yellow) {
        Sheet textDiffSheet = workbook.createSheet("Text Differences");
        Row headerRow = textDiffSheet.createRow(0);
        headerRow.createCell(0).setCellValue("Source 1");
        headerRow.createCell(1).setCellValue("Source 2");
        int rowNum = 1;
        for (TextDifference diff : report.getTextDifferences()) {
            Row row = textDiffSheet.createRow(rowNum++);
            Cell cell1 = row.createCell(0);
            Cell cell2 = row.createCell(1);
            cell1.setCellValue(diff.getText1());
            cell2.setCellValue(diff.getText2());
            switch (diff.getType()) {
                case INSERT:
                    cell2.setCellStyle(green);
                    break;
                case DELETE:
                    cell1.setCellStyle(red);
                    break;
                case CHANGE:
                    cell1.setCellStyle(yellow);
                    cell2.setCellStyle(yellow);
                    break;
            }
        }
        textDiffSheet.autoSizeColumn(0);
        textDiffSheet.autoSizeColumn(1);
    }

    private void createTableDiffSheet(Workbook workbook, ComparisonReport report) {
        Sheet tableDiffSheet = workbook.createSheet("Table Differences");
        Row headerRow = tableDiffSheet.createRow(0);
        headerRow.createCell(0).setCellValue("Table 1 Index");
        headerRow.createCell(1).setCellValue("Table 2 Index");
        headerRow.createCell(2).setCellValue("Row");
        headerRow.createCell(3).setCellValue("Column");
        headerRow.createCell(4).setCellValue("Source 1 Value");
        headerRow.createCell(5).setCellValue("Source 2 Value");
        int rowNum = 1;
        for (TableDifference diff : report.getTableDifferences()) {
            Row row = tableDiffSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(diff.getTableIndex1() >= 0 ? String.valueOf(diff.getTableIndex1() + 1) : "N/A");
            row.createCell(1).setCellValue(diff.getTableIndex2() >= 0 ? String.valueOf(diff.getTableIndex2() + 1) : "N/A");
            row.createCell(2).setCellValue(diff.getRowIndex() >= 0 ? String.valueOf(diff.getRowIndex() + 1) : "N/A");
            row.createCell(3).setCellValue(diff.getColIndex() >= 0 ? String.valueOf(diff.getColIndex() + 1) : "N/A");
            row.createCell(4).setCellValue(diff.getCell1());
            row.createCell(5).setCellValue(diff.getCell2());
        }
    }

    private void createImageDiffSheet(Workbook workbook, ComparisonReport report) {
        Sheet imageDiffSheet = workbook.createSheet("Image Differences");
        int rowNum = 0;
        for (ImageDifference diff : report.getImageDifferences()) {
            try {
                Row headerRow = imageDiffSheet.createRow(rowNum++);
                headerRow.createCell(0).setCellValue("Image Difference: " + diff.getDescription());

                Row imageRow = imageDiffSheet.createRow(rowNum++);
                imageRow.setHeightInPoints(200);

                if (diff.getImage1() != null) {
                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                    ImageIO.write(diff.getImage1(), "png", baos1);
                    int pictureIdx1 = workbook.addPicture(baos1.toByteArray(), Workbook.PICTURE_TYPE_PNG);
                    CreationHelper helper = workbook.getCreationHelper();
                    Drawing<?> drawing = imageDiffSheet.createDrawingPatriarch();
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(0);
                    anchor.setRow1(rowNum -1);
                    Picture pict = drawing.createPicture(anchor, pictureIdx1);
                    pict.resize(1.0);
                }

                if (diff.getImage2() != null) {
                    ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                    ImageIO.write(diff.getImage2(), "png", baos2);
                    int pictureIdx2 = workbook.addPicture(baos2.toByteArray(), Workbook.PICTURE_TYPE_PNG);
                     CreationHelper helper = workbook.getCreationHelper();
                    Drawing<?> drawing = imageDiffSheet.createDrawingPatriarch();
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(5); // Place second image in a different column
                    anchor.setRow1(rowNum - 1);
                    Picture pict = drawing.createPicture(anchor, pictureIdx2);
                    pict.resize(1.0);
                }
                rowNum++; // Add a blank row for spacing
            } catch (Exception e) {
                // Log error or write to a cell
                Row errorRow = imageDiffSheet.createRow(rowNum++);
                errorRow.createCell(0).setCellValue("Error embedding image: " + e.getMessage());
            }
        }
    }
}
