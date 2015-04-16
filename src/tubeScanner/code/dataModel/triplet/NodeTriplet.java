package tubeScanner.code.dataModel.triplet;

import org.opencv.core.Point;
import tubeScanner.code.graph.Node;

import java.util.HashMap;

/**
 * Created by Alex on 11.02.2015.
 */
public class NodeTriplet {
    private Node nodeA;
    private Node nodeB;
    private Node center;

    public NodeTriplet(PointTriplet pointTriplet, HashMap<Point, String> codes) {
        Point pointA = pointTriplet.getPointA();
        Point pointB = pointTriplet.getPointB();
        Point pointCenter = pointTriplet.getCenter();

        nodeA = new Node(codes.get(pointA));
        nodeB = new Node(codes.get(pointB));
        center = new Node(codes.get(pointCenter));
    }

    public NodeTriplet(Node nodeA, Node nodeB, Node center) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
        this.center = center;
    }

    public NodeTriplet copy(){
        String nodeACode = nodeA.getCode();
        String nodeBCode = nodeB.getCode();
        String centerCode =center.getCode();
        NodeTriplet copyTriplet = new NodeTriplet(new Node(nodeACode), new Node(nodeBCode), new Node(centerCode));
        return copyTriplet;
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

    public Node getCenter() {
        return center;
    }

    public void setCenter(Node center) {
        this.center = center;
    }
}
