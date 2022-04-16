import javax.swing.JPanel;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.awt.image.*;
import java.lang.Math;

public class ImagePanel extends JPanel {
    private static int height = 600;
    private static int width = 800;
    private static BufferedImage image = null;
    private static BufferedImage dithered = null;
    private static BufferedImage compressed = null;
    private static boolean isDithered = false;

    final private static int[][] quantizationTable = {
            { 1, 1, 2, 4, 8, 16, 32, 64 },
            { 1, 1, 2, 4, 8, 16, 32, 64 },
            { 2, 2, 2, 4, 8, 16, 32, 64 },
            { 4, 4, 4, 4, 8, 16, 32, 64 },
            { 8, 8, 8, 8, 8, 16, 32, 64 },
            { 16, 16, 16, 16, 16, 16, 32, 64 },
            { 32, 32, 32, 32, 32, 32, 32, 64 },
            { 64, 64, 64, 64, 64, 64, 64, 64 }

    };

    public ImagePanel() {

        setBorder(BorderFactory.createLineBorder(Color.black));
        setPreferredSize(new Dimension(width, height));

    }

    public void repaint(File file) {
        readImageFromFile(file);
        compressImage(image);

    }

    public void resizeImage(int newWidth, int newHeight) {
        if (newHeight < height) {
            newHeight = height;
        }
        setPreferredSize(new Dimension(newWidth * 2, newHeight));

        repaint();
    }

    public void toggleDither() {
        isDithered = !isDithered;
        repaint();

    }

    public void paintComponent(Graphics g) {

        if (image != null) {
            paintImage(g, image, 0, 0);

            if (compressed != null) {
                paintImage(g, compressed, image.getWidth(), 0);
            }
        }

    }

