package com.filecomparator.dto;

import java.util.List;

public class ReportFields {
    private List<String> excel1;
    private String excel2;

    // Getters and Setters
    public List<String> getExcel1() {
        return excel1;
    }

    public void setExcel1(List<String> excel1) {
        this.excel1 = excel1;
    }

    public String getExcel2() {
        return excel2;
    }

    public void setExcel2(String excel2) {
        this.excel2 = excel2;
    }
}
