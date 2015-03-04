package sample;

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
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import sample.utils.*;
import sample.utils.graph.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
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
    private VideoCapture camera;
    private CodeFinder codeFinder;
    private Graph graph;
    private HashMap<Node, Point> oldNodeCoordinates;
    private double oldRadius = -1;
    private AutoScalingGroup group;
    private CanvasGraphVisualiser canvasGraphVisualiser;
    private boolean stop;
    //    private Group group;
    private LatticeBuilder latticeBuilder;
    int oldGraphSize;

    public void initialize() throws Exception {
//        double prefWidth = graphPane.getPrefWidth();
//        double prefHeight = graphPane.getPrefHeight();
//        group = new AutoScalingGroup(prefWidth, prefHeight);
//        group = new Group();
//        group.setAutoSizeChildren(true);
//        group.prefWidth(prefWidth);
//        group.prefHeight(prefHeight);
//        group.setAutoScale(true);
//        graphPane.getChildren().add(group);
        graphPane.setStyle("-fx-border-color: red;");
        codeFinder = new CodeFinder();
        camera = new VideoCapture(0);
        graph = new Graph();
        latticeBuilder = new LatticeBuilder(graph);
        canvasGraphVisualiser = new CanvasGraphVisualiser();
        canvasGraphVisualiser.setCanvas(graphPane);
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
        camera.open(0); //Useless
        boolean wset = camera.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, 1280);
        boolean hset = camera.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, 720);
        if (!camera.isOpened()) {
            System.out.println("Camera Error");
        } else {
            System.out.println("Camera OK?");
            startThread();
        }
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
                camera.release();
            }
        });
        thread.start();
    }

    public void threadCode() {
        Mat coloredFrame = new Mat();
        camera.read(coloredFrame);
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
                drawNodeCircles(oldNodeCoordinates, resized);
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
        DataMatrixInterpreter dataMatrixInterpreter = new DataMatrixInterpreter();
        CodeCleaner codeCleaner = new CodeCleaner();
        HashMap<Point, Node> goodPoints = new HashMap<Point, Node>();
        for (Point center : centers) {
            double width = radius * 2 * resizeFactor;
            double height = radius * 2 * resizeFactor;
            int x = (int) (center.x * resizeFactor - width / 2);
            if (x < 0) {
                x = 0;
            }
            int y = (int) (center.y * resizeFactor - height / 2);
            if (y < 0) {
                y = 0;
            }
            if (x + width > coloredFrame.cols()) {
                width = coloredFrame.cols() - x;
            }

            if (y + height > coloredFrame.rows()) {
                height = coloredFrame.rows() - y;
            }
            Mat circleImage = coloredFrame.submat(new Rect(x, y, (int) width, (int) height));
            Core.flip(circleImage, circleImage, 0);
            Mat code = codeFinder.extractCode(circleImage);
            if (code != null) {
                Mat boundedCode = codeCleaner.getBoundedCode(code);
                if (boundedCode != null) {
                    Mat cleanedCode = codeCleaner.cleanCode(boundedCode);
                    if (cleanedCode != null) {
                        Core.bitwise_not(cleanedCode, cleanedCode);
                        BufferedImage bufferedImage = ImageUtils.matToBufferedImage(cleanedCode);
                        try {
                            String text = dataMatrixInterpreter.decode(bufferedImage);
                            if (text != null) {
//                                Imgproc.circle(resized, center, 6, new Scalar(255, 255, 0), 2);
                                Node node = new Node(text);
                                goodPoints.put(center, node);
                            } else {
//                                Imgproc.circle(resized, center, 6, new Scalar(0, 0, 255), 2);
                            }
                            Imgproc.circle(resized, center, 6, new Scalar(255, 255, 0), 2);
                        } catch (IOException e) {
//                        e.printStackTrace();
                        }
                    }
                }
            }
        }

        cellVectors = correctCellVectors(cellVectors, goodPoints);

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
            addTripletsInGraph(nodeTriplets);

            HashMap<Node, Point> correctedNodeCoordinates = latticeBuilder.calculateNodeCoordinates(goodPoints, cellVectors);
            oldNodeCoordinates = correctedNodeCoordinates;
            drawNodeCircles(correctedNodeCoordinates, resized);
        }
        showFrame(resized, oldNodeCoordinates, !goodPoints.isEmpty());
    }

    public void drawNodeCircles(HashMap<Node, Point> correctedNodeCoordinates, Mat resized) {
        for (Node node : correctedNodeCoordinates.keySet()) {
            Point point = correctedNodeCoordinates.get(node);
            Scalar color = new Scalar(0, 255, 0);
            Imgproc.circle(resized, point, 10, color, 2);
        }
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
                Point equalsNodePoint = findKeyForValueInMap(goodPoints, node);
                HashSet<Node> neighbors = equalsNodeInGraph.getNeighbors();
                for (Node neighbor : neighbors) {
                    Node equalsNeighborNode = NodeUtils.findEqualsNode(allGoodNodes, neighbor);
                    if (equalsNeighborNode != null) {
                        Point equalsNeighborNodePoint = findKeyForValueInMap(goodPoints, neighbor);
                        Point trueVector_1 = PointUtils.minus(equalsNodePoint, equalsNeighborNodePoint);
                        Point trueVector_2 = PointUtils.getPerpendicularVector(trueVector_1);
                        return new Point[]{trueVector_1, trueVector_2};
                    }
                }
            }
        }
        return cellVectors;
    }

    private Point findKeyForValueInMap(HashMap<Point, Node> goodPoints, Node node) {
        for (Point point : goodPoints.keySet()) {
            if (goodPoints.get(point).equals(node)) {
                return point;
            }
        }
        return null;
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
////            System.out.println("in showGraph");
//            group.getChildren().clear();
//            for (Node node : nodeCoordinates.keySet()) {
//                Point coordinate = nodeCoordinates.get(node);
//                javafx.scene.shape.Circle circle = new Circle(coordinate.x, coordinate.y, 20);
//                Tooltip tooltip = new Tooltip(node.getCode());
//                circle.setOnMouseEntered(new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent event) {
//                        tooltip.show(circle, event.getScreenX() + 10, event.getScreenY());
//                        circle.setFill(Color.GREEN);
//                    }
//                });
//
//                circle.setOnMouseExited(new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent event) {
//                        tooltip.hide();
//                        circle.setFill(Color.BLACK);
//                    }
//                });
//                group.getChildren().add(circle);
//            }

            canvasGraphVisualiser.setNodeCoordinates(nodeCoordinates);
            canvasGraphVisualiser.drawGraph();
        }
    }

    public void addTripletsInGraph(List<NodeTriplet> triplets) {
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
    }
}
