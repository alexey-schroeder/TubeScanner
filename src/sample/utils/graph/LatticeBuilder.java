package sample.utils.graph;

import org.opencv.core.Point;
import sample.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Alex on 28.01.2015.
 */
public class LatticeBuilder {
    private Graph graph;

    public LatticeBuilder(Graph graph) {
        this.graph = graph;
    }

    public HashMap<Node, Point> calculateNodeCoordinates(HashMap<Point, Node> goodPoints, Point[] cellVectors) {
        HashSet<Node> allNodes = graph.getAllNodes();
        int graphSize = allNodes.size();
        BasisFinder basisFinder = new BasisFinder();
        ArrayList<Basis> bases = basisFinder.findBases(allNodes, goodPoints, cellVectors);
        HashMap<Node, Point> allNodeCoordinates = new HashMap<>();
        for (Basis basis : bases) {
            HashMap<Node, Point> nodeCoordinates = graphToPoints(basis.getPointBasis(), basis.getNodeBasis());
            allNodeCoordinates.putAll(nodeCoordinates);
            if (allNodeCoordinates.size() == graphSize) {
                break;
            }
        }
        HashMap<Node, Point> correctedNodeCoordinates = correctNodeCoordinates(allNodeCoordinates, goodPoints);
        return correctedNodeCoordinates;
    }

    public HashMap<Node, Point> graphToPoints(PointTriplet pointBasis, NodeTriplet nodeBasis) {
        Node nodeA = nodeBasis.getNodeA();
        Node nodeB = nodeBasis.getNodeB();
        Node nodeCenter = nodeBasis.getCenter();
        HashSet<Node> allNodes = graph.getAllNodes();

        Node nodeAInGraph = NodeUtils.findEqualsNode(allNodes, nodeA);
        Node nodeBInGraph = NodeUtils.findEqualsNode(allNodes, nodeB);
        Node nodeCenterInGraph = NodeUtils.findEqualsNode(allNodes, nodeCenter);

//        Point nodeAVector = PointUtils.minus(pointBasis.getCenter(), pointBasis.getPointA());
//        Point nodeBVector = PointUtils.minus(pointBasis.getCenter(), pointBasis.getPointB());

        HashMap<Node, Point> result = new HashMap<>();
        Point coordinateA = pointBasis.getPointA().clone();
        Point coordinateB = pointBasis.getPointB().clone();
        Point coordinateCenter = pointBasis.getCenter().clone();
        result.put(nodeAInGraph, coordinateA);
        result.put(nodeBInGraph, coordinateB);
        result.put(nodeCenterInGraph, pointBasis.getCenter().clone());

        calculateCoordinateInStraightLine(result, nodeAInGraph, nodeCenterInGraph, coordinateA, coordinateCenter);
        calculateCoordinateInStraightLine(result, nodeCenterInGraph, nodeAInGraph, coordinateCenter, coordinateA);
        calculateCoordinateInStraightLine(result, nodeBInGraph, nodeCenterInGraph, coordinateB, coordinateCenter);
        calculateCoordinateInStraightLine(result, nodeCenterInGraph, nodeBInGraph, coordinateCenter, coordinateB);
        boolean wasAdded = true;
        while (wasAdded) {
            wasAdded = false;
            for (Node node : allNodes) {
                if (!result.keySet().contains(node)) {
                    Point point = getCoordinateForNode(node, result);
                    if (point != null) {
                        result.put(node, point);
                        wasAdded = true;
                    }
                }
            }
        }
        return result;
    }

