package com.byteshrink;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import com.byteshrink.compression.HuffmanCompression;
import java.nio.charset.StandardCharsets;
import java.awt.Desktop;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import com.byteshrink.compression.ShannonFanoCompression;
import javafx.scene.image.ImageView;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import com.byteshrink.compression.ImageCompression;

public class MainController {

    @FXML
    private Button uploadButton;

    @FXML
    private Label fileNameLabel;

    @FXML
    private ComboBox<String> compressionType;

    @FXML
    private TextArea fileDetails;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private TextArea outputLog;

    @FXML
    private Button uploadCompressedButton;

    @FXML
    private Label compressedFileLabel;

    @FXML
    private TextArea decompressFileDetails;

    @FXML
    private ProgressBar decompressProgressBar;

    @FXML
    private Button openCompressedLocation;

    @FXML
    private Button openDecompressedLocation;

    @FXML
    private Button openVisualizerButton;

    @FXML
    private Button getStartedButton;

    @FXML
    private ToggleButton textTabButton;

    @FXML
    private ToggleButton imageTabButton;

    @FXML
    private ToggleButton analyticsTabButton;

    @FXML
    private Button backButton;

    @FXML
    private TextArea textInputArea;

    @FXML
    private Button compressTextButton;

    @FXML
    private Label originalSizeLabel;

    @FXML
    private Label compressedSizeLabel;

    @FXML
    private Label compressionRatioLabel;

    @FXML
    private ToggleButton visualizerTabButton;

    @FXML
    private ImageView textImageButton;

    @FXML
    private ImageView imageImageButton;

    @FXML
    private BarChart<String, Number> compressionBarChart;

    @FXML
    private BarChart<String, Number> timeBarChart;

    @FXML
    private BarChart<String, Number> zoomedBarChart;

    @FXML
    private NumberAxis zoomedYAxis;

    @FXML
    private Label sizeDiffLabel;

    private File selectedFile;
    private File selectedCompressedFile;
    private String lastCompressedPath;
    private String lastDecompressedPath;
    private String selectedCompressionType = "Text Compression";

    @FXML
    public void initialize() {
        if (openCompressedLocation != null) {
            openCompressedLocation.setDisable(true);
        }
        if (openDecompressedLocation != null) {
            openDecompressedLocation.setDisable(true);
        }
        if (getStartedButton != null) {
            getStartedButton.setOnAction(e -> openCompressionView("Text Compression"));
        }
        if (textTabButton != null) {
            textTabButton.setOnAction(e -> openCompressionView("Text Compression"));
        }
        if (imageTabButton != null) {
            imageTabButton.setOnAction(e -> openCompressionView("Image Compression"));
        }
        if (analyticsTabButton != null) {
            analyticsTabButton.setOnAction(e -> showNotImplemented("Analytics view coming soon!"));
        }
        if (backButton != null) {
            backButton.setOnAction(e -> goToHomeScreen());
        }
        if (compressTextButton != null && textInputArea != null) {
            compressTextButton.setOnAction(e -> handleTextCompressionMainScreen());
        }
        if (visualizerTabButton != null) {
            visualizerTabButton.setOnAction(e -> handleOpenVisualizer());
        }
        if (textImageButton != null) {
            textImageButton.setOnMouseClicked(e -> handleTextImageClick());
        }
        if (imageImageButton != null) {
            imageImageButton.setOnMouseClicked(e -> handleImageImageClick());
        }
    }

    @FXML
    private void handleOpenCompressedLocation() {
        if (lastCompressedPath != null) {
            openFileLocation(lastCompressedPath);
        }
    }

    @FXML
    private void handleOpenDecompressedLocation() {
        if (lastDecompressedPath != null) {
            openFileLocation(lastDecompressedPath);
        }
    }

