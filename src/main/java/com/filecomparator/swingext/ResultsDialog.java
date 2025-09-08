package com.filecomparator.swingext;

import com.filecomparator.dto.ComparisonResult;
import com.filecomparator.dto.Result;
import com.filecomparator.exporter.ResultExporter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ResultsDialog extends JDialog {

    private final ComparisonResult comparisonResult;

    public ResultsDialog(Frame owner, ComparisonResult comparisonResult) {
        super(owner, "Comparison Results", true);
        this.comparisonResult = comparisonResult;
        setLayout(new BorderLayout());

        // Summary Panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.add(new JLabel("Total Attributes: " + comparisonResult.getSummary().getTotalAttributes()));
        summaryPanel.add(new JLabel("Matched: " + comparisonResult.getSummary().getMatched()));
        summaryPanel.add(new JLabel("Mismatched: " + comparisonResult.getSummary().getMismatched()));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Results Table
        ResultsTableModel tableModel = new ResultsTableModel(comparisonResult.getResults());
        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportCsvButton = new JButton("Export to CSV");
        JButton exportExcelButton = new JButton("Export to Excel");
        JButton closeButton = new JButton("Close");

        closeButton.addActionListener(e -> setVisible(false));
        exportCsvButton.addActionListener(e -> export("csv"));
        exportExcelButton.addActionListener(e -> export("xlsx"));

        buttonPanel.add(exportCsvButton);
        buttonPanel.add(exportExcelButton);
        buttonPanel.add(closeButton);

        add(summaryPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setSize(800, 600);
        setLocationRelativeTo(owner);
    }

    private void export(String format) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report");
        fileChooser.setFileFilter(new FileNameExtensionFilter(format.toUpperCase() + " Files", format));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith("." + format)) {
                fileToSave = new File(filePath + "." + format);
            }

            try {
                if ("csv".equals(format)) {
                    ResultExporter.exportToCsv(comparisonResult, fileToSave);
                } else {
                    ResultExporter.exportToExcel(comparisonResult, fileToSave);
                }
                JOptionPane.showMessageDialog(this, "Export successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error during export: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class ResultsTableModel extends AbstractTableModel {
        private final List<Result> results;
        private final String[] columnNames = {"Attribute", "Status", "Missing Reports", "Missing Products"};

        public ResultsTableModel(List<Result> results) {
            this.results = results;
        }

        @Override
        public int getRowCount() {
            return results.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Result result = results.get(rowIndex);
            switch (columnIndex) {
                case 0: return result.getAttribute();
                case 1: return result.getStatus();
                case 2: return String.join(", ", result.getMissingReports());
                case 3: return String.join(", ", result.getMissingProducts());
                default: return null;
            }
        }
    }
}
