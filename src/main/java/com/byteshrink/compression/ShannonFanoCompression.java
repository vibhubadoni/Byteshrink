package com.byteshrink.compression;

import java.io.*;
import java.util.*;

public class ShannonFanoCompression {
    public static class ShannonFanoNode {
        public char character;
        public int frequency;
        public ShannonFanoNode left;
        public ShannonFanoNode right;
        public String code = "";
        public ShannonFanoNode(char character, int frequency) {
            this.character = character;
            this.frequency = frequency;
        }
        public ShannonFanoNode(int frequency, ShannonFanoNode left, ShannonFanoNode right) {
            this.character = '\0';
            this.frequency = frequency;
            this.left = left;
            this.right = right;
        }
    }

    private ShannonFanoNode root;
    private Map<Character, String> codes;
    private Map<Character, Integer> frequencyMap;

    public ShannonFanoCompression() {
        codes = new HashMap<>();
        frequencyMap = new HashMap<>();
    }

    private void buildFrequencyMap(String text) {
        for (char c : text.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }
    }

    private List<ShannonFanoNode> buildSortedNodeList() {
        List<ShannonFanoNode> nodes = new ArrayList<>();
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            nodes.add(new ShannonFanoNode(entry.getKey(), entry.getValue()));
        }
        nodes.sort((a, b) -> b.frequency - a.frequency);
        return nodes;
    }

    private void buildShannonFanoTree(List<ShannonFanoNode> nodes) {
        root = buildTreeRecursive(nodes);
    }

    private ShannonFanoNode buildTreeRecursive(List<ShannonFanoNode> nodes) {
        if (nodes.size() == 1) {
            return nodes.get(0);
        }
        int total = nodes.stream().mapToInt(n -> n.frequency).sum();
        int splitIndex = findSplitIndex(nodes, total);
        List<ShannonFanoNode> leftList = nodes.subList(0, splitIndex);
        List<ShannonFanoNode> rightList = nodes.subList(splitIndex, nodes.size());
        ShannonFanoNode left = buildTreeRecursive(leftList);
        ShannonFanoNode right = buildTreeRecursive(rightList);
        return new ShannonFanoNode(left.frequency + right.frequency, left, right);
    }

    private int findSplitIndex(List<ShannonFanoNode> nodes, int total) {
        int sum = 0;
        int minDiff = Integer.MAX_VALUE;
        int splitIndex = 1;
        for (int i = 1; i < nodes.size(); i++) {
            sum += nodes.get(i - 1).frequency;
            int diff = Math.abs((total - sum) - sum);
            if (diff < minDiff) {
                minDiff = diff;
                splitIndex = i;
            }
        }
        return splitIndex;
    }

    private void generateCodes(ShannonFanoNode node, String code) {
        if (node == null) return;
        if (node.left == null && node.right == null) {
            codes.put(node.character, code);
            node.code = code;
            return;
        }
        generateCodes(node.left, code + "0");
        generateCodes(node.right, code + "1");
    }

    public byte[] compress(String text) throws IOException {
        buildFrequencyMap(text);
        List<ShannonFanoNode> nodes = buildSortedNodeList();
        buildShannonFanoTree(nodes);
        generateCodes(root, "");
        StringBuilder encodedText = new StringBuilder();
        for (char c : text.toCharArray()) {
            encodedText.append(codes.get(c));
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
        List<ShannonFanoNode> nodes = new ArrayList<>();
        for (Map.Entry<Character, Integer> entry : freqMap.entrySet()) {
            nodes.add(new ShannonFanoNode(entry.getKey(), entry.getValue()));
        }
        nodes.sort((a, b) -> b.frequency - a.frequency);
        ShannonFanoCompression sf = new ShannonFanoCompression();
        sf.frequencyMap = freqMap;
        sf.buildShannonFanoTree(nodes);
        sf.generateCodes(sf.root, "");
        StringBuilder bits = new StringBuilder();
        for (int i = index; i < compressedData.length; i++) {
            String byteStr = String.format("%8s", Integer.toBinaryString(compressedData[i] & 0xFF)).replace(' ', '0');
            bits.append(byteStr);
        }
        if (padding > 0) {
            bits.setLength(bits.length() - padding);
        }
        StringBuilder result = new StringBuilder();
        ShannonFanoNode node = sf.root;
        for (int i = 0; i < bits.length(); i++) {
            node = bits.charAt(i) == '0' ? node.left : node.right;
            if (node.left == null && node.right == null) {
                result.append(node.character);
                node = sf.root;
            }
        }
        return result.toString();
    }

    public ShannonFanoNode getRoot() {
        return root;
    }
} 