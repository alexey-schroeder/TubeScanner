package sample.test;

import org.testng.Assert;
import org.testng.annotations.Test;
import sample.utils.graph.Graph;
import sample.utils.graph.Node;

import static org.testng.Assert.*;

public class GraphTest {

    // A - C - B
    @Test
    public void testAddNodes_1() throws Exception {
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");
        Graph graph = new Graph();
        graph.addNodes(nodeA, nodeB, nodeC);
        
        Assert.assertEquals(true, graph.getRoot().equals(graph.getNodeByCode("C")));
        Assert.assertEquals(2, graph.getNodeByCode("C").getNeighbors().size());
        Assert.assertEquals(1, graph.getNodeByCode("A").getNeighbors().size());
        Assert.assertEquals(1, graph.getNodeByCode("B").getNeighbors().size());
        Assert.assertEquals(true, graph.getNodeByCode("C").getNeighbors().contains(graph.getNodeByCode("A")));
        Assert.assertEquals(true, graph.getNodeByCode("C").getNeighbors().contains(graph.getNodeByCode("B")));
        Assert.assertEquals(true, graph.getNodeByCode("A").getNeighbors().contains(graph.getNodeByCode("C")));
        Assert.assertEquals(true, graph.getNodeByCode("B").getNeighbors().contains(graph.getNodeByCode("C")));
        Assert.assertEquals(false, graph.getNodeByCode("A").getNeighbors().contains(graph.getNodeByCode("B")));
        Assert.assertEquals(false, graph.getNodeByCode("B").getNeighbors().contains(graph.getNodeByCode("A")));
    }

    //    D
    //    |
    //    A - C - B
    //    |
    //    E
    @Test
    public void testAddNodes_2() throws Exception {
        Graph graph = new Graph();
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");

        graph.addNodes(nodeA, nodeB, nodeC);// erster frame

        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        nodeA = new Node("A");
        graph.addNodes(nodeD, nodeE, nodeA); // zweiter frame

        Assert.assertEquals(3, graph.getNodeByCode("A").getNeighbors().size());
        Assert.assertEquals(1, graph.getNodeByCode("D").getNeighbors().size());
        Assert.assertEquals(1, graph.getNodeByCode("E").getNeighbors().size());
        Assert.assertEquals(true, graph.getNodeByCode("A").getNeighbors().contains(graph.getNodeByCode("C")));
        Assert.assertEquals(true, graph.getNodeByCode("A").getNeighbors().contains(graph.getNodeByCode("D")));
        Assert.assertEquals(true, graph.getNodeByCode("A").getNeighbors().contains(graph.getNodeByCode("E")));
        Assert.assertEquals(true, graph.getNodeByCode("E").getNeighbors().contains(graph.getNodeByCode("A")));
        Assert.assertEquals(true, graph.getNodeByCode("D").getNeighbors().contains(graph.getNodeByCode("A")));
        Assert.assertEquals(false, graph.getNodeByCode("D").getNeighbors().contains(graph.getNodeByCode("E")));
        Assert.assertEquals(false, graph.getNodeByCode("E").getNeighbors().contains(graph.getNodeByCode("D")));
    }

    //      D
    //      |
    //  F - A - C - B
    //      |
    //      E
    @Test
    public void testAddNodes_3() throws Exception {
        Graph graph = new Graph();
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");

        graph.addNodes(nodeA, nodeB, nodeC);

        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        nodeA = new Node("A");
        graph.addNodes(nodeD, nodeE, nodeA);

        Node nodeF = new Node("F");
        nodeA = new Node("A");
        nodeC = new Node("C");
        graph.addNodes(nodeF, nodeC, nodeA);

        Assert.assertEquals(4, graph.getNodeByCode("A").getNeighbors().size());
        Assert.assertEquals(1, graph.getNodeByCode("F").getNeighbors().size());
        Assert.assertEquals(1, graph.getNodeByCode("E").getNeighbors().size());
        Assert.assertEquals(true, graph.getNodeByCode("A").getNeighbors().contains(graph.getNodeByCode("C")));
        Assert.assertEquals(true, graph.getNodeByCode("A").getNeighbors().contains(graph.getNodeByCode("D")));
        Assert.assertEquals(true, graph.getNodeByCode("A").getNeighbors().contains(graph.getNodeByCode("E")));
        Assert.assertEquals(true, graph.getNodeByCode("A").getNeighbors().contains(graph.getNodeByCode("F")));
        Assert.assertEquals(true, graph.getNodeByCode("F").getNeighbors().contains(graph.getNodeByCode("A")));
        Assert.assertEquals(false, graph.getNodeByCode("D").getNeighbors().contains(graph.getNodeByCode("F")));
        Assert.assertEquals(false, graph.getNodeByCode("E").getNeighbors().contains(graph.getNodeByCode("F")));
        Assert.assertEquals(false, graph.getNodeByCode("C").getNeighbors().contains(graph.getNodeByCode("F")));
    }

