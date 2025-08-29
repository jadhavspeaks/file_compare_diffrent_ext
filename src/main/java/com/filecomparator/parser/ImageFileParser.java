package com.filecomparator.parser;

import com.filecomparator.model.FileContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageFileParser implements FileParser {

    private static final Logger logger = LoggerFactory.getLogger(ImageFileParser.class);

    @Override
    public boolean canParse(String input) {
        String lowerCaseInput = input.toLowerCase();
        return lowerCaseInput.endsWith(".jpeg") || lowerCaseInput.endsWith(".jpg") || lowerCaseInput.endsWith(".png");
    }

    @Override
    public FileContent parse(String input) throws IOException {
        logger.info("Parsing image file: {}", input);
        FileContent fileContent = new FileContent();
        BufferedImage image = ImageIO.read(new File(input));
        if (image != null) {
            fileContent.addImage(image);
        } else {
            logger.warn("Could not read image file: {}", input);
        }
        return fileContent;
    }
}
