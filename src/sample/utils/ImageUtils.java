package sample.utils;

import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by Alex on 09.11.2014.
 */
public class ImageUtils {

    public static BufferedImage cropImage(BufferedImage src, Rectangle rect) {
        int x = rect.x;
        if (x < 0) {
            x = 0;
        }

        int y = rect.y;
        if (y < 0) {
            y = 0;
        }

        int width = rect.width;
        if (x + width >= src.getWidth()) {
            width = src.getWidth() - x - 1;
        }

        int height = rect.height;
        if (y + height >= src.getHeight()) {
            height = src.getHeight() - y - 1;
        }

        BufferedImage dest = src.getSubimage(x, y, width, height);
        return dest;
    }

    public static BufferedImage matToBufferedImage(Mat matrix) {
        int cols = matrix.cols();
        int rows = matrix.rows();
        int elemSize = (int) matrix.elemSize();
        byte[] data = new byte[cols * rows * elemSize];
        int type;

        matrix.get(0, 0, data);

        switch (matrix.channels()) {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;

            case 3:
                type = BufferedImage.TYPE_3BYTE_BGR;

                // bgr to rgb
                byte b;
                for (int i = 0; i < data.length; i = i + 3) {
                    b = data[i];
                    data[i] = data[i + 2];
                    data[i + 2] = b;
                }
                break;

            default:
                return null;
        }

        BufferedImage image = new BufferedImage(cols, rows, type);
        image.getRaster().setDataElements(0, 0, cols, rows, data);

        return image;
    }

    public static BufferedImage toBufferedImageOfType(BufferedImage original, int type) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }

        // Don't convert if it already has correct type
        if (original.getType() == type) {
            return original;
        }

        // Create a buffered image
        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), type);

        // Draw the image onto the new buffer
        Graphics2D g = image.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.drawImage(original, 0, 0, null);
        } finally {
            g.dispose();
        }

        return image;
    }
}
