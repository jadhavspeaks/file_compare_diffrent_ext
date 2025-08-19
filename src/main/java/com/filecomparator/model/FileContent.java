package com.filecomparator.model;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class FileContent {

    private String text;
    private final List<List<List<String>>> tables = new ArrayList<>();
    private final List<BufferedImage> images = new ArrayList<>();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<List<List<String>>> getTables() {
        return tables;
    }

    public void addTable(List<List<String>> table) {
        this.tables.add(table);
    }

    public List<BufferedImage> getImages() {
        return images;
    }

    public void addImage(BufferedImage image) {
        this.images.add(image);
    }
}
