# Advanced File and URL Comparator

## 1. Overview

This is a powerful, production-ready, and feature-rich Java-based application for comparing the contents of two sources, which can be local files or web URLs. It is built with a modular and scalable architecture, using industry-standard libraries like Apache POI, PDFBox, and Jsoup.

The tool intelligently extracts a wide range of content—including text, tables, and images—performs a deep, multi-faceted comparison, and generates a clear, structured report in either Microsoft Excel (`.xlsx`) or Word (`.docx`). The primary interface is a user-friendly Swing GUI.

## 2. Comprehensive Feature Set

### 2.1. Universal Source Support
The tool can compare content from any combination of the following sources, making it a highly versatile analysis tool:
- **Web Pages**: Provide any URL (`http` or `https`) to fetch, parse, and analyze live web content.
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
- **Content-Based Image Comparison**: Compares images by the **SHA-256 hash** of their content.

### 2.4. High-Quality, Structured Reporting
- **Dual Format Output**: Automatically generates reports in both **Microsoft Excel (`.xlsx`)** and **Microsoft Word (`.docx`)**.
- **Structured Difference Tables**: Presents mismatches in clear, tabular formats for easy analysis.
- **Visual Highlighting**: Uses colors in the report to highlight text additions, deletions, and changes.
- **Embedded Images**: Places differing images side-by-side in the report for direct visual comparison.
- **Dynamic Naming**: Report files are automatically named with the source types and a timestamp (e.g., `PDF_vs_URL_Compare_20250822_123045.docx`).

### 2.5. Professional & Robust Architecture
- **Industry Standards**: Built with **Java 11** and **Maven**.
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
The application features a graphical user interface (GUI). Run the executable JAR file to launch it:

```bash
java -jar target/file-comparator-1.0.0-jar-with-dependencies.jar
```

### 3.4. Using the Application
1.  Launch the application by running the executable JAR file.
2.  For each of the two sources, select the content type from the dropdown menu (e.g., "PDF", "URL", "PNG").
3.  Enter the full file path or URL into the corresponding text field. For local files, you can also use the "Browse..." button to open a file chooser.
4.  Click the "Compare" button to begin the comparison.
5.  A dialog box will appear asking you to select a **folder** where the output reports will be saved.
6.  The tool will then perform the comparison and save **two report files** in that folder: one in Word format (`.docx`) and one in Excel format (`.xlsx`).
7.  A confirmation message will appear when the process is complete.
8.  Click the "Close" button to exit the application.
