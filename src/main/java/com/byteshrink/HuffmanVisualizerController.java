package com.byteshrink;

import com.byteshrink.compression.HuffmanCompression;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class HuffmanVisualizerController {
    @FXML
    private Button uploadTextButton;
    @FXML
    private Label fileNameLabel;
    @FXML
    private TreeView<String> huffmanTreeView;
    @FXML
    private Pane treePane;

    private File selectedFile;
    private static final double NODE_RADIUS = 20;
    private static final double VERTICAL_GAP = 70;
    private static final double HORIZONTAL_GAP = 30;

    public HuffmanVisualizerController() {}
    public HuffmanVisualizerController(File file) {
        this.selectedFile = file;
    }

    @FXML
    private void initialize() {
        if (selectedFile != null) {
            fileNameLabel.setText(selectedFile.getName());
            buildAndDisplayTree();
        }
    }

    @FXML
    private void handleUploadText() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Text File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.csv", "*.json", "*.xml")
        );
        Stage stage = (Stage) uploadTextButton.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            fileNameLabel.setText(selectedFile.getName());
            buildAndDisplayTree();
        }
    }

    private void buildAndDisplayTree() {
        try {
            String content = Files.readString(selectedFile.toPath(), StandardCharsets.UTF_8);
            HuffmanCompression huffman = new HuffmanCompression();
            huffman.compress(content);
            HuffmanCompression.HuffmanNode root = huffman.getRoot();
            TreeItem<String> rootItem = buildTreeItem(root);
            huffmanTreeView.setRoot(rootItem);
            drawHuffmanTree(root);
        } catch (Exception e) {
            fileNameLabel.setText("Error: " + e.getMessage());
        }
    }

    private TreeItem<String> buildTreeItem(HuffmanCompression.HuffmanNode node) {
        if (node == null) return null;
        String label = (node.character == '\0') ? "[Int] " + node.frequency : "'" + node.character + "' : " + node.frequency;
        TreeItem<String> item = new TreeItem<>(label);
        if (node.left != null) item.getChildren().add(buildTreeItem(node.left));
        if (node.right != null) item.getChildren().add(buildTreeItem(node.right));
        return item;
    }

    // --- Graphical Drawing ---
    private void drawHuffmanTree(HuffmanCompression.HuffmanNode root) {
        treePane.getChildren().clear();
        if (root == null) return;
        int treeWidth = getTreeWidth(root);
        double startX = treePane.getWidth() / 2;
        double startY = NODE_RADIUS + 10;
        drawNode(root, startX, startY, treePane.getWidth() / 2, 0);
    }

    private void drawNode(HuffmanCompression.HuffmanNode node, double x, double y, double xOffset, int depth) {
        if (node == null) return;
        // Draw left child
        if (node.left != null) {
            double childX = x - xOffset / 2;
            double childY = y + VERTICAL_GAP;
            Line line = new Line(x, y, childX, childY);
            treePane.getChildren().add(line);
            drawNode(node.left, childX, childY, xOffset / 2, depth + 1);
        }
        // Draw right child
        if (node.right != null) {
            double childX = x + xOffset / 2;
            double childY = y + VERTICAL_GAP;
            Line line = new Line(x, y, childX, childY);
            treePane.getChildren().add(line);
            drawNode(node.right, childX, childY, xOffset / 2, depth + 1);
        }
        // Draw node (circle)
        Circle circle = new Circle(x, y, NODE_RADIUS);
        circle.setFill(Color.LIGHTBLUE);
        circle.setStroke(Color.DARKBLUE);
        treePane.getChildren().add(circle);
        // Draw label
        String label = (node.character == '\0') ? String.valueOf(node.frequency) : "'" + node.character + "'\n" + node.frequency;
        Text text = new Text(x - NODE_RADIUS / 2, y + 5, label);
        treePane.getChildren().add(text);
    }

    private int getTreeWidth(HuffmanCompression.HuffmanNode node) {
        if (node == null) return 0;
        if (node.left == null && node.right == null) return 1;
        return getTreeWidth(node.left) + getTreeWidth(node.right);
    }
} 