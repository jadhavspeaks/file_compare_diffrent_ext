package com.filecomparator.test;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.filecomparator.service.ComparatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class ImageComparatorTest {

    private ComparatorService comparatorService;

    @BeforeEach
    void setUp() {
        comparatorService = new ComparatorService();
    }

    @Test
    void testIdenticalImages() {
        // Given
        BufferedImage image1 = createGradientImage(64, 64);
        BufferedImage image2 = createGradientImage(64, 64);
        FileContent content1 = new FileContent();
        content1.addImage(image1);
        FileContent content2 = new FileContent();
        content2.addImage(image2);

        // When
        ComparisonReport report = comparatorService.compare(content1, content2);

        // Then
        assertTrue(report.getImageDifferences().isEmpty(), "Identical images should not produce a difference report.");
    }

    @Test
    void testSimilarImages() {
        // Given
        BufferedImage image1 = createGradientImage(64, 64);
        BufferedImage image2 = createGradientImage(64, 64);
        // Modify the second image slightly by drawing a small black rectangle
        Graphics2D g = image2.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(5, 5, 10, 10);
        g.dispose();

        FileContent content1 = new FileContent();
        content1.addImage(image1);
        FileContent content2 = new FileContent();
        content2.addImage(image2);

        // When
        ComparisonReport report = comparatorService.compare(content1, content2);

        // Then
        assertEquals(1, report.getImageDifferences().size(), "Similar images should produce one difference report.");
        assertTrue(report.getImageDifferences().get(0).getSimilarityScore() > 0.8, "Similarity score should be high for similar images.");
        assertTrue(report.getImageDifferences().get(0).getSimilarityScore() < 1.0, "Similarity score should be less than 1.0 for non-identical images.");
    }

    @Test
    void testDifferentImages() {
        // Given
        BufferedImage image1 = createGradientImage(64, 64);   // Gradient
        BufferedImage image2 = createSolidImage(64, 64, Color.BLUE); // Solid Blue
        FileContent content1 = new FileContent();
        content1.addImage(image1);
        FileContent content2 = new FileContent();
        content2.addImage(image2);

        // When
        ComparisonReport report = comparatorService.compare(content1, content2);

        // Then
        assertEquals(2, report.getImageDifferences().size(), "Completely different images should result in two difference reports.");
        assertEquals("No similar image found in source 2.", report.getImageDifferences().get(0).getDescription());
        assertEquals("No similar image found in source 1.", report.getImageDifferences().get(1).getDescription());
    }

    private BufferedImage createGradientImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = (int) (((float)x / width) * 255);
                int rgb = (gray << 16) | (gray << 8) | gray;
                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    private BufferedImage createSolidImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }
}
