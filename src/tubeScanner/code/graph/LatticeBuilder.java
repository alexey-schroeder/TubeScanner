package tubeScanner.code.graph;

import org.opencv.core.Point;
import tubeScanner.code.dataModel.doublet.NodeDoublet;
import tubeScanner.code.dataModel.doublet.PointDoublet;
import tubeScanner.code.dataModel.doublet.PointDoubletFinder;
import tubeScanner.code.utils.FindUtils;
import tubeScanner.code.utils.NodeUtils;
import tubeScanner.code.utils.PointUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Alex on 28.01.2015.
 */
public class LatticeBuilder {
    private Graph graph;
    private HashMap<Node, Point> addedNodes;

    public LatticeBuilder() {
    }

    public LatticeBuilder(Graph graph) {
        this.graph = graph;
    }


    public HashMap<Node, Point> calculateNodeCoordinates(HashMap<Point, Node> goodPoints, Point[] cellVectors) {
        addedNodes = new HashMap<>();

        HashMap<Node, Point> allNodeCoordinates = new HashMap<>();

        HashMap<Node, Point> nodeCoordinatesByGoodPoints = getNodeCoordinatesByGoodPoints(goodPoints);
        allNodeCoordinates.putAll(nodeCoordinatesByGoodPoints);

        HashMap<Node, Point> goodPointsNeighborsCoordinates = getGoodPointsNeighborsCoordinate(goodPoints);
        allNodeCoordinates.putAll(goodPointsNeighborsCoordinates);

        ArrayList<PointDoublet> pointDoublets = getPointDoublets(FindUtils.reverseMap(allNodeCoordinates), cellVectors);
        HashMap<Node, Point> nodeCoordinatesByDoublets = getNodeCoordinatesByDoublets(FindUtils.reverseMap(allNodeCoordinates), pointDoublets, cellVectors);
        allNodeCoordinates.putAll(nodeCoordinatesByDoublets);

        HashMap<Node, Point> coordinateFromFrame = new HashMap<>();
        coordinateFromFrame.putAll(nodeCoordinatesByGoodPoints);
        coordinateFromFrame.putAll(goodPointsNeighborsCoordinates);

        HashMap<Node, Point> correctedNodeCoordinates = correctNodeCoordinates(allNodeCoordinates, FindUtils.reverseMap(coordinateFromFrame));

        return correctedNodeCoordinates;
    }

    //berechnet coordinate für poitn, der zwischen zwei in frame erkannte points leigt
    // die coordinate wird als mittelwert zwischen erkannten points berechnet
    public HashMap<Node, Point> getGoodPointsNeighborsCoordinate(HashMap<Point, Node> goodPoints) {
        HashMap<Node, Point> result = new HashMap<>();
        HashSet<Node> allNodes = graph.getAllNodes();
        for (Node node : allNodes) {
            if (!goodPoints.containsValue(node)) {
                ArrayList<Node> neighborsInAxeA = node.getNeighborsByAxe(Graph.NodeAxe.AXE_A);
                Point point = getAveragePoint(goodPoints, neighborsInAxeA);
                if (point != null) {
                    result.put(node, point);
                } else {
                    ArrayList<Node> neighborsInAxeB = node.getNeighborsByAxe(Graph.NodeAxe.AXE_B);
                    point = getAveragePoint(goodPoints, neighborsInAxeB);
                    if (point != null) {
                        result.put(node, point);
                    }
                }
            }
        }
        return result;
    }

    public Point getAveragePoint(HashMap<Point, Node> goodPoints, ArrayList<Node> neighborsInAxeA) {
        if (neighborsInAxeA.size() == 2) {
            Node neighbor_1 = neighborsInAxeA.get(0);
            Node neighbor_2 = neighborsInAxeA.get(1);
            if (goodPoints.containsValue(neighbor_1) && goodPoints.containsValue(neighbor_2)) {
                Point neighbor_1_Point = FindUtils.findKeyForValueInMap(goodPoints, neighbor_1);
                Point neighbor_2_Point = FindUtils.findKeyForValueInMap(goodPoints, neighbor_2);
                Point pointSumme = PointUtils.plus(neighbor_1_Point, neighbor_2_Point);
                Point nodePoint = new Point(pointSumme.x / 2, pointSumme.y / 2);
                return nodePoint;
            }
        }
        return null;
    }

    public HashMap<Node, Point> getNodeCoordinatesByGoodPoints(HashMap<Point, Node> goodPoints) {
        HashMap<Node, Point> allNodeCoordinates = new HashMap<>();
        HashSet<Node> allNodes = graph.getAllNodes();
        for (Point point : goodPoints.keySet()) {
            Node node = goodPoints.get(point);
            Node equalsNodeInGraph = NodeUtils.findEqualsNode(allNodes, node);
            if (equalsNodeInGraph != null) {
                allNodeCoordinates.put(equalsNodeInGraph, point);
            }
        }
        return allNodeCoordinates;
    }

