package com.filecomparator;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelParser implements FileParser {

    @Override
    public FileContent parse(String filePath) throws IOException {
        FileContent fileContent = new FileContent();
        StringBuilder textBuilder = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                List<List<String>> table = new ArrayList<>();

                for (Row row : sheet) {
                    List<String> tableRow = new ArrayList<>();
                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        tableRow.add(cellValue);
                        textBuilder.append(cellValue).append(" ");
                    }
                    table.add(tableRow);
                }
                fileContent.addTable(table);
                textBuilder.append("\n");
            }
        }

        fileContent.setText(textBuilder.toString());
        return fileContent;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return Double.toString(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
