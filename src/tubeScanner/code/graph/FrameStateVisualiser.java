package tubeScanner.code.graph;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import tubeScanner.code.utils.PointUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Alex on 15.03.2015.
 */
public class FrameStateVisualiser {
    private Mat frame;
    private Scalar notInterpretedCircleColor = new Scalar(0, 0, 255);
    private Scalar interpretedCircleColor = new Scalar(0, 255, 0);
    private Scalar graphNodeColor = new Scalar(0, 255, 0);
    private Scalar cellVectorsColor = new Scalar(255, 0, 0);

    public void drawFrameState(List<Point> notInterpretedCircles, List<Point> interpretedCircles, HashMap<Node, Point> graphNodes, Point[] cellVectors) {
        drawCircles(notInterpretedCircles, 6, notInterpretedCircleColor);
        drawCircles(interpretedCircles, 6, interpretedCircleColor);
        drawCircles(graphNodes.values(), 10, graphNodeColor);
        drawGraphEdges(graphNodes);
        if (cellVectors != null) {
            drawCellVectors(cellVectors);
        }
    }

    private void drawCellVectors(Point[] cellVectors) {
        Point startPoint = new Point(frame.cols() / 2, frame.rows() / 2);
        Point pointA = PointUtils.plus(startPoint, cellVectors[0]);
        Point pointB = PointUtils.plus(startPoint, cellVectors[1]);
        Imgproc.line(frame, startPoint, pointA, cellVectorsColor);
        Imgproc.line(frame, startPoint, pointB, cellVectorsColor);
    }

    private void drawGraphEdges(HashMap<Node, Point> graphNodes) {
        for (Node node : graphNodes.keySet()) {
            Point referencePoint = graphNodes.get(node);
            HashSet<Node> neighbors = node.getNeighbors();
            for (Node neighbor : neighbors) {
                if (graphNodes.containsKey(neighbor)) {
                    Point neighborPoint = graphNodes.get(neighbor);
                    Imgproc.line(frame, referencePoint, neighborPoint, graphNodeColor);
                }
            }
        }
    }

    public void drawCircles(Collection<Point> points, int radius, Scalar color) {
        for (Point point : points) {
            Imgproc.circle(frame, point, radius, color, 2);
        }
    }

    public Mat getFrame() {
        return frame;
    }

    public void setFrame(Mat frame) {
        this.frame = frame;
    }

    public Scalar getNotInterpretedCircleColor() {
        return notInterpretedCircleColor;
    }

    public void setNotInterpretedCircleColor(Scalar notInterpretedCircleColor) {
        this.notInterpretedCircleColor = notInterpretedCircleColor;
    }

    public Scalar getInterpretedCircleColor() {
        return interpretedCircleColor;
    }

    public void setInterpretedCircleColor(Scalar interpretedCircleColor) {
        this.interpretedCircleColor = interpretedCircleColor;
    }

    public Scalar getGraphNodeColor() {
        return graphNodeColor;
    }

    public void setGraphNodeColor(Scalar graphNodeColor) {
        this.graphNodeColor = graphNodeColor;
    }
}
