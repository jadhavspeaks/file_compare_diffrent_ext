package com.filecomparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length != 3) {
            logger.error("Usage: java -jar file-comparator.jar <file1_path> <file2_path> <output_report_path>");
            return;
        }

        String filePath1 = args[0];
        String filePath2 = args[1];
        String outputPath = args[2];

        try {
            logger.info("Starting comparison for files: {} and {}", filePath1, filePath2);

            // Parse file 1
            Optional<FileParser> parser1Opt = ParserFactory.getParser(filePath1);
            if (parser1Opt.isEmpty()) {
                logger.error("Unsupported file type for file: {}", filePath1);
                return;
            }
            logger.info("Parsing file 1: {}", filePath1);
            FileContent content1 = parser1Opt.get().parse(filePath1);

            // Parse file 2
            Optional<FileParser> parser2Opt = ParserFactory.getParser(filePath2);
            if (parser2Opt.isEmpty()) {
                logger.error("Unsupported file type for file: {}", filePath2);
                return;
            }
            logger.info("Parsing file 2: {}", filePath2);
            FileContent content2 = parser2Opt.get().parse(filePath2);

            // Compare the files
            logger.info("Comparing content...");
            ComparatorService comparatorService = new ComparatorService();
            ComparisonReport report = comparatorService.compare(content1, content2);

            // Generate the report
            logger.info("Generating report...");
            ReportGenerator reportGenerator = new ReportGenerator();
            reportGenerator.generateReport(report, outputPath);

            logger.info("Comparison finished. Report generated at: {}", outputPath);

        } catch (IOException e) {
            logger.error("An error occurred during file comparison.", e);
        }
    }
}
