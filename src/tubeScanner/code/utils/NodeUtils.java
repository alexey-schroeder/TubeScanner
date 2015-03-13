package tubeScanner.code.utils;

import tubeScanner.code.graph.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Alex on 28.01.2015.
 */
public class NodeUtils {

    public static Node findEqualsNode(Collection<Node> nodes, Node referenceNode){
        for(Node candidatNode : nodes){
            if(candidatNode.equals(referenceNode)){
                return candidatNode;
            }
        }
        return null;
    }

    public static ArrayList<Node> getJointNeighbors(Node nodeA, Node nodeB) {
        ArrayList<Node> result = new ArrayList<>();
        if(nodeA == null || nodeB == null){
            return result;
        }
        HashSet<Node> neighborsA = nodeA.getNeighbors();
        HashSet<Node> neighborsB = nodeB.getNeighbors();
        for (Node neighborA : neighborsA) {
            if (neighborsB.contains(neighborA)) {
                result.add(neighborA);
            }
        }
        return result;
    }
}
