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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComparatorService {

    public ComparisonReport compare(FileContent content1, FileContent content2) {
        ComparisonReport report = new ComparisonReport();

        compareText(content1.getText(), content2.getText(), report);
        compareTables(content1.getTables(), content2.getTables(), report);
        compareImages(content1.getImages(), content2.getImages(), report);

        // Generate a summary
        if (report.getTextDifferences().isEmpty() && report.getTableDifferences().isEmpty() && report.getImageDifferences().isEmpty()) {
            report.setSummary("Files are identical.");
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
        int maxRows = Math.max(table1.size(), table2.size());
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
        int maxImages = Math.max(images1.size(), images2.size());
        for (int i = 0; i < maxImages; i++) {
            BufferedImage img1 = i < images1.size() ? images1.get(i) : null;
            BufferedImage img2 = i < images2.size() ? images2.get(i) : null;

            if (img1 == null) {
                report.addImageDifference(new ImageDifference(null, img2, "Image only exists in source 2"));
                continue;
            }
            if (img2 == null) {
                report.addImageDifference(new ImageDifference(img1, null, "Image only exists in source 1"));
                continue;
            }

            if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
                String desc = String.format("Different dimensions: (%d x %d) vs (%d x %d)",
                        img1.getWidth(), img1.getHeight(), img2.getWidth(), img2.getHeight());
                report.addImageDifference(new ImageDifference(img1, img2, desc));
            } else {
                try {
                    String hash1 = getImageHash(img1);
                    String hash2 = getImageHash(img2);
                    if (!hash1.equals(hash2)) {
                        report.addImageDifference(new ImageDifference(img1, img2, "Image content is different (hash mismatch)"));
                    }
                } catch (IOException | NoSuchAlgorithmException e) {
                    // Could log this error if more detail is needed
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
