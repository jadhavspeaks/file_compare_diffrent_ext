package com.filecomparator.ui;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.filecomparator.model.diff.TextDifference;
import com.filecomparator.service.ParserFactory;
import com.filecomparator.report.ExcelReportGenerator;
import com.filecomparator.report.WordReportGenerator;
import com.filecomparator.service.ComparatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainFrame extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    private JComboBox<String> type1ComboBox;
    private JTextField path1TextField;
    private JComboBox<String> type2ComboBox;
    private JTextField path2TextField;
    private JButton compareButton;
    private JProgressBar progressBar;
    private JTextArea resultTextArea;

    private final String[] supportedTypes = {"URL", "PDF", "DOCX", "XLSX", "XLS", "PPTX", "CSV", "TXT", "JPEG", "PNG"};

    public MainFrame() {
        setTitle("File Comparator");
        setSize(700, 450); // Increased height for new components
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
    }

    public void init() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Source 1 & 2 Panels ---
        add(createSourcePanel(1), createGbc(0, 0, 4));
        add(createSourcePanel(2), createGbc(0, 1, 4));

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        compareButton = new JButton("Compare");
        JButton closeButton = new JButton("Close");
        buttonPanel.add(compareButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, createGbc(0, 2, 4));

        // --- Progress Bar ---
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false); // Initially hidden
        add(progressBar, createGbc(0, 3, 4));

        // --- Result Text Area ---
        resultTextArea = new JTextArea(8, 60);
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        add(scrollPane, createGbc(0, 4, 4));

        // --- Action Listeners ---
        compareButton.addActionListener(e -> {
            resultTextArea.setText(""); // Clear previous results
            compareButton.setEnabled(false);
            progressBar.setValue(0);
            progressBar.setVisible(true);
            new CompareWorker().execute();
        });

        closeButton.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private JPanel createSourcePanel(int sourceNumber) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> typeComboBox = new JComboBox<>(supportedTypes);
        JTextField pathTextField = new JTextField(30);
        JButton browseButton = new JButton("Browse...");

        if (sourceNumber == 1) {
            type1ComboBox = typeComboBox;
            path1TextField = pathTextField;
        } else {
            type2ComboBox = typeComboBox;
            path2TextField = pathTextField;
        }

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Source " + sourceNumber + " Type:"), gbc);
        gbc.gridx = 1; panel.add(typeComboBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Path / URL:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; panel.add(pathTextField, gbc);
        gbc.gridx = 3; gbc.gridwidth = 1; panel.add(browseButton, gbc);

        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                pathTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        typeComboBox.addActionListener(e -> {
            boolean isUrl = "URL".equals(typeComboBox.getSelectedItem());
            browseButton.setEnabled(!isUrl);
        });

        return panel;
    }

    private GridBagConstraints createGbc(int x, int y, int width) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }


    private class CompareWorker extends SwingWorker<ComparisonReport, Integer> {
        private Exception error = null;

        @Override
        protected ComparisonReport doInBackground() throws Exception {
            String source1 = path1TextField.getText();
            String source2 = path2TextField.getText();

            if (source1.isEmpty() || source2.isEmpty()) {
                throw new Exception("Please provide paths/URLs for both sources.");
            }

            publish(10); // Progress update
            logger.info("Parsing source 1...");
            FileContent content1 = ParserFactory.getParser(source1).orElseThrow(() -> new IOException("Unsupported type for source 1")).parse(source1);

            publish(30);
            logger.info("Parsing source 2...");
            FileContent content2 = ParserFactory.getParser(source2).orElseThrow(() -> new IOException("Unsupported type for source 2")).parse(source2);

            publish(50);
            logger.info("Comparing content...");
            ComparatorService comparator = new ComparatorService();
            ComparisonReport report = comparator.compare(content1, content2);

            publish(70);
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Folder to Save Reports");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
                File outputFolder = fileChooser.getSelectedFile();
                String basePath = outputFolder.getAbsolutePath();
                String type1 = (String) type1ComboBox.getSelectedItem();
                String type2 = (String) type2ComboBox.getSelectedItem();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String baseFileName = String.format("%s_vs_%s_Compare_%s", type1, type2, timestamp);

                String docxPath = basePath + File.separator + baseFileName + ".docx";
                String xlsxPath = basePath + File.separator + baseFileName + ".xlsx";

                publish(80);
                logger.info("Saving Word report to: {}", docxPath);
                new WordReportGenerator().generateReport(report, docxPath);

                publish(90);
                logger.info("Saving Excel report to: {}", xlsxPath);
                new ExcelReportGenerator().generateReport(report, xlsxPath);

                publish(100);
                return report;
            } else {
                // User cancelled the save dialog
                return null;
            }
        }

        @Override
        protected void process(List<Integer> chunks) {
            int latestProgress = chunks.get(chunks.size() - 1);
            progressBar.setValue(latestProgress);
        }

        @Override
        protected void done() {
            compareButton.setEnabled(true);
            progressBar.setValue(100);

            try {
                ComparisonReport report = get();
                if (report != null) {
                    resultTextArea.setText(generateReportSummary(report));
                    JOptionPane.showMessageDialog(MainFrame.this, "Comparison complete and reports saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Handle case where user cancelled save dialog
                    resultTextArea.setText("Comparison cancelled by user.");
                    progressBar.setVisible(false);
                }
            } catch (InterruptedException | ExecutionException e) {
                this.error = (Exception) e.getCause();
                logger.error("An error occurred during comparison", error);
                resultTextArea.setText("An error occurred:\n" + error.getMessage());
                JOptionPane.showMessageDialog(MainFrame.this, "An error occurred:\n" + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private String generateReportSummary(ComparisonReport report) {
            StringBuilder sb = new StringBuilder();
            sb.append("--- Comparison Summary ---\n\n");

            // Text differences
            long textAdditions = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.INSERT).count();
            long textDeletions = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.DELETE).count();
            long textChanges = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.CHANGE).count();
            sb.append(String.format("Text Differences: %d total\n", report.getTextDifferences().size()));
            sb.append(String.format("  - Additions: %d\n", textAdditions));
            sb.append(String.format("  - Deletions: %d\n", textDeletions));
            sb.append(String.format("  - Changes:   %d\n\n", textChanges));

            // Table differences
            sb.append(String.format("Table Differences: %d\n\n", report.getTableDifferences().size()));

            // Image differences
            sb.append(String.format("Image Differences: %d\n\n", report.getImageDifferences().size()));

            sb.append("Reports saved to the selected folder.");

            return sb.toString();
        }
    }
}
