package com.filecomparator.parser;

import com.filecomparator.model.FileContent;

import java.io.IOException;

public interface FileParser {
    FileContent parse(String filePath) throws IOException;
}
