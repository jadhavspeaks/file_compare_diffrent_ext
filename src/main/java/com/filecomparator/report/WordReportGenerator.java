package com.filecomparator.report;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.diff.ImageDifference;
import com.filecomparator.model.diff.TableDifference;
import com.filecomparator.model.diff.TextDifference;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WordReportGenerator {

    public void generateReport(ComparisonReport report, String outputPath) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            createSummary(document, report);
            if (!report.getTextDifferences().isEmpty()) createTextDiffs(document, report);
            if (!report.getTableDifferences().isEmpty()) createTableDiffs(document, report);
            if (!report.getImageDifferences().isEmpty()) createImageDiffs(document, report);

            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                document.write(fileOut);
            }
        }
    }

    private void createSummary(XWPFDocument document, ComparisonReport report) {
        XWPFParagraph summaryHeader = document.createParagraph();
        XWPFRun summaryRun = summaryHeader.createRun();
        summaryRun.setBold(true);
        summaryRun.setFontSize(16);
        summaryRun.setText("Comparison Summary");

        long inserts = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.INSERT).count();
        long deletes = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.DELETE).count();
        long changes = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.CHANGE).count();

        document.createParagraph().createRun().setText(String.format("Found %d text differences (%d additions, %d deletions, %d changes).",
                report.getTextDifferences().size(), inserts, deletes, changes));
        document.createParagraph().createRun().setText(String.format("Found %d table differences.",
                report.getTableDifferences().size()));
        document.createParagraph().createRun().setText(String.format("Found %d image differences.",
                report.getImageDifferences().size()));
    }

    private void createTextDiffs(XWPFDocument document, ComparisonReport report) {
        document.createParagraph().createRun().addBreak();
        XWPFParagraph textHeader = document.createParagraph();
        XWPFRun textRun = textHeader.createRun();
        textRun.setBold(true);
        textRun.setFontSize(14);
        textRun.setText("Text Differences");

        XWPFTable table = document.createTable(1, 2);
        table.setWidth("100%");
        table.getRow(0).getCell(0).setText("Source 1");
        table.getRow(0).getCell(1).setText("Source 2");

        for (TextDifference diff : report.getTextDifferences()) {
            XWPFTableRow row = table.createRow();
            row.getCell(0).setText(diff.getText1());
            row.getCell(1).setText(diff.getText2());
            switch (diff.getType()) {
                case INSERT: row.getCell(1).setColor("C7F0C7"); break;
                case DELETE: row.getCell(0).setColor("F0C7C7"); break;
                case CHANGE:
                    row.getCell(0).setColor("F0C7C7");
                    row.getCell(1).setColor("C7F0C7");
                    break;
            }
        }
    }

    private void createTableDiffs(XWPFDocument document, ComparisonReport report) {
        document.createParagraph().createRun().addBreak();
        XWPFParagraph mainHeader = document.createParagraph();
        XWPFRun mainRun = mainHeader.createRun();
        mainRun.setBold(true);
        mainRun.setFontSize(14);
        mainRun.setText("Table Differences");

        Map<String, List<TableDifference>> diffsByTable = report.getTableDifferences().stream()
              .collect(Collectors.groupingBy(d -> d.getTableIndex1() + ":" + d.getTableIndex2()));

        for (Map.Entry<String, List<TableDifference>> entry : diffsByTable.entrySet()) {
            List<TableDifference> diffs = entry.getValue();
            int tableIndex1 = diffs.get(0).getTableIndex1();
            int tableIndex2 = diffs.get(0).getTableIndex2();

            XWPFParagraph subHeader = document.createParagraph();
            subHeader.setSpacingBefore(200);
            XWPFRun subRun = subHeader.createRun();
            subRun.setBold(true);
            subRun.setItalic(true);
            subRun.setText(String.format("Comparison for Table %d (Source 1) vs Table %d (Source 2)", tableIndex1 + 1, tableIndex2 + 1));

            List<TableDifference> columnDiffs = diffs.stream().filter(d -> d.getType() != TableDifference.DiffType.CELL_DIFFERENCE).collect(Collectors.toList());
            if (!columnDiffs.isEmpty()) {
                XWPFParagraph columnHeader = document.createParagraph();
                XWPFRun columnRun = columnHeader.createRun();
                columnRun.setBold(true);
                columnRun.setText("Column Summary:");
                for (TableDifference diff : columnDiffs) {
                    XWPFParagraph p = document.createParagraph();
                    p.setIndentationLeft(400);
                    XWPFRun pRun = p.createRun();
                    if (diff.getType() == TableDifference.DiffType.COLUMN_DELETED) {
                        pRun.setText("Column Missing in Source 2: '" + diff.getValue1() + "'");
                    } else if (diff.getType() == TableDifference.DiffType.COLUMN_ADDED) {
                        pRun.setText("Column Missing in Source 1: '" + diff.getValue2() + "'");
                    } else if (diff.getType() == TableDifference.DiffType.TABLE_SUMMARY){
                         pRun.setBold(true);
                         pRun.setText(diff.getValue1());
                    }
                }
            }

            List<TableDifference> cellDiffs = diffs.stream().filter(d -> d.getType() == TableDifference.DiffType.CELL_DIFFERENCE).collect(Collectors.toList());
            if (!cellDiffs.isEmpty()) {
                XWPFParagraph cellHeader = document.createParagraph();
                XWPFRun cellRun = cellHeader.createRun();
                cellRun.setBold(true);
                cellRun.setText("Cell Differences:");
                XWPFTable table = document.createTable(1, 4);
                table.setWidth("100%");
                XWPFTableRow headerRow = table.getRow(0);
                headerRow.getCell(0).setText("Row");
                headerRow.getCell(1).setText("Column Index");
                headerRow.getCell(2).setText("Source 1 Value");
                headerRow.getCell(3).setText("Source 2 Value");

                for (TableDifference diff : cellDiffs) {
                    XWPFTableRow row = table.createRow();
                    row.getCell(0).setText(String.valueOf(diff.getRowIndex() + 1));
                    row.getCell(1).setText(String.valueOf(diff.getColIndex() + 1));
                    row.getCell(2).setText(diff.getValue1());
                    row.getCell(3).setText(diff.getValue2());
                }
            }
        }
    }

    private void createImageDiffs(XWPFDocument document, ComparisonReport report) {
        document.createParagraph().createRun().addBreak();
        XWPFParagraph imageHeader = document.createParagraph();
        XWPFRun imageRun = imageHeader.createRun();
        imageRun.setBold(true);
        imageRun.setFontSize(14);
        imageRun.setText("Image Differences");

        for (ImageDifference diff : report.getImageDifferences()) {
            String description = diff.getDescription();
            if (diff.getSimilarityScore() > 0) {
                description = String.format("Similarity: %.1f%%. %s", diff.getSimilarityScore() * 100, diff.getDescription());
            }
            document.createParagraph().createRun().setText("Difference: " + description);
            XWPFTable table = document.createTable(1, 2);
            table.setWidth("100%");
            table.getRow(0).getCell(0).getCTTc().addNewTcPr().addNewTcW().setW(java.math.BigInteger.valueOf(4500));
            table.getRow(0).getCell(1).getCTTc().addNewTcPr().addNewTcW().setW(java.math.BigInteger.valueOf(4500));
            XWPFTableRow row = table.getRow(0);
            try {
                if (diff.getImage1() != null) {
                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                    ImageIO.write(diff.getImage1(), "png", baos1);
                    row.getCell(0).addParagraph().createRun().addPicture(new ByteArrayInputStream(baos1.toByteArray()), XWPFDocument.PICTURE_TYPE_PNG, "image1.png", Units.toEMU(150), Units.toEMU(150));
                }
                if (diff.getImage2() != null) {
                     ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                    ImageIO.write(diff.getImage2(), "png", baos2);
                    row.getCell(1).addParagraph().createRun().addPicture(new ByteArrayInputStream(baos2.toByteArray()), XWPFDocument.PICTURE_TYPE_PNG, "image2.png", Units.toEMU(150), Units.toEMU(150));
                }
            } catch(Exception e) {
                row.getCell(0).setText("Error embedding image: " + e.getMessage());
            }
            document.createParagraph().createRun().addBreak();
        }
    }
}
