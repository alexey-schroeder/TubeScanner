package tubeScanner;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.opencv.core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;
import tubeScanner.code.dataModel.triplet.NodeTriplet;
import tubeScanner.code.dataModel.triplet.PointTripleFinder;
import tubeScanner.code.dataModel.triplet.PointTriplet;
import tubeScanner.code.frameSorce.FrameSource;
import tubeScanner.code.graph.*;
import tubeScanner.code.qrCode.CodeFinder;
import tubeScanner.code.qrCode.PointInterpreter;
import tubeScanner.code.utils.FindUtils;
import tubeScanner.code.utils.ImageUtils;
import tubeScanner.code.utils.NodeUtils;
import tubeScanner.code.utils.PointUtils;

import java.awt.image.BufferedImage;
import java.util.*;

public class Controller {
    @FXML
    private Label numberOfCodes;
    @FXML
    private TableColumn<Node, String> codeTableColumn;
    @FXML
    private TableView<Node> table;
    @FXML
    private Canvas canvas;
    @FXML
    private Canvas graphPane;
    private FrameSource frameSource;
    private CodeFinder codeFinder;
    private Graph graph;
    private HashMap<Node, Point> oldNodeCoordinates;
    private double oldRadius = -1;
    private AutoScalingGroup group;
    private CanvasGraphVisualiser canvasGraphVisualiser;
    private FrameStateVisualiser frameStateVisualiser;
    private boolean stop;
    //    private Group group;
    private LatticeBuilder latticeBuilder;
    private GraphNodesConnector graphNodesConnector;
    int oldGraphSize;

