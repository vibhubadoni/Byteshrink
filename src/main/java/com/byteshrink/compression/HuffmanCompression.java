package com.byteshrink.compression;

import java.io.*;
import java.util.*;

public class HuffmanCompression {
    public static class HuffmanNode implements Comparable<HuffmanNode> {
        public int frequency;
        public char character;
        public HuffmanNode left;
        public HuffmanNode right;

        public HuffmanNode(char character, int frequency) {
            this.character = character;
            this.frequency = frequency;
            left = right = null;
        }

        public HuffmanNode(int frequency, HuffmanNode left, HuffmanNode right) {
            this.frequency = frequency;
            this.character = '\0';
            this.left = left;
            this.right = right;
        }

        @Override
        public int compareTo(HuffmanNode node) {
            return this.frequency - node.frequency;
        }
    }

    private HuffmanNode root;
    private Map<Character, String> huffmanCodes;
    private Map<Character, Integer> frequencyMap;

    public HuffmanCompression() {
        huffmanCodes = new HashMap<>();
        frequencyMap = new HashMap<>();
    }

    private void buildFrequencyMap(String text) {
        for (char c : text.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }
    }

    private void buildHuffmanTree() {
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            pq.offer(new HuffmanNode(entry.getKey(), entry.getValue()));
        }
        while (pq.size() > 1) {
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();
            HuffmanNode parent = new HuffmanNode(
                    left.frequency + right.frequency,
                    left,
                    right
            );
            pq.offer(parent);
        }
        root = pq.poll();
    }

    private void generateHuffmanCodes(HuffmanNode node, String code) {
        if (node == null) return;
        if (node.left == null && node.right == null) {
            huffmanCodes.put(node.character, code);
            return;
        }
        generateHuffmanCodes(node.left, code + "0");
        generateHuffmanCodes(node.right, code + "1");
    }

    public byte[] compress(String text) throws IOException {
        buildFrequencyMap(text);
        buildHuffmanTree();
        generateHuffmanCodes(root, "");
        StringBuilder encodedText = new StringBuilder();
        for (char c : text.toCharArray()) {
            encodedText.append(huffmanCodes.get(c));
        }
        int padding = 8 - (encodedText.length() % 8);
        if (padding == 8) padding = 0;
        for (int i = 0; i < padding; i++) {
            encodedText.append('0');
        }
        byte[] compressedData = new byte[encodedText.length() / 8 + getHeaderSize()];
        writeHeader(compressedData, padding);
        int dataIndex = getHeaderSize();
        for (int i = 0; i < encodedText.length(); i += 8) {
            String byteStr = encodedText.substring(i, i + 8);
            compressedData[dataIndex++] = (byte) Integer.parseInt(byteStr, 2);
        }
        return compressedData;
    }

    private int getHeaderSize() {
        return 5 + (frequencyMap.size() * 5);
    }

    private void writeHeader(byte[] data, int padding) {
        int index = 0;
        data[index++] = (byte) padding;
        int mapSize = frequencyMap.size();
        data[index++] = (byte) (mapSize >> 24);
        data[index++] = (byte) (mapSize >> 16);
        data[index++] = (byte) (mapSize >> 8);
        data[index++] = (byte) mapSize;
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            data[index++] = (byte) entry.getKey().charValue();
            int freq = entry.getValue();
            data[index++] = (byte) (freq >> 24);
            data[index++] = (byte) (freq >> 16);
            data[index++] = (byte) (freq >> 8);
            data[index++] = (byte) freq;
        }
    }

    public static String decompress(byte[] compressedData) throws IOException {
        int index = 0;
        int padding = compressedData[index++] & 0xFF;
        int mapSize = 0;
        for (int i = 0; i < 4; i++) {
            mapSize = (mapSize << 8) | (compressedData[index++] & 0xFF);
        }
        Map<Character, Integer> freqMap = new HashMap<>();
        for (int i = 0; i < mapSize; i++) {
            char c = (char) (compressedData[index++] & 0xFF);
            int freq = 0;
            for (int j = 0; j < 4; j++) {
                freq = (freq << 8) | (compressedData[index++] & 0xFF);
            }
            freqMap.put(c, freq);
        }
        HuffmanCompression huffman = new HuffmanCompression();
        huffman.frequencyMap = freqMap;
        huffman.buildHuffmanTree();
        StringBuilder result = new StringBuilder();
        HuffmanNode current = huffman.root;
        for (int i = index; i < compressedData.length; i++) {
            String bits = String.format("%8s",
                            Integer.toBinaryString(compressedData[i] & 0xFF))
                    .replace(' ', '0');
            if (i == compressedData.length - 1 && padding > 0) {
                bits = bits.substring(0, 8 - padding);
            }
            for (char bit : bits.toCharArray()) {
                if (bit == '0') {
                    current = current.left;
                } else {
                    current = current.right;
                }
                if (current.left == null && current.right == null) {
                    result.append(current.character);
                    current = huffman.root;
                }
            }
        }
        return result.toString();
    }

    public HuffmanNode getRoot() {
        return root;
    }
}
