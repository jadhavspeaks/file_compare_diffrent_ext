package com.filecomparator.service;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

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

        // Heuristic: If the text contains paragraph breaks, use paragraph comparison.
        boolean useParagraphMode = text1.contains("\n\n") || text1.contains("\r\n\r\n");
        List<String> list1;
        List<String> list2;
        String mode;

        if (useParagraphMode) {
            mode = "Paragraph";
            list1 = Arrays.asList(text1.split("(\\r\\n|\\n){2,}"));
            list2 = Arrays.asList(text2.split("(\\r\\n|\\n){2,}"));
        } else {
            mode = "Line";
            list1 = Arrays.asList(text1.split("\\R"));
            list2 = Arrays.asList(text2.split("\\R"));
        }

        Patch<String> patch = DiffUtils.diff(list1, list2);

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            report.addTextDifference(String.format("[%s] %s", mode, delta.toString()));
        }
    }

    private void compareTables(List<List<List<String>>> tables1, List<List<List<String>>> tables2, ComparisonReport report) {
        final double SIMILARITY_THRESHOLD = 0.8;
        JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
        boolean[] table2Matched = new boolean[tables2.size()];

        for (int i = 0; i < tables1.size(); i++) {
            List<List<String>> table1 = tables1.get(i);
            String header1 = table1.isEmpty() ? "" : String.join(" ", table1.get(0));

            int bestMatchIndex = -1;
            double bestMatchScore = -1;

            for (int j = 0; j < tables2.size(); j++) {
                if (table2Matched[j]) continue;

                List<List<String>> table2 = tables2.get(j);
                String header2 = table2.isEmpty() ? "" : String.join(" ", table2.get(0));
                double score = similarity.apply(header1, header2);

                if (score > bestMatchScore) {
                    bestMatchScore = score;
                    bestMatchIndex = j;
                }
            }

            if (bestMatchIndex != -1 && bestMatchScore >= SIMILARITY_THRESHOLD) {
                table2Matched[bestMatchIndex] = true;
                compareSingleTable(table1, tables2.get(bestMatchIndex), i + 1, bestMatchIndex + 1, report);
            } else {
                report.addTableDifference(String.format("Table %d in file 1 has no match in file 2.", i + 1));
            }
        }

        for (int j = 0; j < tables2.size(); j++) {
            if (!table2Matched[j]) {
                report.addTableDifference(String.format("Table %d in file 2 has no match in file 1.", j + 1));
            }
        }
    }

    private void compareSingleTable(List<List<String>> table1, List<List<String>> table2, int table1Index, int table2Index, ComparisonReport report) {
        if (table1.size() != table2.size()) {
            report.addTableDifference(String.format("Matched Table (%d vs %d) has different number of rows: %d vs %d", table1Index, table2Index, table1.size(), table2.size()));
        }

        int maxRows = Math.min(table1.size(), table2.size());
        for (int j = 0; j < maxRows; j++) {
            List<String> row1 = table1.get(j);
            List<String> row2 = table2.get(j);
            if (row1.size() != row2.size()) {
                report.addTableDifference(String.format("Matched Table (%d vs %d), Row %d has different number of columns: %d vs %d", table1Index, table2Index, j + 1, row1.size(), row2.size()));
                continue;
            }
            for (int k = 0; k < row1.size(); k++) {
                String cell1 = row1.get(k);
                String cell2 = row2.get(k);
                if (!cell1.equals(cell2)) {
                    report.addTableDifference(
                            String.format("Mismatch at Matched Table (%d vs %d), Row %d, Col %d: '%s' vs '%s'", table1Index, table2Index, j + 1, k + 1, cell1, cell2)
                    );
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
