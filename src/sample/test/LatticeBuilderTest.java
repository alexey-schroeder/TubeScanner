package sample.test;

import org.opencv.core.Point;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import sample.utils.graph.Graph;
import sample.utils.graph.LatticeBuilder;
import sample.utils.graph.Node;
import sample.utils.graph.NodeUtils;

import java.util.HashMap;
import java.util.HashSet;

import static org.testng.Assert.*;

public class LatticeBuilderTest {
    private Graph graph;

    //  K - D - M
    //  |   |   |
    //  F - A - C - B - Z
    //  |   |   |
    //  L - E - R
    //      |
    //      W
    //      |
    //      U
    @BeforeClass
    public void initGraph() {
        graph = new Graph();

        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");
        Assert.assertTrue(graph.addNodes(nodeA, nodeB, nodeC));

        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        nodeA = new Node("A");
        Assert.assertTrue(graph.addNodes(nodeD, nodeE, nodeA));

        Node nodeF = new Node("F");
        nodeA = new Node("A");
        nodeC = new Node("C");
        Assert.assertTrue(graph.addNodes(nodeF, nodeC, nodeA));

        Node nodeK = new Node("K");
        Node nodeL = new Node("L");
        nodeF = new Node("F");
        Assert.assertTrue(graph.addNodes(nodeK, nodeL, nodeF));

        Node nodeM = new Node("M");
        nodeD = new Node("D");
        nodeK = new Node("K");
        Assert.assertTrue(graph.addNodes(nodeK, nodeM, nodeD));


        Node nodeR = new Node("R");
        nodeL = new Node("L");
        nodeE = new Node("E");
        Assert.assertTrue(graph.addNodes(nodeL, nodeR, nodeE));

        nodeR = new Node("R");
        nodeM = new Node("M");
        nodeC = new Node("C");
        Assert.assertTrue(graph.addNodes(nodeM, nodeR, nodeC));

        Node nodeW = new Node("W");
        nodeE = new Node("E");
        nodeA = new Node("A");
        Assert.assertTrue(graph.addNodes(nodeA, nodeW, nodeE));

        Node nodeU = new Node("U");
        nodeW = new Node("W");
        nodeE = new Node("E");
        Assert.assertTrue(graph.addNodes(nodeE, nodeU, nodeW));

        Node nodeZ = new Node("Z");
        nodeB = new Node("B");
        nodeC = new Node("C");
        Assert.assertTrue(graph.addNodes(nodeC, nodeZ, nodeB));
    }

    @Test
    public void testCalculateNodeCoordinates() throws Exception {

    }

    @Test
    public void testGraphToPoints() throws Exception {

    }

    @Test
    public void testGetCoordinateForNodeByNeighbors() throws Exception {
        HashSet<Node> allNodes = graph.getAllNodes();
        Node nodeA = NodeUtils.findEqualsNode(allNodes, new Node("A"));
        Node nodeE = NodeUtils.findEqualsNode(allNodes, new Node("E"));
        Node nodeC = NodeUtils.findEqualsNode(allNodes, new Node("C"));
        Node nodeR = NodeUtils.findEqualsNode(allNodes, new Node("R"));

        LatticeBuilder latticeBuilder = new LatticeBuilder(graph);
        HashMap<Node, Point> result = new HashMap<>();
        Point point1 = new Point(1, 1);
        Point point2 = new Point(2, 1);
        Point point3 = new Point(2, 2);
        Point point4 = new Point(1, 2);
        result.put(nodeR, point2);
        result.put(nodeC, point3);
        result.put(nodeE, point1);

        Point point = latticeBuilder.getCoordinateForNodeByNeighbors(nodeA, nodeC, nodeE, result);
        Assert.assertEquals(point, point4);

        result.remove(nodeE);
        result.put(nodeA, point4);
        point = latticeBuilder.getCoordinateForNodeByNeighbors(nodeE, nodeA, nodeR, result);
        Assert.assertEquals(point, point1);
    }

    @Test
    public void testCalculateCoordinateInStraightLine() throws Exception {
        LatticeBuilder latticeBuilder = new LatticeBuilder(graph);
        HashMap<Node, Point> result = new HashMap<>();
        // in vertikaler richtung
        Node referencePoint = NodeUtils.findEqualsNode(graph.getAllNodes(), new Node("A"));
        Node lastNeighbor = NodeUtils.findEqualsNode(graph.getAllNodes(), new Node("D"));
        Point referencePointCoordinate = new Point(1, 1);
        Point lastNeighborPointCoordinate = new Point(0, 2);
        latticeBuilder.calculateCoordinateInStraightLine(result, referencePoint, lastNeighbor, referencePointCoordinate, lastNeighborPointCoordinate);
        Assert.assertEquals(3, result.size());

        Node nodeE = new Node("E");
        Node nodeW = new Node("W");
        Node nodeU = new Node("U");
        Assert.assertTrue(result.containsKey(nodeE));
        Assert.assertTrue(result.containsKey(nodeW));
        Assert.assertTrue(result.containsKey(nodeU));

        Assert.assertEquals(new Point(2, 0), result.get(nodeE));
        Assert.assertEquals(new Point(3, -1), result.get(nodeW));
        Assert.assertEquals(new Point(4, -2), result.get(nodeU));

        // in horizontaler richtung
        result = new HashMap<>();
        referencePoint = NodeUtils.findEqualsNode(graph.getAllNodes(), new Node("A"));
        lastNeighbor = NodeUtils.findEqualsNode(graph.getAllNodes(), new Node("F"));
        referencePointCoordinate = new Point(1, 1);
        lastNeighborPointCoordinate = new Point(0, 0);
        latticeBuilder.calculateCoordinateInStraightLine(result, referencePoint, lastNeighbor, referencePointCoordinate, lastNeighborPointCoordinate);
        Assert.assertEquals(3, result.size());

        Node nodeC = new Node("C");
        Node nodeB = new Node("B");
        Node nodeZ = new Node("Z");
        Assert.assertTrue(result.containsKey(nodeC));
        Assert.assertTrue(result.containsKey(nodeB));
        Assert.assertTrue(result.containsKey(nodeZ));

        Assert.assertEquals(new Point(2, 2), result.get(nodeC));
        Assert.assertEquals(new Point(3, 3), result.get(nodeB));
        Assert.assertEquals(new Point(4, 4), result.get(nodeZ));
    }

    @Test
    public void testGetCoordinateForNode() throws Exception {

    }

    @Test
    public void testCorrectNodeCoordinates() throws Exception {

    }

    @Test
    public void testCorrectNodeCoordinatesByNeighbors() throws Exception {

    }

    @Test
    public void testCalculateCoordinateByNeighbors() throws Exception {
        HashSet<Node> allNodes = graph.getAllNodes();
        LatticeBuilder latticeBuilder = new LatticeBuilder(graph);
        Node nodeM = NodeUtils.findEqualsNode(allNodes, new Node("M"));
        Node nodeA = NodeUtils.findEqualsNode(allNodes, new Node("A"));
        Node nodeD = NodeUtils.findEqualsNode(allNodes, new Node("D"));
        HashMap<Node, Point> result = new HashMap<>();
        Point point1 = new Point(1, 1);
        Point point2 = new Point(2, 1);
        Point point3 = new Point(2, 2);
        Point point4 = new Point(1, 2);
        result.put(nodeM, point3);
        result.put(nodeD, point4);
        result.put(nodeA, point1);
        latticeBuilder.calculateCoordinateByNeighbors(result);
        System.out.println(result.size());
    }
}