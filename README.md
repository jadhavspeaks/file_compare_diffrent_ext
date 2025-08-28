# Advanced File and URL Comparator

## 1. Overview

This is a powerful, production-ready, and feature-rich Java-based application for comparing the contents of two sources, which can be local files or web URLs. It is built with a modular and scalable architecture, using industry-standard libraries like Apache POI, PDFBox, and Jsoup.

The tool intelligently extracts a wide range of content—including text, tables, and images—performs a deep, multi-faceted comparison, and generates a clear, structured report in either Microsoft Excel (`.xlsx`) or Word (`.docx`). The primary interface is a user-friendly Swing GUI, but a command-line interface is also available.

## 2. Comprehensive Feature Set

### 2.1. Universal Source Support
The tool can compare content from any combination of the following sources, making it a highly versatile analysis tool:
- **Web Pages**: Provide any URL (`http` or `https`) to fetch, parse, and analyze live web content.
- **Microsoft Word**: Full support for modern `.docx` documents.
- **Microsoft Excel**: Full support for both modern (`.xlsx`) and legacy (`.xls`) formats.
- **Adobe PDF**: Robustly handles `.pdf` documents.
- **Microsoft PowerPoint**: Extracts content from `.pptx` presentations.
- **Delimited Text Files**: Supports both comma-separated (`.csv`) and tab-separated (`.tsv`) files.
- **Image Files**: Supports common image formats (`.jpeg`, `.jpg`, `.png`).
- **Plain Text**: Parses standard `.txt` files.

### 2.2. Intelligent Content Extraction
- **HTML Parsing**: For URLs, the tool uses **Jsoup** to parse the HTML, extracting clean, visible text, all `<table>` elements, and all `<img>` elements with their data.
- **PDF & PowerPoint Intelligence**: Extracts not just running text but also **tables** (using the Tabula library for PDFs) and **images** embedded within the documents.
- **Heuristic Text Table Detection**: For plain `.txt` files, the tool employs a heuristic algorithm to automatically detect and extract table-like structures.
- **Automatic Delimiter Detection**: The CSV/TSV parser automatically detects the most likely delimiter from a set of common characters.

### 2.3. Advanced Comparison Engine
- **Intelligent Table Matching**: The engine uses the **Jaro-Winkler string similarity algorithm** on table headers to intelligently match tables between the two sources.
- **Context-Aware Text Comparison**: Automatically switches between a **line-by-line** and a **paragraph-by-paragraph** comparison for more intuitive results.
- **Perceptual Image Comparison**: Instead of a simple hash check, the tool now uses a **dHash (Difference Hash)** algorithm to compare images. This allows it to detect images that are **visually similar**, not just identical, and provides a similarity score in the final report.

### 2.4. High-Quality, Structured Reporting
- **Dual Format Output**: Automatically generates reports in both **Microsoft Excel (`.xlsx`)** and **Microsoft Word (`.docx`)**.
- **Structured Difference Tables**: Presents mismatches in clear, tabular formats for easy analysis.
- **Visual Highlighting**: Uses colors in the report to highlight text additions, deletions, and changes.
- **Embedded Images**: Places differing images side-by-side in the report for direct visual comparison.
- **Dynamic Naming**: Report files are automatically named with the source types and a timestamp (e.g., `PDF_vs_URL_Compare_20250822_123045.docx`).

### 2.5. Professional & Robust Architecture
- **Industry Standards**: Built with **Java 11** and **Maven**.
- **Plugin-Based Parsers**: The architecture uses Java's **ServiceLoader** to dynamically load file parsers. This makes the application highly extensible—new file types can be supported simply by adding a new parser class to the classpath.
- **Clean Architecture**: The codebase is cleanly separated into a professional package structure (`model`, `parser`, `service`, `report`, `ui`).
- **Configurable Logging**: Uses **SLF4J** and **Logback** for robust logging to both the console and a log file.
- **Robust Build:** Uses the **maven-shade-plugin** to create a reliable executable JAR, correctly handling all necessary dependencies and service files to prevent classpath issues.

## 3. Usage

### 3.1. Prerequisites
- Java 11 or higher
- Apache Maven

### 3.2. How to Build
1.  Clone the repository.
2.  Navigate to the project's root directory.
3.  Run the following Maven command to build the project and create an executable JAR file:
    ```bash
    mvn clean package
    ```
    This will generate a `file-comparator-1.0.0-jar-with-dependencies.jar` file in the `target` directory.

### 3.3. How to Run
The application has two entry points: a graphical user interface (GUI) and a command-line interface (CLI).

#### Running the GUI
To launch the user-friendly Swing GUI, run the following command:
```bash
java -cp target/file-comparator-1.0.0-jar-with-dependencies.jar com.filecomparator.ui.UIMain
```

#### Running the CLI
To run the command-line version, use the following format:
```bash
java -cp target/file-comparator-1.0.0-jar-with-dependencies.jar com.filecomparator.Main -f1 <source1> -f2 <source2> -r <report_path.docx_or_xlsx>
```
Example:
```bash
java -cp target/file-comparator-1.0.0-jar-with-dependencies.jar com.filecomparator.Main -f1 samples/sample1.pdf -f2 samples/sample2.pdf -r my_report.docx
```

### 3.4. Using the Application (GUI)
1.  Launch the application using the command provided above.
2.  For each of the two sources, select the content type from the dropdown menu (e.g., "PDF", "URL", "DOCX").
3.  Enter the full file path or URL into the corresponding text field. For local files, you can also use the "Browse..." button to open a file chooser.
4.  Click the "Compare" button. The button will become disabled, and a **progress bar** will appear to provide feedback during the comparison.
5.  A dialog box will appear asking you to select a **folder** where the output reports will be saved.
6.  The tool will perform the comparison and save two report files in that folder: one in Word format (`.docx`) and one in Excel format (`.xlsx`).
7.  When complete, a **results summary** will be displayed in the text area at the bottom of the window, and a confirmation message will appear.
8.  Click the "Close" button to exit the application.