    public static void compressImage(BufferedImage image) {

        int height = image.getHeight();
        int width = image.getWidth();
        if (height % 8 != 0) {
            height = height - (height % 8);
        }

        if (width % 8 != 0) {

            width = width - (width % 8);
        }
        int[][] redValues = new int[width][height];
        int[][] greenValues = new int[width][height];
        int[][] blueValues = new int[width][height];

        for (int i = 0; i < blueValues.length; i++) {
            for (int j = 0; j < blueValues[0].length; j++) {
                Color color = new Color(image.getRGB(j, i));
                redValues[i][j] = color.getRed();
                greenValues[i][j] = color.getGreen();
                blueValues[i][j] = color.getBlue();
            }
        }

        int[][] redDCT = getDCT(redValues);
        int[][] greenDCT = getDCT(greenValues);
        int[][] blueDCT = getDCT(blueValues);
        int totalBitsBefore = height * width * 8 * 3;
        int redBitsAfter = entropyEncoding(redDCT);
        int greenBitsAfter = entropyEncoding(greenDCT);
        int blueBitsAfter = entropyEncoding(blueDCT);

        int totalBitsAfter = redBitsAfter + greenBitsAfter + blueBitsAfter;
        double compressionRatio = (double) totalBitsBefore / totalBitsAfter;
        System.out.println("Total bits before: " + totalBitsBefore);
        System.out.println("Total bits after: " + totalBitsAfter);
        System.out.println("Compression ratio: " + compressionRatio);

        int[][] redInversedDCTValues = invertDCT(redDCT);
        int[][] greenInversedDCTValues = invertDCT(greenDCT);
        int[][] blueInversedDCTValues = invertDCT(blueDCT);

        compressed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                int red = redInversedDCTValues[i][j];
                int green = greenInversedDCTValues[i][j];
                int blue = blueInversedDCTValues[i][j];

                if (red > 255) {
                    red = 255;
                } else if (red < 0) {
                    red = 0;
                }
                if (green > 255) {
                    green = 255;
                } else if (green < 0) {
                    green = 0;
                }
                if (blue > 255) {
                    blue = 255;
                } else if (blue < 0) {
                    blue = 0;
                }

                Color color = new Color(red, green, blue);

                compressed.setRGB(j, i, color.getRGB());
            }
        }

    }

    public static int entropyEncoding(int[][] matrix) {
        String result = "";

        // int totalBitsBefore = matrix.length * matrix[0].length * 8;
        for (int i = 0; i < matrix.length / 8; i++) {
            for (int j = 0; j < matrix[0].length / 8; j++) {

                int tempArray[][] = new int[8][8];
                for (int u = 0; u < 8; u++) {
                    for (int v = 0; v < 8; v++) {
                        tempArray[u][v] = matrix[u + i * 8][v + j * 8];
                    }
                }
                // System.out.println("input:");
                // printArray(tempArray);
                int[] zigZag = zigZagMatrix(tempArray, 8, 8);
                String s = runLengthEncoding(zigZag);
                // System.out.println("output:" + s);
                result += s;

            }
        }
        int totalBitsAfter = result.getBytes().length * 8;

        return totalBitsAfter;
        // // System.out.println("Total bits before: " + totalBitsBefore);
        // System.out.println("Total bits after: " + totalBitsAfter);
        // System.out.println("Compression ratio: " + (double) totalBitsBefore /
        // (double) totalBitsAfter);
        // System.out.println("Result " + result);

    }

    public static String runLengthEncoding(int[] array) {

        String result = "";
        int count = 1;
        int i = 0;
        for (; i < array.length - 1; i++) {
            if (array[i] == array[i + 1]) {
                count++;

            } else {

                result += count + ":" + array[i] + " ";
                count = 1;

            }

        }
        result += count + "|" + array[i] + " ";

        return result;
    }

    public static int[][] invertDCT(int[][] dctMatrix) {
        int height = dctMatrix.length;
        int width = dctMatrix[0].length;
        int[][] inverseDCT = new int[height][width];
        for (int i = 0; i < height / 8; i++) {
            for (int j = 0; j < width / 8; j++) {
                int[][] temp = new int[8][8];
                for (int k = 0; k < 8; k++) {
                    for (int l = 0; l < 8; l++) {
                        temp[k][l] = dctMatrix[i * 8 + k][j * 8 + l];
                    }
                }
                int inverseTemp[][] = getDCTInverse(temp);
                for (int k = 0; k < inverseTemp.length; k++) {
                    for (int l = 0; l < inverseTemp[0].length; l++) {
                        inverseDCT[i * 8 + k][j * 8 + l] = inverseTemp[k][l];
                    }
                }

            }
        }

        return inverseDCT;
    }

    public static int[][] getDCTInverse(int[][] matrix) {

        int[][] f = new int[matrix.length][matrix[0].length];

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                f[i][j] = Math.round(getFInverse(i, j, matrix));
            }
        }
        return f;
    }

    public static int getFInverse(int i, int j, int[][] F) {

        double cIsZero = Math.sqrt(2) / 2;
        double cIsNotZero = 1;

        double sum = 0.0;
        for (int u = 0; u < F.length; u++) {
            for (int v = 0; v < F.length; v++) {

                double a = Math.cos(((2.0 * i + 1.0) * u * Math.PI) / (2.0 * F.length));
                double b = Math.cos(((2.0 * j + 1.0) * v * Math.PI) / (2.0 * F.length));

                double cu = (u == 0) ? cIsZero : cIsNotZero;
                double cv = (v == 0) ? cIsZero : cIsNotZero;

                sum += (cu * cv) / 4 * a * b * (double) F[u][v];

            }
        }
        return (int) Math.round(sum);

    }

    public static int[][] getDCT(int[][] matrix) {

        int height = matrix.length;
        int width = matrix[0].length;

        int[][] dct = new int[height][width];
        // System.out.println("input matrix: ");
        // printArray(matrix);

        for (int i = 0; i < height / 8; i++) {
            for (int j = 0; j < width / 8; j++) {
                // System.out.println("i: " + i + " j: " + j);
                int n = 8;
                int[][] G = new int[n][n];

                for (int u = 0; u < n; u++) {
                    for (int v = 0; v < n; v++) {

                        G[u][v] = getG(u, v, matrix, i, j);
                    }

                }

                for (int u = 0; u < n; u++) {
                    for (int v = 0; v < n; v++) {
                        dct[u + i * 8][v + j * 8] = (int) Math
                                .round((double) getF(u, v, G, matrix) / quantizationTable[u][v]);
                    }

                }

            }

        }

        return dct;

    }

    public static void printArray(int matrix[][]) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                String s = String.format("%4d ", matrix[i][j]);
                System.out.print(s);
            }
            System.out.println();
        }
        System.out.println();
    }

    public static int getG(int u, int j, int[][] f, int iOffset, int jOffset) {

        double sum = 0.0;
        int n = 8;
        for (int i = 0; i < n; i++) {

            double a = Math.cos(((2.0 * i + 1.0) * u * Math.PI) / (2.0 * n));

            sum += a * (double) f[i + iOffset * 8][j + jOffset * 8];

        }

        double cu = u == 0.0 ? Math.sqrt(2.0) / 2 : 1;

        return (int) Math.round(4.0 / n * cu * sum);

    }

    public static int getF(int u, int v, int[][] G, int[][] f) {
        double sum = 0.0;

        int n = 8;
        for (int j = 0; j < n; j++) {
            double g = G[u][j];
            double b = Math.cos(((2.0 * j + 1.0) * v * Math.PI) / (2.0 * n));

            sum += b * g;
        }
        double cv = v == 0.0 ? Math.sqrt(2.0) / 2 : 1;
        return (int) Math.round(4.0 / n * cv * sum);
    }

    public static void paintPixel(Graphics g, int x, int y, Color color) {
        g.setColor(color);
        g.fillRect(x, y, 1, 1);
    }

    private BufferedImage readImageFromFile(File f) {

        try {

            image = ImageIO.read(f);

            resizeImage(image.getWidth(), image.getHeight());
            System.out.println("Reading complete ");
        } catch (Exception e) {
            System.out.println(e);
        }
        return image;
    }

    private static void paintHistogram(Graphics g, int[] histogram, int max, int width, int height, int x, int y,
            Color color) {

        g.drawRect(x, y, width, height);
        int barWidth = histogram.length / width;

        for (int i = 0; i < histogram.length; i++) {
            int barHeight = (int) ((double) histogram[i] / (double) max * height);
            g.setColor(color);
            g.fillRect(x + i, y + height - barHeight, barWidth, barHeight);

        }

        g.setColor(Color.BLACK);
    }

    private void paintHistogramData(Graphics g, int[] red, int[] green, int[] blue) {
        int redMax = 0;
        int greenMax = 0;
        int blueMax = 0;
        for (int i = 0; i < red.length; i++) {
            if (red[i] > redMax) {
                redMax = red[i];

            }
            if (green[i] > greenMax) {
                greenMax = green[i];
            }
            if (blue[i] > blueMax) {
                blueMax = blue[i];
            }

        }

        int histogramHeight = height / 4;
        int histogramWidth = 256;
        int histogramX = this.getPreferredSize().width - histogramWidth;
        int histogramY = 0;

        paintHistogram(g, red, redMax, histogramWidth, histogramHeight, histogramX, histogramY,
                new Color(255, 0, 0));
        paintHistogram(g, green, greenMax, histogramWidth, histogramHeight,
                histogramX, histogramY + histogramHeight * 1, new Color(0, 255, 0));
        paintHistogram(g, blue, blueMax, histogramWidth, histogramHeight,
                histogramX, histogramY + histogramHeight * 2, new Color(0, 0, 255));
    }

    private static void orderedDithering(Graphics g, BufferedImage image) {
        BufferedImage ditheredImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        double[][] ditherMatrix = {
                { 0.0, 32.0, 8.0, 40.0, 2.0, 36.0, 10.0, 42.0 },
                { 48.0, 16.0, 56.0, 24.0, 50.0, 18.0, 58.0, 26.0 },
                { 12.0, 44.0, 4.0, 36.0, 14.0, 46.0, 6.0, 38.0 },
                { 60.0, 28.0, 52.0, 20.0, 62.0, 30.0, 54.0, 22.0 },
                { 3.0, 35.0, 11.0, 43.0, 1.0, 33.0, 9.0, 41.0 },
                { 51.0, 19.0, 59.0, 27.0, 49.0, 17.0, 57.0, 25.0 },
                { 15.0, 47.0, 7.0, 39.0, 13.0, 45.0, 5.0, 37.0 },
                { 63.0, 31.0, 55.0, 23.0, 61.0, 29.0, 53.0, 21.0 } };

        double multiplier = (double) 1 / 64;
        for (int i = 0; i < ditherMatrix.length; i++) {

            for (int j = 0; j < ditherMatrix.length; j++) {
                ditherMatrix[i][j] = ditherMatrix[i][j] * multiplier;
            }
        }

        int n = ditherMatrix.length;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {

                Color c = new Color(image.getRGB(x, y));
                int i = x % n;
                int j = y % n;
                double red = (double) c.getRed() / 255;
                double blue = (double) c.getBlue() / 255;
                double green = (double) c.getGreen() / 255;

                int redInt, greenInt, blueInt;
                redInt = (red > ditherMatrix[i][j]) ? 255 : 0;
                blueInt = (blue > ditherMatrix[i][j]) ? 255 : 0;
                greenInt = (green > ditherMatrix[i][j]) ? 255 : 0;
                Color newColor = new Color(redInt, greenInt, blueInt);
                ditheredImage.setRGB(x, y, newColor.getRGB());

            }

        }

        paintImage(g, image, 0, 0);
        paintImage(g, ditheredImage, image.getWidth(), 0);
        dithered = ditheredImage;

    }

    static int[] zigZagMatrix(int arr[][], int n, int m) {
        int row = 0, col = 0;
        int res[] = new int[n * m];
        int count = 0;
        // Boolean variable that will true if we
        // need to increment 'row' value otherwise
        // false- if increment 'col' value
        boolean row_inc = false;

        // Print matrix of lower half zig-zag pattern
        int mn = Math.min(m, n);
        for (int len = 1; len <= mn; ++len) {
            for (int i = 0; i < len; ++i) {
                // System.out.print(arr[row][col] + " ");
                res[count++] = arr[row][col];
                if (i + 1 == len)
                    break;
                // If row_increment value is true
                // increment row and decrement col
                // else decrement row and increment
                // col
                if (row_inc) {
                    ++row;
                    --col;
                } else {
                    --row;
                    ++col;
                }
            }

            if (len == mn)
                break;

            // Update row or col value according
            // to the last increment
            if (row_inc) {
                ++row;
                row_inc = false;
            } else {
                ++col;
                row_inc = true;
            }
        }

        // Update the indexes of row and col variable
        if (row == 0) {
            if (col == m - 1)
                ++row;
            else
                ++col;
            row_inc = true;
        } else {
            if (row == n - 1)
                ++col;
            else
                ++row;
            row_inc = false;
        }

        // Print the next half zig-zag pattern
        int MAX = Math.max(m, n) - 1;
        for (int len, diag = MAX; diag > 0; --diag) {

            if (diag > mn)
                len = mn;
            else
                len = diag;

            for (int i = 0; i < len; ++i) {
                // System.out.print(arr[row][col] + " ");
                res[count++] = arr[row][col];
                if (i + 1 == len)
                    break;

                // Update row or col value according
                // to the last increment
                if (row_inc) {
                    ++row;
                    --col;
                } else {
                    ++col;
                    --row;
                }
            }

            // Update the indexes of row and col variable
            if (row == 0 || col == m - 1) {
                if (col == m - 1)
                    ++row;
                else
                    ++col;

                row_inc = true;
            }

            else if (col == 0 || row == n - 1) {
                if (row == n - 1)
                    ++col;
                else
                    ++row;

                row_inc = false;
            }
        }

        return res;
    }

    private static void paintImage(Graphics g, BufferedImage image, int x, int y) {

        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                Color c = new Color(image.getRGB(j, i));
                paintPixel(g, x + j, y + i, c);

            }
        }
    }
}
