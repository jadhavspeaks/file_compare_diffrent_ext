package com.filecomparator;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.filecomparator.report.ExcelReportGenerator;
import com.filecomparator.report.WordReportGenerator;
import com.filecomparator.service.ComparatorService;
import com.filecomparator.service.ParserUtils;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("f1", "file1", true, "First file path or URL");
        options.addOption("f2", "file2", true, "Second file path or URL");
        options.addOption("r", "report", true, "Output report file path (.docx or .xlsx)");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("file-comparator", options);
            System.exit(1);
            return;
        }

        String source1 = cmd.getOptionValue("file1");
        String source2 = cmd.getOptionValue("file2");
        String outputPath = cmd.getOptionValue("report");

        if (source1 == null || source2 == null || outputPath == null) {
            formatter.printHelp("file-comparator", options);
            System.exit(1);
            return;
        }

        try {
            logger.info("Starting comparison for inputs: {} and {}", source1, source2);

            FileContent content1 = ParserUtils.parseAndValidate(source1);
            FileContent content2 = ParserUtils.parseAndValidate(source2);

            ComparatorService comparator = new ComparatorService();
            ComparisonReport report = comparator.compare(content1, content2);

            if (outputPath.toLowerCase().endsWith(".xlsx")) {
                new ExcelReportGenerator().generateReport(report, outputPath);
            } else if (outputPath.toLowerCase().endsWith(".docx")) {
                new WordReportGenerator().generateReport(report, outputPath);
            } else {
                logger.error("Unsupported output file format. Please use .xlsx or .docx");
                return;
            }
            logger.info("Comparison finished. Report generated at: {}", outputPath);

        } catch (Exception e) {
            logger.error("An error occurred: {}", e.getMessage());
        }
    }
}