    public void initialize() {
        graphPane.setStyle("-fx-border-color: red;");
        codeFinder = new CodeFinder();

        graph = new Graph();
        latticeBuilder = new LatticeBuilder(graph);
        canvasGraphVisualiser = new CanvasGraphVisualiser();
        canvasGraphVisualiser.setCanvas(graphPane);
        frameStateVisualiser = new FrameStateVisualiser();
        graphNodesConnector = new GraphNodesConnector();
        graphNodesConnector.setGraph(graph);
        codeTableColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Node, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Node, String> param) {
                return new SimpleStringProperty(param.getValue().getCode());
            }
        });

        table.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Node selectedNode = table.getSelectionModel().getSelectedItem();
                canvasGraphVisualiser.markNodeByCode(selectedNode.getCode());
            }
        });
    }

    public void start() throws Exception {
        startThread();
    }

    public void stop() {
        stop = true;
    }

    private void startThread() throws Exception {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stop) {
                    threadCode();
                }
                frameSource.stop();
            }
        });
        thread.start();
    }

    public void threadCode() {
        Mat coloredFrame = frameSource.getFrame();

        Core.flip(coloredFrame, coloredFrame, 0);
        double resizeFactor = Math.min(coloredFrame.rows(), coloredFrame.cols()) / 300.0;
        if (resizeFactor < 1) {
            resizeFactor = 1;
        }
        Mat resized;
        if (resizeFactor > 1.1) {

            resized = Mat.zeros((int) (coloredFrame.rows() / resizeFactor), (int) (coloredFrame.cols() / resizeFactor), coloredFrame.type());
//            Imgproc.GaussianBlur(coloredFrame, resized, new Size(15, 15), 35);
            Imgproc.erode(coloredFrame, resized, new Mat(), new Point(-1, -1), 2);
//            Imgproc.dilate(coloredFrame, resized, new Mat(), new Point(-1, -1), 5);
            Imgproc.resize(resized, resized, new Size(coloredFrame.cols() / resizeFactor, coloredFrame.rows() / resizeFactor), 0, 0, Imgproc.INTER_CUBIC);
//            Mat frame = new Mat();
//            Imgproc.cvtColor(resized, frame, Imgproc.COLOR_RGB2GRAY);
//            Imgproc.equalizeHist(frame, frame);
//            resized = frame;
        } else {
            resized = coloredFrame;
        }


        MatOfKeyPoint codeKeyPoints = computeKeyPoints(resized);
        List<KeyPoint> keyPointsList = codeKeyPoints.toList();
        List<Point> centers = new ArrayList<Point>(keyPointsList.size());
        for (KeyPoint keyPoint : keyPointsList) {
            Point center = keyPoint.pt;
            centers.add(center);
//            Imgproc.circle(resized, center, 10, new Scalar(0, 255, 0), 2);
        }
        CodeFinder codeFinder = new CodeFinder();
        Point[] cellVectors = codeFinder.calculateCellVectors(centers);
        if (cellVectors == null && oldRadius < 0) {
            if (oldNodeCoordinates != null) {
                frameStateVisualiser.setFrame(resized);
                frameStateVisualiser.drawFrameState(Collections.emptyList(), Collections.emptyList(), oldNodeCoordinates, Collections.emptyMap(), cellVectors);
                showFrame(resized, oldNodeCoordinates, false);
                oldNodeCoordinates = null;
                System.out.println("old circles drawded");
            } else {
                showFrame(resized, null, false);
            }
            return;
        }


        double radius;
        if (cellVectors != null) {
            Point vectorA = cellVectors[0];
            radius = PointUtils.getVectorLength(vectorA) / 3.0;
            oldRadius = radius;
        } else {
            radius = oldRadius;
        }
        PointInterpreter pointInterpreter = new PointInterpreter();
        pointInterpreter.interpretPoints(coloredFrame, resizeFactor, radius, centers);
        HashMap<Point, Node> goodPoints = pointInterpreter.getGoodPoints();
        List<Point> notInterpretedCircles = pointInterpreter.getNotInterpretedCircles();
        List<Point> interpretedCircles = pointInterpreter.getInterpretedCircles();

        cellVectors = correctCellVectors(cellVectors, goodPoints);


        if (cellVectors != null) {
            List<Point> notFoundedLatticePoints = foundLatticePoints(goodPoints, cellVectors, coloredFrame.width(), coloredFrame.height());

            pointInterpreter.interpretPoints(coloredFrame, resizeFactor, radius, notFoundedLatticePoints);
            HashMap<Point, Node> goodPoints_2 = pointInterpreter.getGoodPoints();
            List<Point> notInterpretedCircles_2 = pointInterpreter.getNotInterpretedCircles();
            List<Point> interpretedCircles_2 = pointInterpreter.getInterpretedCircles();

            goodPoints.putAll(goodPoints_2);
            notInterpretedCircles.addAll(notInterpretedCircles_2);
            interpretedCircles.addAll(interpretedCircles_2);


            boolean graphChanged = true;
            HashMap<Node, Point> graphNodeCoordinates = new HashMap<>();
            while (graphChanged) {
                findAndAddTripletsInGraph(goodPoints, cellVectors);
                graphNodesConnector.connectGraphByBaseis(goodPoints, cellVectors);
                HashMap<Node, Point> tempResult = latticeBuilder.calculateNodeCoordinates(goodPoints, cellVectors);
                HashMap<Point, Node> flippedTempResult = FindUtils.reverseMap(tempResult);
                flippedTempResult.putAll(goodPoints);
                graphNodesConnector.connectGraphNodesByNeighbors(tempResult);

                graphChanged = findAndAddTripletsInGraph(flippedTempResult, cellVectors);
                graphNodeCoordinates = tempResult;
            }

            HashMap<Node, Point> addedByNeighbors = latticeBuilder.getAddedNodes();
            oldNodeCoordinates = graphNodeCoordinates;
            frameStateVisualiser.setFrame(resized);
            frameStateVisualiser.drawFrameState(notInterpretedCircles, interpretedCircles, graphNodeCoordinates, addedByNeighbors, cellVectors);
        }
        showFrame(resized, oldNodeCoordinates, !goodPoints.isEmpty());
    }

    private List<Point> foundLatticePoints(HashMap<Point, Node> goodPoints, Point[] cellVectors, int width, int height) {
        LatticePointsCalculator latticePointsCalculator = new LatticePointsCalculator(width, height, oldRadius);
        List<Point> latticePoints = latticePointsCalculator.calculateLatticePoints(goodPoints, cellVectors);
        return latticePoints;
    }

    public boolean findAndAddTripletsInGraph(HashMap<Point, Node> goodPoints, Point[] cellVectors) {
        PointTripleFinder pointTripleFinder = new PointTripleFinder();
        ArrayList<PointTriplet> pointTriplets = pointTripleFinder.findTriplets(goodPoints.keySet(), cellVectors);
        if (!pointTriplets.isEmpty()) {
            List<NodeTriplet> nodeTriplets = new ArrayList<>(pointTriplets.size());
            for (PointTriplet pointTriplet : pointTriplets) {
                Node nodeA = goodPoints.get(pointTriplet.getPointA());
                Node nodeB = goodPoints.get(pointTriplet.getPointB());
                Node nodeCenter = goodPoints.get(pointTriplet.getCenter());
                NodeTriplet nodeTriplet = new NodeTriplet(nodeA, nodeB, nodeCenter);
                nodeTriplets.add(nodeTriplet);
            }
            return addTripletsInGraph(nodeTriplets);
        }
        return false;
    }

    private Point[] correctCellVectors(Point[] cellVectors, HashMap<Point, Node> goodPoints) {
        if (graph.isEmpty()) {
            return cellVectors;
        }
        HashSet<Node> allNodesInGraph = graph.getAllNodes();
        Collection<Node> allGoodNodes = goodPoints.values();
        for (Node node : allGoodNodes) {
            Node equalsNodeInGraph = NodeUtils.findEqualsNode(allNodesInGraph, node);
            if (equalsNodeInGraph != null) {
                Point equalsNodePoint = FindUtils.findKeyForValueInMap(goodPoints, node);
                HashSet<Node> neighbors = equalsNodeInGraph.getNeighbors();
                for (Node neighbor : neighbors) {
                    Node equalsNeighborNode = NodeUtils.findEqualsNode(allGoodNodes, neighbor);
                    if (equalsNeighborNode != null) {
                        Point equalsNeighborNodePoint = FindUtils.findKeyForValueInMap(goodPoints, neighbor);
                        Point trueVector_1 = PointUtils.minus(equalsNodePoint, equalsNeighborNodePoint);
                        Point trueVector_2 = PointUtils.getPerpendicularVector(trueVector_1);
                        return new Point[]{trueVector_1, trueVector_2};
                    }
                }
            }
        }
        return cellVectors;
    }


    public MatOfKeyPoint computeKeyPoints(Mat mat) {
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.GRID_SIMPLEBLOB);
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        try {
            featureDetector.detect(mat, keypoints);
        } catch (Exception e) {

        }
        return keypoints;
    }

    public void showFrame(Mat frame, HashMap<Node, Point> nodeCoordinates, boolean repaintGraphGroup) {
        BufferedImage image = ImageUtils.matToBufferedImage(frame);
        final WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                canvas.getGraphicsContext2D().drawImage(fxImage, 0, 0);
                if (repaintGraphGroup) {
                    showGraph(nodeCoordinates);
                }
                int currentGraphSize = graph.getAllNodes().size();
                if (currentGraphSize > oldGraphSize) {
                    oldGraphSize = currentGraphSize;
                    refreshTable();
                    refreshNumberOfCodes(currentGraphSize);
                }
            }
        });
    }

    private void refreshNumberOfCodes(int size) {
        numberOfCodes.setText(String.valueOf(size));
    }

    private void refreshTable() {
        ObservableList<Node> nodes = FXCollections.observableArrayList();
        HashSet<Node> allNodes = graph.getAllNodes();
        nodes.addAll(allNodes);
        table.setItems(nodes);
    }

    private void showGraph(HashMap<Node, Point> nodeCoordinates) {
        if (nodeCoordinates != null && !nodeCoordinates.isEmpty()) {
            canvasGraphVisualiser.setNodeCoordinates(nodeCoordinates);
            canvasGraphVisualiser.drawGraph();
        }
    }

    public boolean addTripletsInGraph(List<NodeTriplet> triplets) {
        List<NodeTriplet> tripletsForAdd = new ArrayList<>();
        List<NodeTriplet> notAddedTriplets = new ArrayList<>(triplets);
        while (notAddedTriplets.size() != tripletsForAdd.size()) {
            tripletsForAdd = notAddedTriplets;
            notAddedTriplets = new ArrayList<>();
            for (NodeTriplet nodeTriplet : tripletsForAdd) {
                Node nodeA = nodeTriplet.getNodeA();
                Node nodeB = nodeTriplet.getNodeB();
                Node center = nodeTriplet.getCenter();
                boolean isAdded = graph.addNodes(nodeA, nodeB, center);
                if (!isAdded) {
                    notAddedTriplets.add(nodeTriplet);
                }
            }
        }
        return notAddedTriplets.size() != triplets.size();
    }

    public FrameSource getFrameSource() {
        return frameSource;
    }

    public void setFrameSource(FrameSource frameSource) {
        this.frameSource = frameSource;
    }
}