    //  K   D
    //  |   |
    //  F - A - C - B
    //  |   |
    //  L   E
    @Test
    public void testAddNodes_4() throws Exception {
        Graph graph = new Graph();
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");

        graph.addNodes(nodeA, nodeB, nodeC);

        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        nodeA = new Node("A");
        graph.addNodes(nodeD, nodeE, nodeA);

        Node nodeF = new Node("F");
        nodeA = new Node("A");
        nodeC = new Node("C");
        graph.addNodes(nodeF, nodeC, nodeA);

        Node nodeK = new Node("K");
        Node nodeL = new Node("L");
        nodeF = new Node("F");
        graph.addNodes(nodeK, nodeL, nodeF);

        Assert.assertEquals(3, graph.getNodeByCode("F").getNeighbors().size());
        Assert.assertEquals(1, graph.getNodeByCode("K").getNeighbors().size());
        Assert.assertEquals(1, graph.getNodeByCode("L").getNeighbors().size());
        Assert.assertEquals(true, graph.getNodeByCode("F").getNeighbors().contains(graph.getNodeByCode("A")));
        Assert.assertEquals(true, graph.getNodeByCode("F").getNeighbors().contains(graph.getNodeByCode("K")));
        Assert.assertEquals(true, graph.getNodeByCode("F").getNeighbors().contains(graph.getNodeByCode("L")));
        Assert.assertEquals(true, graph.getNodeByCode("K").getNeighbors().contains(graph.getNodeByCode("F")));
        Assert.assertEquals(true, graph.getNodeByCode("L").getNeighbors().contains(graph.getNodeByCode("F")));
        Assert.assertEquals(false, graph.getNodeByCode("K").getNeighbors().contains(graph.getNodeByCode("L")));
        Assert.assertEquals(false, graph.getNodeByCode("L").getNeighbors().contains(graph.getNodeByCode("K")));
    }

