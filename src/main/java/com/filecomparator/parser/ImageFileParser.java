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
    public FileContent parse(String filePath) throws IOException {
        logger.info("Parsing image file: {}", filePath);
        FileContent fileContent = new FileContent();

        BufferedImage image = ImageIO.read(new File(filePath));
        if (image != null) {
            fileContent.addImage(image);
            logger.debug("Image file parsing complete.");
        } else {
            logger.warn("Could not read image file: {}", filePath);
        }

        return fileContent;
    }
}
