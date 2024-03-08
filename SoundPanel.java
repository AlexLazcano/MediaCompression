
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.*;
import java.io.FileWriter;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.LinkedHashMap;

class HuffmanNode {
	int item;
	int c;
	HuffmanNode left;
	HuffmanNode right;
}

// For comparing the nodes
class ImplementComparator implements Comparator<HuffmanNode> {
	public int compare(HuffmanNode x, HuffmanNode y) {
		return x.item - y.item;
	}
}

public class SoundPanel extends JPanel {
	static LinkedHashMap<Integer, String> huffmanCodes = new LinkedHashMap<Integer, String>();
	static LinkedHashMap<Integer, Integer> huffmanCodesBinary = new LinkedHashMap<Integer, Integer>();
	private static final Color green = new Color(0, 255, 0);
	private static final Color red = new Color(255, 0, 0);
	private static final int height = 500;
	private static final int width = 700;
	private static final int hCenter = height / 2 - 1;
	private static final int wCenter = width / 2 - 1;
	private static byte[] audioBytes;
	private static int numberOfChannels;
	private static int bitsPerSample;
	private static int numberOfFrames;
	public static double CompressionRatio;
	public static int BeforeCompression;
	public static int AfterCompression;

	public static void printCode(HuffmanNode root, String s) {
		if (root.left == null && root.right == null && Integer.valueOf(root.c) != null) {
			huffmanCodes.put(Integer.valueOf(root.c), s);
			huffmanCodesBinary.put(Integer.valueOf(root.c), Integer.valueOf(s, 2));

			return;
		}
		printCode(root.left, s + "0");
		printCode(root.right, s + "1");
	}

	public SoundPanel() {
		setBorder(BorderFactory.createLineBorder(Color.black));
		setPreferredSize(new Dimension(width, height));

	}

	public void repaint(byte[] audio, int numFrames, int bitsPSample, int numChannels) {
		audioBytes = audio;
		numberOfChannels = numChannels;
		bitsPerSample = bitsPSample;
		numberOfFrames = numFrames;

		repaint();
		compressBytes();
	}

	public void paintComponent(Graphics g) {

		Point one = new Point(0, 0);
		Point two = new Point(width, 0);
		one = translatePointToScreen(one);
		two = translatePointToScreen(two);

		g.drawLine((int) one.getX(), (int) one.getY(), (int) two.getX(), (int) two.getY());

		if (audioBytes != null) {
			int bytesPerSample = bitsPerSample / 8;
			int bytesPerFrame = bytesPerSample * numberOfChannels;
			int totalFrames = audioBytes.length / bytesPerFrame;

			double xFactor = (double) width / (double) totalFrames;
			double yFactor = (double) (hCenter / numberOfChannels) / Math.pow(2, bytesPerSample * 8 - 1);

			Point[] prev = new Point[numberOfChannels];
			for (int i = 0; i < numberOfChannels; i++) {
				prev[i] = new Point(0, 0);
			}
			for (int i = 0; i < totalFrames; i++) {
				int[] value = { 0, 0 };
				for (int c = 0; c < numberOfChannels; c++) {

					for (int b = 0; b < bytesPerSample; b++) {
						int byteI = i * bytesPerFrame;
						int byteC = c * bytesPerSample;
						int index = byteI + byteC + b;

						value[c] += audioBytes[index] << (b * 8);

					}

				}

				Point curr[] = new Point[numberOfChannels];

				curr[0] = new Point((int) (i * xFactor), (int) (value[0] * yFactor));

				if (numberOfChannels > 1) {
					curr[0] = translatePointToScreen(curr[0], -1);
					curr[1] = new Point((int) (i * xFactor), (int) (value[1] * yFactor));
					curr[1] = translatePointToScreen(curr[1], 1);
					g.setColor(green);
					paintLine(g, prev[0], curr[0]);
					g.setColor(red);
					paintLine(g, prev[1], curr[1]);
					prev = curr.clone();

				} else {
					curr[0] = translatePointToScreen(curr[0], 0);
					paintLine(g, prev[0], curr[0]);
					prev = curr;
				}

			}

		}

	}

	public void paintLine(Graphics g, Point p1, Point p2) {
		g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
	}

	public Point translatePointToScreen(Point point) {
		return new Point(point.x, -point.y + hCenter);
	}

	public Point translatePointToScreen(Point point, int sign) {

		return new Point(point.x, -point.y + hCenter + (hCenter / 2 * sign));
	}

	public static void compressBytes() {

		int[] intArray = new int[audioBytes.length];

		int[] freq = new int[256];

		for (int i = 0; i < audioBytes.length; i++) {
			int num = byteToUnsignedInt(audioBytes[i]);
			intArray[i] = num;
			freq[num]++;
		}

		int[] intArrayNoDups = new int[256];
		for (int i = 0; i < intArrayNoDups.length; i++) {
			intArrayNoDups[i] = i;
		}

		int numberOfBits = audioBytes.length * 8;
		int numberOfBitsEncoded = encodeHuffman(intArrayNoDups, freq, intArray);

		System.out.println("Number of bits before encoded: " + numberOfBits);
		System.out.println("Number of bits encoded: " + numberOfBitsEncoded);
		BeforeCompression = numberOfBits;
		AfterCompression = numberOfBitsEncoded;
		double compressionRatio = (double) numberOfBits / (double) numberOfBitsEncoded;
		CompressionRatio = compressionRatio;
		System.out.println("Compression ratio: " + compressionRatio);
	}

	public static void printArray(int[] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i] + " ");
		}
		System.out.println();
		System.out.println();
	}

	// signed to unsigned
	public static int byteToUnsignedInt(Byte b) {
		return b & 0xFF;
	}

	public static int encodeHuffman(int[] intArray, int[] freq, int[] stream) {
		int n = intArray.length;

		PriorityQueue<HuffmanNode> q = new PriorityQueue<HuffmanNode>(n, new ImplementComparator());

		for (int i = 0; i < n; i++) {
			HuffmanNode node = new HuffmanNode();

			node.c = intArray[i];
			node.item = freq[i];

			node.left = null;
			node.right = null;

			q.add(node);
		}

		System.out.println("Finished queue");
		HuffmanNode root = null;

		while (q.size() > 1) {

			HuffmanNode x = q.peek();
			q.poll();

			HuffmanNode y = q.peek();
			q.poll();

			HuffmanNode f = new HuffmanNode();

			f.item = x.item + y.item;
			f.c = '-';
			f.left = x;
			f.right = y;
			root = f;

			q.add(f);
		}

		printCode(root, "");

		// String output = "";
		System.out.println("Encoding using huffman.... ");
		int length = 0;
		int info[] = new int[stream.length];
		for (int i = 0; i < stream.length; i++) {
			info[i] = huffmanCodesBinary.get(stream[i]);
		}
		try {
			FileWriter w = new FileWriter("compressedSoundFile.txt");
			for (int i = 0; i < info.length; i++) {
				w.write(info[i] + " ");
			}
			w.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < stream.length; i++) {
			length += huffmanCodes.get(stream[i]).length();

		}

		return length;
	}
}
