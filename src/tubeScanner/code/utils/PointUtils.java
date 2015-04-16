package tubeScanner.code.utils;

import org.opencv.core.Point;
import tubeScanner.code.clustering.Cluster;
import tubeScanner.code.clustering.DBSCANClusterer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

    public static Point flip(Point point) {
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


    public static Point[] calculateCellVectors(List<Point> points) {
        Point[] result = new Point[2];
        ArrayList<Point> differenceVectoren = calculateDifferenceVectoren(points);
        double maxDistanceInCluster = 5;
        DBSCANClusterer dbscanClusterer = new DBSCANClusterer(maxDistanceInCluster, 3);
        List<Cluster<Point>> clusters = dbscanClusterer.cluster(differenceVectoren);
//        System.out.println("clusters size: " + clusters.size());
        if (clusters.size() < 3) {
            return null;
        }
        Collections.sort(clusters, new Comparator<Cluster<Point>>() {
            @Override
            public int compare(Cluster<Point> o1, Cluster<Point> o2) {
                return o2.getPoints().size() - o1.getPoints().size();
            }
        });

        Point point_0 = calculateClusterCenter(clusters.get(0));
//        Point point_1 = calculateClusterCenter(clusters.get(1));
//        double angleDiff = Math.abs(90 - getAngleBetweenVectors(point_0, point_1));
//        double vectorLength = getVectorLength(point_1);
        ArrayList<Point> candidates = new ArrayList<>();
        double maxAngleDiff = 10;
        for (int i = 1; i < clusters.size(); i++) {
            Cluster<Point> tempCluster = clusters.get(i);
            Point tempPoint = calculateClusterCenter(tempCluster);
//            double tempVectorLength = getVectorLength(tempPoint);
            double tempAngleDiff = Math.abs(90 - PointUtils.getAngleBetweenVectors(point_0, tempPoint));

            if (tempAngleDiff < maxAngleDiff) {
                candidates.add(tempPoint);
            }
        }

        Collections.sort(candidates, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return (int) (PointUtils.getVectorLength(o1) - PointUtils.getVectorLength(o2));
            }
        });

        if (candidates.size() < 1) {
            return null;
        }
        Point point_1 = candidates.get(0);
        double maxProcentDiff = 15;
        double length_0 = PointUtils.getVectorLength(point_0);
        double length_1 = PointUtils.getVectorLength(point_1);
        double min = Math.min(length_0, length_1);
        double max = Math.max(length_0, length_1);
        double procentDiff = (max - min) / max  * 100;
        if(procentDiff > maxProcentDiff){
            return  null;
        }
        result[0] = point_0;
        result[1] = candidates.get(0);
        return result;
    }

    public static Point calculateClusterCenter(Cluster<Point> cluster) {
        List<Point> points = cluster.getPoints();
        double x = 0;
        double y = 0;
        for (Point point : points) {
            x = x + point.x;
            y = y + point.y;
        }
        return new Point(x / points.size(), y / points.size());
    }

    public static ArrayList<Point> calculateDifferenceVectoren(List<Point> points) {
        ArrayList<Point> result = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            Point referencePoint = points.get(i);
            for (int j = i + 1; j < points.size(); j++) {
                Point point = points.get(j);
                Point vector = PointUtils.minus(referencePoint, point);
                result.add(vector);
                Point negativVector = new Point(-vector.x, -vector.y);
                result.add(negativVector);
            }
        }
        return result;
    }
}
