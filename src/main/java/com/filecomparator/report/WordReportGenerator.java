package com.filecomparator.report;

import com.filecomparator.model.ComparisonReport;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.FileOutputStream;
import java.io.IOException;

public class WordReportGenerator {

    public void generateReport(ComparisonReport report, String outputPath) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            // Summary
            XWPFParagraph summaryHeader = document.createParagraph();
            XWPFRun summaryRun = summaryHeader.createRun();
            summaryRun.setBold(true);
            summaryRun.setText("Comparison Summary");
            document.createParagraph().createRun().setText(report.getSummary());

            // Text Differences
            if (!report.getTextDifferences().isEmpty()) {
                document.createParagraph().createRun().addBreak();
                XWPFParagraph textHeader = document.createParagraph();
                XWPFRun textRun = textHeader.createRun();
                textRun.setBold(true);
                textRun.setText("Text Differences");
                for (String diff : report.getTextDifferences()) {
                    document.createParagraph().createRun().setText("- " + diff);
                }
            }

            // Table Differences
            if (!report.getTableDifferences().isEmpty()) {
                document.createParagraph().createRun().addBreak();
                XWPFParagraph tableHeader = document.createParagraph();
                XWPFRun tableRun = tableHeader.createRun();
                tableRun.setBold(true);
                tableRun.setText("Table Differences");

                XWPFTable table = document.createTable();
                XWPFTableRow headerRow = table.getRow(0);
                headerRow.getCell(0).setText("Table");
                headerRow.addNewTableCell().setText("Row");
                headerRow.addNewTableCell().setText("Column");
                headerRow.addNewTableCell().setText("File 1 Value");
                headerRow.addNewTableCell().setText("File 2 Value");

                for (String diff : report.getTableDifferences()) {
                    XWPFTableRow row = table.createRow();
                    if (diff.startsWith("Mismatch at")) {
                        try {
                            String[] parts = diff.split(":");
                            String[] location = parts[0].replaceAll("[^0-9,]", "").split(",");
                            String[] values = parts[1].split("' vs '");
                            row.getCell(0).setText(location[0]);
                            row.getCell(1).setText(location[1]);
                            row.getCell(2).setText(location[2]);
                            row.getCell(3).setText(values[0].substring(2));
                            row.getCell(4).setText(values[1].substring(0, values[1].length() - 1));
                        } catch (Exception e) {
                            row.getCell(0).setText(diff); // Fallback
                        }
                    } else {
                        row.getCell(0).setText(diff);
                    }
                }
            }

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
                document.write(fileOut);
            }
        }
    }
}
