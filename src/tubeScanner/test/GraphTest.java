package tubeScanner.test;

import org.testng.Assert;
import org.testng.annotations.Test;
import tubeScanner.code.graph.Graph;
import tubeScanner.code.graph.Node;

import java.util.ArrayList;

public class GraphTest {

    // A - C - B
    @Test
    public void testAddNodes_1() throws Exception {
        Graph graph = new Graph();

        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");
        Assert.assertTrue(graph.addNodes(nodeA, nodeB, nodeC));

        nodeA = graph.getNodeByCode("A");
        nodeB = graph.getNodeByCode("B");
        nodeC = graph.getNodeByCode("C");

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
        Assert.assertEquals(true, nodeC.getOppositeNeighbor(nodeB).equals(nodeA));
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
        Assert.assertTrue(graph.addNodes(nodeA, nodeB, nodeC));// erster frame

        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        nodeA = new Node("A");
        Assert.assertTrue(graph.addNodes(nodeD, nodeE, nodeA)); // zweiter frame

        nodeA = graph.getNodeByCode("A");
        nodeE = graph.getNodeByCode("E");
        nodeC = graph.getNodeByCode("C");
        nodeD = graph.getNodeByCode("D");

        Assert.assertEquals(3, nodeA.getNeighbors().size());
        Assert.assertEquals(1, nodeD.getNeighbors().size());
        Assert.assertEquals(1, nodeE.getNeighbors().size());
        Assert.assertEquals(true, nodeA.getNeighbors().contains(nodeC));
        Assert.assertEquals(true, nodeA.getNeighbors().contains(nodeD));
        Assert.assertEquals(true, nodeA.getNeighbors().contains(nodeE));
        Assert.assertEquals(true, nodeE.getNeighbors().contains(nodeA));
        Assert.assertEquals(true, nodeD.getNeighbors().contains(nodeA));
        Assert.assertEquals(false, nodeD.getNeighbors().contains(nodeE));
        Assert.assertEquals(false, nodeE.getNeighbors().contains(nodeD));
        Assert.assertEquals(true, nodeA.getOppositeNeighbor(nodeE).equals(nodeD));
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
        Assert.assertTrue(graph.addNodes(nodeA, nodeB, nodeC));

        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        nodeA = new Node("A");
        Assert.assertTrue(graph.addNodes(nodeD, nodeE, nodeA));

        Node nodeF = new Node("F");
        nodeA = new Node("A");
        nodeC = new Node("C");
        Assert.assertTrue(graph.addNodes(nodeF, nodeC, nodeA));

        nodeA = graph.getNodeByCode("A");
        nodeB = graph.getNodeByCode("B");
        nodeC = graph.getNodeByCode("C");
        nodeD = graph.getNodeByCode("D");
        nodeE = graph.getNodeByCode("E");
        nodeF = graph.getNodeByCode("F");

        Assert.assertEquals(4, nodeA.getNeighbors().size());
        Assert.assertEquals(1, nodeF.getNeighbors().size());
        Assert.assertEquals(1, nodeE.getNeighbors().size());
        Assert.assertEquals(true, nodeA.getNeighbors().contains(nodeC));
        Assert.assertEquals(true, nodeA.getNeighbors().contains(nodeD));
        Assert.assertEquals(true, nodeA.getNeighbors().contains(nodeE));
        Assert.assertEquals(true, nodeA.getNeighbors().contains(nodeF));
        Assert.assertEquals(true, nodeF.getNeighbors().contains(nodeA));
        Assert.assertEquals(false, nodeD.getNeighbors().contains(nodeF));
        Assert.assertEquals(false, nodeE.getNeighbors().contains(nodeF));
        Assert.assertEquals(false, nodeC.getNeighbors().contains(nodeF));
        Assert.assertEquals(true, nodeA.getOppositeNeighbor(nodeF).equals(nodeC));
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

        nodeA = graph.getNodeByCode("A");
        nodeB = graph.getNodeByCode("B");
        nodeC = graph.getNodeByCode("C");
        nodeD = graph.getNodeByCode("D");
        nodeE = graph.getNodeByCode("E");
        nodeF = graph.getNodeByCode("F");
        nodeL = graph.getNodeByCode("L");

        Assert.assertEquals(3, nodeF.getNeighbors().size());
        Assert.assertEquals(1, nodeK.getNeighbors().size());
        Assert.assertEquals(1, nodeL.getNeighbors().size());
        Assert.assertEquals(true, nodeF.getNeighbors().contains(nodeA));
        Assert.assertEquals(true, nodeF.getNeighbors().contains(nodeK));
        Assert.assertEquals(true, nodeF.getNeighbors().contains(nodeL));
        Assert.assertEquals(true, nodeK.getNeighbors().contains(nodeF));
        Assert.assertEquals(true, nodeL.getNeighbors().contains(nodeF));
        Assert.assertEquals(false, nodeK.getNeighbors().contains(nodeL));
        Assert.assertEquals(false, nodeL.getNeighbors().contains(nodeK));
        Assert.assertEquals(true, nodeF.getOppositeNeighbor(nodeK).equals(nodeL));
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
        Assert.assertTrue(graph.addNodes(nodeA, nodeB, nodeC));

        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
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
        nodeK = new Node("K");
        nodeD = new Node("D");
        Assert.assertTrue(graph.addNodes(nodeK, nodeM, nodeD));

        nodeA = graph.getNodeByCode("A");
        nodeB = graph.getNodeByCode("B");
        nodeC = graph.getNodeByCode("C");
        nodeD = graph.getNodeByCode("D");
        nodeE = graph.getNodeByCode("E");
        nodeF = graph.getNodeByCode("F");
        nodeK = graph.getNodeByCode("K");
        nodeL = graph.getNodeByCode("L");
        nodeM = graph.getNodeByCode("M");

        Assert.assertEquals(3, nodeD.getNeighbors().size());
        Assert.assertEquals(2, nodeK.getNeighbors().size());
        Assert.assertEquals(1, nodeM.getNeighbors().size());
        Assert.assertEquals(true, nodeD.getNeighbors().contains(nodeA));
        Assert.assertEquals(true, nodeD.getNeighbors().contains(nodeK));
        Assert.assertEquals(true, nodeD.getNeighbors().contains(nodeM));
        Assert.assertEquals(true, nodeK.getNeighbors().contains(nodeD));
        Assert.assertEquals(true, nodeM.getNeighbors().contains(nodeD));
        Assert.assertEquals(false, nodeK.getNeighbors().contains(nodeM));
        Assert.assertEquals(false, nodeM.getNeighbors().contains(nodeK));
        Assert.assertEquals(true, nodeD.getOppositeNeighbor(nodeK).equals(nodeM));

        Node nodeR = new Node("R");
        nodeL = new Node("L");
        nodeE = new Node("E");
        Assert.assertTrue(graph.addNodes(nodeL, nodeR, nodeE));

        nodeA = graph.getNodeByCode("A");
        nodeB = graph.getNodeByCode("B");
        nodeC = graph.getNodeByCode("C");
        nodeD = graph.getNodeByCode("D");
        nodeE = graph.getNodeByCode("E");
        nodeF = graph.getNodeByCode("F");
        nodeK = graph.getNodeByCode("K");
        nodeL = graph.getNodeByCode("L");
        nodeM = graph.getNodeByCode("M");

        Assert.assertEquals(3, nodeE.getNeighbors().size());
        Assert.assertEquals(2, nodeL.getNeighbors().size());
        Assert.assertEquals(1, nodeR.getNeighbors().size());
        Assert.assertEquals(true, nodeE.getNeighbors().contains(nodeA));
        Assert.assertEquals(true, nodeE.getNeighbors().contains(nodeL));
        Assert.assertEquals(true, nodeE.getNeighbors().contains(nodeR));
        Assert.assertEquals(true, nodeL.getNeighbors().contains(nodeE));
        Assert.assertEquals(true, nodeR.getNeighbors().contains(nodeE));
        Assert.assertEquals(false, nodeL.getNeighbors().contains(nodeR));
        Assert.assertEquals(false, nodeR.getNeighbors().contains(nodeL));
        Assert.assertEquals(true, nodeE.getOppositeNeighbor(nodeL).equals(nodeR));
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

        nodeA = graph.getNodeByCode("A");
        nodeB = graph.getNodeByCode("B");
        nodeC = graph.getNodeByCode("C");
        nodeD = graph.getNodeByCode("D");
        nodeE = graph.getNodeByCode("E");
        nodeF = graph.getNodeByCode("F");
        nodeK = graph.getNodeByCode("K");
        nodeL = graph.getNodeByCode("L");
        nodeM = graph.getNodeByCode("M");
        nodeR = graph.getNodeByCode("R");

        Assert.assertEquals(4, nodeC.getNeighbors().size());
        Assert.assertEquals(2, nodeM.getNeighbors().size());
        Assert.assertEquals(2, nodeR.getNeighbors().size());
        Assert.assertEquals(3, nodeE.getNeighbors().size());
        Assert.assertEquals(3, nodeD.getNeighbors().size());
        Assert.assertEquals(true, nodeC.getNeighbors().contains(nodeA));
        Assert.assertEquals(true, nodeC.getNeighbors().contains(nodeB));
        Assert.assertEquals(true, nodeC.getNeighbors().contains(nodeM));
        Assert.assertEquals(true, nodeC.getNeighbors().contains(nodeR));
        Assert.assertEquals(true, nodeM.getNeighbors().contains(nodeC));
        Assert.assertEquals(true, nodeR.getNeighbors().contains(nodeC));
        Assert.assertEquals(false, nodeM.getNeighbors().contains(nodeR));
        Assert.assertEquals(false, nodeR.getNeighbors().contains(nodeM));
        Assert.assertEquals(true, nodeD.getNeighbors().contains(nodeM));
        Assert.assertEquals(true, nodeD.getNeighbors().contains(nodeA));
        Assert.assertEquals(true, nodeD.getNeighbors().contains(nodeK));
        Assert.assertEquals(true, nodeC.getOppositeNeighbor(nodeM).equals(nodeR));
    }

