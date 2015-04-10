package tubeScanner.code.graph;

import org.opencv.core.Point;
import tubeScanner.code.basis.Basis;
import tubeScanner.code.basis.BasisFinder;
import tubeScanner.code.dataModel.triplet.NodeTriplet;
import tubeScanner.code.utils.NodeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Alex on 10.04.2015.
 */
public class GraphNodesConnector {
    private Graph graph;

    public void connectGraphByBaseis(HashMap<Point, Node> points, Point[] cellVectors) {
        HashSet<Node> allNodes = graph.getAllNodes();
        BasisFinder basisFinder = new BasisFinder();
        ArrayList<Basis> bases = basisFinder.findBases(allNodes, points, cellVectors);
        boolean isGraphChanged = true;
        while (isGraphChanged) {
            isGraphChanged = false;
            for (Basis basis : bases) {
                boolean connected = connectGraphNodesByBasis(basis.getNodeBasis(), allNodes);
                if(connected){
                    isGraphChanged = true;
                }
            }
        }
    }

    public boolean connectGraphNodesByBasis(NodeTriplet nodeTriplet, HashSet<Node> allNodes) {
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
            if (isNodeAContains && isNodeBContains || !isNodeAContains && !isNodeBContains) {//beide nodes aus triplet sind entweder nachbarn oder nicht nachbarn
                return false; // wir können nichts machen
            } else {
                if (isNodeAContains) {// nodeA ist als nachbar bekannt
                    Graph.NodeAxe axe = equalsNodeCenter.getNeighborsAxe(equalsNodeA);
                    Graph.NodeAxe otherAxe = Graph.getOtherNodeAxe(axe);
                    equalsNodeCenter.addNeighbor(equalsNodeB, otherAxe);
                    return  true;
                } else {// nodeB ist als nachbar bekannt
                    Graph.NodeAxe axe = equalsNodeCenter.getNeighborsAxe(equalsNodeB);
                    Graph.NodeAxe otherAxe = Graph.getOtherNodeAxe(axe);
                    equalsNodeCenter.addNeighbor(equalsNodeA, otherAxe);
                    return true;
                }
            }
        }
        return false;
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

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
}
