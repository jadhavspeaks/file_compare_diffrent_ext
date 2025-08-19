# File Comparator

This is a Java-based command-line tool for comparing the contents of two files. It supports a variety of file types and can perform cross-combination comparisons (e.g., Excel vs. PDF).

## Supported File Types

-   Excel (.xlsx, .xls)
-   PDF (.pdf)
-   PowerPoint (.pptx)
-   CSV (.csv)
-   Text (.txt)

## Features

-   **Cross-Combination Comparison:** Compare any two supported file types.
-   **Content Extraction:** Extracts text and tables from the files.
-   **Comparison Report:** Generates a structured Excel report detailing the differences.

## Prerequisites

-   Java 11 or higher
-   Apache Maven

## How to Build

1.  Clone the repository.
2.  Navigate to the project's root directory.
3.  Run the following Maven command to build the project and create an executable JAR file:

    ```bash
    mvn clean install
    ```

    This will generate a `file-comparator-1.0.0-jar-with-dependencies.jar` file in the `target` directory.

## How to Run

Run the application from the command line using the following format:

```bash
java -jar target/file-comparator-1.0.0-jar-with-dependencies.jar <path_to_file1> <path_to_file2> <path_to_output_report.xlsx>
```

### Example

To compare the two sample text files included in the project and generate a report named `report.xlsx`, run the following command from the project's root directory:

```bash
java -jar target/file-comparator-1.0.0-jar-with-dependencies.jar src/main/resources/samples/sample1.txt src/main/resources/samples/sample2.txt report.xlsx
```

## Limitations

-   **PPTX Table Extraction:** The tool does not yet extract tables from PowerPoint files.
-   **Image Comparison:** The tool detects the presence and number of images but does not perform a visual or hash-based comparison.
-   **Excel Format Support:** The tool now supports both `.xls` (legacy) and `.xlsx` (modern) Excel formats.
-   **Logging:** The application now produces meaningful logs during its execution.
-   **CSV Delimiter:** The CSV parser assumes a comma as the delimiter.
-   **Large Files:** Performance may degrade with very large files.
