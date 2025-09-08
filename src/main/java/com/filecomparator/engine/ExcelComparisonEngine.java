package com.filecomparator.engine;

import com.filecomparator.dto.ComparisonResult;
import com.filecomparator.dto.Result;
import com.filecomparator.dto.Summary;
import com.filecomparator.parser.GenericExcelParser.ReportProductPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ExcelComparisonEngine {

    public ComparisonResult compare(Map<String, Map<String, List<String>>> excel1Data,
                                    Map<String, List<ReportProductPair>> excel2Data) {

        List<Result> results = new ArrayList<>();
        int matchedCount = 0;
        int mismatchedCount = 0;

        for (Map.Entry<String, Map<String, List<String>>> entry : excel1Data.entrySet()) {
            String attribute = entry.getKey();
            Map<String, List<String>> excel1Values = entry.getValue();
            List<String> excel1Reports = excel1Values.get("Reports");
            List<String> excel1Products = excel1Values.get("Products");

            List<String> missingReports = new ArrayList<>();
            List<String> missingProducts = new ArrayList<>();
            String status = "PASS";

            if (!excel2Data.containsKey(attribute)) {
                status = "FAIL";
                missingReports.addAll(excel1Reports);
                missingProducts.addAll(excel1Products);
            } else {
                List<ReportProductPair> excel2Pairs = excel2Data.get(attribute);
                Set<String> excel2Reports = excel2Pairs.stream()
                                                       .map(ReportProductPair::getReport)
                                                       .collect(Collectors.toSet());
                Set<String> excel2Products = excel2Pairs.stream()
                                                        .map(ReportProductPair::getProduct)
                                                        .collect(Collectors.toSet());

                for (String report : excel1Reports) {
                    if (!excel2Reports.contains(report)) {
                        missingReports.add(report);
                    }
                }

                for (String product : excel1Products) {
                    if (!excel2Products.contains(product)) {
                        missingProducts.add(product);
                    }
                }

                if (!missingReports.isEmpty() || !missingProducts.isEmpty()) {
                    status = "FAIL";
                }
            }

            if (status.equals("PASS")) {
                matchedCount++;
            } else {
                mismatchedCount++;
            }
            results.add(new Result(attribute, status, missingReports, missingProducts));
        }

        Summary summary = new Summary(excel1Data.size(), matchedCount, mismatchedCount);
        return new ComparisonResult(summary, results);
    }
}
