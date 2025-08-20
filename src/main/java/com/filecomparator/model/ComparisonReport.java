package com.filecomparator.model;

import com.filecomparator.model.diff.ImageDifference;
import com.filecomparator.model.diff.TableDifference;
import com.filecomparator.model.diff.TextDifference;

import java.util.ArrayList;
import java.util.List;

public class ComparisonReport {

    private String summary;
    private final List<TextDifference> textDifferences = new ArrayList<>();
    private final List<TableDifference> tableDifferences = new ArrayList<>();
    private final List<ImageDifference> imageDifferences = new ArrayList<>();


    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<TextDifference> getTextDifferences() {
        return textDifferences;
    }

    public void addTextDifference(TextDifference diff) {
        this.textDifferences.add(diff);
    }

    public List<TableDifference> getTableDifferences() {
        return tableDifferences;
    }

    public void addTableDifference(TableDifference diff) {
        this.tableDifferences.add(diff);
    }

    public List<ImageDifference> getImageDifferences() {
        return imageDifferences;
    }

    public void addImageDifference(ImageDifference diff) {
        this.imageDifferences.add(diff);
    }
}
