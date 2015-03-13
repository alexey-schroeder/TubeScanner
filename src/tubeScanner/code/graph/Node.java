package tubeScanner.code.graph;

import tubeScanner.code.utils.NodeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Alex on 27.01.2015.
 */
public class Node {
    private HashSet<Node> neighbors;
    private HashMap<Node, Graph.NodeAxe> nodeToAxeMap;
    private String code;

    public Node() {
        neighbors = new HashSet<>(4);
        nodeToAxeMap = new HashMap<>(4);
    }

    public Node(String code) {
        this();
        this.code = code;
    }

    public Node getNeighborByCode(String code) {
        for (Node node : neighbors) {
            if (node.code.equals(code)) {
                return node;
            }
        }
        return null;
    }

    public Node getOppositeNeighbor(Node node) {
        Graph.NodeAxe axe = nodeToAxeMap.get(node);
        if (axe == null) {
            return null;
        }
        for (Node tempNode : nodeToAxeMap.keySet()) {
            Graph.NodeAxe tempAxe = nodeToAxeMap.get(tempNode);
            if (tempAxe.equals(axe) && !tempNode.equals(node)) {
                return tempNode;
            }
        }
        return null;
    }

    public boolean hasNodeAsNeighbor(Node node) {
        return neighbors.contains(node);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean addNeighbor(Node node, Graph.NodeAxe axe) {
        if (node == null) {
            throw new RuntimeException("neighbor node can not be null!");
        }
        if (neighbors.size() == 4) {
            String text = "The node has 4 neighbors yet!" + System.lineSeparator()
                    + this + System.lineSeparator()
                    + " the node are try to add in axe "+ axe +": " + node;

            throw new RuntimeException(text);
        }

        if (getNeighborsByAxe(axe).size() > 1) {
            String text = "There are always 2 node in the axe "  + axe + "!" + System.lineSeparator()
                    + this + System.lineSeparator()
                    + " the node are try to add in axe "+ axe +": " + node;
            throw new RuntimeException(text);
        }
        if (!neighbors.contains(node)) {
            neighbors.add(node);
            nodeToAxeMap.put(node, axe);
            if (!node.hasNodeAsNeighbor(this)) {
                node.addNeighbor(this, axe);
            }
            return true;
        }

        return false;
    }

    public ArrayList<Node> getNeighborsByAxe(Graph.NodeAxe axe) {
        ArrayList<Node> result = new ArrayList<>();
        for (Node node : nodeToAxeMap.keySet()) {
            Graph.NodeAxe tempAxe = nodeToAxeMap.get(node);
            if (tempAxe.equals(axe)) {
                result.add(node);
            }
        }
        return result;
    }

    public boolean replaceNeighbor(Node node) {
        Node neighbor = NodeUtils.findEqualsNode(neighbors, node);
        if (neighbor != null) {
            Graph.NodeAxe axe = nodeToAxeMap.get(neighbor);
            neighbors.remove(neighbor);
            neighbors.add(node);
            nodeToAxeMap.remove(neighbor);
            nodeToAxeMap.put(neighbor, axe);
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return code.equals(node.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    public HashSet<Node> getNeighbors() {
        return new HashSet<Node>(neighbors);
    }

    public Graph.NodeAxe getNeighborsAxe(Node neighbor) {
        ArrayList<Node> neighborsInAxeA = getNeighborsByAxe(Graph.NodeAxe.AXE_A);
        if (neighborsInAxeA.contains(neighbor)) {
            return Graph.NodeAxe.AXE_A;
        } else {
            ArrayList<Node> neighborsInAxeB = getNeighborsByAxe(Graph.NodeAxe.AXE_B);
            if (neighborsInAxeB.contains(neighbor)) {
                return Graph.NodeAxe.AXE_B;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        ArrayList<Node> neighborsInAxeA = getNeighborsByAxe(Graph.NodeAxe.AXE_A);
        String neighborsInAxeAasString = "";
        for (Node node : neighborsInAxeA) {
            neighborsInAxeAasString = neighborsInAxeAasString + ", " + node.getCode();
        }
        neighborsInAxeAasString = neighborsInAxeAasString.replaceFirst(", ", "");

        ArrayList<Node> neighborsInAxeB = getNeighborsByAxe(Graph.NodeAxe.AXE_B);
        String neighborsInAxeBasString = "";
        for (Node node : neighborsInAxeB) {
            neighborsInAxeBasString = neighborsInAxeBasString + ", " + node.getCode();
        }
        neighborsInAxeBasString = neighborsInAxeBasString.replaceFirst(", ", "");
        return "Node{" +
                "code='" + code + '\'' +
                " neighbors in axe A = " + neighborsInAxeAasString +
                " neighbors in axe B = " + neighborsInAxeBasString +
                '}';
    }
}
