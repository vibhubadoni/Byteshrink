package com.byteshrink.compression;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.*;
import java.util.*;

public class ImageCompression {
    private static class HuffmanNode implements Comparable<HuffmanNode> {
        int frequency;
        int pixelValue;
        HuffmanNode left;
        HuffmanNode right;

        public HuffmanNode(int pixelValue, int frequency) {
            this.pixelValue = pixelValue;
            this.frequency = frequency;
            left = right = null;
        }

        public HuffmanNode(int frequency, HuffmanNode left, HuffmanNode right) {
            this.frequency = frequency;
            this.pixelValue = -1;
            this.left = left;
            this.right = right;
        }

        @Override
        public int compareTo(HuffmanNode node) {
            return this.frequency - node.frequency;
        }
    }

    private HuffmanNode root;
    private Map<Integer, String> huffmanCodes;
    private Map<Integer, Integer> frequencyMap;
    private int width;
    private int height;
    private boolean isGrayscale;

    public ImageCompression() {
        huffmanCodes = new HashMap<>();
        frequencyMap = new HashMap<>();
    }

    public byte[] compress(String imagePath, boolean convertToGrayscale) throws IOException {
        // Validate input
        if (imagePath == null || imagePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Image path cannot be null or empty");
        }
        
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            throw new IllegalArgumentException("Image file does not exist: " + imagePath);
        }
        
        if (!imageFile.canRead()) {
            throw new IllegalArgumentException("Cannot read image file: " + imagePath);
        }
        
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IllegalArgumentException("Failed to read image file: " + imagePath);
        }
        
        this.width = image.getWidth();
        this.height = image.getHeight();
        this.isGrayscale = convertToGrayscale;
        
        // Validate image dimensions
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid image dimensions: " + width + "x" + height);
        }
        
        // Check for extremely large images that might cause memory issues
        if (width > 10000 || height > 10000) {
            throw new IllegalArgumentException("Image too large: " + width + "x" + height + ". Maximum supported size is 10000x10000");
        }
        
        int[] pixels = getPixelData(image, convertToGrayscale);
        buildFrequencyMap(pixels);
        
        // Check if we have enough pixel variety for compression
        if (frequencyMap.size() < 2) {
            throw new IllegalArgumentException("Image has insufficient pixel variety for Huffman compression");
        }
        
        buildHuffmanTree();
        generateHuffmanCodes(root, "");
        
        StringBuilder encodedData = new StringBuilder();
        for (int pixel : pixels) {
            String code = huffmanCodes.get(pixel);
            if (code == null) {
                throw new IllegalStateException("No Huffman code found for pixel value: " + pixel);
            }
            encodedData.append(code);
        }
        
        int padding = 8 - (encodedData.length() % 8);
        if (padding == 8) padding = 0;
        for (int i = 0; i < padding; i++) {
            encodedData.append('0');
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(outputStream);
        writeHeader(dataOut, padding);
        for (int i = 0; i < encodedData.length(); i += 8) {
            String byteStr = encodedData.substring(i, i + 8);
            dataOut.write((byte) Integer.parseInt(byteStr, 2));
        }
        return outputStream.toByteArray();
    }

    private int[] getPixelData(BufferedImage image, boolean convertToGrayscale) {
        int[] pixels = new int[width * height];
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                if (convertToGrayscale) {
                    int gray = (int) (0.299 * color.getRed() + 
                                      0.587 * color.getGreen() + 
                                      0.114 * color.getBlue());
                    pixels[index++] = gray;
                } else {
                    pixels[index++] = color.getRed();
                }
            }
        }
        return pixels;
    }

    private void buildFrequencyMap(int[] pixels) {
        for (int pixel : pixels) {
            frequencyMap.put(pixel, frequencyMap.getOrDefault(pixel, 0) + 1);
        }
    }

    private void buildHuffmanTree() {
        if (frequencyMap.isEmpty()) {
            throw new IllegalStateException("Frequency map is empty");
        }
        
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            pq.offer(new HuffmanNode(entry.getKey(), entry.getValue()));
        }
        
        // Handle case with only one pixel value
        if (pq.size() == 1) {
            HuffmanNode singleNode = pq.poll();
            root = new HuffmanNode(singleNode.frequency, singleNode, null);
            return;
        }
        
        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();
            pq.offer(new HuffmanNode(left.frequency + right.frequency, left, right));
        }
        root = pq.poll();
        
        if (root == null) {
            throw new IllegalStateException("Failed to build Huffman tree");
        }
    }

    private void generateHuffmanCodes(HuffmanNode node, String code) {
        if (node == null) return;
        if (node.left == null && node.right == null) {
            huffmanCodes.put(node.pixelValue, code);
            return;
        }
        generateHuffmanCodes(node.left, code + "0");
        generateHuffmanCodes(node.right, code + "1");
    }

    private void writeHeader(DataOutputStream out, int padding) throws IOException {
        out.writeInt(width);
        out.writeInt(height);
        out.writeBoolean(isGrayscale);
        out.writeByte(padding);
        out.writeInt(frequencyMap.size());
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            out.writeInt(entry.getKey());
            out.writeInt(entry.getValue());
        }
    }

    public static BufferedImage decompress(byte[] compressedData) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(compressedData));
        int width = in.readInt();
        int height = in.readInt();
        boolean isGrayscale = in.readBoolean();
        int padding = in.readByte() & 0xFF;
        int mapSize = in.readInt();
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            int pixelValue = in.readInt();
            int frequency = in.readInt();
            freqMap.put(pixelValue, frequency);
        }
        ImageCompression imgComp = new ImageCompression();
        imgComp.frequencyMap = freqMap;
        imgComp.buildHuffmanTree();
        List<Byte> compressedBytes = new ArrayList<>();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            for (int i = 0; i < bytesRead; i++) {
                compressedBytes.add(buffer[i]);
            }
        }
        List<Integer> decodedPixels = new ArrayList<>();
        HuffmanNode current = imgComp.root;
        for (int i = 0; i < compressedBytes.size(); i++) {
            String bits = String.format("%8s", 
                Integer.toBinaryString(compressedBytes.get(i) & 0xFF))
                .replace(' ', '0');
            if (i == compressedBytes.size() - 1 && padding > 0) {
                bits = bits.substring(0, 8 - padding);
            }
            for (char bit : bits.toCharArray()) {
                if (bit == '0') {
                    current = current.left;
                } else {
                    current = current.right;
                }
                if (current.left == null && current.right == null) {
                    decodedPixels.add(current.pixelValue);
                    current = imgComp.root;
                }
            }
        }
        BufferedImage outputImage = new BufferedImage(width, height, 
            isGrayscale ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_INT_RGB);
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = decodedPixels.get(index++);
                if (isGrayscale) {
                    Color gray = new Color(pixel, pixel, pixel);
                    outputImage.setRGB(x, y, gray.getRGB());
                } else {
                    Color color = new Color(pixel, pixel, pixel);
                    outputImage.setRGB(x, y, color.getRGB());
                }
            }
        }
        return outputImage;
    }
}
