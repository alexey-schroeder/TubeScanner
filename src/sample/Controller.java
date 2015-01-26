package sample;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import sample.utils.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public class Controller {
    public Canvas canvas;
    VideoCapture camera;
    CodeFinder codeFinder;

    public void initialize() throws Exception {
        codeFinder = new CodeFinder();
        camera = new VideoCapture(0);

        camera.open(0); //Useless
        boolean wset = camera.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, 1280);
        boolean hset = camera.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, 720);
        if (!camera.isOpened()) {
            System.out.println("Camera Error");
        } else {
            System.out.println("Camera OK?");
            startThread();
        }


    }

    private void startThread() throws Exception {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
//                threadCode();
                    threadCode_2();
                }
            }
        });
        thread.start();
    }

    public void threadCode_2() {
        Mat coloredFrame = new Mat();
        camera.read(coloredFrame);

        double resizeFactor = Math.min(coloredFrame.rows(), coloredFrame.cols()) / 300.0;
        if (resizeFactor < 1) {
            resizeFactor = 1;
        }
        Mat resized;
        if (resizeFactor > 1.1) {

            resized = Mat.zeros((int) (coloredFrame.rows() / resizeFactor), (int) (coloredFrame.cols() / resizeFactor), coloredFrame.type());
//            Imgproc.GaussianBlur(coloredFrame, resized, new Size(15, 15), 35);
            Imgproc.erode(coloredFrame, resized, new Mat(), new Point(-1, -1), 2);
//            Imgproc.dilate(coloredFrame, resized, new Mat(), new Point(-1, -1), 5);
            Imgproc.resize(resized, resized, new Size(coloredFrame.cols() / resizeFactor, coloredFrame.rows() / resizeFactor), 0, 0, Imgproc.INTER_CUBIC);
//            Mat frame = new Mat();
//            Imgproc.cvtColor(resized, frame, Imgproc.COLOR_RGB2GRAY);
//            Imgproc.equalizeHist(frame, frame);
//            resized = frame;
        } else {
            resized = coloredFrame;
        }


        MatOfKeyPoint codeKeyPoints = computeKeyPoints(resized);
        List<KeyPoint> keyPointsList = codeKeyPoints.toList();
        List<Point> centers = new ArrayList<>(keyPointsList.size());
        for (KeyPoint keyPoint : keyPointsList) {
            Point center = keyPoint.pt;
            centers.add(center);
//            Imgproc.circle(resized, center, 10, new Scalar(0, 255, 0), 2);
        }
        CodeFinder codeFinder = new CodeFinder();
        Point[] cellVectors = codeFinder.calculateCellVectors(centers);
        if (cellVectors == null) {
            showFrame(resized);
            return;
        }
        Point vectorA = cellVectors[0];
        double radius = codeFinder.getVectorLength(vectorA) / 3.0;

        DataMatrixInterpreter dataMatrixInterpreter = new DataMatrixInterpreter();
        CodeCleaner codeCleaner = new CodeCleaner();
        for (Point center : centers) {
            double width = radius * 2 * resizeFactor;
            double height = radius * 2 * resizeFactor;
            int x = (int) (center.x * resizeFactor - width / 2);
            if (x < 0) {
                x = 0;
            }
            int y = (int) (center.y * resizeFactor - height / 2);
            if (y < 0) {
                y = 0;
            }
            if (x + width > coloredFrame.cols()) {
                width = coloredFrame.cols() - x;
            }

            if (y + height > coloredFrame.rows()) {
                height = coloredFrame.rows() - y;
            }
            Mat circleImage = coloredFrame.submat(new Rect(x, y, (int) width, (int) height));
            Mat code = codeFinder.extractCode(circleImage);
            if (code != null) {

                Mat boundedCode = codeCleaner.getBoundedCode(code);
                if(boundedCode == null){
                    showFrame(resized);
                    return;
                }
                Mat cleanedCode = codeCleaner.cleanCode(boundedCode);
                if(cleanedCode == null){
                    showFrame(resized);
                    return;
                }
                Core.bitwise_not(cleanedCode, cleanedCode);
                BufferedImage bufferedImage = ImageUtils.matToBufferedImage(cleanedCode);
                try {
                    String text = dataMatrixInterpreter.decode(bufferedImage);
                    System.out.println(text);
                    if (text != null) {
                        Imgproc.circle(resized, center, 10, new Scalar(0, 255, 0), 2);
                    }
                } catch (IOException e) {
//                        e.printStackTrace();
                }
            }
        }

//        for(KeyPoint keyPoint : keyPointsArray){
//            Point center = keyPoint.pt;
//            Imgproc.circle(resized, center, 10, new Scalar(0, 255, 0), 2);
//        }
//        Features2d.drawKeypoints(resized, codeKeyPoints, resized);
        showFrame(resized);
    }


    public MatOfKeyPoint computeKeyPoints(Mat mat) {
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.GRID_SIMPLEBLOB);
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        featureDetector.detect(mat, keypoints);
        return keypoints;
    }


    public void threadCode() {
        while (true) {
            Mat coloredFrame = new Mat();
            camera.read(coloredFrame);
            Mat frame = new Mat();
            Imgproc.cvtColor(coloredFrame, frame, Imgproc.COLOR_RGB2GRAY);
            try {
                LinkedList<RotatedRect> codeAreas = codeFinder.findCodes(frame);
                int resizeFactor = codeFinder.getResizeFactor();
                CodeCleaner codeCleaner = new CodeCleaner();
                DataMatrixInterpreter dataMatrixInterpreter = new DataMatrixInterpreter();
                for (RotatedRect rotatedRect : codeAreas) {
                    Point center = rotatedRect.center;
                    double width = rotatedRect.size.width * resizeFactor;
                    double height = rotatedRect.size.height * resizeFactor;
                    int x = (int) (center.x * resizeFactor - width / 2);
                    if (x < 0) {
                        x = 0;
                    }
                    int y = (int) (center.y * resizeFactor - height / 2);
                    if (y < 0) {
                        y = 0;
                    }

                    Mat circleImage = frame.submat(new Rect(x, y, (int) width, (int) height));
                    Mat code = codeFinder.extractCode(circleImage);
                    if (code != null) {

                        Mat boundedCode = codeCleaner.getBoundedCode(code);
                        Mat cleanedCode = codeCleaner.cleanCode(boundedCode);
                        Core.bitwise_not(cleanedCode, cleanedCode);
                        BufferedImage bufferedImage = ImageUtils.matToBufferedImage(cleanedCode);
                        String text = dataMatrixInterpreter.decode(bufferedImage);
                        if (text != null) {
                            Point[] rect_points = new Point[4];
                            rotatedRect.points(rect_points);
                            Scalar color = new Scalar(0, 0, 255);
                            for (int j = 0; j < 4; j++) {
                                Point a = rect_points[j].clone();
                                a.x = a.x * resizeFactor;
                                a.y = a.y * resizeFactor;
                                Point b = rect_points[(j + 1) % 4].clone();
                                b.x = b.x * resizeFactor;
                                b.y = b.y * resizeFactor;
                                Imgproc.line(coloredFrame, a, b, color, 1);
                                BufferedImage image = ImageUtils.matToBufferedImage(coloredFrame);
                                final WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        canvas.getGraphicsContext2D().drawImage(fxImage, 0, 0);
                                    }
                                });
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showFrame(Mat frame) {
        BufferedImage image = ImageUtils.matToBufferedImage(frame);
        final WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                canvas.getGraphicsContext2D().drawImage(fxImage, 0, 0);
            }
        });
    }


}
