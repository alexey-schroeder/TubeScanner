package tubeScanner.code.graph;

import org.opencv.core.Point;
import tubeScanner.code.basis.Basis;
import tubeScanner.code.basis.BasisFinder;
import tubeScanner.code.dataModel.doublet.NodeDoublet;
import tubeScanner.code.dataModel.doublet.PointDoublet;
import tubeScanner.code.dataModel.doublet.PointDoubletFinder;
import tubeScanner.code.dataModel.triplet.NodeTriplet;
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

    public LatticeBuilder(Graph graph) {
        this.graph = graph;
    }

    public HashMap<Node, Point> calculateNodeCoordinates(HashMap<Point, Node> goodPoints, Point[] cellVectors) {
        addedNodes = new HashMap<>();
        correctGraphByBaseis(goodPoints, cellVectors);

        HashMap<Node, Point> allNodeCoordinates = new HashMap<>();

        HashMap<Node, Point> nodeCoordinatesByGoodPoints = getNodeCoordinatesByGoodPoints(goodPoints);
        allNodeCoordinates.putAll(nodeCoordinatesByGoodPoints);

        ArrayList<PointDoublet> pointDoublets = getPointDoublets(goodPoints, cellVectors);
        HashMap<Node, Point> nodeCoordinatesByDoublets = getNodeCoordinatesByDoublets(goodPoints, pointDoublets);
        allNodeCoordinates.putAll(nodeCoordinatesByDoublets);

        HashMap<Node, Point> correctedNodeCoordinates = correctNodeCoordinates(allNodeCoordinates, goodPoints);


        return correctedNodeCoordinates;
    }

    public HashMap<Node, Point> getNodeCoordinatesByGoodPoints(HashMap<Point, Node> goodPoints){
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

    public HashMap<Node, Point> getNodeCoordinatesByDoublets(HashMap<Point, Node> goodPoints, ArrayList<PointDoublet> pointDoublets){
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
                HashMap<Node, Point> nodeCoordinates = getCoordinateByDoublet(pointDoublet, nodeDoublet);
                allNodeCoordinates.putAll(nodeCoordinates);
                if (allNodeCoordinates.size() == graphSize) {
                    break;
                }
            }
        }
        return allNodeCoordinates;
    }

    public ArrayList<PointDoublet> getPointDoublets(HashMap<Point, Node> goodPoints, Point[] cellVectors){
        PointDoubletFinder doubletFinder = new PointDoubletFinder();
        ArrayList<PointDoublet> pointDoublets = doubletFinder.findDoublets(goodPoints.keySet(), cellVectors);
        return pointDoublets;
    }

    public void correctGraphByBaseis(HashMap<Point, Node> goodPoints, Point[] cellVectors) {
        HashSet<Node> allNodes = graph.getAllNodes();
        BasisFinder basisFinder = new BasisFinder();
        ArrayList<Basis> bases = basisFinder.findBases(allNodes, goodPoints, cellVectors);
        for (Basis basis : bases) {
            correctGraphByBasis(basis.getNodeBasis(), allNodes);
        }
    }

    public void correctGraphByBasis(NodeTriplet nodeTriplet, HashSet<Node> allNodes) {
        Node nodeA = nodeTriplet.getNodeA();
        Node nodeB = nodeTriplet.getNodeB();
        Node centerNode = nodeTriplet.getCenter();

        Node equalsNodeCenter = NodeUtils.findEqualsNode(allNodes, centerNode);
        HashSet<Node> centerNeighbors = equalsNodeCenter.getNeighbors();
        if (centerNeighbors.size() < 4) {
            Node equalsNodeA = NodeUtils.findEqualsNode(allNodes, nodeA);
            Node equalsNodeB = NodeUtils.findEqualsNode(allNodes, nodeB);
            boolean isNodeAContains = centerNeighbors.contains(equalsNodeA);
            boolean isNodeBContains = centerNeighbors.contains(equalsNodeB);
            if (isNodeAContains && isNodeBContains || !isNodeAContains && !isNodeBContains) {//beide nodes aus triplet sind entweder nachbarn odern nicht nachbarn
                return; // wir können nichts machen
            } else {
                if (isNodeAContains) {// nodeA ist als nachbar bekannt
                    Graph.NodeAxe axe = equalsNodeCenter.getNeighborsAxe(equalsNodeA);
                    Graph.NodeAxe otherAxe = Graph.getOtherNodeAxe(axe);
                    equalsNodeCenter.addNeighbor(equalsNodeB, otherAxe);
                } else {// nodeB ist als nachbar bekannt
                    Graph.NodeAxe axe = equalsNodeCenter.getNeighborsAxe(equalsNodeB);
                    Graph.NodeAxe otherAxe = Graph.getOtherNodeAxe(axe);
                    equalsNodeCenter.addNeighbor(equalsNodeA, otherAxe);
                }
            }
        }
    }

    public HashMap<Node, Point> getCoordinateByDoublet(PointDoublet pointDoublet, NodeDoublet nodeDoublet) {
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

        HashMap<Node, Point> pointsInDirection_1 = calculateCoordinateInStraightLine(nodeAInGraph, nodeBInGraph, coordinateA, coordinateB);
        HashMap<Node, Point> pointsInDirection_2 = calculateCoordinateInStraightLine(nodeBInGraph, nodeAInGraph, coordinateB, coordinateA);

        result.putAll(pointsInDirection_1);
        result.putAll(pointsInDirection_2);

        addCoordinateForNodeByDiagonallyNeighbors(result);
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


    public HashMap<Node, Point> calculateCoordinateInStraightLine(Node referencePoint, Node lastNeighbor, Point referencePointCoordinate, Point lastNeighborCoordinate) {
        HashMap<Node, Point> result = new HashMap<>();
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
        return result;
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

        connectGraphNodesByNeighbors(result);
//        addedNodes.putAll(tempAddedNodes);

        if (allGraphNodes.size() != result.size()) {
            addCoordinateForNodeByDiagonallyNeighbors(result);
        }

        connectGraphNodesByNeighbors(result);
//        addedNodes.putAll(tempAddedNodes_2);
//        System.out.println(allGraphNodes.size() + " / " + nodeCoordinates.size() + " / " + result.size());

        for (Point goodPointCoordinate : goodPoints.keySet()) {
            Node goodNode = goodPoints.get(goodPointCoordinate);
            Node equalsGoodNodeInGraph = NodeUtils.findEqualsNode(allGraphNodes, goodNode);
            if (result.containsKey(equalsGoodNodeInGraph)) {
                result.put(equalsGoodNodeInGraph, goodPointCoordinate);
            }
        }
        return result;
    }

    public void addCoordinateForNodeByDiagonallyNeighbors(HashMap<Node, Point> result) {
        HashSet<Node> allGraphNodes = graph.getAllNodes();
        boolean wasAdded = true;
        while (wasAdded) {
            wasAdded = false;
            for (Node node : allGraphNodes) {
                if (!result.keySet().contains(node)) {
                    Point point = getCoordinateForNodeByDiagonallyNeighbors(node, result);
                    if (point != null) {
                        result.put(node, point);
                        addedNodes.put(node, point);
                        wasAdded = true;
                    }
                }
            }
        }
    }

    public void connectGraphNodesByNeighbors(HashMap<Node, Point> result) {
        HashMap<Node, Point> addedNodes = new HashMap<>();
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
                            addedNodes.put(neighborInAxeA_1, coordinateNeighbor_1);
                            wasAdded = true;
//                            System.out.println("added");
                        } else if (result.containsKey(neighborInAxeA_1) && !result.containsKey(neighborInAxeA_2)) {// nachbarn 2 ist nicht in resultat, aber nachbarn 1 schon
                            Point coordinateNeighbor_2 = new Point();
                            Point coordinateNeighbor_1 = result.get(neighborInAxeA_1);
                            Point parentCoordinate = result.get(nodeInResult);
                            coordinateNeighbor_2.x = 2 * parentCoordinate.x - coordinateNeighbor_1.x;
                            coordinateNeighbor_2.y = 2 * parentCoordinate.y - coordinateNeighbor_1.y;
                            result.put(neighborInAxeA_2, coordinateNeighbor_2);
                            addedNodes.put(neighborInAxeA_2, coordinateNeighbor_2);
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
                            addedNodes.put(neighborInAxeB_1, coordinateNeighbor_1);
                            wasAdded = true;
//                            System.out.println("added");
                        } else if (result.containsKey(neighborInAxeB_1) && !result.containsKey(neighborInAxeB_2)) {// nachbarn 2 ist nicht in resultat, aber nachbarn 1 schon
                            Point coordinateNeighbor_2 = new Point();
                            Point coordinateNeighbor_1 = result.get(neighborInAxeB_1);
                            Point parentCoordinate = result.get(nodeInResult);
                            coordinateNeighbor_2.x = 2 * parentCoordinate.x - coordinateNeighbor_1.x;
                            coordinateNeighbor_2.y = 2 * parentCoordinate.y - coordinateNeighbor_1.y;
                            result.put(neighborInAxeB_2, coordinateNeighbor_2);
                            addedNodes.put(neighborInAxeB_2, coordinateNeighbor_2);
                            wasAdded = true;
//                            System.out.println("added");
                        }
                    }
                }
            }
        }
//        return addedNodes;
    }

    public HashMap<Node, Point> getAddedNodes() {
        return addedNodes;
    }
}