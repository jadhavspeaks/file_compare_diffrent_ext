package com.filecomparator.model.diff;

public class TextDifference {

    public enum DiffType {
        INSERT, DELETE, CHANGE, EQUAL
    }

    private final DiffType type;
    private final String text1;
    private final String text2;

    public TextDifference(DiffType type, String text1, String text2) {
        this.type = type;
        this.text1 = text1;
        this.text2 = text2;
    }

    public DiffType getType() {
        return type;
    }

    public String getText1() {
        return text1;
    }

    public String getText2() {
        return text2;
    }
}
