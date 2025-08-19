package com.filecomparator;

import java.util.ArrayList;
import java.util.List;

public class ComparisonReport {

    private String summary;
    private final List<String> textDifferences = new ArrayList<>();
    private final List<String> tableDifferences = new ArrayList<>();

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getTextDifferences() {
        return textDifferences;
    }

    public void addTextDifference(String diff) {
        this.textDifferences.add(diff);
    }

    public List<String> getTableDifferences() {
        return tableDifferences;
    }

    public void addTableDifference(String diff) {
        this.tableDifferences.add(diff);
    }
}
