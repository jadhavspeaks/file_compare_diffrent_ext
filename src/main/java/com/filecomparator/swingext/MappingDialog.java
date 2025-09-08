package com.filecomparator.swingext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filecomparator.dto.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MappingDialog extends JDialog {

    private JComboBox<String> attribute1, attribute2;
    private JList<String> report1, product1;
    private JComboBox<String> report2, product2;

    private String mappingJson = null;
    private final String file1Path;
    private final String file2Path;

    public MappingDialog(Frame owner, List<String> headers1, List<String> headers2, String file1Path, String file2Path) {
        super(owner, "Configure Excel Mapping", true);
        this.file1Path = file1Path;
        this.file2Path = file2Path;

        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createMappingPanel("Attribute Field (1-to-1)", headers1, headers2, false));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createMappingPanel("Report Fields (Many-to-1)", headers1, headers2, true));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createMappingPanel("Product Fields (Many-to-1)", headers1, headers2, true));

        add(mainPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel createMappingPanel(String title, List<String> headers1, List<String> headers2, boolean isMultiSelect) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Excel 1 Column(s):"), gbc);

        gbc.gridx = 1;
        if (isMultiSelect) {
            JList<String> list = new JList<>(headers1.toArray(new String[0]));
            list.setVisibleRowCount(5);
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            if (title.contains("Report")) report1 = list; else product1 = list;
            panel.add(new JScrollPane(list), gbc);
        } else {
            attribute1 = new JComboBox<>(headers1.toArray(new String[0]));
            panel.add(attribute1, gbc);
        }

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Excel 2 Column:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> combo = new JComboBox<>(headers2.toArray(new String[0]));
        if (title.contains("Attribute")) attribute2 = combo;
        else if (title.contains("Report")) report2 = combo;
        else product2 = combo;
        panel.add(combo, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            if(generateMappingJson()) {
                setVisible(false);
            }
        });
        cancelButton.addActionListener(e -> {
            mappingJson = null;
            setVisible(false);
        });

        panel.add(okButton);
        panel.add(cancelButton);
        return panel;
    }

    private boolean generateMappingJson() {
        if (attribute1.getSelectedItem() == null || attribute2.getSelectedItem() == null ||
            report2.getSelectedItem() == null || product2.getSelectedItem() == null ||
            report1.getSelectedValuesList().isEmpty() || product1.getSelectedValuesList().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be mapped.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        ExcelMapping mapping = new ExcelMapping();

        FilePaths paths = new FilePaths();
        paths.setExcel1(file1Path);
        paths.setExcel2(file2Path);
        mapping.setFiles(paths);

        Mappings mappings = new Mappings();

        AttributeField attrField = new AttributeField();
        attrField.setExcel1((String) attribute1.getSelectedItem());
        attrField.setExcel2((String) attribute2.getSelectedItem());
        mappings.setAttributeField(attrField);

        ReportFields rptFields = new ReportFields();
        rptFields.setExcel1(report1.getSelectedValuesList());
        rptFields.setExcel2((String) report2.getSelectedItem());
        mappings.setReportFields(rptFields);

        ProductFields prodFields = new ProductFields();
        prodFields.setExcel1(product1.getSelectedValuesList());
        prodFields.setExcel2((String) product2.getSelectedItem());
        mappings.setProductFields(prodFields);

        mapping.setMappings(mappings);

        try {
            mappingJson = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(mapping);
        } catch (JsonProcessingException ex) {
            JOptionPane.showMessageDialog(this, "Error generating mapping: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public String getMappingJson() {
        return mappingJson;
    }
}
