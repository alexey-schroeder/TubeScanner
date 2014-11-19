package sample;

import com.googlecode.javacv.OpenCVFrameGrabber;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import sample.utils.CircleFinder;
import sample.utils.ImageUtils;
import sample.utils.LineFinder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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


            List<Circle> circles = CircleFinder.extractCircles(source);
            List<Line> allLines = new ArrayList<Line>();
            int counter = 1;
            for (Circle circle : circles) {
                int x = (int) (circle.x - circle.radius);
                if (x < 0) {
                    x = 0;
                }

                int y = (int) (circle.y - circle.radius);
                if (y < 0) {
                    y = 0;
                }

                int width = (int) (circle.radius * 2);
                if (width + x > source.cols()) {
                    width = source.cols() - x;
                }
                int height = (int) (circle.radius * 2);
                if (height + y > source.rows()) {
                    height = source.rows() - y;
                }
                Mat circleImage = source.submat(new Rect(x, y, width, height));
                Highgui.imwrite("circles/circle_" + counter + ".bmp", circleImage);

                List<Line> lines = LineFinder.extractLines(circleImage);

                drawLines(circleImage, lines);
//                Highgui.imwrite("lines/lines_" + counter + ".bmp", circleImage);
                System.out.println("Lines in " + counter + ": " +lines.size());
                for (Line line : lines) {

                    Point point_1 = line.getPoint_1();
                    Point point_2 = line.getPoint_2();

                    point_1.x = point_1.x + circle.x - circle.radius;
                    point_2.x = point_2.x + circle.x - circle.radius;

                    point_1.y = point_1.y + circle.y - circle.radius;
                    point_2.y = point_2.y + circle.y - circle.radius;

                }
                allLines.addAll(lines);
                counter++;
            }

            Mat coloredSource = Highgui.imread(file.getAbsolutePath(), Highgui.CV_LOAD_IMAGE_COLOR);
            drawCircles(coloredSource, circles);
            Highgui.imwrite("circles.bmp", coloredSource);
            drawLines(coloredSource, allLines);
            Highgui.imwrite("lines.bmp", coloredSource);
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

    public static void drawLines(Mat image, List<Line> lines) {
        for (Line line : lines) {
            Core.line(image, line.getPoint_1(), line.getPoint_2(), new Scalar(0, 0, 255), 2);
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
