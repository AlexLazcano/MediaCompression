import javax.swing.JPanel;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.awt.image.*;

public class ImagePanel extends JPanel {
    private static int height = 600;
    private static int width = 800;
    private static BufferedImage image = null;
    private static BufferedImage dithered = null;
    private static boolean isDithered = false;

    public ImagePanel() {

        setBorder(BorderFactory.createLineBorder(Color.black));
        setPreferredSize(new Dimension(width, height));

    }

    public void repaint(File file) {
        readImageFromFile(file);

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
            compressImage(image);
        }

    }

    public static void compressImage(BufferedImage image) {
        

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

    private static void paintImage(Graphics g, BufferedImage image, int x, int y) {

        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                Color c = new Color(image.getRGB(j, i));
                paintPixel(g, x + j, y + i, c);

            }
        }
    }
}
