package tubeScanner.code.qrCode;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import tubeScanner.code.graph.Node;
import tubeScanner.code.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Alex on 30.03.2015.
 */
public class PointInterpreter {
    private HashMap<Point, Node> goodPoints;
    private List<Point> notInterpretedCircles;
    private List<Point> interpretedCircles;
    private CodeFinder codeFinder;
    private CodeCleaner codeCleaner;
    private DataMatrixInterpreter dataMatrixInterpreter;

    public PointInterpreter() {
        dataMatrixInterpreter = new DataMatrixInterpreter();
        codeCleaner = new CodeCleaner();
        codeFinder = new CodeFinder();
        goodPoints = new HashMap<>();
        interpretedCircles = new ArrayList<>();
        notInterpretedCircles = new ArrayList<>();
    }

    public void interpretPoints(Mat coloredFrame, double resizeFactor, double radius, List<Point> centers) {
        clear();
        double width = radius * 2 * resizeFactor;
        double height = radius * 2 * resizeFactor;
        for (Point center : centers) {
            int x = (int) (center.x * resizeFactor - width / 2);
            if (x < 0) {
                x = 0;
            }
            int y = (int) (center.y * resizeFactor - height / 2);
            if (y < 0) {
                y = 0;
            }
            if (x + width > coloredFrame.cols()) {
                width = coloredFrame.cols() - x - 1;
            }

            if (y + height > coloredFrame.rows()) {
                height = coloredFrame.rows() - y - 1;
            }

            if (width > 24 && height > 24) {
                Mat circleImage = coloredFrame.submat(new Rect(x, y, (int) width, (int) height));
                Core.flip(circleImage, circleImage, 0);
                Mat code = codeFinder.extractCode(circleImage);
                if (code != null) {
                    Mat boundedCode = codeCleaner.getBoundedCode(code);
                    if (boundedCode != null) {
                        Mat cleanedCode = codeCleaner.cleanCode(boundedCode);
                        if (cleanedCode != null) {
                            Core.bitwise_not(cleanedCode, cleanedCode);
                            BufferedImage bufferedImage = ImageUtils.matToBufferedImage(cleanedCode);
                            try {
                                String text = dataMatrixInterpreter.decode(bufferedImage);
                                if (text != null) {
                                    Node node = new Node(text);
                                    if (!goodPoints.values().contains(node)) {
                                        interpretedCircles.add(center);
                                        goodPoints.put(center, node);
                                    }
                                } else {
                                    notInterpretedCircles.add(center);
                                }

                            } catch (IOException e) {
//                        e.printStackTrace();
                            }
                        }
                    }
                }
            }else {
                notInterpretedCircles.add(center);
            }
        }
    }

    private void clear() {
        goodPoints.clear();
        interpretedCircles.clear();
        notInterpretedCircles.clear();
    }

    public HashMap<Point, Node> getGoodPoints() {
        return new HashMap<>(goodPoints);
    }

    public List<Point> getNotInterpretedCircles() {
        return new ArrayList<>(notInterpretedCircles);
    }

    public List<Point> getInterpretedCircles() {
        return new ArrayList<>(interpretedCircles);
    }
}
