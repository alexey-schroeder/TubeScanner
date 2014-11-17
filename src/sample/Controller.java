package sample;

import com.googlecode.javacv.OpenCVFrameGrabber;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import sample.utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Controller {
    public TextField aX;
    public TextField aY;
    public TextField bX;
    public TextField bY;
    public TextField cX;
    public TextField cY;
    public TextField dX;
    public TextField dY;
    private OpenCVFrameGrabber grabber;
    private FrameGrabber frameGrabber;
    public Canvas canvas;
    private Marker marker;

    public void initialize() {
        marker = new Marker();
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent t) {
                        if (aX.isFocused() || aY.isFocused()) {
                            aX.setText(String.valueOf(t.getX()));
                            aY.setText(String.valueOf(t.getY()));
                            marker.setA(new Point2D(t.getX(), t.getY()));
                            bX.requestFocus();
                        } else if (bX.isFocused() || bY.isFocused()) {
                            bX.setText(String.valueOf(t.getX()));
                            bY.setText(String.valueOf(t.getY()));
                            marker.setB(new Point2D(t.getX(), t.getY()));
                            cX.requestFocus();
                        } else if (cX.isFocused() || cY.isFocused()) {
                            cX.setText(String.valueOf(t.getX()));
                            cY.setText(String.valueOf(t.getY()));
                            marker.setC(new Point2D(t.getX(), t.getY()));
                            dX.requestFocus();
                        } else if (dX.isFocused() || dY.isFocused()) {
                            dX.setText(String.valueOf(t.getX()));
                            dY.setText(String.valueOf(t.getY()));
                            marker.setD(new Point2D(t.getX(), t.getY()));
                            dX.getParent().requestFocus();
                        }
                    }
                });
//        try {
////            String[] deviceDescriptions = OpenCVFrameGrabber.getDeviceDescriptions();
//            System.out.println(OpenCVFrameGrabber.getDeviceDescriptions());
//        } catch (com.googlecode.javacv.FrameGrabber.Exception e) {
//            e.printStackTrace();
//        }

        grabber = new OpenCVFrameGrabber(0);
        frameGrabber = new FrameGrabber(canvas, grabber, marker);
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
        BufferedImage frame = frameGrabber.getLastFrame();
        try {
            ImageIO.write(frame, "bmp", new File("tube.bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onCalculateClick(ActionEvent actionEvent) throws IOException {
        if (frameGrabber.isRunning()) {
            doScreen();
            frameGrabber.pauseGrabber();
            File file = new File("tube.bmp");
            Mat source = Highgui.imread(file.getAbsolutePath(), CvType.CV_8UC4);
            Mat binImage = new Mat(source.rows(), source.cols(), source.type());
            Imgproc.adaptiveThreshold(source, binImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 5, 2);

            Highgui.imwrite("binImage.bmp", binImage);

            List<Circle> circles = CircleFinder.extractCircles(binImage);
            Mat coloredSource = Highgui.imread(file.getAbsolutePath(), Highgui.CV_LOAD_IMAGE_COLOR);
            drawCircles(coloredSource, circles);
            Highgui.imwrite("circles.bmp", coloredSource);
            writeDataMatrixImages(ImageIO.read(new File("tube.bmp")), circles);
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
            Core.circle(image, new Point(ball.x, ball.y), (int) ball.radius, new Scalar(0, 255, 0), 2);
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
