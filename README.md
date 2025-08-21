# Advanced File and URL Comparator

## 1. Overview

This is a powerful, production-ready, and feature-rich Java-based command-line tool for comparing the contents of two sources, which can be local files or web URLs. It is built with a modular and scalable architecture, using industry-standard libraries like Apache POI, PDFBox, and Jsoup.

The tool intelligently extracts a wide range of content—including text, tables, and images—performs a deep, multi-faceted comparison, and generates a clear, structured report in either Microsoft Excel (`.xlsx`) or Word (`.docx`).

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
The tool goes beyond simple text extraction to build a deep understanding of the source content.
- **HTML Parsing**: For URLs, the tool uses **Jsoup** to parse the HTML, extracting clean, visible text, all `<table>` elements, and all `<img>` elements with their data.
- **PDF & PowerPoint Intelligence**: Extracts not just running text but also **tables** (using the Tabula library for PDFs) and **images** embedded within the documents.
- **Heuristic Text Table Detection**: For plain `.txt` files, the tool employs a heuristic algorithm to automatically detect and extract table-like structures based on consistent column layouts.
- **Automatic Delimiter Detection**: The CSV/TSV parser is not hardcoded. It automatically detects the most likely delimiter (comma, semicolon, or tab) for maximum flexibility.

### 2.3. Advanced Comparison Engine
The comparison logic is designed to be both powerful and insightful.
- **Intelligent Table Matching**: The engine uses the **Jaro-Winkler string similarity algorithm** on table headers to intelligently match tables between the two sources. This avoids incorrect comparisons and allows the final report to show which tables were matched, which were unique, and which had no logical counterpart.
- **Context-Aware Text Comparison**: The text diffing engine automatically analyzes the text and switches between a **line-by-line** comparison and a more logical **paragraph-by-paragraph** comparison, providing the most intuitive results for the given content.
- **Content-Based Image Comparison**: The tool verifies if images are truly identical by comparing the **SHA-256 hash** of their content, rather than relying on superficial checks like dimensions or file names.

### 2.4. High-Quality, Structured Reporting
- **Dual Format Output**: Generates user-friendly reports in either **Microsoft Excel (`.xlsx`)** or **Microsoft Word (`.docx`)**, determined by the output file extension.
- **Structured Difference Tables**: Mismatches are not just listed; they are presented in clear, structured tables. For example, table differences are shown with columns for `Table`, `Row`, `Column`, `Source 1 Value`, and `Source 2 Value`.

### 2.5. Professional & Robust Architecture
- **Industry Standards**: Built with **Java 11** and **Maven** for reliable dependency management and builds.
- **Modular Design**: The codebase is cleanly separated into a professional package structure (`model`, `parser`, `service`, `report`) for scalability and maintainability.
- **Configurable Logging**: Uses **SLF4J** and a **Logback** configuration (`logback.xml`) for robust, configurable logging to both the console and a dedicated log file (`file-comparator.log`).
- **Resilient Parsing**: All parsers are designed to be robust and will not fail if a source is missing certain elements (e.g., a PDF with no tables or a web page with no images).

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
The application now features a graphical user interface (GUI). Run the executable JAR file to launch it:

```bash
java -jar target/file-comparator-1.0.0-jar-with-dependencies.jar
```

### 3.4. Using the Application
1.  Launch the application by running the executable JAR file.
2.  For each of the two sources ("Source 1" and "Source 2"), select the content type from the dropdown menu (e.g., "PDF", "URL", "PNG").
3.  Enter the full file path or URL into the corresponding text field. For local files, you can also use the "Browse..." button to open a file chooser.
4.  Click the "Compare" button to begin the comparison.
5.  A dialog box will appear asking you to select a **folder** where the output reports will be saved.
6.  After you select a folder, the tool will perform the comparison and save **two report files** in that folder: one in Microsoft Word format (`.docx`) and one in Microsoft Excel format (`.xlsx`). The filenames will be automatically generated based on the source types and a timestamp (e.g., `PDF_vs_URL_Compare_20250821_123045.docx`).
7.  A confirmation message will appear when the process is complete.
8.  Click the "Close" button to exit the application.

## 4. Limitations
- **Dynamic Web Pages:** The URL parser works best with static HTML content and may not correctly parse content loaded dynamically with JavaScript.
- **Complex Table Matching:** The table matching is based on header similarity. It may be less effective for tables without headers or with very generic headers.
- **Visual Image Diffing:** The tool can detect if images are different via hashing but does not provide a visual "diff" of the image changes.
