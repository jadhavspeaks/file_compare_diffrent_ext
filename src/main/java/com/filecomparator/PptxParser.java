package com.filecomparator;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class PptxParser implements FileParser {

    @Override
    public FileContent parse(String filePath) throws IOException {
        FileContent fileContent = new FileContent();
        StringBuilder textBuilder = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             XMLSlideShow ppt = new XMLSlideShow(fis)) {

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
        return fileContent;
    }
}
