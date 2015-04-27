package tubeScanner.code.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Alex on 21.04.2015.
 */
public class NodeCoordinateInTubeCalculator {
    private String[] charArray = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"};
    private int[] numberArray = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    private Graph graph;
    private Node zeroPoint;
    private Graph.NodeAxe charAxe;
    private Graph.NodeAxe numberAxe;
    private HashMap<Node, TubeCoordinate> nodeTubeCoordinatesMap;

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        nodeTubeCoordinatesMap = new HashMap<>();
    }

    public Node getZeroPoint() {
        return zeroPoint;
    }

    public void setZeroPoint(Node zeroPoint) {
        this.zeroPoint = zeroPoint;
        nodeTubeCoordinatesMap.put(zeroPoint, new TubeCoordinate("A", 1));
        charAxe = null;
        numberAxe = null;
        nodeTubeCoordinatesMap.clear();
    }

    public TubeCoordinate getTubeCoordinateForNode(Node node) {
        if (charAxe == null) {
//            initAxes();//todo
        }
        TubeCoordinate tubeCoordinate = nodeTubeCoordinatesMap.get(node);
        if (tubeCoordinate == null) {
            HashSet<Node> allNodes = graph.getAllNodes();
            if (allNodes.contains(node)) {
                calculateAllNodeCoordinates();
                return nodeTubeCoordinatesMap.get(node);
            } else {
                return null;
            }
        } else {
            return tubeCoordinate;
        }
    }

    private void calculateAllNodeCoordinates() {
        HashSet<Node> allNodes = graph.getAllNodes();
        boolean wasChanged = true;
        while (wasChanged && nodeTubeCoordinatesMap.size() < allNodes.size()) {
            wasChanged = false;
            for (Node node : allNodes) {
                TubeCoordinate tubeCoordinate = nodeTubeCoordinatesMap.get(node);
                if (tubeCoordinate == null) {
                    tubeCoordinate = calculateTubeCoordinateForNode(node);
                    if (tubeCoordinate != null) {
                        nodeTubeCoordinatesMap.put(node, tubeCoordinate);
                        wasChanged = true;
                    }
                }
            }
        }
    }

    private TubeCoordinate calculateTubeCoordinateForNode(Node node) {
        ArrayList<Node> neighborsInCharAxe = node.getNeighborsByAxe(charAxe);
        TubeCoordinate tubeCoordinate = compareWithZeroPoint(neighborsInCharAxe);
        if (tubeCoordinate != null) {
            return tubeCoordinate;
        }

        ArrayList<Node> neighborsInNumberAxe = node.getNeighborsByAxe(numberAxe);
        tubeCoordinate = compareWithZeroPoint(neighborsInNumberAxe);
        if (tubeCoordinate != null) {
            return tubeCoordinate;
        }

        tubeCoordinate = getTubeCoordinateByNeighborsAsAverageNode(neighborsInCharAxe);
        if (tubeCoordinate != null) {
            return tubeCoordinate;
        }

        tubeCoordinate = getTubeCoordinateByNeighborsAsAverageNode(neighborsInNumberAxe);
        if (tubeCoordinate != null) {
            return tubeCoordinate;
        }

        tubeCoordinate = getTubeCoordinateByNextNeighbors(node);
        return tubeCoordinate;
    }

    private TubeCoordinate compareWithZeroPoint(ArrayList<Node> neighborsInAxe) {
        if (!neighborsInAxe.isEmpty()) {
            Node neighborInAxe_1 = neighborsInAxe.get(0);
            Node neighborInAxe_2 = null;
            if (neighborsInAxe.size() > 1) {
                neighborInAxe_2 = neighborsInAxe.get(1);
            }
            if (zeroPoint.equals(neighborInAxe_1)) {
                TubeCoordinate tubeCoordinate = new TubeCoordinate("B", 1);
                return tubeCoordinate;
            }

            if (zeroPoint.equals(neighborInAxe_2)) {
                TubeCoordinate tubeCoordinate = new TubeCoordinate("B", 1);
                return tubeCoordinate;
            }
        }
        return null;
    }

    private TubeCoordinate getTubeCoordinateByNeighborsAsAverageNode(ArrayList<Node> neighborsInAxe) {
        if (neighborsInAxe.size() > 1) {
            Node neighborInAxe_1 = neighborsInAxe.get(0);
            TubeCoordinate neighbor_1_Coordinate = nodeTubeCoordinatesMap.get(neighborInAxe_1);
            Node neighborInAxe_2 = neighborsInAxe.get(1);
            TubeCoordinate neighbor_2_Coordinate = nodeTubeCoordinatesMap.get(neighborInAxe_2);
            TubeCoordinate averageTubeCoordinate = calculateAverageCoordinate(neighbor_1_Coordinate, neighbor_2_Coordinate);
            if (averageTubeCoordinate != null) {
                return averageTubeCoordinate;
            }
        }
        return null;
    }

    private TubeCoordinate calculateAverageCoordinate(TubeCoordinate neighbor_1_coordinate, TubeCoordinate neighbor_2_coordinate) {
        if (neighbor_1_coordinate == null || neighbor_2_coordinate == null) {
            return null;
        }

        String char_1 = neighbor_1_coordinate.getCharCoordinate();
        String char_2 = neighbor_2_coordinate.getCharCoordinate();
        String averageChar = getAverageChar(char_1, char_2);

        int number_1 = neighbor_1_coordinate.getNumberCoordinate();
        int number_2 = neighbor_2_coordinate.getNumberCoordinate();
        int averageNumber = (number_1 + number_2) / 2;
        return new TubeCoordinate(averageChar, averageNumber);
    }

    private String getAverageChar(String char_1, String char_2) {
        int char_1_position = getCharPosition(char_1);
        int char_2_position = getCharPosition(char_2);
        int averageCharPosition = (char_1_position + char_2_position) / 2;
        return charArray[averageCharPosition];
    }

    private int getCharPosition(String charString) {
        for (int i = 0; i < charArray.length; i++) {
            String charStringFromArray = charArray[i];
            if (charStringFromArray.equalsIgnoreCase(charString)) {
                return i;
            }
        }
        return -100;
    }

    private TubeCoordinate getTubeCoordinateByNextNeighbors(Node node) {
        ArrayList<Node> neighborsInCharAxe = node.getNeighborsByAxe(charAxe);
        TubeCoordinate tubeCoordinate = getTubeCoordinateByNextNeighbors(node, neighborsInCharAxe);
        if (tubeCoordinate != null) {
            return tubeCoordinate;
        }

        ArrayList<Node> neighborsInNumberAxe = node.getNeighborsByAxe(numberAxe);
        tubeCoordinate = getTubeCoordinateByNextNeighbors(node, neighborsInNumberAxe);
        return tubeCoordinate;
    }

    private TubeCoordinate getTubeCoordinateByNextNeighbors(Node node, ArrayList<Node> neighborsInAxe) {
        if (!neighborsInAxe.isEmpty()) {
            Node neighborInAxe_1 = neighborsInAxe.get(0);
            TubeCoordinate tubeCoordinate = getTubeCoordinateByNextNeighbor(node, neighborInAxe_1);
            if (tubeCoordinate != null) {
                return tubeCoordinate;
            }

            if (neighborsInAxe.size() > 1) {
                Node neighborInAxe_2 = neighborsInAxe.get(1);
                tubeCoordinate = getTubeCoordinateByNextNeighbor(node, neighborInAxe_2);
                if (tubeCoordinate != null) {
                    return tubeCoordinate;
                }
            }
        }
        return null;
    }

    private TubeCoordinate getTubeCoordinateByNextNeighbor(Node node, Node neighbor) {
        TubeCoordinate neighborCoordinate = nodeTubeCoordinatesMap.get(neighbor);
        if (neighborCoordinate == null) {
            return null;
        }
        Node otherNeighbor = neighbor.getOppositeNeighbor(node);
        if (otherNeighbor != null) {
            TubeCoordinate otherNodeCoordinate = nodeTubeCoordinatesMap.get(otherNeighbor);
            if (otherNodeCoordinate != null) {
                TubeCoordinate nodeCoordinate = calculateNextCoordinate(otherNodeCoordinate, neighborCoordinate);
                return nodeCoordinate;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private TubeCoordinate calculateNextCoordinate(TubeCoordinate firstCoordinate, TubeCoordinate secondCoordinate) {
        String firstCharCoordinate = firstCoordinate.getCharCoordinate();
        String secondCharCoordinate = secondCoordinate.getCharCoordinate();
        int firstCharPosition = getCharPosition(firstCharCoordinate);
        int secondCharPosition = getCharPosition(secondCharCoordinate);
        int resultCharPosition = getNextPosition(firstCharPosition, secondCharPosition);
        String resultChar = charArray[resultCharPosition];

        int firstNumberCoordinate = firstCoordinate.getNumberCoordinate();
        int secondNumberCoordinate = secondCoordinate.getNumberCoordinate();
        int resultNumberCoordinate = getNextPosition(firstNumberCoordinate, secondNumberCoordinate);
        return new TubeCoordinate(resultChar, resultNumberCoordinate);
    }

    private int getNextPosition(int firstPosition, int secondPosition) {
        int diff = secondPosition - firstPosition;
        int result = secondPosition + diff;// oder anders : result = 2 * secondPosition - firstPosition
        return result;
    }

    public void setCharAxe(Graph.NodeAxe charAxe) {
        this.charAxe = charAxe;
        numberAxe = Graph.getOtherNodeAxe(charAxe);
    }
}
