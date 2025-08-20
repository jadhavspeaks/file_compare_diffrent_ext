package com.filecomparator.model.diff;

public class TableDifference {
    private final int tableIndex1;
    private final int tableIndex2;
    private final int rowIndex;
    private final int colIndex;
    private final String cell1;
    private final String cell2;

    public TableDifference(int tableIndex1, int tableIndex2, int rowIndex, int colIndex, String cell1, String cell2) {
        this.tableIndex1 = tableIndex1;
        this.tableIndex2 = tableIndex2;
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.cell1 = cell1;
        this.cell2 = cell2;
    }

    // Getters for all fields
    public int getTableIndex1() { return tableIndex1; }
    public int getTableIndex2() { return tableIndex2; }
    public int getRowIndex() { return rowIndex; }
    public int getColIndex() { return colIndex; }
    public String getCell1() { return cell1; }
    public String getCell2() { return cell2; }
}
