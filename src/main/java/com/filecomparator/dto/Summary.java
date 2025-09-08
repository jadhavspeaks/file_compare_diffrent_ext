package com.filecomparator.dto;

public class Summary {
    private int totalAttributes;
    private int matched;
    private int mismatched;

    public Summary(int totalAttributes, int matched, int mismatched) {
        this.totalAttributes = totalAttributes;
        this.matched = matched;
        this.mismatched = mismatched;
    }

    // Getters and Setters
    public int getTotalAttributes() {
        return totalAttributes;
    }

    public void setTotalAttributes(int totalAttributes) {
        this.totalAttributes = totalAttributes;
    }

    public int getMatched() {
        return matched;
    }

    public void setMatched(int matched) {
        this.matched = matched;
    }

    public int getMismatched() {
        return mismatched;
    }

    public void setMismatched(int mismatched) {
        this.mismatched = mismatched;
    }
}