    //  K - D   M
    //  |   |   |
    //  F - A - C - B
    //  |   |   |
    //  L - E - R
    @Test
    public void testAddNodes_7() throws Exception {
        Graph graph = new Graph();

        Node nodeA = new Node("A");
        Node nodeB = new Node("B");
        Node nodeC = new Node("C");
        Assert.assertTrue(graph.addNodes(nodeA, nodeB, nodeC));

        Node nodeD = new Node("D");
        Node nodeE = new Node("E");
        Assert.assertTrue(graph.addNodes(nodeD, nodeE, nodeA));

        Node nodeF = new Node("F");
        nodeA = new Node("A");
        nodeC = new Node("C");
        Assert.assertTrue(graph.addNodes(nodeF, nodeC, nodeA));

        Node nodeK = new Node("K");
        Node nodeL = new Node("L");
        nodeF = new Node("F");
        Assert.assertTrue(graph.addNodes(nodeK, nodeL, nodeF));

        nodeE = new Node("E");
        nodeL = new Node("L");
        Node nodeR = new Node("R");
        Assert.assertTrue(graph.addNodes(nodeL, nodeR, nodeE));

        Node nodeM = new Node("M");
        nodeR = new Node("R");
        Assert.assertTrue(graph.addNodes(nodeM, nodeR, nodeC));

        nodeA = graph.getNodeByCode("A");
        nodeB = graph.getNodeByCode("B");
        nodeC = graph.getNodeByCode("C");
        nodeD = graph.getNodeByCode("D");
        nodeE = graph.getNodeByCode("E");
        nodeF = graph.getNodeByCode("F");
        nodeK = graph.getNodeByCode("K");
        nodeL = graph.getNodeByCode("L");
        nodeM = graph.getNodeByCode("M");
        nodeR = graph.getNodeByCode("R");

        Assert.assertEquals(4, nodeC.getNeighbors().size());
        Assert.assertEquals(2, nodeR.getNeighbors().size());
        Assert.assertEquals(1, nodeM.getNeighbors().size());
        Assert.assertEquals(true, nodeC.getNeighbors().contains(nodeA));
        Assert.assertEquals(true, nodeC.getNeighbors().contains(nodeM));
        Assert.assertEquals(true, nodeC.getNeighbors().contains(nodeR));
        Assert.assertEquals(true, nodeR.getNeighbors().contains(nodeC));
        Assert.assertEquals(true, nodeM.getNeighbors().contains(nodeC));
        Assert.assertEquals(false, nodeM.getNeighbors().contains(nodeR));
        Assert.assertEquals(false, nodeR.getNeighbors().contains(nodeM));
        Assert.assertEquals(true, nodeC.getOppositeNeighbor(nodeM).equals(nodeR));
    }

