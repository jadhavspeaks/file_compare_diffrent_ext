package com.filecomparator.ui;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.filecomparator.parser.ParserFactory;
import com.filecomparator.report.ExcelReportGenerator;
import com.filecomparator.report.WordReportGenerator;
import com.filecomparator.service.ComparatorService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

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

        // --- Compare Button ---
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton compareButton = new JButton("Compare");
        add(compareButton, gbc);

        // Add Listeners
        addListeners(browse1Button, path1TextField, type1ComboBox);
        addListeners(browse2Button, path2TextField, type2ComboBox);

        compareButton.addActionListener(e -> new CompareWorker().execute());

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
            fileChooser.setDialogTitle("Save Report");
            int userSelection = fileChooser.showSaveDialog(MainFrame.this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                String outputPath = fileChooser.getSelectedFile().getAbsolutePath();

                try {
                    // This is where the magic happens - calling the backend
                    FileContent content1 = ParserFactory.getParser(source1).orElseThrow().parse(source1);
                    FileContent content2 = ParserFactory.getParser(source2).orElseThrow().parse(source2);

                    ComparatorService comparator = new ComparatorService();
                    ComparisonReport report = comparator.compare(content1, content2);

                    if (outputPath.toLowerCase().endsWith(".docx")) {
                        new WordReportGenerator().generateReport(report, outputPath);
                    } else { // Default to xlsx
                        new ExcelReportGenerator().generateReport(report, outputPath);
                    }
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
