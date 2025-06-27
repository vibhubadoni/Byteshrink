# ByteShrink - File Compression Tool

A JavaFX-based application for compressing text and image files with a modern and user-friendly interface.

## Features

- Support for text and image file compression
- Modern, intuitive user interface
- Real-time compression progress tracking
- Detailed file information display
- Support for multiple file formats
  - Text: .txt, .csv, .json, .xml
  - Images: .png, .jpg, .jpeg, .gif, .bmp

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Building the Application

1. Clone the repository
2. Navigate to the project directory
3. Run the following command:
   ```bash
   mvn clean package
   ```

## Running the Application

After building, you can run the application using:

```bash
mvn javafx:run
```

## Usage

1. Launch the application
2. Select the compression type (Text or Image)
3. Click "Upload Files" to choose a file
4. View file details in the information panel
5. Click "Compress" to start the compression process
6. Monitor progress in the progress bar
7. Find the compressed file in the same directory as the input file with "_compressed" suffix

## Note

This is a basic implementation. The current version simulates the compression process. To implement actual compression algorithms, you would need to modify the `handleCompression()` method in the `MainController` class. 