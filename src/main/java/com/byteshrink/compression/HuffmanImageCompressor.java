package com.byteshrink.compression;

import java.io.*;
import java.util.*;

class HuffmanNode implements Serializable {
    int data;
    byte b;
    HuffmanNode left, right;
}

public class HuffmanImageCompressor {
    private static Map<Byte, String> huffmanCodes;
    private static PriorityQueue<HuffmanNode> queue;

    public static void compress(File input, File output) throws IOException {
        byte[] imageBytes = java.nio.file.Files.readAllBytes(input.toPath());
        Map<Byte, Integer> freqMap = buildFrequencyMap(imageBytes);
        HuffmanNode root = buildTree(freqMap);
        huffmanCodes = new HashMap<>();
        buildCodes(root, "");
        StringBuilder encoded = new StringBuilder();
        for (byte b : imageBytes) {
            encoded.append(huffmanCodes.get(b));
        }
        byte[] compressedData = binaryStringToByteArray(encoded.toString());
        try (FileOutputStream fos = new FileOutputStream(output)) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(freqMap);
            oos.write(compressedData);
        }
    }

    private static Map<Byte, Integer> buildFrequencyMap(byte[] data) {
        Map<Byte, Integer> freqMap = new HashMap<>();
        for (byte b : data) {
            freqMap.put(b, freqMap.getOrDefault(b, 0) + 1);
        }
        return freqMap;
    }

    private static HuffmanNode buildTree(Map<Byte, Integer> freqMap) {
        queue = new PriorityQueue<>(Comparator.comparingInt(n -> n.data));
        for (Map.Entry<Byte, Integer> entry : freqMap.entrySet()) {
            HuffmanNode node = new HuffmanNode();
            node.b = entry.getKey();
            node.data = entry.getValue();
            node.left = node.right = null;
            queue.add(node);
        }
        while (queue.size() > 1) {
            HuffmanNode x = queue.poll();
            HuffmanNode y = queue.poll();
            HuffmanNode f = new HuffmanNode();
            f.data = x.data + y.data;
            f.left = x;
            f.right = y;
            queue.add(f);
        }
        return queue.poll();
    }

    private static void buildCodes(HuffmanNode root, String code) {
        if (root.left == null && root.right == null) {
            huffmanCodes.put(root.b, code);
            return;
        }
        buildCodes(root.left, code + "0");
        buildCodes(root.right, code + "1");
    }

    private static byte[] binaryStringToByteArray(String binaryString) {
        int len = binaryString.length();
        int byteLength = (len + 7) / 8;
        byte[] byteArray = new byte[byteLength];
        for (int i = 0; i < len; i += 8) {
            int end = Math.min(len, i + 8);
            String byteStr = binaryString.substring(i, end);
            byteArray[i / 8] = (byte) Integer.parseInt(byteStr, 2);
        }
        return byteArray;
    }
} 