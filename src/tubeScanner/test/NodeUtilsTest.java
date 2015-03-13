package tubeScanner.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import tubeScanner.code.graph.Graph;
import tubeScanner.code.graph.Node;
import tubeScanner.code.utils.NodeUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class NodeUtilsTest {

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
    public void testGetJointNeighbors() throws Exception {
        HashSet<Node> allNodes = graph.getAllNodes();

        Node nodeA = NodeUtils.findEqualsNode(allNodes, new Node("A"));
        Node nodeR = NodeUtils.findEqualsNode(allNodes, new Node("R"));
        ArrayList<Node> joinNeighbors = NodeUtils.getJointNeighbors(nodeA, nodeR);
        Assert.assertEquals(2, joinNeighbors.size());
        Assert.assertTrue(joinNeighbors.contains(new Node("C")));
        Assert.assertTrue(joinNeighbors.contains(new Node("E")));

        Node nodeE = NodeUtils.findEqualsNode(allNodes, new Node("E"));
        joinNeighbors = NodeUtils.getJointNeighbors(nodeA, nodeE);
        Assert.assertEquals(0, joinNeighbors.size());
    }
}