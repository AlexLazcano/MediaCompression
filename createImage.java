
import java.awt.image.*;
import java.io.File;

import javax.imageio.ImageIO;

public class createImage {
    public static void main(String[] args) {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 10; j++) {
                image.setRGB(i, j, 0xFF0000);
            }
            for (int j = 10; j < 20; j++) {
                image.setRGB(i, j, 0x00FF00);
            }
            for (int j = 20; j < 32; j++) {
                image.setRGB(i, j, 0x0000FF);
            }
        }
        System.out.println("Creating image");
        // save image
        try {
            File outputfile = new File("image.png");
            ImageIO.write(image, "png", outputfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
