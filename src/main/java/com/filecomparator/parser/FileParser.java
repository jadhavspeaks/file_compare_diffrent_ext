package com.filecomparator.parser;

import com.filecomparator.model.FileContent;
import java.io.IOException;

public interface FileParser {
    boolean canParse(String input);
    FileContent parse(String input) throws IOException;
}
