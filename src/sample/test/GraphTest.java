package sample.test;

import org.testng.Assert;
import org.testng.annotations.Test;
import sample.utils.graph.Graph;
import sample.utils.graph.Node;

import static org.testng.Assert.*;

public class GraphTest {

    @Test
    public void testAddNodes() throws Exception {
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");
        Graph graph = new Graph();
        graph.addNodes(nodeA, nodeB, nodeC);
        Assert.assertEquals(true, graph.getRoot().equals(nodeC));
        Assert.assertEquals(2, nodeC.getNeighbors().size());
        Assert.assertEquals(1, nodeA.getNeighbors().size());
        Assert.assertEquals(1, nodeB.getNeighbors().size());
        Assert.assertEquals(true, nodeC.getNeighbors().contains(nodeA));
        Assert.assertEquals(true, nodeC.getNeighbors().contains(nodeB));
        Assert.assertEquals(true, nodeA.getNeighbors().contains(nodeC));
        Assert.assertEquals(true, nodeB.getNeighbors().contains(nodeC));
        Assert.assertEquals(false, nodeA.getNeighbors().contains(nodeB));
        Assert.assertEquals(false, nodeB.getNeighbors().contains(nodeA));
    }
}