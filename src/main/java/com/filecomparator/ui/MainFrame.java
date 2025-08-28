package com.filecomparator.ui;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.filecomparator.service.ParserFactory;
import com.filecomparator.report.ExcelReportGenerator;
import com.filecomparator.report.WordReportGenerator;
import com.filecomparator.service.ComparatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {

    private static final Logger logger = LoggerFactory.getLogger(MainFrame.class);
    private JComboBox<String> type1ComboBox;
    private JTextField path1TextField;
    private JComboBox<String> type2ComboBox;
    private JTextField path2TextField;

    private final String[] supportedTypes = {"URL", "PDF", "XLSX", "XLS", "PPTX", "CSV", "TXT", "JPEG", "PNG"};

    public MainFrame() {
        setTitle("File Comparator");
        setSize(700, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame
        setLayout(new GridBagLayout());
    }

    public void init() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Source 1 ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Source 1 Type:"), gbc);

        gbc.gridx = 1;
        type1ComboBox = new JComboBox<>(supportedTypes);
        add(type1ComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Path / URL:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        path1TextField = new JTextField(30);
        add(path1TextField, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        JButton browse1Button = new JButton("Browse...");
        add(browse1Button, gbc);

        // --- Source 2 ---
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Source 2 Type:"), gbc);

        gbc.gridx = 1;
        type2ComboBox = new JComboBox<>(supportedTypes);
        add(type2ComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Path / URL:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        path2TextField = new JTextField(30);
        add(path2TextField, gbc);

        gbc.gridx = 3;
        gbc.gridwidth = 1;
        JButton browse2Button = new JButton("Browse...");
        add(browse2Button, gbc);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton compareButton = new JButton("Compare");
        JButton closeButton = new JButton("Close");
        buttonPanel.add(compareButton);
        buttonPanel.add(closeButton);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        add(buttonPanel, gbc);


        // Add Listeners
        addListeners(browse1Button, path1TextField, type1ComboBox);
        addListeners(browse2Button, path2TextField, type2ComboBox);

        compareButton.addActionListener(e -> new CompareWorker().execute());

        closeButton.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private void addListeners(JButton browseButton, JTextField pathField, JComboBox<String> typeComboBox) {
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                pathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        typeComboBox.addActionListener(e -> {
            boolean isUrl = "URL".equals(typeComboBox.getSelectedItem());
            browseButton.setEnabled(!isUrl);
        });
    }

    private class CompareWorker extends SwingWorker<Void, Void> {
        private Exception error = null;

        @Override
        protected Void doInBackground() throws Exception {
            String source1 = path1TextField.getText();
            String source2 = path2TextField.getText();

            if (source1.isEmpty() || source2.isEmpty()) {
                throw new Exception("Please provide paths/URLs for both sources.");
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Folder to Save Reports");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);

            if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
                File outputFolder = fileChooser.getSelectedFile();

                try {
                    logger.info("Source 1: {}", source1);
                    logger.info("Source 2: {}", source2);
                    logger.info("Selected output folder: {}", outputFolder.getAbsolutePath());

                    if (!outputFolder.exists()) {
                        logger.info("Output folder does not exist. Attempting to create...");
                        if (!outputFolder.mkdirs()) {
                            throw new IOException("Could not create output directory: " + outputFolder.getAbsolutePath());
                        }
                    }
                    if (!outputFolder.isDirectory()) {
                        throw new IOException("The selected path is not a directory: " + outputFolder.getAbsolutePath());
                    }

                    String basePath = outputFolder.getAbsolutePath();
                    String type1 = (String) type1ComboBox.getSelectedItem();
                    String type2 = (String) type2ComboBox.getSelectedItem();
                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String baseFileName = String.format("%s_vs_%s_Compare_%s", type1, type2, timestamp);

                    logger.info("Parsing source 1...");
                    FileContent content1 = ParserFactory.getParser(source1).orElseThrow(() -> new IOException("Unsupported type for source 1")).parse(source1);
                    logger.info("Parsing source 2...");
                    FileContent content2 = ParserFactory.getParser(source2).orElseThrow(() -> new IOException("Unsupported type for source 2")).parse(source2);

                    logger.info("Comparing content...");
                    ComparatorService comparator = new ComparatorService();
                    ComparisonReport report = comparator.compare(content1, content2);

                    String docxPath = basePath + File.separator + baseFileName + ".docx";
                    String xlsxPath = basePath + File.separator + baseFileName + ".xlsx";

                    logger.info("Saving Word report to: {}", docxPath);
                    new WordReportGenerator().generateReport(report, docxPath);

                    logger.info("Saving Excel report to: {}", xlsxPath);
                    new ExcelReportGenerator().generateReport(report, xlsxPath);

                } catch (Exception e) {
                    this.error = e;
                }
            }
            return null;
        }

        @Override
        protected void done() {
            if (error != null) {
                JOptionPane.showMessageDialog(MainFrame.this, "An error occurred:\n" + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(MainFrame.this, "Comparison complete!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
