package com.filecomparator.service;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.filecomparator.model.diff.ImageDifference;
import com.filecomparator.model.diff.TableDifference;
import com.filecomparator.model.diff.TextDifference;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

public class ComparatorService {

    private static final double IMAGE_SIMILARITY_THRESHOLD = 0.2;
    private static final double TABLE_SIMILARITY_THRESHOLD = 0.5;

    public ComparisonReport compare(FileContent content1, FileContent content2) {
        ComparisonReport report = new ComparisonReport(content1, content2);
        compareText(content1.getText(), content2.getText(), report);
        compareTables(content1.getTables(), content2.getTables(), report);
        compareImages(content1.getImages(), content2.getImages(), report);
        return report;
    }

    private void compareText(String text1, String text2, ComparisonReport report) {
        if (text1 == null || text2 == null || (text1.isEmpty() && text2.isEmpty())) {
            return;
        }

        List<String> list1 = Arrays.asList(text1.split("\\R"));
        List<String> list2 = Arrays.asList(text2.split("\\R"));
        Patch<String> patch = DiffUtils.diff(list1, list2, false);

        for (AbstractDelta<String> delta : patch.getDeltas()) {
            TextDifference.DiffType type;
            switch (delta.getType()) {
                case INSERT: type = TextDifference.DiffType.INSERT; break;
                case DELETE: type = TextDifference.DiffType.DELETE; break;
                default: type = TextDifference.DiffType.CHANGE; break;
            }
            report.addTextDifference(new TextDifference(type, String.join("\n", delta.getSource().getLines()), String.join("\n", delta.getTarget().getLines())));
        }
    }

    private List<String> findHeaderRow(List<List<String>> table, int[] headerRowIndex) {
        for (int i = 0; i < table.size(); i++) {
            List<String> row = table.get(i);
            if (row != null && !row.stream().allMatch(String::isEmpty)) {
                headerRowIndex[0] = i;
                return row;
            }
        }
        headerRowIndex[0] = -1;
        return new ArrayList<>();
    }

    private double calculateJaccardSimilarity(List<String> list1, List<String> list2) {
        Set<String> set1 = list1.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> set2 = list2.stream().map(String::toLowerCase).collect(Collectors.toSet());

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 1.0 : (double) intersection.size() / union.size();
    }

    private void compareTables(List<List<List<String>>> tables1, List<List<List<String>>> tables2, ComparisonReport report) {
        boolean[] table2Matched = new boolean[tables2.size()];

        for (int i = 0; i < tables1.size(); i++) {
            List<List<String>> table1 = tables1.get(i);
            List<String> header1 = findHeaderRow(table1, new int[1]);
            int bestMatchIndex = -1;
            double bestMatchScore = -1;

            for (int j = 0; j < tables2.size(); j++) {
                if (table2Matched[j]) continue;
                List<List<String>> table2 = tables2.get(j);
                List<String> header2 = findHeaderRow(table2, new int[1]);
                double score = calculateJaccardSimilarity(header1, header2);
                if (score > bestMatchScore) {
                    bestMatchScore = score;
                    bestMatchIndex = j;
                }
            }

            if (bestMatchIndex != -1 && bestMatchScore >= TABLE_SIMILARITY_THRESHOLD) {
                table2Matched[bestMatchIndex] = true;
                compareSingleTable(table1, tables2.get(bestMatchIndex), i, bestMatchIndex, report);
            } else {
                 report.addTableDifference(new TableDifference(i, -1, "No matching table found for table " + (i + 1)));
            }
        }

        for (int j = 0; j < tables2.size(); j++) {
            if (!table2Matched[j]) {
                report.addTableDifference(new TableDifference(-1, j, "Table " + (j + 1) + " from source 2 had no match in source 1."));
            }
        }
    }

    private void compareSingleTable(List<List<String>> table1, List<List<String>> table2, int table1Index, int table2Index, ComparisonReport report) {
        int[] headerRowIndex1Arr = {-1};
        int[] headerRowIndex2Arr = {-1};
        List<String> header1 = findHeaderRow(table1, headerRowIndex1Arr);
        List<String> header2 = findHeaderRow(table2, headerRowIndex2Arr);
        int h1_idx = headerRowIndex1Arr[0];
        int h2_idx = headerRowIndex2Arr[0];

        if (h1_idx == -1 || h2_idx == -1) return;

        Map<Integer, Integer> columnMap = new HashMap<>();
        boolean[] header2Matched = new boolean[header2.size()];

        for (int i = 0; i < header1.size(); i++) {
            String h1 = header1.get(i);
            int bestMatch = -1;
            for (int j = 0; j < header2.size(); j++) {
                if (!header2Matched[j] && h1.equalsIgnoreCase(header2.get(j))) {
                    bestMatch = j;
                    break;
                }
            }
            if (bestMatch != -1) {
                columnMap.put(i, bestMatch);
                header2Matched[bestMatch] = true;
            } else {
                report.addTableDifference(new TableDifference(TableDifference.DiffType.COLUMN_DELETED, table1Index, table2Index, h1));
            }
        }

        for (int j = 0; j < header2.size(); j++) {
            if (!header2Matched[j]) {
                report.addTableDifference(new TableDifference(TableDifference.DiffType.COLUMN_ADDED, table1Index, table2Index, header2.get(j)));
            }
        }

        List<List<String>> dataRows1 = table1.subList(h1_idx + 1, table1.size());
        List<List<String>> dataRows2 = table2.subList(h2_idx + 1, table2.size());
        int maxDataRows = Math.max(dataRows1.size(), dataRows2.size());

        for (int i = 0; i < maxDataRows; i++) {
            List<String> row1 = i < dataRows1.size() ? dataRows1.get(i) : new ArrayList<>();
            List<String> row2 = i < dataRows2.size() ? dataRows2.get(i) : new ArrayList<>();

            for (Map.Entry<Integer, Integer> entry : columnMap.entrySet()) {
                int col1 = entry.getKey();
                int col2 = entry.getValue();
                String cell1 = col1 < row1.size() ? row1.get(col1) : "";
                String cell2 = col2 < row2.size() ? row2.get(col2) : "";
                if (!cell1.equals(cell2)) {
                    report.addTableDifference(new TableDifference(table1Index, table2Index, h1_idx + 1 + i, col1, cell1, cell2));
                }
            }
        }
    }

    private void compareImages(List<BufferedImage> images1, List<BufferedImage> images2, ComparisonReport report) {
        List<String> hashes1 = images1.stream().map(this::getDHash).collect(Collectors.toList());
        List<String> hashes2 = images2.stream().map(this::getDHash).collect(Collectors.toList());
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
                hash.append(resizedImage.getRGB(x, y) < resizedImage.getRGB(x + 1, y) ? "1" : "0");
            }
        }
        return hash.toString();
    }

    private int getHammingDistance(String hash1, String hash2) {
        int distance = 0;
        for (int i = 0; i < Math.min(hash1.length(), hash2.length()); i++) {
            if (hash1.charAt(i) != hash2.charAt(i)) {
                distance++;
            }
        }
        return distance;
    }
}
