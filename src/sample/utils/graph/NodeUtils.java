package sample.utils.graph;

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
}
