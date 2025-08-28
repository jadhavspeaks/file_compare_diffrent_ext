package com.filecomparator.parser;

import com.filecomparator.model.FileContent;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UrlParser implements FileParser {

    private static final Logger logger = LoggerFactory.getLogger(UrlParser.class);
    private final CloseableHttpClient httpClient;

    public UrlParser() {
        this.httpClient = HttpClients.createDefault();
    }

    @Override
    public boolean canParse(String input) {
        String lowerCaseInput = input.toLowerCase();
        return lowerCaseInput.startsWith("http://") || lowerCaseInput.startsWith("https://");
    }

    @Override
    public FileContent parse(String urlString) throws IOException {
        logger.info("Parsing URL: {}", urlString);
        FileContent fileContent = new FileContent();

        HttpGet request = new HttpGet(urlString);
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            HttpEntity entity = response.getEntity();
            String htmlContent = EntityUtils.toString(entity);
            Document doc = Jsoup.parse(htmlContent, urlString);

            // Extract text, tables, and images
            extractText(doc, fileContent);
            extractTables(doc, fileContent);
            extractImages(doc, fileContent);
        }

        return fileContent;
    }

    private void extractText(Document doc, FileContent fileContent) {
        fileContent.setText(doc.body().text());
        logger.debug("URL text extraction complete.");
    }

    private void extractTables(Document doc, FileContent fileContent) {
        Elements tables = doc.select("table");
        for (Element tableElement : tables) {
            List<List<String>> table = new ArrayList<>();
            for (Element rowElement : tableElement.select("tr")) {
                List<String> row = new ArrayList<>();
                for (Element cellElement : rowElement.select("td, th")) {
                    row.add(cellElement.text());
                }
                table.add(row);
            }
            fileContent.addTable(table);
        }
        logger.info("URL table extraction complete. Found {} tables.", fileContent.getTables().size());
    }

    private void extractImages(Document doc, FileContent fileContent) {
        Elements images = doc.select("img");
        for (Element imgElement : images) {
            String imageUrl = imgElement.absUrl("src");
            if (!imageUrl.isEmpty()) {
                try {
                    HttpGet imageRequest = new HttpGet(imageUrl);
                    try (CloseableHttpResponse imageResponse = httpClient.execute(imageRequest)) {
                        HttpEntity imageEntity = imageResponse.getEntity();
                        if (imageEntity != null) {
                            try (InputStream imageStream = imageEntity.getContent()) {
                                BufferedImage image = ImageIO.read(imageStream);
                                if (image != null) {
                                    fileContent.addImage(image);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Could not download or process image from URL: {}", imageUrl, e);
                }
            }
        }
        logger.info("URL image extraction complete. Found {} images.", fileContent.getImages().size());
    }
}
