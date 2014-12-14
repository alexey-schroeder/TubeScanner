package sample.utils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.datamatrix.DataMatrixReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 30.11.2014.
 */
public class DataMatrixInterpreter {
    public String decode(File file) throws IOException {
        BufferedImage image = ImageIO.read(new FileInputStream(file));
        return decode(image);
    }

    public String decode(BufferedImage image) throws IOException {
        int angle = 0;
        HashMap<DecodeHintType, Boolean> hintMap = new HashMap<DecodeHintType, Boolean>();
        hintMap.put(DecodeHintType.PURE_BARCODE, true);
        while (true) {
            try {
                BufferedImage rotatedImage = ImageUtils.rotate(image, Math.toRadians(angle));
                BinaryBitmap binaryBitmap = null;
                binaryBitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(rotatedImage)));
                DataMatrixReader multiFormatReader = new DataMatrixReader();
                Result qrCodeResult = multiFormatReader.decode(binaryBitmap, hintMap);
                String text = qrCodeResult.getText();
                return text;
            } catch (Exception e) {
                if (angle == 270) {
                    return null;
                } else {
                    angle = angle + 90;
                }
            }
        }
    }
}
