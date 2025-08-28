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

public class WordReportGenerator {

    public void generateReport(ComparisonReport report, String outputPath) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            // Summary
            createSummary(document, report);

            // Text Differences
            if (!report.getTextDifferences().isEmpty()) {
                createTextDiffs(document, report);
            }

            // Table Differences
            if (!report.getTableDifferences().isEmpty()) {
                createTableDiffs(document, report);
            }

            // Image Differences
            if (!report.getImageDifferences().isEmpty()) {
                createImageDiffs(document, report);
            }

            // Write the output to a file
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

        String textSummary = String.format("Found %d text differences (%d additions, %d deletions, %d changes).",
                report.getTextDifferences().size(), inserts, deletes, changes);

        String tableSummary = String.format("Found %d table cell differences.",
                report.getTableDifferences().size());

        String imageSummary = String.format("Found %d image differences.",
                report.getImageDifferences().size());

        document.createParagraph().createRun().setText(textSummary);
        document.createParagraph().createRun().setText(tableSummary);
        document.createParagraph().createRun().setText(imageSummary);
    }

    private void createTextDiffs(XWPFDocument document, ComparisonReport report) {
        document.createParagraph().createRun().addBreak();
        XWPFParagraph textHeader = document.createParagraph();
        XWPFRun textRun = textHeader.createRun();
        textRun.setBold(true);
        textRun.setFontSize(14);
        textRun.setText("Text Differences");

        XWPFTable table = document.createTable(report.getTextDifferences().size() + 1, 2);
        table.setWidth("100%");
        // Header
        table.getRow(0).getCell(0).setText("Source 1");
        table.getRow(0).getCell(1).setText("Source 2");

        int rowNum = 1;
        for (TextDifference diff : report.getTextDifferences()) {
            XWPFTableRow row = table.getRow(rowNum++);
            row.getCell(0).setText(diff.getText1());
            row.getCell(1).setText(diff.getText2());
            switch (diff.getType()) {
                case INSERT:
                    row.getCell(1).setColor("C7F0C7"); // Light Green
                    break;
                case DELETE:
                    row.getCell(0).setColor("F0C7C7"); // Light Red
                    break;
                case CHANGE:
                    row.getCell(0).setColor("F0C7C7"); // Light Red
                    row.getCell(1).setColor("C7F0C7"); // Light Green
                    break;
            }
        }
    }

    private void createTableDiffs(XWPFDocument document, ComparisonReport report) {
        document.createParagraph().createRun().addBreak();
        XWPFParagraph tableHeader = document.createParagraph();
        XWPFRun tableRun = tableHeader.createRun();
        tableRun.setBold(true);
        tableRun.setFontSize(14);
        tableRun.setText("Table Differences");

        XWPFTable table = document.createTable(report.getTableDifferences().size() + 1, 6);
        table.setWidth("100%");
        // Header
        XWPFTableRow headerRow = table.getRow(0);
        headerRow.getCell(0).setText("Table 1 Index");
        headerRow.getCell(1).setText("Table 2 Index");
        headerRow.getCell(2).setText("Row");
        headerRow.getCell(3).setText("Column");
        headerRow.getCell(4).setText("Source 1 Value");
        headerRow.getCell(5).setText("Source 2 Value");

        for (TableDifference diff : report.getTableDifferences()) {
            if (diff.getRowIndex() < 0) { // This is a summary row
                // Add a formatted paragraph for the summary
                XWPFParagraph summaryP = document.createParagraph();
                summaryP.setSpacingBefore(200);
                XWPFRun summaryRun = summaryP.createRun();
                summaryRun.setBold(true);
                summaryRun.setText(String.format("Summary for Matched Tables (%d vs %d): %s", diff.getTableIndex1() + 1, diff.getTableIndex2() + 1, diff.getCell2()));
            } else {
                 XWPFTableRow row = table.createRow();
                 row.getCell(0).setText(diff.getTableIndex1() >= 0 ? String.valueOf(diff.getTableIndex1() + 1) : "N/A");
                 row.getCell(1).setText(diff.getTableIndex2() >= 0 ? String.valueOf(diff.getTableIndex2() + 1) : "N/A");
                 row.getCell(2).setText(diff.getRowIndex() >= 0 ? String.valueOf(diff.getRowIndex() + 1) : "N/A");
                 row.getCell(3).setText(diff.getColIndex() >= 0 ? String.valueOf(diff.getColIndex() + 1) : "N/A");
                 row.getCell(4).setText(diff.getCell1());
                 row.getCell(5).setText(diff.getCell2());
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
