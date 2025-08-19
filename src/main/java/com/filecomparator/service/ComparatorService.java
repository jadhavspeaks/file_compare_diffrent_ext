package com.filecomparator.service;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class ComparatorService {

    public ComparisonReport compare(FileContent content1, FileContent content2) {
        ComparisonReport report = new ComparisonReport();

        compareText(content1.getText(), content2.getText(), report);
        compareTables(content1.getTables(), content2.getTables(), report);
        compareImages(content1.getImages(), content2.getImages(), report);

        // Generate a summary
        if (report.getTextDifferences().isEmpty() && report.getTableDifferences().isEmpty()) {
            report.setSummary("Files are identical.");
        } else {
            report.setSummary("Files have differences.");
        }

        return report;
    }

    private void compareText(String text1, String text2, ComparisonReport report) {
        if (text1 == null || text2 == null) {
            if (text1 == null && text2 != null) report.addTextDifference("Text exists only in the second file.");
            if (text1 != null && text2 == null) report.addTextDifference("Text exists only in the first file.");
            return;
        }

        List<String> text1Lines = Arrays.asList(text1.split("\\R"));
        List<String> text2Lines = Arrays.asList(text2.split("\\R"));

        Patch<String> patch = DiffUtils.diff(text1Lines, text2Lines);

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            report.addTextDifference(delta.toString());
        }
    }

    private void compareTables(List<List<List<String>>> tables1, List<List<List<String>>> tables2, ComparisonReport report) {
        if (tables1.size() != tables2.size()) {
            report.addTableDifference(String.format("Different number of tables: %d vs %d", tables1.size(), tables2.size()));
            return;
        }

        for (int i = 0; i < tables1.size(); i++) {
            List<List<String>> table1 = tables1.get(i);
            List<List<String>> table2 = tables2.get(i);

            if (table1.size() != table2.size()) {
                report.addTableDifference(String.format("Table %d has different number of rows: %d vs %d", i + 1, table1.size(), table2.size()));
                continue;
            }

            for (int j = 0; j < table1.size(); j++) {
                List<String> row1 = table1.get(j);
                List<String> row2 = table2.get(j);
                if (row1.size() != row2.size()) {
                    report.addTableDifference(String.format("Table %d, Row %d has different number of columns: %d vs %d", i + 1, j + 1, row1.size(), row2.size()));
                    continue;
                }
                for (int k = 0; k < row1.size(); k++) {
                    String cell1 = row1.get(k);
                    String cell2 = row2.get(k);
                    if (!cell1.equals(cell2)) {
                        report.addTableDifference(
                                String.format("Mismatch at Table %d, Row %d, Col %d: '%s' vs '%s'", i + 1, j + 1, k + 1, cell1, cell2)
                        );
                    }
                }
            }
        }
    }

    private void compareImages(List<BufferedImage> images1, List<BufferedImage> images2, ComparisonReport report) {
        if (images1.size() != images2.size()) {
            report.addTextDifference(String.format("Different number of images: %d vs %d", images1.size(), images2.size()));
            return;
        }

        for (int i = 0; i < images1.size(); i++) {
            BufferedImage img1 = images1.get(i);
            BufferedImage img2 = images2.get(i);
            if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
                report.addTextDifference(
                        String.format("Image %d has different dimensions: (%d x %d) vs (%d x %d)",
                                i + 1, img1.getWidth(), img1.getHeight(), img2.getWidth(), img2.getHeight())
                );
            } else {
                try {
                    String hash1 = getImageHash(img1);
                    String hash2 = getImageHash(img2);
                    if (!hash1.equals(hash2)) {
                        report.addTextDifference(String.format("Image %d has different content (hash mismatch)", i + 1));
                    }
                } catch (IOException | NoSuchAlgorithmException e) {
                    report.addTextDifference(String.format("Could not compute hash for image %d", i + 1));
                }
            }
        }
    }

    private String getImageHash(BufferedImage image) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageData = baos.toByteArray();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(imageData);

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
