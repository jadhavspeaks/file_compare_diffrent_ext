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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExcelReportGenerator {

    public void generateReport(ComparisonReport report, String outputPath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            createSummarySheet(workbook, report);
            if (!report.getTextDifferences().isEmpty()) createTextDiffSheet(workbook, report);
            if (!report.getTableDifferences().isEmpty()) createTableDiffSheet(workbook, report);
            if (!report.getImageDifferences().isEmpty()) createImageDiffSheet(workbook, report);

            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                workbook.write(fileOut);
            }
        }
    }

    private CellStyle createStyle(Workbook workbook, IndexedColors color, boolean isBold) {
        CellStyle style = workbook.createCellStyle();
        if (color != null) {
            style.setFillForegroundColor(color.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        if(isBold) {
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
        }
        return style;
    }

    private void createSummarySheet(Workbook workbook, ComparisonReport report) {
        Sheet summarySheet = workbook.createSheet("Summary");
        summarySheet.createRow(0).createCell(0).setCellValue("Comparison Summary");
        long inserts = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.INSERT).count();
        long deletes = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.DELETE).count();
        long changes = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.CHANGE).count();

        summarySheet.createRow(1).createCell(0).setCellValue(String.format("Found %d text differences (%d additions, %d deletions, %d changes). See 'Text Differences' sheet.",
                report.getTextDifferences().size(), inserts, deletes, changes));
        summarySheet.createRow(2).createCell(0).setCellValue(String.format("Found %d table differences. See 'Table Differences' sheet.",
                report.getTableDifferences().size()));
        summarySheet.createRow(3).createCell(0).setCellValue(String.format("Found %d image differences. See 'Image Differences' sheet.",
                report.getImageDifferences().size()));
    }

    private void createTextDiffSheet(Workbook workbook, ComparisonReport report) {
        Sheet textDiffSheet = workbook.createSheet("Text Differences");
        CellStyle green = createStyle(workbook, IndexedColors.LIGHT_GREEN, false);
        CellStyle red = createStyle(workbook, IndexedColors.ROSE, false);
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
            if (diff.getType() == TextDifference.DiffType.INSERT) cell2.setCellStyle(green);
            if (diff.getType() == TextDifference.DiffType.DELETE) cell1.setCellStyle(red);
            if (diff.getType() == TextDifference.DiffType.CHANGE) {
                cell1.setCellStyle(red);
                cell2.setCellStyle(green);
            }
        }
        textDiffSheet.autoSizeColumn(0);
        textDiffSheet.autoSizeColumn(1);
    }

    private void createTableDiffSheet(Workbook workbook, ComparisonReport report) {
        Sheet sheet = workbook.createSheet("Table Differences");
        int rowNum = 0;

        CellStyle addedStyle = createStyle(workbook, IndexedColors.LIGHT_GREEN, false);
        CellStyle deletedStyle = createStyle(workbook, IndexedColors.ROSE, false);
        CellStyle headerStyle = createStyle(workbook, IndexedColors.GREY_25_PERCENT, true);

        Map<String, List<TableDifference>> diffsByTable = report.getTableDifferences().stream()
              .collect(Collectors.groupingBy(d -> d.getTableIndex1() + ":" + d.getTableIndex2()));

        for (Map.Entry<String, List<TableDifference>> entry : diffsByTable.entrySet()) {
            List<TableDifference> diffs = entry.getValue();
            int tableIndex1 = diffs.get(0).getTableIndex1();
            int tableIndex2 = diffs.get(0).getTableIndex2();

            Row subHeaderRow = sheet.createRow(rowNum++);
            Cell subHeaderCell = subHeaderRow.createCell(0);
            subHeaderCell.setCellValue(String.format("Comparison for Table %d (Source 1) vs Table %d (Source 2)", tableIndex1 + 1, tableIndex2 + 1));
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 5));
            subHeaderCell.setCellStyle(headerStyle);

            List<TableDifference> columnDiffs = diffs.stream().filter(d -> d.getType() != TableDifference.DiffType.CELL_DIFFERENCE).collect(Collectors.toList());
            if (!columnDiffs.isEmpty()) {
                Row columnHeader = sheet.createRow(rowNum++);
                Cell columnHeaderCell = columnHeader.createCell(0);
                columnHeaderCell.setCellValue("Column Changes");
                columnHeaderCell.setCellStyle(headerStyle);
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

                for (TableDifference diff : columnDiffs) {
                    Row row = sheet.createRow(rowNum++);
                    String changeType;
                    String value;
                    CellStyle style;
                    if (diff.getType() == TableDifference.DiffType.COLUMN_DELETED) {
                        changeType = "Column Missing in Source 2";
                        value = diff.getValue1();
                        style = deletedStyle;
                    } else if (diff.getType() == TableDifference.DiffType.COLUMN_ADDED) {
                        changeType = "Column Missing in Source 1";
                        value = diff.getValue2();
                        style = addedStyle;
                    } else {
                        changeType = "Summary";
                        value = diff.getValue1();
                        style = createStyle(workbook, IndexedColors.GREY_25_PERCENT, false);
                    }
                    row.createCell(0).setCellValue(changeType);
                    row.createCell(1).setCellValue(value);
                    row.getCell(0).setCellStyle(style);
                }
            }

            List<TableDifference> cellDiffs = diffs.stream().filter(d -> d.getType() == TableDifference.DiffType.CELL_DIFFERENCE).collect(Collectors.toList());
            if (!cellDiffs.isEmpty()) {
                rowNum++; // Blank row for spacing
                Row cellHeaderRow = sheet.createRow(rowNum++);
                String[] headers = {"Row", "Column Index", "Source 1 Value", "Source 2 Value"};
                for(int i=0; i<headers.length; i++){
                    Cell cell = cellHeaderRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                for(TableDifference diff : cellDiffs) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(diff.getRowIndex() + 1);
                    row.createCell(1).setCellValue(diff.getColIndex() + 1);
                    row.createCell(2).setCellValue(diff.getValue1());
                    row.createCell(3).setCellValue(diff.getValue2());
                }
            }
            rowNum += 2;
        }
    }

    private void createImageDiffSheet(Workbook workbook, ComparisonReport report) {
        Sheet sheet = workbook.createSheet("Image Differences");
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        CreationHelper helper = workbook.getCreationHelper();
        int rowNum = 0;

        for (ImageDifference diff : report.getImageDifferences()) {
            try {
                String description = diff.getDescription();
                if (diff.getSimilarityScore() > 0) {
                    description = String.format("Similarity: %.1f%%. %s", diff.getSimilarityScore() * 100, diff.getDescription());
                }
                Row descRow = sheet.createRow(rowNum++);
                descRow.createCell(0).setCellValue("Image Difference: " + description);
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 9));

                Row imageRow = sheet.createRow(rowNum);
                imageRow.setHeightInPoints(200);

                if (diff.getImage1() != null) addPictureToSheet(workbook, drawing, helper, diff.getImage1(), 0, rowNum);
                if (diff.getImage2() != null) addPictureToSheet(workbook, drawing, helper, diff.getImage2(), 5, rowNum);
                rowNum += 12;
            } catch (Exception e) {
                sheet.createRow(rowNum++).createCell(0).setCellValue("Error embedding image: " + e.getMessage());
            }
        }
    }

    private void addPictureToSheet(Workbook wb, Drawing<?> drawing, CreationHelper helper, java.awt.image.BufferedImage img, int col, int row) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        int pictureIdx = wb.addPicture(baos.toByteArray(), Workbook.PICTURE_TYPE_PNG);
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(col);
        anchor.setRow1(row);
        anchor.setCol2(col + 4);
        anchor.setRow2(row + 1);
        drawing.createPicture(anchor, pictureIdx);
    }

    private String truncate(String text) {
        if (text != null && text.length() > 32767) {
            return text.substring(0, 32752) + " [...truncated]";
        }
        return text;
    }
}
