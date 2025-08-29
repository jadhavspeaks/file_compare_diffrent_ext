package com.filecomparator.model.diff;

public class TableDifference {

    public enum DiffType {
        CELL_DIFFERENCE,
        COLUMN_DELETED,
        COLUMN_ADDED,
        TABLE_SUMMARY // For table-level messages like "No Match Found"
    }

    private final DiffType type;
    private final int tableIndex1;
    private final int tableIndex2;
    private final int rowIndex;
    private final int colIndex;
    private final String value1;
    private final String value2;

    // Constructor for cell differences
    public TableDifference(int tableIndex1, int tableIndex2, int rowIndex, int colIndex, String value1, String value2) {
        this.type = DiffType.CELL_DIFFERENCE;
        this.tableIndex1 = tableIndex1;
        this.tableIndex2 = tableIndex2;
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.value1 = value1;
        this.value2 = value2;
    }

    // Constructor for column differences
    public TableDifference(DiffType type, int tableIndex1, int tableIndex2, String columnName) {
        if (type != DiffType.COLUMN_ADDED && type != DiffType.COLUMN_DELETED) {
            throw new IllegalArgumentException("This constructor is for column-level differences only.");
        }
        this.type = type;
        this.tableIndex1 = tableIndex1;
        this.tableIndex2 = tableIndex2;
        this.rowIndex = -1;
        this.colIndex = -1;
        this.value1 = (type == DiffType.COLUMN_DELETED) ? columnName : "";
        this.value2 = (type == DiffType.COLUMN_ADDED) ? columnName : "";
    }

    // Constructor for summary messages
    public TableDifference(int tableIndex1, int tableIndex2, String summaryMessage) {
        this.type = DiffType.TABLE_SUMMARY;
        this.tableIndex1 = tableIndex1;
        this.tableIndex2 = tableIndex2;
        this.rowIndex = -1;
        this.colIndex = -1;
        this.value1 = summaryMessage;
        this.value2 = "";
    }


    // Getters
    public DiffType getType() { return type; }
    public int getTableIndex1() { return tableIndex1; }
    public int getTableIndex2() { return tableIndex2; }
    public int getRowIndex() { return rowIndex; }
    public int getColIndex() { return colIndex; }
    public String getValue1() { return value1; }
    public String getValue2() { return value2; }
}