    //  K - D - M
    //  |   |   |
    //  F - A - C - B
    //  |   |   |
    //  L - E - R
    @Test
    public void testGetDiagonallyNeighbors() throws Exception {
        Graph graph = new Graph();

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

        nodeA = graph.getNodeByCode("A");
        nodeB = graph.getNodeByCode("B");
        nodeC = graph.getNodeByCode("C");
        nodeD = graph.getNodeByCode("D");
        nodeE = graph.getNodeByCode("E");
        nodeF = graph.getNodeByCode("F");
        nodeK = graph.getNodeByCode("K");
        nodeL = graph.getNodeByCode("L");
        nodeM = graph.getNodeByCode("M");
        nodeR = graph.getNodeByCode("R");

        ArrayList<Node> neighborsOfA = graph.getDiagonallyNeighbors(nodeA);
        Assert.assertTrue(neighborsOfA.size() == 4);
        Assert.assertTrue(neighborsOfA.contains(nodeK));
        Assert.assertTrue(neighborsOfA.contains(nodeM));
        Assert.assertTrue(neighborsOfA.contains(nodeL));
        Assert.assertTrue(neighborsOfA.contains(nodeR));

        ArrayList<Node> neighborsOfM = graph.getDiagonallyNeighbors(nodeM);
        Assert.assertTrue(neighborsOfM.size() == 1);
        Assert.assertTrue(neighborsOfM.contains(nodeA));

        ArrayList<Node> neighborsOfC = graph.getDiagonallyNeighbors(nodeC);
        Assert.assertTrue(neighborsOfC.size() == 2);
        Assert.assertTrue(neighborsOfC.contains(nodeD));
        Assert.assertTrue(neighborsOfC.contains(nodeE));

        ArrayList<Node> neighborsOfD = graph.getDiagonallyNeighbors(nodeD);
        Assert.assertTrue(neighborsOfD.size() == 2);
        Assert.assertTrue(neighborsOfD.contains(nodeF));
        Assert.assertTrue(neighborsOfD.contains(nodeC));
    }
}