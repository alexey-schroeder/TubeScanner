package sample;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.googlecode.javacv.OpenCVFrameGrabber;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import sample.utils.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller {
    private OpenCVFrameGrabber grabber;
    private FrameGrabber frameGrabber;
    public Canvas canvas;

    public void initialize() {
        grabber = new OpenCVFrameGrabber(0);
        frameGrabber = new FrameGrabber(canvas, grabber);
    }

    public void initGrabber() {
        frameGrabber.initGrabber();
    }

    public void startGrabber() {
        frameGrabber.start();
    }

    public void stopGrabber() {
        frameGrabber.stopGrabber();
    }

    public FrameGrabber getFrameGrabber() {
        return frameGrabber;
    }

    public void onScreenClick(ActionEvent actionEvent) {
        doScreen();
    }

    private void doScreen() {
        doScreen("screen.bmp");
    }

    private void doScreen(String fileName) {
        BufferedImage frame = frameGrabber.getLastFrame();
        try {
            ImageIO.write(frame, "bmp", new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onCalculateClick(ActionEvent actionEvent) throws IOException {
        if (frameGrabber.isRunning()) {
            String fileName = "tube.bmp";
            doScreen(fileName);
            frameGrabber.pauseGrabber();
            File file = new File(fileName);
            Mat source = Imgcodecs.imread(file.getAbsolutePath(), CvType.CV_8UC4);
            Mat coloredSource = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
            double iCannyLowerThreshold = 25;
            double iCannyUpperThreshold = 70;
            Mat binImage = new Mat(source.rows(), source.cols(), source.type());
            Imgproc.Canny(source, binImage, iCannyLowerThreshold, iCannyUpperThreshold);
            Imgcodecs.imwrite("cannyImage.bmp", binImage);

            List<Circle> circles = CircleFinder.extractCircles(source);
            List<Line> allLines = new ArrayList<Line>();
            int counter = 1;
            int puffer = 15;
            CodeFinder codeFinder = new CodeFinder();
            CodeCleaner codeCleaner = new CodeCleaner();
            DataMatrixInterpreter dataMatrixInterpreter = new DataMatrixInterpreter();
            for (Circle circle : circles) {
                int x = (int) (circle.x - circle.radius - puffer);
                if (x < 0) {
                    x = 0;
                }

                int y = (int) (circle.y - circle.radius - puffer);
                if (y < 0) {
                    y = 0;
                }

                int width = (int) (circle.radius * 2 + puffer * 2);
                if (width + x > source.cols()) {
                    width = source.cols() - x;
                }
                int height = (int) (circle.radius * 2 + puffer * 2);
                if (height + y > source.rows()) {
                    height = source.rows() - y;
                }
                Mat circleImage = source.submat(new Rect(x, y, width, height));
                Mat coloredCircleImage = coloredSource.submat(new Rect(x, y, width, height));
//                Imgcodecs.imwrite("lines/code_" + counter + "_0.bmp", circleImage);

                Mat code = codeFinder.extractCode(circleImage, coloredCircleImage);
                if (code != null) {
                    Imgcodecs.imwrite("lines/code_" + counter + "_0.bmp", circleImage);
                    Imgcodecs.imwrite("lines/code_" + counter + "_1.bmp", code);

                    Mat boundedCode = codeCleaner.getBoundedCode(code);
                    Imgcodecs.imwrite("lines/code_" + counter + "_2.bmp", boundedCode);
                    Mat cleanedCode = codeCleaner.cleanCode(boundedCode);
                    Core.bitwise_not(cleanedCode, cleanedCode);
                    Imgcodecs.imwrite("lines/code_" + counter + "_3.bmp", cleanedCode);
                    BufferedImage bufferedImage = ImageUtils.matToBufferedImage(cleanedCode);
                    String text = dataMatrixInterpreter.decode(bufferedImage);
                    System.out.println("lines/code_" + counter + "_3.bmp: " + text);
                }
                counter++;
            }

            coloredSource = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
            drawCircles(coloredSource, circles);
            Imgcodecs.imwrite("circles.bmp", coloredSource);
            drawLines(coloredSource, allLines);
            Imgcodecs.imwrite("lines.bmp", coloredSource);
//            Mat coloredSource = Highgui.imread(file.getAbsolutePath(), Highgui.CV_LOAD_IMAGE_COLOR);
//            drawCircles(coloredSource, circles);
//            Highgui.imwrite("circles.bmp", coloredSource);
//            writeDataMatrixImages(ImageIO.read(new File("tube.bmp")), circles);
//            try {
//                BufferedImage imageWithCircles = ImageIO.read(new File("circles.bmp"));
//                final WritableImage fxImage = SwingFXUtils.toFXImage(imageWithCircles, null);
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        canvas.getGraphicsContext2D().drawImage(fxImage, 0, 0);
//                    }
//                });
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            decode();
        } else {
            try {
                BufferedImage imageWithCircles = ImageIO.read(new File("circles.bmp"));
                final WritableImage fxImage = SwingFXUtils.toFXImage(imageWithCircles, null);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        canvas.getGraphicsContext2D().drawImage(fxImage, 0, 0);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void decode() throws IOException {
        File file = new File("lines");
        File[] codes = file.listFiles();

        DataMatrixInterpreter dataMatrixInterpreter = new DataMatrixInterpreter();
        for (File code : codes) {
            String text = dataMatrixInterpreter.decode(code);
            System.out.println(code.getName() + ": " + text);
        }
    }

    public String decode(File file) throws IOException {
        int angle = 0;
        BinaryBitmap binaryBitmap = null;
        BufferedImage image = ImageIO.read(new FileInputStream(file));
        while (true) {
            try {
                System.out.println(angle);
//                AffineTransform transform = new AffineTransform();
//                transform.rotate(Math.toRadians(angle), image.getWidth() / 2, image.getHeight() / 2);
//                AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
                BufferedImage rotatedImage = ImageUtils.rotate(image, Math.toRadians(angle));
                File imageFile = new File("code_" + angle + ".bmp");
                boolean written = ImageIO.write(rotatedImage, "bmp", imageFile);

                System.out.println(imageFile.getAbsolutePath() + " written: " + written);
                binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                        new BufferedImageLuminanceSource(rotatedImage)));

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


    public static void writeDataMatrixImages(BufferedImage image, List<Circle> circles) {
        int counter = 1;
        for (Circle circle : circles) {
            Rectangle rectangle = new Rectangle((int) (circle.x - circle.radius), (int) (circle.y - circle.radius), (int) (2 * circle.radius), (int) (2 * circle.radius));

            BufferedImage circleImage = ImageUtils.cropImage(image, rectangle);
            try {
                ImageIO.write(circleImage, "bmp", new File("circles", "circle_" + counter + ".bmp"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            counter++;
        }
    }

    public static void drawCircles(Mat image, List<Circle> circles) {
        for (Circle ball : circles) {
            Imgproc.circle(image, new Point(ball.x, ball.y), (int) ball.radius, new Scalar(0, 255, 0), 2);
        }
    }

    public static void drawLines(Mat image, List<Line> lines) {
        for (Line line : lines) {
            Imgproc.line(image, line.getPoint_1(), line.getPoint_2(), new Scalar(0, 0, 255), 2);
        }
    }

    public void onVideoStreamClick(ActionEvent actionEvent) {
        if (!frameGrabber.isRunning()) {
            frameGrabber.start();
        }
    }

    public void onShowBinImageClick(ActionEvent actionEvent) {
        try {
            BufferedImage imageWithCircles = ImageIO.read(new File("binImage.bmp"));
            final WritableImage fxImage = SwingFXUtils.toFXImage(imageWithCircles, null);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    canvas.getGraphicsContext2D().drawImage(fxImage, 0, 0);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