    //  K - D - M
    //  |   |
    //  F - A - C - B
    //  |   |
    //  L - E - R
    @Test
    public void testAddNodes_5() throws Exception {
        Graph graph = new Graph();
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");

        graph.addNodes(nodeA, nodeB, nodeC);

        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        graph.addNodes(nodeD, nodeE, nodeA);

        Node nodeF = new Node("F");
        nodeA = new Node("A");
        nodeC = new Node("C");
        graph.addNodes(nodeF, nodeC, nodeA);

        Node nodeK = new Node("K");
        Node nodeL = new Node("L");
        nodeF = new Node("F");
        graph.addNodes(nodeK, nodeL, nodeF);

        Node nodeM = new Node("M");
        nodeK = new Node("K");
        nodeD = new Node("D");
        graph.addNodes(nodeK, nodeM, nodeD);

        Assert.assertEquals(3, graph.getNodeByCode("D").getNeighbors().size());
        Assert.assertEquals(2, graph.getNodeByCode("K").getNeighbors().size());
        Assert.assertEquals(1, graph.getNodeByCode("M").getNeighbors().size());
        Assert.assertEquals(true, graph.getNodeByCode("D").getNeighbors().contains(graph.getNodeByCode("A")));
        Assert.assertEquals(true, graph.getNodeByCode("D").getNeighbors().contains(graph.getNodeByCode("K")));
        Assert.assertEquals(true, graph.getNodeByCode("D").getNeighbors().contains(graph.getNodeByCode("M")));
        Assert.assertEquals(true, graph.getNodeByCode("K").getNeighbors().contains(graph.getNodeByCode("D")));
        Assert.assertEquals(true, graph.getNodeByCode("M").getNeighbors().contains(graph.getNodeByCode("D")));
        Assert.assertEquals(false, graph.getNodeByCode("K").getNeighbors().contains(graph.getNodeByCode("M")));
        Assert.assertEquals(false, graph.getNodeByCode("M").getNeighbors().contains(graph.getNodeByCode("K")));

        Node nodeR = new Node("R");
        nodeL = new Node("L");
        nodeE = new Node("E");
        graph.addNodes(nodeL, nodeR, nodeE);

        Assert.assertEquals(3, graph.getNodeByCode("E").getNeighbors().size());
        Assert.assertEquals(2, graph.getNodeByCode("L").getNeighbors().size());
        Assert.assertEquals(1, graph.getNodeByCode("R").getNeighbors().size());
        Assert.assertEquals(true, graph.getNodeByCode("E").getNeighbors().contains(graph.getNodeByCode("A")));
        Assert.assertEquals(true, graph.getNodeByCode("E").getNeighbors().contains(graph.getNodeByCode("L")));
        Assert.assertEquals(true, graph.getNodeByCode("E").getNeighbors().contains(graph.getNodeByCode("R")));
        Assert.assertEquals(true, graph.getNodeByCode("L").getNeighbors().contains(graph.getNodeByCode("E")));
        Assert.assertEquals(true, graph.getNodeByCode("R").getNeighbors().contains(graph.getNodeByCode("E")));
        Assert.assertEquals(false, graph.getNodeByCode("L").getNeighbors().contains(graph.getNodeByCode("R")));
        Assert.assertEquals(false, graph.getNodeByCode("R").getNeighbors().contains(graph.getNodeByCode("L")));
    }

    //  K - D - M
    //  |   |   |
    //  F - A - C - B
    //  |   |   |
    //  L - E - R
    @Test
    public void testAddNodes_6() throws Exception {
        Graph graph = new Graph();
        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");

        graph.addNodes(nodeA, nodeB, nodeC);

        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        nodeA = new Node("A");
        graph.addNodes(nodeD, nodeE, nodeA);

        Node nodeF = new Node("F");
        nodeA = new Node("A");
        nodeC = new Node("C");
        graph.addNodes(nodeF, nodeC, nodeA);

        Node nodeK = new Node("K");
        Node nodeL = new Node("L");
        nodeF = new Node("F");
        graph.addNodes(nodeK, nodeL, nodeF);

        Node nodeM = new Node("M");
        nodeD = new Node("D");
        nodeK = new Node("K");
        graph.addNodes(nodeK, nodeM, nodeD);



        Node nodeR = new Node("R");
        nodeL = new Node("L");
        nodeE = new Node("E");
        graph.addNodes(nodeL, nodeR, nodeE);

        nodeR = new Node("R");
        nodeM = new Node("M");
        nodeC = new Node("C");
        graph.addNodes(nodeM, nodeR, nodeC);

        Assert.assertEquals(4, graph.getNodeByCode("C").getNeighbors().size());
        Assert.assertEquals(2, graph.getNodeByCode("M").getNeighbors().size());
        Assert.assertEquals(2, graph.getNodeByCode("R").getNeighbors().size());
        Assert.assertEquals(true, graph.getNodeByCode("C").getNeighbors().contains(graph.getNodeByCode("A")));
        Assert.assertEquals(true, graph.getNodeByCode("C").getNeighbors().contains(graph.getNodeByCode("B")));
        Assert.assertEquals(true, graph.getNodeByCode("C").getNeighbors().contains(graph.getNodeByCode("M")));
        Assert.assertEquals(true, graph.getNodeByCode("C").getNeighbors().contains(graph.getNodeByCode("R")));
        Assert.assertEquals(true, graph.getNodeByCode("M").getNeighbors().contains(graph.getNodeByCode("C")));
        Assert.assertEquals(true, graph.getNodeByCode("R").getNeighbors().contains(graph.getNodeByCode("C")));
        Assert.assertEquals(false, graph.getNodeByCode("M").getNeighbors().contains(graph.getNodeByCode("R")));
        Assert.assertEquals(false, graph.getNodeByCode("R").getNeighbors().contains(graph.getNodeByCode("M")));
    }
}