// // Huffman Coding in Java

// import java.util.PriorityQueue;
// import java.util.Comparator;
// import java.util.HashSet;
// import java.util.LinkedHashMap;

// class HuffmanNode {
//     int item;
//     int c;
//     HuffmanNode left;
//     HuffmanNode right;
// }

// // For comparing the nodes
// class ImplementComparator implements Comparator<HuffmanNode> {
//     public int compare(HuffmanNode x, HuffmanNode y) {
//         return x.item - y.item;
//     }
// }

// // IMplementing the huffman algorithm
// public class huffmanTest {
//     static LinkedHashMap<Integer, String> huffmanCodes = new LinkedHashMap<Integer, String>();

//     public static void printCode(HuffmanNode root, String s) {
//         if (root.left == null && root.right == null && Integer.valueOf(root.c) != null) {
//             huffmanCodes.put(Integer.valueOf(root.c), s);

//             String formatted = String.format("%5s | %s", root.c, s);
//             System.out.println(formatted);

//             return;
//         }
//         printCode(root.left, s + "0");
//         printCode(root.right, s + "1");
//     }

//     // public static void main(String[] args) {

//     //     Byte[] bytes = {
//     //             0, 1, 127
//     //     };

//     //     int[] intArray = new int[bytes.length];

//     //     int[] freq = new int[256];

//     //     for (int i = 0; i < bytes.length; i++) {
//     //         int num = byteToUnsignedInt(bytes[i]);
//     //         intArray[i] = num;
//     //         freq[num]++;
//     //     }
//     //     System.out.println("Input: ");
//     //     printArray(intArray);
//     //     // System.out.println("Frequency: ");
//     //     // printArray(freq);

//     //     String s = encodeHuffman(removeDuplicates(intArray), freq, intArray);
//     //     System.out.println("Output encoding: " + s);

//     //     int numberOfBits = bytes.length * 8;
//     //     System.out.println("Number of bits: " + numberOfBits);
//     //     int numberOfBitsEncoded = s.length();
//     //     System.out.println("Number of bits encoded: " + numberOfBitsEncoded);
//     //     double compressionRatio = (double) numberOfBitsEncoded / (double) numberOfBits;
//     //     System.out.println("Compression ratio: " + compressionRatio);

//     // }

//     public static void printArray(int[] array) {
//         for (int i = 0; i < array.length; i++) {
//             System.out.print(array[i] + " ");
//         }
//         System.out.println();
//         System.out.println();
//     }

//     // remove duplicates from integer array
//     public static int[] removeDuplicates(int[] intArray) {
//         HashSet<Integer> set = new HashSet<Integer>();
//         for (int i = 0; i < intArray.length; i++) {
//             set.add(intArray[i]);
//         }
//         int[] newArray = new int[set.size()];
//         int i = 0;
//         for (int num : set) {
//             newArray[i] = num;
//             i++;
//         }
//         return newArray;
//     }

//     // signed to unsigned
//     public static int byteToUnsignedInt(Byte b) {
//         return b & 0xFF;
//     }

//     public static String encodeHuffman(int[] intArray, int[] freq, int[] stream) {
//         int n = intArray.length;

//         PriorityQueue<HuffmanNode> q = new PriorityQueue<HuffmanNode>(n, new ImplementComparator());

//         for (int i = 0; i < n; i++) {
//             HuffmanNode hn = new HuffmanNode();

//             hn.c = intArray[i];
//             hn.item = freq[i];

//             hn.left = null;
//             hn.right = null;

//             q.add(hn);
//         }

//         HuffmanNode root = null;

//         while (q.size() > 1) {

//             HuffmanNode x = q.peek();
//             q.poll();

//             HuffmanNode y = q.peek();
//             q.poll();

//             HuffmanNode f = new HuffmanNode();

//             f.item = x.item + y.item;
//             f.c = '-';
//             f.left = x;
//             f.right = y;
//             root = f;

//             q.add(f);
//         }
//         System.out.println("Int | Huffman code ");
//         System.out.println("--------------------");

//         printCode(root, "");
//         // System.out.println(huffmanCodes);

//         String output = "";
//         for (int i = 0; i < stream.length; i++) {
//             output = output + huffmanCodes.get(stream[i]) + " ";
//         }

//         return output;

//     }
// }