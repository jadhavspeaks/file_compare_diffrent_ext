package com.filecomparator.parser;

import com.filecomparator.model.FileContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TxtParser implements FileParser {

    private static final Logger logger = LoggerFactory.getLogger(TxtParser.class);

    @Override
    public FileContent parse(String filePath) throws IOException {
        logger.info("Parsing text file: {}", filePath);
        FileContent fileContent = new FileContent();
        String text = new String(Files.readAllBytes(Paths.get(filePath)));
        fileContent.setText(text);
        logger.debug("Text file parsing complete. Length: {}", text.length());
        return fileContent;
    }
}
