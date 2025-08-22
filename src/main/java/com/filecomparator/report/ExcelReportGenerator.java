package com.filecomparator.report;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.diff.ImageDifference;
import com.filecomparator.model.diff.TableDifference;
import com.filecomparator.model.diff.TextDifference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelReportGenerator {

    public void generateReport(ComparisonReport report, String outputPath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Create styles
            CellStyle greenStyle = createStyle(workbook, IndexedColors.LIGHT_GREEN);
            CellStyle redStyle = createStyle(workbook, IndexedColors.ROSE);

            // Summary Sheet
            createSummarySheet(workbook, report);

            // Text Differences Sheet
            createTextDiffSheet(workbook, report, greenStyle, redStyle);

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

        long inserts = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.INSERT).count();
        long deletes = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.DELETE).count();
        long changes = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.CHANGE).count();

        String textSummary = String.format("Found %d text differences (%d additions, %d deletions, %d changes). See 'Text Differences' sheet.",
                report.getTextDifferences().size(), inserts, deletes, changes);

        String tableSummary = String.format("Found %d table cell differences. See 'Table Differences' sheet.",
                report.getTableDifferences().size());

        String imageSummary = String.format("Found %d image differences. See 'Image Differences' sheet.",
                report.getImageDifferences().size());

        summarySheet.createRow(1).createCell(0).setCellValue(textSummary);
        summarySheet.createRow(2).createCell(0).setCellValue(tableSummary);
        summarySheet.createRow(3).createCell(0).setCellValue(imageSummary);
    }

    private void createTextDiffSheet(Workbook workbook, ComparisonReport report, CellStyle green, CellStyle red) {
        Sheet textDiffSheet = workbook.createSheet("Text Differences");
        Row headerRow = textDiffSheet.createRow(0);
        headerRow.createCell(0).setCellValue("Source 1");
        headerRow.createCell(1).setCellValue("Source 2");
        int rowNum = 1;
        for (TextDifference diff : report.getTextDifferences()) {
            Row row = textDiffSheet.createRow(rowNum++);
            Cell cell1 = row.createCell(0);
            Cell cell2 = row.createCell(1);
            cell1.setCellValue(truncate(diff.getText1()));
            cell2.setCellValue(truncate(diff.getText2()));
            switch (diff.getType()) {
                case INSERT:
                    cell2.setCellStyle(green);
                    break;
                case DELETE:
                    cell1.setCellStyle(red);
                    break;
                case CHANGE:
                    cell1.setCellStyle(red);
                    cell2.setCellStyle(green);
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
            if (diff.getRowIndex() < 0) { // This is a summary row
                Cell summaryCell = row.createCell(0);
                summaryCell.setCellValue(String.format("Summary for Matched Tables (%d vs %d): %s", diff.getTableIndex1() + 1, diff.getTableIndex2() + 1, diff.getCell2()));
                tableDiffSheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 5));
                Font boldFont = workbook.createFont();
                boldFont.setBold(true);
                CellStyle boldStyle = workbook.createCellStyle();
                boldStyle.setFont(boldFont);
                summaryCell.setCellStyle(boldStyle);
            } else {
                row.createCell(0).setCellValue(diff.getTableIndex1() >= 0 ? String.valueOf(diff.getTableIndex1() + 1) : "N/A");
                row.createCell(1).setCellValue(diff.getTableIndex2() >= 0 ? String.valueOf(diff.getTableIndex2() + 1) : "N/A");
                row.createCell(2).setCellValue(diff.getRowIndex() >= 0 ? String.valueOf(diff.getRowIndex() + 1) : "N/A");
                row.createCell(3).setCellValue(diff.getColIndex() >= 0 ? String.valueOf(diff.getColIndex() + 1) : "N/A");
                row.createCell(4).setCellValue(diff.getCell1());
                row.createCell(5).setCellValue(diff.getCell2());
            }
        }
    }

    private void createImageDiffSheet(Workbook workbook, ComparisonReport report) {
        Sheet sheet = workbook.createSheet("Image Differences");
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        CreationHelper helper = workbook.getCreationHelper();
        int rowNum = 0;

        for (ImageDifference diff : report.getImageDifferences()) {
            try {
                // Description row
                Row descRow = sheet.createRow(rowNum++);
                descRow.createCell(0).setCellValue("Image Difference: " + diff.getDescription());
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 9));

                // Image row
                Row imageRow = sheet.createRow(rowNum);
                imageRow.setHeightInPoints(200);

                if (diff.getImage1() != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(diff.getImage1(), "png", baos);
                    int pictureIdx = workbook.addPicture(baos.toByteArray(), Workbook.PICTURE_TYPE_PNG);
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(0);
                    anchor.setRow1(rowNum);
                    anchor.setCol2(4);
                    anchor.setRow2(rowNum + 1);
                    drawing.createPicture(anchor, pictureIdx);
                }

                if (diff.getImage2() != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(diff.getImage2(), "png", baos);
                    int pictureIdx = workbook.addPicture(baos.toByteArray(), Workbook.PICTURE_TYPE_PNG);
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(5);
                    anchor.setRow1(rowNum);
                    anchor.setCol2(9);
                    anchor.setRow2(rowNum + 1);
                    drawing.createPicture(anchor, pictureIdx);
                }

                rowNum += 12;
            } catch (Exception e) {
                Row errorRow = sheet.createRow(rowNum++);
                errorRow.createCell(0).setCellValue("Error embedding image: " + e.getMessage());
            }
        }
    }

    private String truncate(String text) {
        int MAX_LENGTH = 32767;
        if (text != null && text.length() > MAX_LENGTH) {
            return text.substring(0, MAX_LENGTH - 15) + " [...truncated]";
        }
        return text;
    }
}
