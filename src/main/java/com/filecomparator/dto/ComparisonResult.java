package com.filecomparator.dto;

import java.util.List;

public class ComparisonResult {
    private Summary summary;
    private List<Result> results;

    public ComparisonResult(Summary summary, List<Result> results) {
        this.summary = summary;
        this.results = results;
    }

    // Getters and Setters
    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }
}
