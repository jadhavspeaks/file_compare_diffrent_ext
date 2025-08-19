package com.filecomparator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TxtParser implements FileParser {

    @Override
    public FileContent parse(String filePath) throws IOException {
        FileContent fileContent = new FileContent();
        String text = new String(Files.readAllBytes(Paths.get(filePath)));
        fileContent.setText(text);
        return fileContent;
    }
}
