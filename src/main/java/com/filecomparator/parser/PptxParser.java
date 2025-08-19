package com.filecomparator.parser;

import com.filecomparator.model.FileContent;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class PptxParser implements FileParser {

    private static final Logger logger = LoggerFactory.getLogger(PptxParser.class);

    @Override
    public FileContent parse(String filePath) throws IOException {
        logger.info("Parsing PowerPoint file: {}", filePath);
        FileContent fileContent = new FileContent();
        StringBuilder textBuilder = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             XMLSlideShow ppt = new XMLSlideShow(fis)) {

            logger.debug("Found {} slides in the PowerPoint file.", ppt.getSlides().size());
            for (XSLFSlide slide : ppt.getSlides()) {
                List<XSLFShape> shapes = slide.getShapes();
                for (XSLFShape shape : shapes) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        textBuilder.append(textShape.getText()).append("\n");
                    }
                }
            }
        }

        fileContent.setText(textBuilder.toString());
        logger.debug("PowerPoint file parsing complete.");
        return fileContent;
    }
}
