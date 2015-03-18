package tubeScanner.code.dataModel.triplet;

import org.opencv.core.Point;
import tubeScanner.code.utils.FindUtils;
import tubeScanner.code.utils.ImageUtils;
import tubeScanner.code.utils.PointUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Alex on 09.02.2015.
 * Klasse findet set aus drei point, die auf einer gerade liegen.
 */
public class PointTripleFinder {

    public ArrayList<PointTriplet> findTriplets(Collection<Point> points, Point[] cellVectors) {
        ArrayList<PointTriplet> result = new ArrayList<>();
        if (cellVectors == null) {
            return result;
        }
        //todo den code muss man in utils ubertragen, da er im mehreren klassen verwendet wird
        Point vector_1 = cellVectors[0];
        Point vector_2 = cellVectors[1];
        double averageCellVectorLength = (ImageUtils.getVectorLength(vector_1) + ImageUtils.getVectorLength(vector_2)) / 2;
        double circleRadius = averageCellVectorLength / 3;
        double maxError = circleRadius / 2;
        for (Point point : points) {
            ArrayList<Point> neighbors = FindUtils.findNeighbor(point, points, averageCellVectorLength, maxError, cellVectors);
            for (Point neighbor : neighbors) {
                ArrayList<Point> thirdPoints = findThirdPoint(point, neighbor, points, averageCellVectorLength, maxError);
                for (Point thirdPoint : thirdPoints) {
                    PointTriplet triplet = createTriplet(point, neighbor, thirdPoint);
                    result.add(triplet);
                }
            }
        }
        ArrayList<PointTriplet> filteredResult = FindUtils.filterDublicate(result);
        return filteredResult;
    }

    public ArrayList<Point> findThirdPoint(Point pointA, Point pointB, Collection<Point> points, double referenceDistance, double maxError) {
        ArrayList<Point> result = new ArrayList<>();
        double maxAngleError = 15;//in degrees
        for (Point tempPoint : points) {
            //der fall, wann pointA ist in der mitte zwischen pointB und tempPoint
            double distanceDiff_1 = Math.abs(referenceDistance - ImageUtils.getDistance(pointA, tempPoint));
            double distanceDiff_2 = Math.abs(2 * referenceDistance - ImageUtils.getDistance(pointB, tempPoint));
            if (distanceDiff_1 <= maxError && distanceDiff_2 <= 2 * maxError) {
                Point vectorAB = PointUtils.minus(pointA, pointB);
                Point vectorATemp = PointUtils.minus(tempPoint, pointA);
                double angle = PointUtils.getAngleBetweenVectors(vectorAB, vectorATemp);
                //der winkel zwischen vectors muss theoretisch 0 sein, da die drei points auf einer gerade liegen
                // Achtung! die prüfung muss stattfinden, da ich hier bug hatte,
                // wenn die cellVectoren ein bisschen kleiner waren als die distance zwischen points
                if (Math.abs(angle) < maxAngleError) {
                    result.add(tempPoint);
                }
            } else {
                //der fall, wann pointB ist in der mitte zwischen pointA und tempPoint
                double distanceDiff_3 = Math.abs(2 * referenceDistance - ImageUtils.getDistance(pointA, tempPoint));
                double distanceDiff_4 = Math.abs(referenceDistance - ImageUtils.getDistance(pointB, tempPoint));
                if (distanceDiff_3 <= 2 * maxError && distanceDiff_4 <= maxError) {
                    Point vectorAB = PointUtils.minus(pointA, pointB);
                    Point vectorBTemp = PointUtils.minus(tempPoint, pointB);
                    double angle = PointUtils.getAngleBetweenVectors(vectorAB, vectorBTemp);
                    //der winkel zwischen vectors muss theoretisch 0 sein, da die drei points auf einer gerade liegen
                    // Achtung! die prüfung muss stattfinden, da ich hier bug hatte,
                    // wenn die cellVectoren ein bisschen kleiner waren als die distance zwischen points
                    if (Math.abs(angle) < maxAngleError) {
                        result.add(tempPoint);
                    }
                }
            }
        }
        return result;
    }

    public PointTriplet createTriplet(Point point1, Point point2, Point point3) {
        double distance1 = ImageUtils.getDistance(point1, point2);
        double distance2 = ImageUtils.getDistance(point1, point3);
        double distance3 = ImageUtils.getDistance(point2, point3);
        if (distance1 > distance2 && distance1 > distance3) {
            return new PointTriplet(point1, point2, point3);
        } else if (distance2 > distance1 && distance2 > distance3) {
            return new PointTriplet(point1, point3, point2);
        } else {
            return new PointTriplet(point2, point3, point1);
        }
    }
}
