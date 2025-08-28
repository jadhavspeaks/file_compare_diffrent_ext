package com.filecomparator;

import com.filecomparator.model.ComparisonReport;
import com.filecomparator.model.FileContent;
import com.filecomparator.parser.FileParser;
import com.filecomparator.service.ParserFactory;
import com.filecomparator.report.ExcelReportGenerator;
import com.filecomparator.report.WordReportGenerator;
import com.filecomparator.service.ComparatorService;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

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

            Optional<FileParser> parser1Opt = ParserFactory.getParser(source1);
            if (parser1Opt.isEmpty()) {
                logger.error("Unsupported input type for: {}", source1);
                return;
            }
            logger.info("Parsing input 1: {}", source1);
            FileContent content1 = parser1Opt.get().parse(source1);

            Optional<FileParser> parser2Opt = ParserFactory.getParser(source2);
            if (parser2Opt.isEmpty()) {
                logger.error("Unsupported input type for: {}", source2);
                return;
            }
            logger.info("Parsing input 2: {}", source2);
            FileContent content2 = parser2Opt.get().parse(source2);

            logger.info("Comparing content...");
            ComparatorService comparator = new ComparatorService();
            ComparisonReport report = comparator.compare(content1, content2);

            logger.info("Generating report...");
            if (outputPath.toLowerCase().endsWith(".xlsx")) {
                new ExcelReportGenerator().generateReport(report, outputPath);
            } else if (outputPath.toLowerCase().endsWith(".docx")) {
                new WordReportGenerator().generateReport(report, outputPath);
            } else {
                logger.error("Unsupported output file format. Please use .xlsx or .docx");
                return;
            }

            logger.info("Comparison finished. Report generated at: {}", outputPath);

        } catch (IOException e) {
            logger.error("An error occurred during file comparison.", e);
        }
    }
}
