package com.filecomparator.ui;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.filecomparator.model.diff.TextDifference;
import com.filecomparator.report.ExcelReportGenerator;
import com.filecomparator.report.WordReportGenerator;
import com.filecomparator.service.ComparatorService;
import com.filecomparator.service.ParserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.filecomparator.controller.ComparisonController;
import com.filecomparator.dto.ComparisonResult;
import com.filecomparator.parser.GenericExcelParser;
import com.filecomparator.swingext.MappingDialog;
import com.filecomparator.swingext.ResultsDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
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
    String basePath ="";
    private final String[] supportedTypes = {"URL", "PDF", "DOCX", "XLSX", "XLS", "PPTX", "CSV", "TXT", "JPEG", "PNG"};

    public MainFrame() {
        setTitle("File Comparator");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
    }

    public void init() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        add(createSourcePanel(1), createGbc(0, 0, 4));
        add(createSourcePanel(2), createGbc(0, 1, 4));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        compareButton = new JButton("Compare");
        JButton genericCompareButton = new JButton("Generic Excel Compare");
        JButton closeButton = new JButton("Close");
        buttonPanel.add(compareButton);
        buttonPanel.add(genericCompareButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, createGbc(0, 2, 4));

        genericCompareButton.addActionListener(e -> new GenericCompareSetupWorker().execute());

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        add(progressBar, createGbc(0, 3, 4));

        resultTextArea = new JTextArea(8, 60);
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        add(scrollPane, createGbc(0, 4, 4));

        compareButton.addActionListener(e -> {
            resultTextArea.setText("");
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

        typeComboBox.addActionListener(e -> browseButton.setEnabled(!"URL".equals(typeComboBox.getSelectedItem())));

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
        @Override
        protected ComparisonReport doInBackground() throws Exception {
            String source1 = path1TextField.getText();
            String source2 = path2TextField.getText();

            if (source1.isEmpty() || source2.isEmpty()) {
                throw new Exception("Please provide paths/URLs for both sources.");
            }

            publish(10);
            FileContent content1 = ParserUtils.parseAndValidate(source1);
            publish(30);
            FileContent content2 = ParserUtils.parseAndValidate(source2);
            publish(50);
            ComparatorService comparator = new ComparatorService();
            ComparisonReport report = comparator.compare(content1, content2);
            publish(70);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Folder to Save Reports");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
                File outputFolder = fileChooser.getSelectedFile();
                basePath = outputFolder.getAbsolutePath();
                String type1 = (String) type1ComboBox.getSelectedItem();
                String type2 = (String) type2ComboBox.getSelectedItem();
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String baseFileName = String.format("%s_vs_%s_Compare_%s", type1, type2, timestamp);

                publish(80);
                new WordReportGenerator().generateReport(report, basePath + File.separator + baseFileName + ".docx");
                publish(90);
                new ExcelReportGenerator().generateReport(report, basePath + File.separator + baseFileName + ".xlsx");
                publish(100);
                return report;
            }
            return null;
        }

        @Override
        protected void process(List<Integer> chunks) {
            progressBar.setValue(chunks.get(chunks.size() - 1));
        }

        @Override
        protected void done() {
            compareButton.setEnabled(true);
            progressBar.setValue(100);
            try {
                ComparisonReport report = get();
                if (report != null) {
                    resultTextArea.setText(generateReportSummary(report));
                    JOptionPane.showMessageDialog(MainFrame.this, "Comparison complete!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    resultTextArea.setText("Comparison cancelled by user.");
                    progressBar.setVisible(false);
                }
            } catch (Exception e) {
                logger.error("An error occurred during comparison", e.getCause());
                resultTextArea.setText("An error occurred:\n" + e.getCause().getMessage());
                JOptionPane.showMessageDialog(MainFrame.this, "An error occurred:\n" + e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private String generateReportSummary(ComparisonReport report) {
            StringBuilder sb = new StringBuilder("--- Comparison Summary ---\n\n");
            long textAdditions = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.INSERT).count();
            long textDeletions = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.DELETE).count();
            long textChanges = report.getTextDifferences().stream().filter(d -> d.getType() == TextDifference.DiffType.CHANGE).count();
            sb.append(String.format("Text Differences: %d total\n", report.getTextDifferences().size()));
            sb.append(String.format("  - Additions: %d\n", textAdditions));
            sb.append(String.format("  - Deletions: %d\n", textDeletions));
            sb.append(String.format("  - Changes:   %d\n\n", textChanges));
            sb.append(String.format("Table Differences: %d\n\n", report.getTableDifferences().size()));
            sb.append(String.format("Image Differences: %d\n\n", report.getImageDifferences().size()));
            sb.append("Reports saved to the selected folder : ").append(basePath);
            return sb.toString();
        }
    }

    private class GenericCompareSetupWorker extends SwingWorker<String, Void> {
        private File file1, file2;

        @Override
        protected String doInBackground() throws Exception {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select First Excel File");
            if (fc.showOpenDialog(MainFrame.this) != JFileChooser.APPROVE_OPTION) return null;
            file1 = fc.getSelectedFile();

            fc.setDialogTitle("Select Second Excel File");
            if (fc.showOpenDialog(MainFrame.this) != JFileChooser.APPROVE_OPTION) return null;
            file2 = fc.getSelectedFile();

            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);

            List<String> headers1 = GenericExcelParser.getHeaders(file1.getAbsolutePath());
            List<String> headers2 = GenericExcelParser.getHeaders(file2.getAbsolutePath());

            MappingDialog dialog = new MappingDialog(MainFrame.this, headers1, headers2, file1.getAbsolutePath(), file2.getAbsolutePath());
            dialog.setVisible(true);

            return dialog.getMappingJson();
        }

        @Override
        protected void done() {
            progressBar.setIndeterminate(false);
            progressBar.setVisible(false);
            try {
                String mappingJson = get();
                if (mappingJson != null) {
                    resultTextArea.setText("Mapping created. Starting comparison...");
                    new GenericCompareWorker(mappingJson).execute();
                } else {
                    resultTextArea.setText("Generic comparison cancelled.");
                }
            } catch (Exception e) {
                logger.error("Error in generic compare setup", e);
                JOptionPane.showMessageDialog(MainFrame.this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class GenericCompareWorker extends SwingWorker<ComparisonResult, Void> {
        private final String mappingJson;

        public GenericCompareWorker(String mappingJson) {
            this.mappingJson = mappingJson;
        }

        @Override
        protected ComparisonResult doInBackground() throws Exception {
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            ComparisonController controller = new ComparisonController();
            return controller.compare(mappingJson);
        }

        @Override
        protected void done() {
            progressBar.setIndeterminate(false);
            progressBar.setVisible(false);
            try {
                ComparisonResult result = get();
                resultTextArea.setText("Generic Comparison Complete. Showing results...");
                ResultsDialog resultsDialog = new ResultsDialog(MainFrame.this, result);
                resultsDialog.setVisible(true);
            } catch (Exception e) {
                logger.error("Error in generic comparison", e);
                JOptionPane.showMessageDialog(MainFrame.this, "Error during comparison: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