    public HashMap<Node, Point> getNodeCoordinatesByDoublets(HashMap<Point, Node> goodPoints, ArrayList<PointDoublet> pointDoublets, Point[] cellVectors) {
        HashSet<Node> allNodes = graph.getAllNodes();
        int graphSize = allNodes.size();
        HashMap<Node, Point> allNodeCoordinates = new HashMap<>();
        for (PointDoublet pointDoublet : pointDoublets) {
            Node nodeA = goodPoints.get(pointDoublet.getPointA());
            Node nodeB = goodPoints.get(pointDoublet.getPointB());
            Node equalsNodeA = NodeUtils.findEqualsNode(allNodes, nodeA);
            Node equalsNodeB = NodeUtils.findEqualsNode(allNodes, nodeB);
            if (equalsNodeA != null && equalsNodeB != null) {
                NodeDoublet nodeDoublet = new NodeDoublet(equalsNodeA, equalsNodeB);
                HashMap<Node, Point> nodeCoordinates = getCoordinateByDoublet(pointDoublet, nodeDoublet, cellVectors);
                allNodeCoordinates.putAll(nodeCoordinates);
                if (allNodeCoordinates.size() == graphSize) {
                    break;
                }
            }
        }
        return allNodeCoordinates;
    }

    public ArrayList<PointDoublet> getPointDoublets(HashMap<Point, Node> goodPoints, Point[] cellVectors) {
        PointDoubletFinder doubletFinder = new PointDoubletFinder();
        ArrayList<PointDoublet> pointDoublets = doubletFinder.findDoublets(goodPoints.keySet(), cellVectors);
        return pointDoublets;
    }

    public HashMap<Node, Point> getCoordinateByDoublet(PointDoublet pointDoublet, NodeDoublet nodeDoublet, Point[] cellVectors) {
        Node nodeA = nodeDoublet.getNodeA();
        Node nodeB = nodeDoublet.getNodeB();
        HashSet<Node> allNodes = graph.getAllNodes();

        Node nodeAInGraph = NodeUtils.findEqualsNode(allNodes, nodeA);
        Node nodeBInGraph = NodeUtils.findEqualsNode(allNodes, nodeB);

        HashMap<Node, Point> result = new HashMap<>();
        Point coordinateA = pointDoublet.getPointA().clone();
        Point coordinateB = pointDoublet.getPointB().clone();
        result.put(nodeAInGraph, coordinateA);
        result.put(nodeBInGraph, coordinateB);

        HashMap<Node, Point> pointsInDirection_1 = calculateCoordinateInStraightLine(nodeAInGraph, nodeBInGraph, coordinateA, coordinateB, cellVectors);
        HashMap<Node, Point> pointsInDirection_2 = calculateCoordinateInStraightLine(nodeBInGraph, nodeAInGraph, coordinateB, coordinateA, cellVectors);

        result.putAll(pointsInDirection_1);
        result.putAll(pointsInDirection_2);

        HashMap<Node, Point> nodesByDiagonallyNeighbors = getCoordinateForNodeByDiagonallyNeighbors(result);
        for (Node node : nodesByDiagonallyNeighbors.keySet()) {
            if (!result.containsKey(node)) {
                Point point = nodesByDiagonallyNeighbors.get(node);
                result.put(node, point);
            }
        }

        return result;
    }

    public Point getCoordinateForNodeByDiagonallyNeighbors(Node node, Node neighborA, Node neighborB, HashMap<Node, Point> result) {
        if (neighborA == null || neighborB == null || node == null) {
            return null;
        }
        if (result.keySet().contains(neighborA) && result.keySet().contains(neighborB)) {
            ArrayList<Node> diagonallyNeighbors_1 = NodeUtils.getJointNeighbors(neighborA, neighborB);
            if (diagonallyNeighbors_1 != null) {
                diagonallyNeighbors_1.remove(node);
                if (!diagonallyNeighbors_1.isEmpty()) {
                    Node diagonallyNeighbor = diagonallyNeighbors_1.get(0);
                    if (result.keySet().contains(diagonallyNeighbor)) {
                        Point resultPoint = PointUtils.calculateQuadratEdge(result.get(neighborA), result.get(diagonallyNeighbor), result.get(neighborB));
                        return resultPoint;
                    }
                }
            }
        }
        return null;
    }

    public HashMap<Node, Point> calculateCoordinateInStraightLine(Node referencePoint, Node lastNeighbor, Point referencePointCoordinate, Point lastNeighborCoordinate, Point[] cellVectors) {
        HashMap<Node, Point> result = new HashMap<>();
        Point calculatedVector = PointUtils.minus(referencePointCoordinate, lastNeighborCoordinate);
        Point cellVector = getSuitableCellVector(cellVectors, calculatedVector);
        Node oppositeNeighbor = referencePoint.getOppositeNeighbor(lastNeighbor);
        Point lastPoint = referencePointCoordinate;
        while (oppositeNeighbor != null) {
            Point lastPointFromCell = PointUtils.plus(lastPoint, cellVector);
            lastPoint = lastPointFromCell;
            result.put(oppositeNeighbor, lastPoint);
            lastNeighbor = referencePoint;
            referencePoint = oppositeNeighbor;
            oppositeNeighbor = referencePoint.getOppositeNeighbor(lastNeighbor);
        }
        return result;
    }

