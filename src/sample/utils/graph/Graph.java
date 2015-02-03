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
//
//    public static Graph merge(Graph graphA, Graph graphB) {
//        HashSet<Node> NodesOfGraphA = graphA.getAllNodes();
//        HashSet<Node> NodesOfGraphB = graphB.getAllNodes();
//        for ()
//    }

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

//    public boolean addNodes(ArrayList<Node> newNodes) {
//        boolean result = false;
//        HashSet<Node> oldNodes = getAllNodes();
//        ArrayList<Node> addedNodes = new ArrayList<>();
//        for (Node oldNode : oldNodes) {
//            for (Node newNode : newNodes) {
//                if (newNode.equals(oldNode)) {
//                    result = true;
//                    HashSet<Node> oldNodeNeighbors = oldNode.getNeighbors();
//                    HashSet<Node> newNodeNeighbors = newNode.getNeighbors();
//                    for (Node newNodeNeighbor : newNodeNeighbors) {
//                        if (!oldNodeNeighbors.contains(newNodeNeighbor)) {
//                            Node newNodeNeighborCopy = new Node();
//                            newNodeNeighborCopy.setCode(newNodeNeighbor.getCode());
//                            oldNode.addNeighbor(newNodeNeighborCopy);
//                            newNodeNeighborCopy.addNeighbor(oldNode);
//                            addedNodes.add(newNodeNeighbor);
//                        }
//                    }
//                }
//            }
//        }
//        if (!addedNodes.isEmpty()) {
//            addNodes(addedNodes);
//        }
//        return result;
//    }

    public void addNode(Node parent, Node child, NodeAxe axe) {
        if (root == null) {
            root = parent;
        }
        parent.addNeighbor(child, axe);
    }

    public void addNodes(Node nodeA, Node nodeB, Node parent) {
        if (root == null) {// graph ist leer
            root = parent;
            parent.addNeighbor(nodeA, NodeAxe.AXE_A);
            parent.addNeighbor(nodeB, NodeAxe.AXE_A);
        } else {
            HashSet<Node> allNodes = getAllNodes();
            Node equalsParent = Utils.findEqualsNode(allNodes, parent);
            if (equalsParent != null) {// der node gibt es schon in graph
                HashSet<Node> neighbors = equalsParent.getNeighbors();
                Node equalsNodeA = Utils.findEqualsNode(neighbors, nodeA);
                if (equalsNodeA != null) {//der parent hat schon einen gleichen nachbarn
                    Graph.NodeAxe nodeA_Axe = equalsParent.getNeighborsAxe(equalsNodeA);
                    if (nodeA_Axe != null) {
                        equalsParent.addNeighbor(nodeB, nodeA_Axe);
                    }
                } else {
                    Node equalsNodeB = Utils.findEqualsNode(neighbors, nodeB);
                    if (equalsNodeB != null) {//der parent hat schon einen gleichen nachbarn
                        Graph.NodeAxe nodeB_Axe = equalsParent.getNeighborsAxe(equalsNodeB);
                        if (nodeB_Axe != null) {
                            equalsParent.addNeighbor(nodeA, nodeB_Axe);
                        }
                    }
                }
            } else {//parent wurde in graph nicht gefunden
                Node equalsNodeA = Utils.findEqualsNode(allNodes, nodeA);
                if(equalsNodeA != null && equalsNodeA.getNeighbors().size() == 3){// es gibt genau ein freies platz, d.h. der platz von neuem nachbarn ist eindeutig
                    ArrayList<Node> neighborsInAxeA = equalsNodeA.getNeighborsByAxe(NodeAxe.AXE_A);
                    if(neighborsInAxeA.size() == 1){
                        equalsNodeA.addNeighbor(parent, NodeAxe.AXE_A);
                    } else {
                        equalsNodeA.addNeighbor(parent, NodeAxe.AXE_B);
                    }
                } else {
                    Node equalsNodeB = Utils.findEqualsNode(allNodes, nodeB);
                    if(equalsNodeB != null && equalsNodeB.getNeighbors().size() == 3){// es gibt genau ein freies platz, d.h. der platz von neuem nachbarn ist eindeutig
                        ArrayList<Node> neighborsInAxeA = equalsNodeB.getNeighborsByAxe(NodeAxe.AXE_A);
                        if(neighborsInAxeA.size() == 1){
                            equalsNodeB.addNeighbor(parent, NodeAxe.AXE_A);
                        } else {
                            equalsNodeB.addNeighbor(parent, NodeAxe.AXE_B);
                        }
                    }
                }
            }
        }
    }

    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }
}
