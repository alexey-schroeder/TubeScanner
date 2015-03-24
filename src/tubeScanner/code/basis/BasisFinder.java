package tubeScanner.code.basis;

import org.opencv.core.Point;
import tubeScanner.code.dataModel.triplet.NodeTriplet;
import tubeScanner.code.dataModel.triplet.PointTriplet;
import tubeScanner.code.graph.Node;
import tubeScanner.code.utils.ImageUtils;
import tubeScanner.code.utils.PointUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Alex on 16.02.2015.
 */
public class BasisFinder {

    public ArrayList<Basis> findBases(HashSet<Node> allNodes, HashMap<Point, Node> currentCodes, Point[] cellVectors) {
        ArrayList<Basis> result = new ArrayList<>();
        if (allNodes.size() < 3) {
            return result;
        }
        for (Point point : currentCodes.keySet()) {
            Node node = currentCodes.get(point);
            if (allNodes.contains(node)) {
                ArrayList<Basis> bases = getBasisByCenter(allNodes, point, currentCodes, cellVectors);
                result.addAll(bases);
            }
        }
        return result;
    }

    public ArrayList<Basis> getBasisByCenter(HashSet<Node> allNodes, Point center, HashMap<Point, Node> currentCodes, Point[] cellVectors) {
        ArrayList<Basis> result = new ArrayList<>();
        Point vectorA = cellVectors[0];
        Point vectorB = cellVectors[1];
        double averageCellVectorLength = (ImageUtils.getVectorLength(vectorA) + ImageUtils.getVectorLength(vectorB)) / 2;
        double circleRadius = averageCellVectorLength / 3;
        double maxError = circleRadius / 2;
        Set<Point> currentPoints = currentCodes.keySet();

        Point pointInDirectionA_1 = getNeighborByDirection(center, currentPoints, vectorA, maxError);
        Node nodeInDirectionA_1 = currentCodes.get(pointInDirectionA_1);
        if(!allNodes.contains(nodeInDirectionA_1)){
            pointInDirectionA_1 = null;
        }
        Point pointInDirectionA_2 = getNeighborByDirection(center, currentPoints, PointUtils.flip(vectorA), maxError);
        Node nodeInDirectionA_2 = currentCodes.get(pointInDirectionA_2);
        if(!allNodes.contains(nodeInDirectionA_2)){
            pointInDirectionA_2 = null;
        }
        if (pointInDirectionA_1 == null && pointInDirectionA_2 == null) {
            return result;
        }

        Point pointInDirectionB_1 = getNeighborByDirection(center, currentPoints, vectorB, maxError);
        Node nodeInDirectionB_1 = currentCodes.get(pointInDirectionB_1);
        if(!allNodes.contains(nodeInDirectionB_1)){
            pointInDirectionB_1 = null;
        }
        Point pointInDirectionB_2 = getNeighborByDirection(center, currentPoints, PointUtils.flip(vectorB), maxError);
        Node nodeInDirectionB_2 = currentCodes.get(pointInDirectionB_2);
        if(!allNodes.contains(nodeInDirectionB_2)){
            pointInDirectionB_2 = null;
        }
        if (pointInDirectionB_1 == null && pointInDirectionB_2 == null) {
            return result;
        }

        if (pointInDirectionA_1 != null && pointInDirectionB_1 != null) {
            PointTriplet pointBasis = new PointTriplet(pointInDirectionA_1, pointInDirectionB_1, center);
            NodeTriplet nodeBasis = new NodeTriplet(currentCodes.get(pointInDirectionA_1), currentCodes.get(pointInDirectionB_1), currentCodes.get(center));
            Basis basis = new Basis(pointBasis, nodeBasis);
            result.add(basis);
        }

        if (pointInDirectionA_1 != null && pointInDirectionB_2 != null) {
            PointTriplet pointBasis = new PointTriplet(pointInDirectionA_1, pointInDirectionB_2, center);
            NodeTriplet nodeBasis = new NodeTriplet(currentCodes.get(pointInDirectionA_1), currentCodes.get(pointInDirectionB_2), currentCodes.get(center));
            Basis basis = new Basis(pointBasis, nodeBasis);
            result.add(basis);
        }

        if (pointInDirectionA_2 != null && pointInDirectionB_1 != null) {
            PointTriplet pointBasis = new PointTriplet(pointInDirectionA_2, pointInDirectionB_1, center);
            NodeTriplet nodeBasis = new NodeTriplet(currentCodes.get(pointInDirectionA_2), currentCodes.get(pointInDirectionB_1), currentCodes.get(center));
            Basis basis = new Basis(pointBasis, nodeBasis);
            result.add(basis);
        }

        if (pointInDirectionA_2 != null && pointInDirectionB_2 != null) {
            PointTriplet pointBasis = new PointTriplet(pointInDirectionA_2, pointInDirectionB_2, center);
            NodeTriplet nodeBasis = new NodeTriplet(currentCodes.get(pointInDirectionA_2), currentCodes.get(pointInDirectionB_2), currentCodes.get(center));
            Basis basis = new Basis(pointBasis, nodeBasis);
            result.add(basis);
        }

        return result;
    }

    public Point getNeighborByDirection(Point center, Set<Point> points, Point vector, double maxError) {
        for (Point point : points) {
            Point diff = PointUtils.minus(point, center);
            double error = PointUtils.getDistance(diff, vector);
            if (error <= maxError) {
                return point;
            }
        }
        return null;
    }
}
