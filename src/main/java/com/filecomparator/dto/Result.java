package com.filecomparator.dto;

import java.util.List;

public class Result {
    private String attribute;
    private String status;
    private List<String> missingReports;
    private List<String> missingProducts;

    public Result(String attribute, String status, List<String> missingReports, List<String> missingProducts) {
        this.attribute = attribute;
        this.status = status;
        this.missingReports = missingReports;
        this.missingProducts = missingProducts;
    }

    // Getters and Setters
    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getMissingReports() {
        return missingReports;
    }

    public void setMissingReports(List<String> missingReports) {
        this.missingReports = missingReports;
    }

    public List<String> getMissingProducts() {
        return missingProducts;
    }

    public void setMissingProducts(List<String> missingProducts) {
        this.missingProducts = missingProducts;
    }
}
