package tubeScanner.code.searchModeHandlers;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import org.opencv.core.*;
import org.opencv.core.Point;
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

import java.awt.*;
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
    private String lastFoundedCode;

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
        lastFoundedCode = null;
    }

    public String threadCode() {
        Mat coloredFrame = frameSource.getFrame();
        return searchCode(coloredFrame, (int) radius);
    }

    public String searchCode(Mat frame, int radius) {
        Mat flippedFrame = new Mat(frame.rows(), frame.cols(), frame.type());
        Core.flip(frame, flippedFrame, 0);

        int width = flippedFrame.width();
        int height = flippedFrame.height();
        Point center = new Point(width / 2, height / 2);
        int radiusForSearch = radius * 10;
        Rect searchRect = new Rect((int) (center.x - radiusForSearch), (int) (center.y - radiusForSearch), 2 * radiusForSearch, 2 * radiusForSearch);
        Mat codeArea = flippedFrame.submat(searchRect).clone();
        double resizeFactor = Math.min(flippedFrame.rows(), flippedFrame.cols()) / 300.0;
        if (resizeFactor < 1) {
            resizeFactor = 1;
        }
        Mat erodedCodeArea = new Mat();
        Imgproc.erode(codeArea, erodedCodeArea, new Mat(), new Point(-1, -1), 2);
        Imgproc.resize(erodedCodeArea, erodedCodeArea, new Size(searchRect.width / resizeFactor, searchRect.width / resizeFactor), 0, 0, Imgproc.INTER_CUBIC);

        MatOfKeyPoint codeKeyPoints = PointUtils.computeKeyPoints(erodedCodeArea);
        List<KeyPoint> keyPointsList = codeKeyPoints.toList();
        List<Point> centers = new ArrayList<Point>(keyPointsList.size());
        for (KeyPoint keyPoint : keyPointsList) {
            Point codeCenter = keyPoint.pt;
            centers.add(codeCenter);
        }
        PointInterpreter pointInterpreter = new PointInterpreter();
        pointInterpreter.interpretPoints(codeArea, resizeFactor, radius, centers);
        HashMap<Point, Node> goodPoints = pointInterpreter.getGoodPoints();
        List<Point> notInterpretedCircles = pointInterpreter.getNotInterpretedCircles();
        List<Point> interpretedCircles = pointInterpreter.getInterpretedCircles();
        Imgproc.rectangle(flippedFrame, searchRect.tl(), searchRect.br(), interpretedCircleColor, (int) (2 * resizeFactor));
        drawCircles(flippedFrame, searchRect, resizeFactor, notInterpretedCircles, (int) (6 * resizeFactor), notInterpretedCircleColor);
        drawCircles(flippedFrame, searchRect, resizeFactor, interpretedCircles, (int) (6 * resizeFactor), interpretedCircleColor);
        String foundedCode = null;
        if (goodPoints.size() == 1) {
            Collection<Node> nodes = goodPoints.values();
            ArrayList<Node> nodeArray = new ArrayList<>(nodes);
            Node node = nodeArray.get(0);
            foundedCode = node.getCode();
            if(foundedCode != null){
                lastFoundedCode = foundedCode;
            }
            if (foundedCode.equalsIgnoreCase(code)) {
                drawCircles(flippedFrame, searchRect, resizeFactor, goodPoints.keySet(), (int) (10 * resizeFactor), graphNodeColor);
            } else {
                drawCircles(flippedFrame, searchRect, resizeFactor, goodPoints.keySet(), (int) (10 * resizeFactor), notInterpretedCircleColor);
            }
        }

        Mat resized;
        if (resizeFactor > 1.1) {
            resized = Mat.zeros((int) (flippedFrame.rows() / resizeFactor), (int) (flippedFrame.cols() / resizeFactor), flippedFrame.type());
            Imgproc.resize(flippedFrame, resized, new Size(flippedFrame.cols() / resizeFactor, flippedFrame.rows() / resizeFactor), 0, 0, Imgproc.INTER_CUBIC);
        } else {
            resized = flippedFrame;
        }
        showFrame(resized);
        return foundedCode;
    }

    public void drawCircles(Mat frame, Rect rect, double resizeFactor, Collection<Point> points, int radius, Scalar color) {
        for (Point pointInRect : points) {
            Point pointInFrame = new Point(pointInRect.x * resizeFactor + rect.x, pointInRect.y * resizeFactor + rect.y);
            Imgproc.circle(frame, pointInFrame, radius, color, (int) (2 * resizeFactor));
        }
    }

    public void showFrame(Mat frame) {
        BufferedImage image = ImageUtils.matToBufferedImage(frame);
        Graphics graphics = image.getGraphics();
        graphics.setColor(Color.GREEN);
        graphics.setFont(new Font("Arial Black", Font.BOLD, 20));
        graphics.drawString("Search for " + code, 10, 22);
        String foundedString;
        if(lastFoundedCode != null){
            foundedString = "Founded " + lastFoundedCode;
            if(!lastFoundedCode.equalsIgnoreCase(code)){
                graphics.setColor(Color.RED);
                foundedString = foundedString + "(INCORRECT!)";
            } else {
                graphics.setColor(Color.GREEN);
                foundedString = foundedString + "(CORRECT)";
            }
        } else {
            graphics.setColor(Color.YELLOW);
            foundedString = "Nothing founded";
        }
        graphics.drawString(foundedString, 10, 45);

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
