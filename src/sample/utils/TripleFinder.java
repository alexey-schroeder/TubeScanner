package sample.utils;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 09.02.2015.
 */
public class TripleFinder {
    private List<Point> centers;

    public ArrayList<Triplet> findTriplets(List<Point> points, Point[] cellVectors) {
        ArrayList<Triplet> result = new ArrayList<>();
        Point vector_1 = cellVectors[0];
        Point vector_2 = cellVectors[1];
        double averageCellVectorLength = (ImageUtils.getVectorLength(vector_1) + ImageUtils.getVectorLength(vector_2)) / 2;
        double circleRadius = averageCellVectorLength / 3;
        double maxError = circleRadius / 2;
        for (Point point : points) {
            ArrayList<Point> neighbors = findNeighbor(point, points, averageCellVectorLength, maxError);
            for (Point neighbor : neighbors) {
                ArrayList<Point> thirdPoints = findThirdPoint(point, neighbor, points, averageCellVectorLength, maxError);
                for(Point thirdPoint : thirdPoints){
                    Triplet triplet = createTriplet(point, neighbor, thirdPoint);
                    result.add(triplet);
                }
            }
        }
        ArrayList<Triplet> filteredResult =  filterDublicateTriplets(result);
        return filteredResult;
    }

    public ArrayList<Triplet> filterDublicateTriplets(List<Triplet> triplets) {
        ArrayList<Triplet> result = new ArrayList<>();
        for(Triplet triplet : triplets){
            boolean contains = false;
            for(Triplet filteredTriplet : result){
                if(triplet.equals(filteredTriplet)){
                    contains = true;
                    break;
                }
            }
            if(!contains){
                result.add(triplet);
            }
        }
        return result;
    }

    public ArrayList<Point> findNeighbor(Point point, List<Point> points, double referenceDistance, double maxError) {
        ArrayList<Point> neighbors = new ArrayList<>();
        for (Point tempPoint : points) {
            double distanceDiff = Math.abs(referenceDistance - ImageUtils.getDistance(point, tempPoint));
            if (distanceDiff <= maxError) {
                neighbors.add(tempPoint);
            }
        }
        return neighbors;
    }

    public ArrayList<Point> findThirdPoint(Point pointA, Point pointB, List<Point> points, double referenceDistance, double maxError) {
        ArrayList<Point> result = new ArrayList<>();
        for (Point tempPoint : points) {
            double distanceDiff_1 = Math.abs(referenceDistance - ImageUtils.getDistance(pointA, tempPoint));
            double distanceDiff_2 = Math.abs(2 * referenceDistance - ImageUtils.getDistance(pointB, tempPoint));
            if (distanceDiff_1 <= maxError && distanceDiff_2 <= 2 * maxError) {
                result.add(tempPoint);
            } else {
                double distanceDiff_3 = Math.abs(2 * referenceDistance - ImageUtils.getDistance(pointA, tempPoint));
                double distanceDiff_4 = Math.abs(referenceDistance - ImageUtils.getDistance(pointB, tempPoint));
                if (distanceDiff_3 <= 2 * maxError && distanceDiff_4 <= maxError) {
                    result.add(tempPoint);
                }
            }
        }
        return result;
    }

    public Triplet createTriplet(Point point1, Point point2, Point point3){
        double distance1 = ImageUtils.getDistance(point1, point2);
        double distance2 = ImageUtils.getDistance(point1, point3);
        double distance3 = ImageUtils.getDistance(point2, point3);
        if(distance1 > distance2 && distance1 > distance3){
            return new Triplet(point1, point2, point3);
        } else if(distance2 > distance1 && distance2 > distance3){
            return new Triplet(point1, point3, point2);
        } else {
            return new Triplet(point2, point3, point1);
        }
    }


}