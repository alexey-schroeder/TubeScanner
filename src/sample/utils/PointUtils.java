package sample.utils;

import org.opencv.core.Point;

/**
 * Created by Alex on 11.02.2015.
 */
public class PointUtils {

    public static Point plus(Point a, Point b) {
        return new Point(a.x + b.x, a.y + b.y);
    }

    public static Point minus(Point a, Point b) {
        return new Point(a.x - b.x, a.y - b.y);
    }

    //in degrees
    public static double getAngleBetweenVectors(Point a, Point b) {
        double nenner = a.x * b.x + a.y * b.y;
        double zeller = getVectorLength(a) * getVectorLength(b);
        double cosA = nenner / zeller;
        double arccos = Math.acos(cosA);
        return Math.toDegrees(arccos);
    }

    public static double getVectorLength(Point point) {
        return Math.sqrt(point.x * point.x + point.y * point.y);
    }

    public static double getDistance(Point pointA, Point pointB) {
        Point diff = minus(pointA, pointB);
        return getVectorLength(diff);
    }

    public static Point multWithFactor(Point point, double factor){
        return new Point(point.x * factor, point.y * factor);
    }

    public static  Point flipByY(Point point, Point flipCenterPoint){
        double yDiff = flipCenterPoint.y - point.y;
        return new Point(point.x, flipCenterPoint.y + yDiff);
    }

    public static Point turnOver(Point point) {
        return new Point(-point.x, -point.y);
    }

    public static Point getPerpendicularVector(Point vector) {
        return rotateVector(vector, 90);
    }

    /**
     * @param point
     * @param angle angle in degrees
     * @return
     */
    public static Point rotateVector(Point point, double angle) {
        angle = Math.toRadians(angle);
        Point rotated_point = new Point();
        rotated_point.x = point.x * Math.cos(angle) - point.y * Math.sin(angle);
        rotated_point.y = point.x * Math.sin(angle) + point.y * Math.cos(angle);
        return rotated_point;
    }

    public static Point calculateQuadratEdge(Point point1, Point point2, Point point3) {// point1 und point3 liegen auf der diagonal des quadrates!
        double x4 = point1.x + point3.x - point2.x;
        double y4 = point1.y + point3.y - point2.y;
        return new Point(x4, y4);
    }
}
