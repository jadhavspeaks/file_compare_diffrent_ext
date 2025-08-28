package com.filecomparator.service;

import com.filecomparator.parser.FileParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.ServiceLoader;

public class ParserFactory {

    private static final Logger logger = LoggerFactory.getLogger(ParserFactory.class);
    private static final ServiceLoader<FileParser> loader = ServiceLoader.load(FileParser.class);

    public static Optional<FileParser> getParser(String input) {
        logger.info("Attempting to find parser for input: {}", input);
        for (FileParser parser : loader) {
            if (parser.canParse(input)) {
                logger.info("Found suitable parser: {}", parser.getClass().getSimpleName());
                return Optional.of(parser);
            }
        }
        logger.warn("No suitable parser found for input: {}", input);
        return Optional.empty();
    }
}
