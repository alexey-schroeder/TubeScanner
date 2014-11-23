package sample;

import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_highgui;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Alex
 * Date: 21.02.14
 * Time: 23:14
 * To change this template use File | Settings | File Templates.
 */
public class FrameGrabber {
    private Canvas canvas;
    private OpenCVFrameGrabber grabber;
    private boolean running = true;
    private int frameRate = 15;
    private BufferedImage lastFrame;
    private int waitTime;
    private opencv_highgui.CvCapture capture;

    public FrameGrabber(Canvas canvas, OpenCVFrameGrabber grabber) {
        this.canvas = canvas;
        this.grabber = grabber;
    }

    public void initGrabber() {
        boolean grabberStartet = false;
        while (!grabberStartet) {
            try {
                grabber.start();
                grabberStartet = true;
            } catch (Exception e) {
                System.out.println("Error by Grabber start. Noch ein Versuch...");
            }
        }
        waitTime = 1000 / frameRate;
        capture = opencv_highgui.cvCreateCameraCapture(0);

        opencv_highgui.cvSetCaptureProperty(capture, opencv_highgui.CV_CAP_PROP_FRAME_HEIGHT, 720);
        opencv_highgui.cvSetCaptureProperty(capture, opencv_highgui.CV_CAP_PROP_FRAME_WIDTH, 1280);
//        opencv_highgui.cvSetCaptureProperty(capture, opencv_highgui.CV_CAP_PROP_FRAME_HEIGHT, 1080);
//        opencv_highgui.cvSetCaptureProperty(capture, opencv_highgui.CV_CAP_PROP_FRAME_WIDTH, 1920);
        System.out.println("Grabber startet");
    }

    public void start() {

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                running = true;

                while (running) {
                    opencv_core.IplImage cvimg;
                    try {
                        // show image on window
                        cvimg = opencv_highgui.cvQueryFrame(capture);
                        BufferedImage bufImg = cvimg.getBufferedImage();
                        lastFrame = bufImg;
                        final WritableImage fxImage = SwingFXUtils.toFXImage(lastFrame, null);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                canvas.getGraphicsContext2D().drawImage(fxImage, 0, 0);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }


                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
//        Platform.runLater(task);
        Thread th = new Thread(task);
////        th.setDaemon(true);
        th.start();
    }
//
//    public BufferedImage drawCircles(BufferedImage image) {
//        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
//        Mat source = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4);
//        source.put(0, 0, pixels);
//        Mat source_2 = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4);
//        Imgproc.cvtColor(source, source_2, Imgproc.COLOR_RGBA2GRAY);
//        Mat binImage = new Mat(source_2.rows(), source_2.cols(), source_2.type());
//        Imgproc.adaptiveThreshold(source_2, binImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 5, 2);
//
//        List<Circle> circles = CircleFinder.extractCircles(binImage);
//        drawCircles(binImage, circles);
//        BufferedImage imageWithCircles = matToBufferedImage(binImage);
//        return imageWithCircles;
//    }

    public static void drawCircles(BufferedImage image, List<Circle> circles) {
        Graphics graphics = image.getGraphics();
        graphics.setColor(java.awt.Color.GREEN);
        for (Circle ball : circles) {
            double halfRadius = ball.radius / 2;
            int x = (int) (ball.x - halfRadius);
            int y = (int) (ball.y - halfRadius);
            graphics.drawOval(x, y, (int) ball.radius, (int) ball.radius);
        }
    }


    public static void drawCircles(Mat image, List<Circle> circles) {
        for (Circle ball : circles) {
            Imgproc.circle(image, new Point(ball.x, ball.y), (int) ball.radius, new Scalar(0, 255, 0), 2);
        }
    }

    public void stopGrabber() {
        running = false;
        try {
            grabber.stop();
            grabber.release();
            System.out.println("Grabber stopped");
        } catch (Exception e) {
            System.out.println("Error by stop of Grabber");
        }
    }

    public void pauseGrabber() {
        running = false;
    }

    public BufferedImage getLastFrame() {
        return lastFrame;
    }

    public boolean isRunning() {
        return running;
    }
}