    public Point getCoordinateForNodeByNeighbors(Node node, Node neighborA, Node neighborB, HashMap<Node, Point> result) {
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


    public void calculateCoordinateInStraightLine(HashMap<Node, Point> result, Node referencePoint, Node lastNeighbor, Point referencePointCoordinate, Point lastNeighborCoordinate) {
        Point vector = PointUtils.minus(referencePointCoordinate, lastNeighborCoordinate);
        Node oppositeNeighbor = referencePoint.getOppositeNeighbor(lastNeighbor);
        Point lastPoint = referencePointCoordinate;
        while (oppositeNeighbor != null) {
            lastPoint = PointUtils.plus(lastPoint, vector);
            result.put(oppositeNeighbor, lastPoint);
            lastNeighbor = referencePoint;
            referencePoint = oppositeNeighbor;
            oppositeNeighbor = referencePoint.getOppositeNeighbor(lastNeighbor);
        }
    }

    public Point getCoordinateForNode(Node node, HashMap<Node, Point> result) {
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
        Point point = getCoordinateForNodeByNeighbors(node, neighbor_1_A, neighbor_1_B, result);
        if (point != null) {
            return point;
        }

        point = getCoordinateForNodeByNeighbors(node, neighbor_1_A, neighbor_2_B, result);
        if (point != null) {
            return point;
        }

        point = getCoordinateForNodeByNeighbors(node, neighbor_2_A, neighbor_1_B, result);
        if (point != null) {
            return point;
        }

        point = getCoordinateForNodeByNeighbors(node, neighbor_2_A, neighbor_2_B, result);
        if (point != null) {
            return point;
        }

        return null;
    }

    public HashMap<Node, Point> correctNodeCoordinates(HashMap<Node, Point> nodeCoordinates, HashMap<Point, Node> goodPoints) {
        HashSet<Node> allGraphNodes = graph.getAllNodes();
        HashMap<Node, Point> result = new HashMap<>(nodeCoordinates);
        for (Point goodPointCoordinate : goodPoints.keySet()) {
            Node goodNode = goodPoints.get(goodPointCoordinate);
            if (nodeCoordinates.containsKey(goodNode)) {
                result.remove(goodNode);
                Node equalsGoodNodeInGraph = NodeUtils.findEqualsNode(allGraphNodes, goodNode);
                result.put(equalsGoodNodeInGraph, goodPointCoordinate);
            }
        }

        correctNodeCoordinatesByNeighbors(result);

        if (allGraphNodes.size() != result.size()) {
            boolean wasAdded = true;
            while (wasAdded) {
                wasAdded = false;
                for (Node node : allGraphNodes) {
                    if (!result.keySet().contains(node)) {
                        Point point = getCoordinateForNode(node, result);
                        if (point != null) {
                            result.put(node, point);
                            wasAdded = true;
                        }
                    }
                }
            }
        }

        correctNodeCoordinatesByNeighbors(result);
//        System.out.println(allGraphNodes.size() + " / " + nodeCoordinates.size() + " / " + result.size());
        return result;
    }

    public void correctNodeCoordinatesByNeighbors(HashMap<Node, Point> result) {
        boolean wasAdded = true;
        HashSet<Node> allGraphNodes = graph.getAllNodes();
        while (wasAdded) {
            wasAdded = false;
            if (allGraphNodes.size() == result.size()) {
//                System.out.println("alls");
//                return result;
            } else {
                HashMap<Node, Point> resultCopy = new HashMap<>(result);
                for (Node nodeInResult : resultCopy.keySet()) {
//                    if(nodeInResult.getCode().equalsIgnoreCase("0122708290")){
//                        System.out.println();
//                    }
                    ArrayList<Node> neighborsInAxeA = nodeInResult.getNeighborsByAxe(Graph.NodeAxe.AXE_A);
                    if (neighborsInAxeA.size() == 2) {
                        Node neighborInAxeA_1 = neighborsInAxeA.get(0);
                        Node neighborInAxeA_2 = neighborsInAxeA.get(1);
                        if (!result.containsKey(neighborInAxeA_1) && result.containsKey(neighborInAxeA_2)) {// nachbarn 1 ist nicht in resultat, aber nachbarn 2 schon
                            Point coordinateNeighbor_1 = new Point();
                            Point coordinateNeighbor_2 = result.get(neighborInAxeA_2);
                            Point parentCoordinate = result.get(nodeInResult);
                            coordinateNeighbor_1.x = 2 * parentCoordinate.x - coordinateNeighbor_2.x;
                            coordinateNeighbor_1.y = 2 * parentCoordinate.y - coordinateNeighbor_2.y;
                            result.put(neighborInAxeA_1, coordinateNeighbor_1);
                            wasAdded = true;
//                            System.out.println("added");
                        } else if (result.containsKey(neighborInAxeA_1) && !result.containsKey(neighborInAxeA_2)) {// nachbarn 2 ist nicht in resultat, aber nachbarn 1 schon
                            Point coordinateNeighbor_2 = new Point();
                            Point coordinateNeighbor_1 = result.get(neighborInAxeA_1);
                            Point parentCoordinate = result.get(nodeInResult);
                            coordinateNeighbor_2.x = 2 * parentCoordinate.x - coordinateNeighbor_1.x;
                            coordinateNeighbor_2.y = 2 * parentCoordinate.y - coordinateNeighbor_1.y;
                            result.put(neighborInAxeA_2, coordinateNeighbor_2);
                            wasAdded = true;
//                            System.out.println("added");
                        }
                    }
                    ArrayList<Node> neighborsInAxeB = nodeInResult.getNeighborsByAxe(Graph.NodeAxe.AXE_B);
                    if (neighborsInAxeB.size() == 2) {
                        Node neighborInAxeB_1 = neighborsInAxeB.get(0);
                        Node neighborInAxeB_2 = neighborsInAxeB.get(1);
                        if (!result.containsKey(neighborInAxeB_1) && result.containsKey(neighborInAxeB_2)) {// nachbarn 1 ist nicht inresultat, aber nachbarn 2 schon
                            Point coordinateNeighbor_1 = new Point();
                            Point coordinateNeighbor_2 = result.get(neighborInAxeB_2);
                            Point parentCoordinate = result.get(nodeInResult);
                            coordinateNeighbor_1.x = 2 * parentCoordinate.x - coordinateNeighbor_2.x;
                            coordinateNeighbor_1.y = 2 * parentCoordinate.y - coordinateNeighbor_2.y;
                            result.put(neighborInAxeB_1, coordinateNeighbor_1);
                            wasAdded = true;
//                            System.out.println("added");
                        } else if (result.containsKey(neighborInAxeB_1) && !result.containsKey(neighborInAxeB_2)) {// nachbarn 2 ist nicht in resultat, aber nachbarn 1 schon
                            Point coordinateNeighbor_2 = new Point();
                            Point coordinateNeighbor_1 = result.get(neighborInAxeB_1);
                            Point parentCoordinate = result.get(nodeInResult);
                            coordinateNeighbor_2.x = 2 * parentCoordinate.x - coordinateNeighbor_1.x;
                            coordinateNeighbor_2.y = 2 * parentCoordinate.y - coordinateNeighbor_1.y;
                            result.put(neighborInAxeB_2, coordinateNeighbor_2);
                            wasAdded = true;
//                            System.out.println("added");
                        }
                    }
                }
            }
        }
    }
}
