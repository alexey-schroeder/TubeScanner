package tubeScanner.code.graph;

import org.opencv.core.Point;
import tubeScanner.code.utils.ImageUtils;
import tubeScanner.code.utils.PointUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Alex on 29.03.2015.
 */
public class LatticePointsCalculator {
    private int width;
    private int height;
    private double radius;

    public LatticePointsCalculator(int width, int height, double radius) {
        this.width = width;
        this.height = height;
        this.radius = radius;
    }

    public List<Point> calculateLatticePoints(HashMap<Point, Node> goodPoints, Point[] cellVectors) {
        ArrayList<Point> result = new ArrayList<>();
        for (Point referencePoint : goodPoints.keySet()) {
            List<Point> result_1 = calculateCoordinateInStraightLine(referencePoint, cellVectors[0]);
            List<Point> result_2 = calculateCoordinateInStraightLine(referencePoint, PointUtils.flip(cellVectors[0]));
            List<Point> result_3 = calculateCoordinateInStraightLine(referencePoint, cellVectors[1]);
            List<Point> result_4 = calculateCoordinateInStraightLine(referencePoint, PointUtils.flip(cellVectors[1]));
            result.addAll(result_1);
            result.addAll(result_2);
            result.addAll(result_3);
            result.addAll(result_4);
        }

        ArrayList<Point> filteredResult = filterByGoodPoints(goodPoints, result);
        return filteredResult;
    }

    private ArrayList<Point> filterByGoodPoints(HashMap<Point, Node> goodPoints, ArrayList<Point> points) {
        ArrayList<Point> result = new ArrayList<>();
        Set<Point> goodPointsSet = goodPoints.keySet();
        for (Point point : points) {
            boolean hasEqualsGoodPoints = containEqualsPoint(goodPointsSet, point);
            if (!hasEqualsGoodPoints) {
                result.add(point);
            }
        }
        return result;
    }

    private boolean containEqualsPoint(Set<Point> goodPointsSet, Point point) {
        for(Point pointInSet : goodPointsSet){
            if(isEquals(pointInSet, point)){
                return true;
            }
        }
        return false;
    }

    private boolean isEquals(Point pointInSet, Point point) {
        double distance = PointUtils.getDistance(pointInSet, point);
        return distance < radius;
    }

    public ArrayList<Point> calculateCoordinateInStraightLine(Point referencePoint, Point cellVector) {
        ArrayList<Point> result = new ArrayList<>();
        Point lastPoint = referencePoint;
        Point lastPointFromCell = PointUtils.plus(lastPoint, cellVector);
        while (ImageUtils.isInBounds(width, height, lastPointFromCell)) {
            result.add(lastPointFromCell);
            lastPoint = lastPointFromCell;
            lastPointFromCell = PointUtils.plus(lastPoint, cellVector);
        }
        return result;
    }
}