    private void openFileLocation(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(file.getParentFile());
            } else {
                showAlert("Error", "The output folder no longer exists.");
            }
        } catch (IOException e) {
            showAlert("Error", "Could not open the output location: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Compress");
        File initialDir = new File("D:/sample_files");
        if (!initialDir.exists()) {
            initialDir.mkdirs();
        }
        fileChooser.setInitialDirectory(initialDir);
        updateFileChooserExtensions(fileChooser);
        Stage stage = (Stage) uploadButton.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            fileNameLabel.setText(selectedFile.getName());
            updateFileDetails(selectedFile, fileDetails);
        }
    }

    @FXML
    private void handleCompressedUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Compressed File");
        File initialDir = new File("D:/sample_files/huffman_output");
        if (!initialDir.exists()) {
            initialDir.mkdirs();
        }
        fileChooser.setInitialDirectory(initialDir);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Compressed Files", "*.huffimg", "*.huff", "*.txt", "*.csv", "*.json", "*.xml")
        );
        Stage stage = (Stage) uploadCompressedButton.getScene().getWindow();
        selectedCompressedFile = fileChooser.showOpenDialog(stage);
        if (selectedCompressedFile != null) {
            compressedFileLabel.setText(selectedCompressedFile.getName());
            updateFileDetails(selectedCompressedFile, decompressFileDetails);
        }
    }

    private void updateFileChooserExtensions(FileChooser fileChooser) {
        FileChooser.ExtensionFilter filter;
        String selected = selectedCompressionType;

        if ("Image Compression".equals(selected)) {
            filter = new FileChooser.ExtensionFilter(
                "Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp");
        } else {
            filter = new FileChooser.ExtensionFilter(
                "Text Files", "*.txt", "*.csv", "*.json", "*.xml");
        }

        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(filter);
    }

    private void updateFileDetails(File file, TextArea targetArea) {
        if (file != null) {
            StringBuilder details = new StringBuilder();
            details.append("File Name: ").append(file.getName()).append("\n");
            details.append("Size: ").append(formatFileSize(file.length())).append("\n");
            details.append("Location: ").append(file.getAbsolutePath());
            targetArea.setText(details.toString());
        }
    }

    private String formatFileSize(long size) {
        final String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double fileSize = size;

        while (fileSize > 1024 && unitIndex < units.length - 1) {
            fileSize /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", fileSize, units[unitIndex]);
    }

    @FXML
    private void handleCompression() {
        if (selectedFile == null) {
            showAlert("Error", "Please select a file first!");
            return;
        }

        String type = selectedCompressionType;

        // Start compression in a background thread
        Thread compressionThread = new Thread(() -> {
            try {
                updateProgress(progressBar, 0.0);
                updateOutput("Starting compression...");

                if ("Text Compression".equals(type)) {
                    compressTextFile();
                } else if ("Image Compression".equals(type)) {
                    compressImageFile();
                } else {
                    // For now, just copy the file for image compression
                    String outputPath = createOutputFileName(selectedFile, "_compressed");
                    Files.copy(selectedFile.toPath(), Paths.get(outputPath), StandardCopyOption.REPLACE_EXISTING);
                    updateOutput("Image compression not yet implemented.\nFile copied to: " + outputPath);
                }

                updateProgress(progressBar, 1.0);

            } catch (Exception e) {
                updateOutput("Error during compression: " + e.getMessage());
            }
        });

        compressionThread.setDaemon(true);
        compressionThread.start();
    }

    @FXML
    private void handleDecompression() {
        if (selectedCompressedFile == null) {
            showAlert("Error", "Please select a compressed file first!");
            return;
        }
        Thread decompressionThread = new Thread(() -> {
            try {
                updateProgress(decompressProgressBar, 0.0);
                updateOutput("Starting decompression...");
                if (selectedCompressedFile.getName().endsWith(".huffimg")) {
                    String outputPath = createOutputFileName(selectedCompressedFile, "_decompressed").replaceAll("\\.[^.]+$", ".png");
                    decompressImageFile(selectedCompressedFile, new File(outputPath));
                } else {
                    decompressTextFile();
                }
                updateProgress(decompressProgressBar, 1.0);
            } catch (Exception e) {
                updateOutput("Error during decompression: " + e.getMessage());
            }
        });
        decompressionThread.setDaemon(true);
        decompressionThread.start();
    }

    private void compressTextFile() throws IOException {
        // Read the input file
        String content = Files.readString(selectedFile.toPath(), StandardCharsets.UTF_8);
        updateProgress(progressBar, 0.1);
        updateOutput("File read, starting Huffman and Shannon-Fano compression...");

        // --- Huffman Compression ---
        long huffmanStart = System.nanoTime();
        HuffmanCompression huffman = new HuffmanCompression();
        byte[] huffmanData = huffman.compress(content);
        long huffmanEnd = System.nanoTime();
        long huffmanTimeMs = (huffmanEnd - huffmanStart) / 1_000_000;
        updateProgress(progressBar, 0.3);
        String huffmanDir = selectedFile.getParent() + File.separator + "huffman_output";
        Files.createDirectories(Paths.get(huffmanDir));
        String huffmanPath = huffmanDir + File.separator + selectedFile.getName().replaceFirst("\\.", "_compressed.");
        Files.write(Paths.get(huffmanPath), huffmanData);

        // --- Shannon-Fano Compression ---
        long shannonStart = System.nanoTime();
        ShannonFanoCompression shannon = new ShannonFanoCompression();
        byte[] shannonData = shannon.compress(content);
        long shannonEnd = System.nanoTime();
        long shannonTimeMs = (shannonEnd - shannonStart) / 1_000_000;
        updateProgress(progressBar, 0.6);
        String shannonDir = selectedFile.getParent() + File.separator + "shannonfano_output";
        Files.createDirectories(Paths.get(shannonDir));
        String shannonPath = shannonDir + File.separator + selectedFile.getName().replaceFirst("\\.", "_compressed.");
        Files.write(Paths.get(shannonPath), shannonData);

        // --- Size Comparison ---
        double originalSize = selectedFile.length();
        double huffmanSize = huffmanData.length;
        double shannonSize = shannonData.length;
        double huffmanRatio = (1 - (huffmanSize / originalSize)) * 100;
        double shannonRatio = (1 - (shannonSize / originalSize)) * 100;
        double diff = Math.abs(huffmanSize - shannonSize);

        // Store the Huffman output path for visualizer
        lastCompressedPath = huffmanPath;
        Platform.runLater(() -> openCompressedLocation.setDisable(false));

        updateProgress(progressBar, 0.9);
        updateOutput(String.format(
            "Compression completed!\n" +
            "Original size: %s\n" +
            "Huffman compressed: %s (%.2f%% smaller)\n" +
            "Shannon-Fano compressed: %s (%.2f%% smaller)\n" +
            "Size difference: %s\n" +
            "Huffman output: %s\n" +
            "Shannon-Fano output: %s",
            formatFileSize((long)originalSize),
            formatFileSize((long)huffmanSize), huffmanRatio,
            formatFileSize((long)shannonSize), shannonRatio,
            formatFileSize((long)diff),
            huffmanPath,
            shannonPath
        ));
        updateProgress(progressBar, 1.0);

        // Update the bar charts with compression results
        Platform.runLater(() -> {
            updateCompressionChart((long)originalSize, huffmanData.length, shannonData.length);
            updateTimeChart(huffmanTimeMs, shannonTimeMs);
            updateZoomedChart(huffmanData.length, shannonData.length);
        });
    }

    private void compressImageFile() throws IOException {
        if (selectedFile == null) {
            showAlert("Error", "Please select an image file first!");
            return;
        }
        String outputPath = createOutputFileName(selectedFile, "_compressed").replaceAll("\\.[^.]+$", ".huffimg");
        ImageCompression imgComp = new ImageCompression();
        byte[] compressed = imgComp.compress(selectedFile.getAbsolutePath(), false); // false = color
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(compressed);
        }
        long originalSize = selectedFile.length();
        long compressedSize = new File(outputPath).length();
        double ratio = originalSize == 0 ? 0 : (1.0 - ((double) compressedSize / originalSize)) * 100;
        updateOutput(String.format(
            "Image compression completed!\nOriginal size: %s\nCompressed size: %s (%.2f%% smaller)\nOutput: %s",
            formatFileSize(originalSize),
            formatFileSize(compressedSize),
            ratio,
            outputPath
        ));
    }

    private void decompressImageFile(File compressedFile, File outputImageFile) throws IOException {
        byte[] compressedBytes = java.nio.file.Files.readAllBytes(compressedFile.toPath());
        java.awt.image.BufferedImage decompressedImage = com.byteshrink.compression.ImageCompression.decompress(compressedBytes);
        javax.imageio.ImageIO.write(decompressedImage, "png", outputImageFile);
        updateOutput("Decompression completed!\nOutput image: " + outputImageFile.getAbsolutePath());
    }

    private void decompressTextFile() throws IOException {
        updateProgress(decompressProgressBar, 0.2);
        updateOutput("Reading compressed file...");

        // Read the compressed file
        byte[] compressedData = Files.readAllBytes(selectedCompressedFile.toPath());
        updateProgress(decompressProgressBar, 0.4);

        // Decompress the data
        updateOutput("Decompressing data...");
        String decompressedContent = HuffmanCompression.decompress(compressedData);
        updateProgress(decompressProgressBar, 0.6);

        // Write the decompressed content to a file
        String outputPath = createOutputFileName(selectedCompressedFile, "_decompressed");
        Files.writeString(Paths.get(outputPath), decompressedContent, StandardCharsets.UTF_8);
        updateProgress(decompressProgressBar, 0.8);

        // Store the output path and enable the button
        lastDecompressedPath = outputPath;
        Platform.runLater(() -> openDecompressedLocation.setDisable(false));

        // Calculate sizes
        double compressedSize = selectedCompressedFile.length();
        double decompressedSize = Files.size(Paths.get(outputPath));

        updateOutput(String.format(
            "Decompression completed successfully!\n" +
            "Compressed size: %s\n" +
            "Decompressed size: %s\n" +
            "Output saved to: %s",
            formatFileSize((long)compressedSize),
            formatFileSize((long)decompressedSize),
            outputPath
        ));
    }

    private String createOutputFileName(File file, String suffix) {
        String originalName = file.getName();
        String extension = originalName.substring(originalName.lastIndexOf('.'));
        String baseName = originalName.substring(0, originalName.lastIndexOf('.'));
        return file.getParent() + File.separator + baseName + suffix + extension;
    }

    private void updateProgress(ProgressBar bar, double progress) {
        Platform.runLater(() -> bar.setProgress(progress));
    }

    private void updateOutput(String message) {
        Platform.runLater(() -> outputLog.setText(message));
    }

    @FXML
    private void handleClear() {
        selectedFile = null;
        fileNameLabel.setText("No file selected");
        fileDetails.clear();
        outputLog.clear();
        progressBar.setProgress(0);
        lastCompressedPath = null;
        openCompressedLocation.setDisable(true);
    }

    @FXML
    private void handleDecompressClear() {
        selectedCompressedFile = null;
        compressedFileLabel.setText("No file selected");
        decompressFileDetails.clear();
        outputLog.clear();
        decompressProgressBar.setProgress(0);
        lastDecompressedPath = null;
        openDecompressedLocation.setDisable(true);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void handleOpenVisualizer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/huffman_visualizer.fxml"));
            // Pass lastCompressedPath if available
            loader.setControllerFactory(param -> {
                if (param == HuffmanVisualizerController.class && lastCompressedPath != null) {
                    return new HuffmanVisualizerController(new File(lastCompressedPath));
                }
                try {
                    return param.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Huffman Tree Visualizer");
            stage.setScene(new Scene(root, 1000, 800));
            stage.setMinWidth(900);
            stage.setMinHeight(700);
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Could not open Huffman Visualizer: " + e.getMessage());
        }
    }

    private void openCompressionView(String type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/compression.fxml"));
            Parent compressionRoot = loader.load();
            MainController controller = loader.getController();
            controller.setSelectedCompressionType(type);
            Scene scene = (getStartedButton != null ? getStartedButton.getScene() : textTabButton.getScene());
            scene.setRoot(compressionRoot);
        } catch (Exception e) {
            showAlert("Error", "Could not load compression view: " + e.getMessage());
        }
    }

    public void setSelectedCompressionType(String type) {
        this.selectedCompressionType = type;
    }

    private void showNotImplemented(String msg) {
        showAlert("Not Implemented", msg);
    }

    private void goToHomeScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent homeRoot = loader.load();
            Scene scene = backButton.getScene();
            scene.setRoot(homeRoot);
        } catch (Exception e) {
            showAlert("Error", "Could not load home screen: " + e.getMessage());
        }
    }

    private void handleTextCompressionMainScreen() {
        String input = textInputArea.getText();
        if (input == null || input.isEmpty()) {
            showAlert("Error", "Please enter some text to compress.");
            return;
        }
        try {
            HuffmanCompression huffman = new HuffmanCompression();
            byte[] compressed = huffman.compress(input);
            int originalSize = input.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            int compressedSize = compressed.length;
            double ratio = originalSize == 0 ? 0 : (1.0 - ((double) compressedSize / originalSize)) * 100;
            if (originalSizeLabel != null) originalSizeLabel.setText(formatFileSize(originalSize));
            if (compressedSizeLabel != null) compressedSizeLabel.setText(formatFileSize(compressedSize));
            if (compressionRatioLabel != null) compressionRatioLabel.setText(String.format("%.0f%%", ratio));
        } catch (Exception ex) {
            showAlert("Error", "Compression failed: " + ex.getMessage());
        }
    }

    @FXML
    private void handleTextImageClick() {
        openCompressionView("Text Compression");
    }

    @FXML
    private void handleImageImageClick() {
        openCompressionView("Image Compression");
    }

    private void updateZoomedChart(long huffman, long shannon) {
        if (zoomedBarChart == null || zoomedYAxis == null) return;
        zoomedBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        double huffmanKB = huffman / 1024.0;
        double shannonKB = shannon / 1024.0;
        series.getData().add(new XYChart.Data<>("Huffman", huffmanKB));
        series.getData().add(new XYChart.Data<>("Shannon-Fano", shannonKB));
        zoomedBarChart.getData().add(series);
        // Set bar colors
        zoomedBarChart.applyCss();
        for (XYChart.Data<String, Number> data : series.getData()) {
            String color = data.getXValue().equals("Huffman") ? "orange" : "red";
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-bar-fill: " + color + ";");
            }
        }
        // Set axis bounds for zoom
        double min = Math.min(huffmanKB, shannonKB);
        double max = Math.max(huffmanKB, shannonKB);
        zoomedYAxis.setLowerBound(Math.floor(min * 100) / 100 - 1); // a bit below min
        zoomedYAxis.setUpperBound(Math.ceil(max * 100) / 100 + 1); // a bit above max
        zoomedYAxis.setTickUnit(1);
        zoomedYAxis.setLabel("Size (KB)");
        // Update size difference label
        if (sizeDiffLabel != null) {
            double diff = Math.abs(huffmanKB - shannonKB);
            String winner = huffmanKB < shannonKB ? "Huffman" : "Shannon-Fano";
            String arrow = huffmanKB < shannonKB ? "🟧 ⇨ 🔴" : "🔴 ⇨ 🟧";
            sizeDiffLabel.setText(String.format("%s Difference: %.2f KB", arrow, diff));
        }
    }

    private void updateCompressionChart(long original, long huffman, long shannon) {
        if (compressionBarChart == null) return;
        compressionBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Original", original));
        series.getData().add(new XYChart.Data<>("Huffman", huffman));
        series.getData().add(new XYChart.Data<>("Shannon-Fano", shannon));
        compressionBarChart.getData().add(series);
        // Update axis label for robustness
        if (compressionBarChart.getYAxis() instanceof NumberAxis axis) {
            axis.setLabel("Size (bytes)");
        }
    }

    private void updateTimeChart(long huffmanMs, long shannonMs) {
        if (timeBarChart == null) return;
        timeBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("shannon-fano", huffmanMs));
        series.getData().add(new XYChart.Data<>("huffman ", shannonMs));
        timeBarChart.getData().add(series);
        if (timeBarChart.getYAxis() instanceof NumberAxis axis) {
            axis.setLabel("Time (ms)");
        }
    }
}