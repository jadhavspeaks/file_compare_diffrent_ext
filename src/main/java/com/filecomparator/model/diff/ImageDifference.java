package com.filecomparator.model.diff;

import java.awt.image.BufferedImage;

public class ImageDifference {
    private final BufferedImage image1;
    private final BufferedImage image2;
    private final String description;
    private final double similarityScore;

    public ImageDifference(BufferedImage image1, BufferedImage image2, String description, double similarityScore) {
        this.image1 = image1;
        this.image2 = image2;
        this.description = description;
        this.similarityScore = similarityScore;
    }

    public BufferedImage getImage1() {
        return image1;
    }

    public BufferedImage getImage2() {
        return image2;
    }

    public String getDescription() {
        return description;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }
}
