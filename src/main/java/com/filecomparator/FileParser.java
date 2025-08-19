package com.filecomparator;

import java.io.IOException;

public interface FileParser {
    FileContent parse(String filePath) throws IOException;
}
