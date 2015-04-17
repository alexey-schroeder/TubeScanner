package tubeScanner.code.searchModeHandlers;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import tubeScanner.Controller;
import tubeScanner.code.frameSorce.FrameSource;
import tubeScanner.code.graph.CanvasGraphVisualiser;
import tubeScanner.code.graph.FrameStateVisualiser;
import tubeScanner.code.graph.Node;
import tubeScanner.code.qrCode.PointInterpreter;
import tubeScanner.code.utils.ImageUtils;
import tubeScanner.code.utils.PointUtils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Alex on 16.04.2015.
 */
public class SearchSingleCodeModeHandler {

    private Canvas canvas;
    private String code;
    private FrameSource frameSource;
    private CanvasGraphVisualiser canvasGraphVisualiser;
    private Scalar notInterpretedCircleColor = new Scalar(0, 0, 255);
    private Scalar interpretedCircleColor = new Scalar(0, 255, 0);
    private Scalar graphNodeColor = new Scalar(0, 255, 0);
    private double radius;
    private Controller controller;

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void threadCode() {
        Mat coloredFrame = frameSource.getFrame();
        Core.flip(coloredFrame, coloredFrame, 0);
        searchCode(coloredFrame, (int) radius);
    }

    public String searchCode(Mat frame, int radius) {
        Mat flipped = new Mat();
        Core.flip(frame, flipped, 0);
        double resizeFactor = Math.min(frame.rows(), frame.cols()) / 300.0;
        if (resizeFactor < 1) {
            resizeFactor = 1;
        }
        Mat resized;
        if (resizeFactor > 1.1) {

            resized = Mat.zeros((int) (flipped.rows() / resizeFactor), (int) (flipped.cols() / resizeFactor), flipped.type());
            Imgproc.resize(resized, resized, new Size(flipped.cols() / resizeFactor, flipped.rows() / resizeFactor), 0, 0, Imgproc.INTER_CUBIC);
        } else {
            resized = frame;
        }
        int width = frame.width();
        int height = frame.height();
        Point center = new Point(width / 2, height / 2);
        int radiusForSearch = radius * 2;
        Mat codeArea = new Mat();
        Imgproc.cvtColor(frame, codeArea, Imgproc.COLOR_RGB2GRAY);
        codeArea = codeArea.submat(new Rect((int) (center.x - radiusForSearch), (int) (center.y - radiusForSearch), 2 * radiusForSearch, 2 * radiusForSearch));
        MatOfKeyPoint codeKeyPoints = PointUtils.computeKeyPoints(codeArea);
        List<KeyPoint> keyPointsList = codeKeyPoints.toList();
        List<Point> centers = new ArrayList<Point>(keyPointsList.size());
        for (KeyPoint keyPoint : keyPointsList) {
            Point codeCenter = keyPoint.pt;
            centers.add(codeCenter);
        }
        PointInterpreter pointInterpreter = new PointInterpreter();
        pointInterpreter.interpretPoints(codeArea, 1, radius, centers);
        HashMap<Point, Node> goodPoints = pointInterpreter.getGoodPoints();
        List<Point> notInterpretedCircles = pointInterpreter.getNotInterpretedCircles();
        List<Point> interpretedCircles = pointInterpreter.getInterpretedCircles();
        drawCircles(resized, notInterpretedCircles, 6, notInterpretedCircleColor);
        drawCircles(resized, interpretedCircles, 6, interpretedCircleColor);
        String foundedCode = null;
        if (goodPoints.size() == 1) {
            Collection<Node> nodes = goodPoints.values();
            ArrayList<Node> nodeArray = new ArrayList<>(nodes);
            Node node = nodeArray.get(0);
            foundedCode = node.getCode();
            if (foundedCode.equalsIgnoreCase(code)) {
                drawCircles(resized, goodPoints.keySet(), 10, graphNodeColor);
            } else {
                drawCircles(resized, goodPoints.keySet(), 10, notInterpretedCircleColor);
            }
        }
        showFrame(resized);
        return null;
    }

    public void drawCircles(Mat frame, Collection<Point> points, int radius, Scalar color) {
        for (Point point : points) {
            Imgproc.circle(frame, point, radius, color, 2);
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

    public void setFrameSource(FrameSource frameSource) {
        this.frameSource = frameSource;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}
