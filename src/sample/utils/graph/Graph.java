package sample.utils.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by Alex on 27.01.2015.
 */
public class Graph {
    private Node root;

    public static enum NodeAxe {AXE_A, AXE_B}

    ;


    public HashSet<Node> getAllNodes() {
        if (root == null) {
            return new HashSet<>();
        }
        HashSet<Node> allNodes = new HashSet<>();
        allNodes.add(root);
        LinkedList<Node> notVisitedNodes = new LinkedList<>();
        notVisitedNodes.addAll(root.getNeighbors());
        while (!notVisitedNodes.isEmpty()) {
            Node node = notVisitedNodes.getFirst();
            allNodes.add(node);
            notVisitedNodes.removeFirst();
            for (Node neighbor : node.getNeighbors()) {
                if (!notVisitedNodes.contains(neighbor) && !allNodes.contains(neighbor)) {
                    notVisitedNodes.add(neighbor);
                }
            }
        }
        return allNodes;
    }

    public Node getNodeByCode(String code) {
        HashSet<Node> allNodes = getAllNodes();
        for (Node node : allNodes) {
            String tempCode = node.getCode();
            if (code.equals(tempCode)) {
                return node;
            }
        }
        return null;
    }

    public boolean addNodes(Node nodeA, Node nodeB, Node parent) {
        if (root == null) {// graph ist leer
            return addNodeInCaseEmptyGraph(nodeA, nodeB, parent);
        } else {
            HashSet<Node> allNodes = getAllNodes();
            Node equalsParentInGraph = NodeUtils.findEqualsNode(allNodes, parent);
            Node equalsNodeAInGraph = NodeUtils.findEqualsNode(allNodes, nodeA);
            Node equalsNodeBInGraph = NodeUtils.findEqualsNode(allNodes, nodeB);
            if (equalsParentInGraph != null) {// der node gibt es schon in graph
                HashSet<Node> neighbors = equalsParentInGraph.getNeighbors();
                Node equalsNodeAInNeighbors = NodeUtils.findEqualsNode(neighbors, nodeA);
                Node equalsNodeBInNeighbors = NodeUtils.findEqualsNode(neighbors, nodeB);
                // der equalsParent hat schon einen gleichen nachbanrn.
                //der andere equalsNeighbor is in nachbarliste von parent nicht vorhanden
                //der andere equalsNeighbor is im graph auch nicht vorhanden
                if (equalsNodeAInNeighbors != null && equalsNodeBInNeighbors == null && equalsNodeBInGraph == null) {//der parent hat schon einen gleichen nachbarn
                    Graph.NodeAxe nodeA_Axe = equalsParentInGraph.getNeighborsAxe(equalsNodeAInNeighbors);
                    if (nodeA_Axe != null) {
                        equalsParentInGraph.addNeighbor(nodeB, nodeA_Axe);
                        return true;
                    }
                }

                // der equalsParent hat schon einen gleichen nachbanrn.
                //der andere equalsNeighbor is in nachbarliste von parent nicht vorhanden
                //der andere equalsNeighbor is im graph auch nicht vorhanden
                if (equalsNodeBInNeighbors != null && equalsNodeAInNeighbors == null && equalsNodeAInGraph == null) {//der parent hat schon einen gleichen nachbarn
                    Graph.NodeAxe nodeB_Axe = equalsParentInGraph.getNeighborsAxe(equalsNodeBInNeighbors);
                    if (nodeB_Axe != null) {
                        equalsParentInGraph.addNeighbor(nodeA, nodeB_Axe);
                        return true;
                    }
                }

                // beide nachbarn sind in nachbarnliste von parent nicht vorhanden
                // eins von nachbarn ist schon in graph
                if (equalsNodeAInNeighbors == null && equalsNodeAInGraph != null && equalsNodeBInGraph == null) {
                    HashSet<Node> equalsParentNeighbors = equalsParentInGraph.getNeighbors();
                    if (!equalsParentNeighbors.isEmpty()) { // es gibt ein nachbarn in anderer axe
                        Node equalsParentNeighbor = equalsParentNeighbors.iterator().next();
                        NodeAxe nodeAxe = equalsParentInGraph.getNeighborsAxe(equalsParentNeighbor);
                        NodeAxe currentAxe = getOtherNodeAxe(nodeAxe);
                        equalsParentInGraph.addNeighbor(equalsNodeAInGraph, currentAxe);
                        equalsParentInGraph.addNeighbor(nodeB, currentAxe);
                        return true;
                    }
                }

                // beide nachbarn sind in nachbarnliste von parent nicht vorhanden
                // eins von nachbarn ist schon in graph
                if (equalsNodeBInNeighbors == null && equalsNodeBInGraph != null && equalsNodeAInGraph == null) {
                    HashSet<Node> equalsParentNeighbors = equalsParentInGraph.getNeighbors();
                    if (!equalsParentNeighbors.isEmpty()) { // es gibt ein nachbarn in anderer axe
                        Node equalsParentNeighbor = equalsParentNeighbors.iterator().next();
                        NodeAxe nodeAxe = equalsParentInGraph.getNeighborsAxe(equalsParentNeighbor);
                        NodeAxe currentAxe = getOtherNodeAxe(nodeAxe);
                        equalsParentInGraph.addNeighbor(equalsNodeBInGraph, currentAxe);
                        equalsParentInGraph.addNeighbor(nodeA, currentAxe);
                        return true;
                    }
                }

                // beide nachbarn sind schon in graph aber nicht als nachbarn von parent
                if (equalsNodeAInNeighbors == null && equalsNodeBInNeighbors == null && equalsNodeAInGraph != null && equalsNodeBInGraph != null) {
                    HashSet<Node> equalsParentNeighbors = equalsParentInGraph.getNeighbors();
                    if (equalsParentNeighbors.size() < 4) {
                        NodeAxe currentAxe = null;
                        if (!equalsParentNeighbors.isEmpty()) { // es gibt ein nachbarn in anderer axe
                            Node equalsParentNeighbor = equalsParentNeighbors.iterator().next();
                            NodeAxe nodeAxe = equalsParentInGraph.getNeighborsAxe(equalsParentNeighbor);
                            currentAxe = getOtherNodeAxe(nodeAxe);
                        }

                        if (currentAxe == null) {
                            HashSet<Node> equalsNodeAInGraphNeighbors = equalsNodeAInGraph.getNeighbors();
                            if (equalsNodeAInGraphNeighbors.size() == 3) {// es gibt genau einen freien platz
                                ArrayList<Node> neighborsInAxeA = equalsNodeAInGraph.getNeighborsByAxe(NodeAxe.AXE_A);
                                if (neighborsInAxeA.size() == 1) {
                                    currentAxe = NodeAxe.AXE_A;
                                } else {
                                    currentAxe = NodeAxe.AXE_B;
                                }
                            }
                        }

                        if (currentAxe == null) {
                            HashSet<Node> equalsNodeBInGraphNeighbors = equalsNodeBInGraph.getNeighbors();
                            if (equalsNodeBInGraphNeighbors.size() == 3) {// es gibt genau einen freien platz
                                ArrayList<Node> neighborsInAxeA = equalsNodeBInGraph.getNeighborsByAxe(NodeAxe.AXE_A);
                                if (neighborsInAxeA.size() == 1) {
                                    currentAxe = NodeAxe.AXE_A;
                                } else {
                                    currentAxe = NodeAxe.AXE_B;
                                }
                            }
                        }
                        if (currentAxe != null) {
                            equalsParentInGraph.addNeighbor(equalsNodeAInGraph, currentAxe);
                            equalsParentInGraph.addNeighbor(equalsNodeBInGraph, currentAxe);
                            return true;
                        }
                    }
                }

                //beide nachbarn sind im graph nicht vorhanden
                if (equalsNodeAInGraph == null && equalsNodeBInGraph == null) {
                    addNodeInCaseBothNodesAreNotInGraph(nodeA, nodeB, parent);
                }
            } else {//parent wurde in graph nicht gefunden
                if (equalsNodeAInGraph != null && equalsNodeAInGraph.getNeighbors().size() == 3) {// es gibt genau ein freies platz, d.h. der platz von neuem nachbarn ist eindeutig
                    ArrayList<Node> neighborsInAxeA = equalsNodeAInGraph.getNeighborsByAxe(NodeAxe.AXE_A);
                    if (neighborsInAxeA.size() == 1) {
                        equalsNodeAInGraph.addNeighbor(parent, NodeAxe.AXE_A);
                    } else {
                        equalsNodeAInGraph.addNeighbor(parent, NodeAxe.AXE_B);
                    }
                    return true;
                }

                if (equalsNodeBInGraph != null && equalsNodeBInGraph.getNeighbors().size() == 3) {// es gibt genau ein freies platz, d.h. der platz von neuem nachbarn ist eindeutig
                    ArrayList<Node> neighborsInAxeA = equalsNodeBInGraph.getNeighborsByAxe(NodeAxe.AXE_A);
                    if (neighborsInAxeA.size() == 1) {
                        equalsNodeBInGraph.addNeighbor(parent, NodeAxe.AXE_A);
                    } else {
                        equalsNodeBInGraph.addNeighbor(parent, NodeAxe.AXE_B);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean addNodeInCaseEmptyGraph(Node nodeA, Node nodeB, Node parent) {
        root = parent;
        parent.addNeighbor(nodeA, NodeAxe.AXE_A);
        parent.addNeighbor(nodeB, NodeAxe.AXE_A);
        return true;
    }

    protected void addNodeInCaseBothNodesAreNotInGraph(Node nodeA, Node nodeB, Node parent) {
        HashSet<Node> allNodes = getAllNodes();
        Node equalsParentInGraph = NodeUtils.findEqualsNode(allNodes, parent);
        HashSet<Node> neighbors = equalsParentInGraph.getNeighbors();
        if (neighbors.size() > 0) { // in einer von axen gibt es ein nachbarn
            Node oldNode = neighbors.iterator().next();
            Graph.NodeAxe oldNodeAxe = equalsParentInGraph.getNeighborsAxe(oldNode);
            Graph.NodeAxe currentAxe = getOtherNodeAxe(oldNodeAxe);
            equalsParentInGraph.addNeighbor(nodeA, currentAxe);
            equalsParentInGraph.addNeighbor(nodeB, currentAxe);
            return;
        }
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public NodeAxe getOtherNodeAxe(NodeAxe nodeAxe) {
        if (nodeAxe.equals(NodeAxe.AXE_A)) {
            return NodeAxe.AXE_B;
        } else {
            return NodeAxe.AXE_A;
        }
    }

    public ArrayList<Node> getDiagonallyNeighbors(Node node) {
        ArrayList<Node> result = new ArrayList<Node>();
        ArrayList<Node> neighborsInAxeA = node.getNeighborsByAxe(NodeAxe.AXE_A);
        ArrayList<Node> neighborsInAxeB = node.getNeighborsByAxe(NodeAxe.AXE_B);
        if (neighborsInAxeA.isEmpty() || neighborsInAxeB.isEmpty()) {
            return result; // der node hat keine nachbarn
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

        ArrayList<Node> diagonallyNeighbors_1 = NodeUtils.getJointNeighbors(neighbor_1_A, neighbor_1_B);
        if (diagonallyNeighbors_1 != null) {
            diagonallyNeighbors_1.remove(node);
            result.addAll(diagonallyNeighbors_1);
        }

        ArrayList<Node> diagonallyNeighbors_2 = NodeUtils.getJointNeighbors(neighbor_1_A, neighbor_2_B);
        if (diagonallyNeighbors_2 != null) {
            diagonallyNeighbors_2.remove(node);
            result.addAll(diagonallyNeighbors_2);
        }

        ArrayList<Node> diagonallyNeighbors_3 = NodeUtils.getJointNeighbors(neighbor_2_A, neighbor_1_B);
        if (diagonallyNeighbors_3 != null) {
            diagonallyNeighbors_3.remove(node);
            result.addAll(diagonallyNeighbors_3);
        }

        ArrayList<Node> diagonallyNeighbors_4 = NodeUtils.getJointNeighbors(neighbor_2_A, neighbor_2_B);
        if (diagonallyNeighbors_4 != null) {
            diagonallyNeighbors_4.remove(node);
            result.addAll(diagonallyNeighbors_4);
        }

        return result;
    }


}
