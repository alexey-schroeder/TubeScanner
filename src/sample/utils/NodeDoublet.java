package sample.utils;

import sample.utils.graph.Node;

/**
 * Created by Alex on 13.03.2015.
 */
public class NodeDoublet {
    private Node nodeA;
    private Node nodeB;

    public NodeDoublet(Node nodeA, Node nodeB) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
    }

    public Node getNodeA() {
        return nodeA;
    }

    public void setNodeA(Node nodeA) {
        this.nodeA = nodeA;
    }

    public Node getNodeB() {
        return nodeB;
    }

    public void setNodeB(Node nodeB) {
        this.nodeB = nodeB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeDoublet that = (NodeDoublet) o;

        if (!nodeA.equals(that.nodeA) && !nodeA.equals(that.nodeB)) return false;
        if (!nodeB.equals(that.nodeB) && !nodeB.equals(that.nodeA)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return nodeA.hashCode() + nodeB.hashCode();
    }
}
