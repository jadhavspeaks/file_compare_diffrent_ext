# Advanced File and URL Comparator

## Overview

This is a powerful, production-ready Java-based command-line tool for comparing the contents of two sources, which can be local files or web URLs. It intelligently extracts text, tables, and images from a wide variety of formats, performs a deep comparison, and generates a structured report in either Microsoft Excel (`.xlsx`) or Word (`.docx`).

## Key Features

### Multi-Format Support
The tool can compare content from any combination of the following sources:
- **Web Pages** (via URL)
- **Microsoft Excel** (`.xlsx`, `.xls`)
- **Adobe PDF** (`.pdf`)
- **Microsoft PowerPoint** (`.pptx`)
- **CSV / TSV** (`.csv`, `.tsv`)
- **Plain Text** (`.txt`)

### Intelligent Content Extraction
- **From URLs:** Fetches and parses HTML content to extract clean text, tables, and images.
- **From PDF/PowerPoint:** Extracts not only text but also tables and images embedded within the documents.
- **From Excel/CSV:** Reads all tabular data. The CSV parser automatically detects common delimiters (comma, semicolon, tab).
- **From Text:** Extracts plain text and heuristically detects and parses table-like structures.

### Advanced Comparison Engine
- **Text Comparison:** Automatically switches between line-by-line and paragraph-by-paragraph comparison for the most relevant results.
- **Intelligent Table Matching:** Instead of just comparing tables in order, the engine uses header similarity (Jaro-Winkler algorithm) to intelligently match tables between sources before comparing them.
- **Image Content Comparison:** Compares images not just by their dimensions but by the SHA-256 hash of their content, ensuring that visually identical images are correctly matched.

### Structured Reporting
- **Dual Format:** Generates comparison reports in either Microsoft Excel (`.xlsx`) or Word (`.docx`).
- **Clear and Detailed:** Differences are presented in a structured format. For example, table mismatches are shown in a table with columns for `Table`, `Row`, `Column`, `File 1 Value`, and `File 2 Value` for easy analysis.

## Prerequisites

- Java 11 or higher
- Apache Maven

## How to Build

1.  Clone the repository.
2.  Navigate to the project's root directory.
3.  Run the following Maven command to build the project and create an executable JAR file:
    ```bash
    mvn clean package
    ```
    This will generate a `file-comparator-1.0.0-jar-with-dependencies.jar` file in the `target` directory.

## Usage

Run the application from the command line using the following format. The output format is determined by the file extension of the report path (`.xlsx` or `.docx`).

```bash
java -jar target/file-comparator-1.0.0-jar-with-dependencies.jar <source1_path_or_url> <source2_path_or_url> <output_report_path>
```

### Example 1: Comparing a Local File and a URL

```bash
java -jar target/file-comparator-1.0.0-jar-with-dependencies.jar src/main/resources/samples/sample1.csv https://www.w3.org/WAI/ARIA/apg/patterns/table/examples/sortable-table/ report.docx
```

### Example 2: Comparing Two Local Files with an Excel Report

```bash
java -jar target/file-comparator-1.0.0-jar-with-dependencies.jar src/main/resources/samples/sample1.txt src/main/resources/samples/sample2.txt report.xlsx
```

## Limitations

- **Complex Web Pages:** The URL parser may struggle with highly dynamic, JavaScript-heavy web pages. It works best with static HTML content.
- **Advanced Table Matching:** The table matching is based on header similarity. It may not be perfect for tables without headers or with very generic headers.
- **Deep Image Analysis:** Image comparison is based on a cryptographic hash. It will detect if images are different but will not provide a visual diff of the changes.
