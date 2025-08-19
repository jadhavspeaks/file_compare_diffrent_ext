package com.filecomparator.service;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

import java.util.Arrays;
import java.util.List;

public class ComparatorService {

    public ComparisonReport compare(FileContent content1, FileContent content2) {
        ComparisonReport report = new ComparisonReport();

        compareText(content1.getText(), content2.getText(), report);
        compareTables(content1.getTables(), content2.getTables(), report);
        compareImages(content1.getImages().size(), content2.getImages().size(), report);

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
            report.addTableDifference("Different number of tables: " + tables1.size() + " vs " + tables2.size());
            return;
        }

        for (int i = 0; i < tables1.size(); i++) {
            List<List<String>> table1 = tables1.get(i);
            List<List<String>> table2 = tables2.get(i);

            if (table1.size() != table2.size()) {
                report.addTableDifference("Table " + (i + 1) + " has different number of rows.");
                continue;
            }

            for (int j = 0; j < table1.size(); j++) {
                if (!table1.get(j).equals(table2.get(j))) {
                    report.addTableDifference("Difference in Table " + (i + 1) + ", Row " + (j + 1));
                }
            }
        }
    }

    private void compareImages(int imageCount1, int imageCount2, ComparisonReport report) {
        if (imageCount1 != imageCount2) {
            // For now, we just compare the number of images.
            // A more advanced comparison could involve image hashes or visual diffs.
        }
    }
}