    public Point getSuitableCellVector(Point[] cellVectors, Point calculatedVector) {
        Point vector_0 = cellVectors[0];
        Point vector_1 = cellVectors[1];
        Point flippedVector_0 = PointUtils.flip(vector_0);
        Point flippedVector_1 = PointUtils.flip(vector_1);
        double distance_0 = PointUtils.getDistance(calculatedVector, vector_0);
        double distance_1 = PointUtils.getDistance(calculatedVector, vector_1);
        double flipDistance_0 = PointUtils.getDistance(calculatedVector, flippedVector_0);
        double flipDistance_1 = PointUtils.getDistance(calculatedVector, flippedVector_1);
        if (distance_0 < distance_1 && distance_0 < flipDistance_0 && distance_0 < flipDistance_1) {
            return vector_0.clone();
        } else if (distance_1 < distance_0 && distance_1 < flipDistance_0 && distance_1 < flipDistance_1) {
            return vector_1.clone();
        } else if (flipDistance_0 < distance_0 && flipDistance_0 < distance_1 && flipDistance_0 < flipDistance_1) {
            return flippedVector_0;
        }
        return flippedVector_1;
    }

    public Point getCoordinateForNodeByDiagonallyNeighbors(Node node, HashMap<Node, Point> result) {
        ArrayList<Node> neighborsInAxeA = node.getNeighborsByAxe(Graph.NodeAxe.AXE_A);
        ArrayList<Node> neighborsInAxeB = node.getNeighborsByAxe(Graph.NodeAxe.AXE_B);
        if (neighborsInAxeA.isEmpty() || neighborsInAxeB.isEmpty()) {
            return null; // der node hat keine nachbarn
        }

        Node neighbor_1_A = neighborsInAxeA.get(0);
        Node neighbor_2_A = null;
        if (neighborsInAxeA.size() > 1) {
            neighbor_2_A = neighborsInAxeA.get(1);
        }

        Node neighbor_1_B = neighborsInAxeB.get(0);
        Node neighbor_2_B = null;
        if (neighborsInAxeB.size() > 1) {
            neighbor_2_B = neighborsInAxeB.get(1);
        }
        Point point = getCoordinateForNodeByDiagonallyNeighbors(node, neighbor_1_A, neighbor_1_B, result);
        if (point != null) {
            return point;
        }

        point = getCoordinateForNodeByDiagonallyNeighbors(node, neighbor_1_A, neighbor_2_B, result);
        if (point != null) {
            return point;
        }

        point = getCoordinateForNodeByDiagonallyNeighbors(node, neighbor_2_A, neighbor_1_B, result);
        if (point != null) {
            return point;
        }

        point = getCoordinateForNodeByDiagonallyNeighbors(node, neighbor_2_A, neighbor_2_B, result);
        if (point != null) {
            return point;
        }

        return null;
    }

    public HashMap<Node, Point> correctNodeCoordinates(HashMap<Node, Point> calculatedNodeCoordinates, HashMap<Point, Node> goodPoints) {
        HashSet<Node> allGraphNodes = graph.getAllNodes();
        HashMap<Node, Point> result = new HashMap<>(calculatedNodeCoordinates);
        for (Point goodPointCoordinate : goodPoints.keySet()) {
            Node goodNode = goodPoints.get(goodPointCoordinate);
            if (calculatedNodeCoordinates.containsKey(goodNode)) {
                result.remove(goodNode);
                Node equalsGoodNodeInGraph = NodeUtils.findEqualsNode(allGraphNodes, goodNode);
                result.put(equalsGoodNodeInGraph, goodPointCoordinate);
            }
        }

        return result;
    }

    public HashMap<Node, Point> getCoordinateForNodeByDiagonallyNeighbors(HashMap<Node, Point> points) {
        HashSet<Node> allGraphNodes = graph.getAllNodes();
        HashMap<Node, Point> result = new HashMap<>();
        HashMap<Node, Point> tempPoints = new HashMap<>(points);// wir dürfen den input nicht ändern, deswegen erstellen wir eine copie
        boolean wasAdded = true;
        while (wasAdded) {
            wasAdded = false;
            for (Node node : allGraphNodes) {
                if (!tempPoints.keySet().contains(node)) {
                    Point point = getCoordinateForNodeByDiagonallyNeighbors(node, tempPoints);
                    if (point != null) {
                        tempPoints.put(node, point);
                        result.put(node, point);
                        addedNodes.put(node, point);
                        wasAdded = true;
                    }
                }
            }
        }
        return result;
    }

    public HashMap<Node, Point> getAddedNodes() {
        return addedNodes;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
}
