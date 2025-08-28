package com.filecomparator.service;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.filecomparator.model.diff.ImageDifference;
import com.filecomparator.model.diff.TableDifference;
import com.filecomparator.model.diff.TextDifference;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComparatorService {

    private static final double IMAGE_SIMILARITY_THRESHOLD = 0.2; // Max normalized Hamming distance

    public ComparisonReport compare(FileContent content1, FileContent content2) {
        ComparisonReport report = new ComparisonReport();

        compareText(content1.getText(), content2.getText(), report);
        compareTables(content1.getTables(), content2.getTables(), report);
        compareImages(content1.getImages(), content2.getImages(), report);

        if (report.getTextDifferences().isEmpty() && report.getTableDifferences().isEmpty() && report.getImageDifferences().isEmpty()) {
            report.setSummary("Files are identical or highly similar.");
        } else {
            report.setSummary("Files have differences.");
        }

        return report;
    }

    private void compareText(String text1, String text2, ComparisonReport report) {
        if (text1 == null && text2 != null) {
            report.addTextDifference(new TextDifference(TextDifference.DiffType.INSERT, "", text2));
            return;
        }
        if (text1 != null && text2 == null) {
            report.addTextDifference(new TextDifference(TextDifference.DiffType.DELETE, text1, ""));
            return;
        }
        if (text1 == null && text2 == null) {
            return;
        }

        boolean useParagraphMode = text1.contains("\n\n") || text1.contains("\r\n\r\n");
        List<String> list1;
        List<String> list2;

        if (useParagraphMode) {
            list1 = Arrays.asList(text1.split("(\\r\\n|\\n){2,}"));
            list2 = Arrays.asList(text2.split("(\\r\\n|\\n){2,}"));
        } else {
            list1 = Arrays.asList(text1.split("\\R"));
            list2 = Arrays.asList(text2.split("\\R"));
        }

        Patch<String> patch = DiffUtils.diff(list1, list2, false);

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            switch (delta.getType()) {
                case CHANGE:
                    report.addTextDifference(new TextDifference(TextDifference.DiffType.CHANGE,
                            String.join("\n", delta.getSource().getLines()),
                            String.join("\n", delta.getTarget().getLines())));
                    break;
                case DELETE:
                    report.addTextDifference(new TextDifference(TextDifference.DiffType.DELETE,
                            String.join("\n", delta.getSource().getLines()),
                            ""));
                    break;
                case INSERT:
                    report.addTextDifference(new TextDifference(TextDifference.DiffType.INSERT,
                            "",
                            String.join("\n", delta.getTarget().getLines())));
                    break;
            }
        }
    }

    private String getHeaderString(List<List<String>> table) {
        if (table == null || table.isEmpty()) {
            return "";
        }
        for (List<String> row : table) {
            String joined = String.join(" ", row).trim();
            if (!joined.isEmpty()) {
                return joined;
            }
        }
        return "";
    }

    private void compareTables(List<List<List<String>>> tables1, List<List<List<String>>> tables2, ComparisonReport report) {
        final double SIMILARITY_THRESHOLD = 0.8;
        JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
        boolean[] table2Matched = new boolean[tables2.size()];

        for (int i = 0; i < tables1.size(); i++) {
            List<List<String>> table1 = tables1.get(i);
            String header1 = getHeaderString(table1);

            int bestMatchIndex = -1;
            double bestMatchScore = -1;

            for (int j = 0; j < tables2.size(); j++) {
                if (table2Matched[j]) continue;

                List<List<String>> table2 = tables2.get(j);
                String header2 = getHeaderString(table2);
                double score = similarity.apply(header1, header2);

                if (score > bestMatchScore) {
                    bestMatchScore = score;
                    bestMatchIndex = j;
                }
            }

            if (bestMatchIndex != -1 && bestMatchScore >= SIMILARITY_THRESHOLD) {
                table2Matched[bestMatchIndex] = true;
                compareSingleTable(table1, tables2.get(bestMatchIndex), i, bestMatchIndex, report);
            } else {
                report.addTableDifference(new TableDifference(i, -1, -1, -1, "Full Table", "No Match Found"));
            }
        }

        for (int j = 0; j < tables2.size(); j++) {
            if (!table2Matched[j]) {
                report.addTableDifference(new TableDifference(-1, j, -1, -1, "No Match Found", "Full Table"));
            }
        }
    }

    private void compareSingleTable(List<List<String>> table1, List<List<String>> table2, int table1Index, int table2Index, ComparisonReport report) {
        int rows1 = table1.size();
        int rows2 = table2.size();
        int cols1 = rows1 > 0 ? table1.get(0).size() : 0;
        int cols2 = rows2 > 0 ? table2.get(0).size() : 0;

        if (rows1 != rows2 || cols1 != cols2) {
            report.addTableDifference(new TableDifference(table1Index, table2Index, -1, -1, "Dimensions",
                    String.format("(%d rows, %d cols) vs (%d rows, %d cols)", rows1, cols1, rows2, cols2)));
        }

        int maxRows = Math.max(rows1, rows2);
        for (int j = 0; j < maxRows; j++) {
            List<String> row1 = j < table1.size() ? table1.get(j) : new ArrayList<>();
            List<String> row2 = j < table2.size() ? table2.get(j) : new ArrayList<>();
            int maxCols = Math.max(row1.size(), row2.size());
            for (int k = 0; k < maxCols; k++) {
                String cell1 = k < row1.size() ? row1.get(k) : "";
                String cell2 = k < row2.size() ? row2.get(k) : "";
                if (!cell1.equals(cell2)) {
                    report.addTableDifference(new TableDifference(table1Index, table2Index, j, k, cell1, cell2));
                }
            }
        }
    }

    private void compareImages(List<BufferedImage> images1, List<BufferedImage> images2, ComparisonReport report) {
        List<String> hashes1 = new ArrayList<>();
        for (BufferedImage img : images1) {
            hashes1.add(getDHash(img));
        }
        List<String> hashes2 = new ArrayList<>();
        for (BufferedImage img : images2) {
            hashes2.add(getDHash(img));
        }

        boolean[] image2Matched = new boolean[images2.size()];

        for (int i = 0; i < hashes1.size(); i++) {
            String hash1 = hashes1.get(i);
            int bestMatchIndex = -1;
            double minDistance = Double.MAX_VALUE;

            for (int j = 0; j < hashes2.size(); j++) {
                if (image2Matched[j]) continue;

                String hash2 = hashes2.get(j);
                int distance = getHammingDistance(hash1, hash2);
                double normalizedDistance = (double) distance / 64.0;

                if (normalizedDistance < minDistance) {
                    minDistance = normalizedDistance;
                    bestMatchIndex = j;
                }
            }

            if (bestMatchIndex != -1 && minDistance <= IMAGE_SIMILARITY_THRESHOLD) {
                image2Matched[bestMatchIndex] = true;
                if (minDistance > 0) {
                    String desc = String.format("Images are similar with a distance of %.3f (%.1f%% similarity).", minDistance, (1 - minDistance) * 100);
                    report.addImageDifference(new ImageDifference(images1.get(i), images2.get(bestMatchIndex), desc, 1 - minDistance));
                }
            } else {
                report.addImageDifference(new ImageDifference(images1.get(i), null, "No similar image found in source 2.", 0));
            }
        }

        for (int j = 0; j < images2.size(); j++) {
            if (!image2Matched[j]) {
                report.addImageDifference(new ImageDifference(null, images2.get(j), "No similar image found in source 1.", 0));
            }
        }
    }

    private String getDHash(BufferedImage image) {
        BufferedImage resizedImage = new BufferedImage(9, 8, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, 9, 8, null);
        g.dispose();

        StringBuilder hash = new StringBuilder();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int pixel1 = resizedImage.getRGB(x, y);
                int pixel2 = resizedImage.getRGB(x + 1, y);
                hash.append(pixel1 < pixel2 ? "1" : "0");
            }
        }
        return hash.toString();
    }

    private int getHammingDistance(String hash1, String hash2) {
        int distance = 0;
        for (int i = 0; i < hash1.length(); i++) {
            if (hash1.charAt(i) != hash2.charAt(i)) {
                distance++;
            }
        }
        return distance;
    }
}
