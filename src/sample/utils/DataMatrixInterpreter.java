package sample.utils;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Alex on 30.11.2014.
 */
public class DataMatrixInterpreter {
    public String decode(File file) throws IOException {
        int angle = 0;
        BinaryBitmap binaryBitmap = null;
        BufferedImage image = ImageIO.read(new FileInputStream(file));
        while (true) {
            try {
                BufferedImage rotatedImage = ImageUtils.rotate(image, Math.toRadians(angle));
                binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(rotatedImage)));
                Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap);
                String text = qrCodeResult.getText();
                return text;
            } catch (NotFoundException e) {
                if (angle == 270) {
                    return null;
                } else {
                    angle = angle + 90;
                }
            }
        }
    }
}
