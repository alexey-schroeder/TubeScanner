package sample;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import sample.utils.*;
import sample.utils.graph.Graph;
import sample.utils.graph.Node;
import sample.utils.graph.NodeUtils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public class Controller {
    public Canvas canvas;
    VideoCapture camera;
    CodeFinder codeFinder;
    private Graph graph;

    public void initialize() throws Exception {
        codeFinder = new CodeFinder();
        camera = new VideoCapture(0);

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

    private void startThread() throws Exception {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
//                threadCode();
                    threadCode_2();
                }
            }
        });
        thread.start();
    }

    public void threadCode_2() {
        Mat coloredFrame = new Mat();
        camera.read(coloredFrame);

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
        if (cellVectors == null) {
            showFrame(resized);
            return;
        }
        Point vectorA = cellVectors[0];
        double radius = PointUtils.getVectorLength(vectorA) / 3.0;

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
            Mat code = codeFinder.extractCode(circleImage);
            if (code != null) {
                Mat boundedCode = codeCleaner.getBoundedCode(code);
                if (boundedCode == null) {
                    showFrame(resized);
                    return;
                }
                Mat cleanedCode = codeCleaner.cleanCode(boundedCode);
                if (cleanedCode == null) {
                    showFrame(resized);
                    return;
                }
                Core.bitwise_not(cleanedCode, cleanedCode);
                BufferedImage bufferedImage = ImageUtils.matToBufferedImage(cleanedCode);
                try {
                    String text = dataMatrixInterpreter.decode(bufferedImage);
//                    System.out.println(text);
                    if (text != null) {
                        Imgproc.circle(resized, center, 6, new Scalar(255, 255, 0), 2);
                        Node node = new Node(text);
                        goodPoints.put(center, node);
                    }
                    else {
                        Imgproc.circle(resized, center, 10, new Scalar(0, 0, 255), 2);
                    }
                } catch (IOException e) {
//                        e.printStackTrace();
                }
            }
        }

        PointTripleFinder pointTripleFinder = new PointTripleFinder();
        ArrayList<PointTriplet> pointTriplets = pointTripleFinder.findTriplets(goodPoints.keySet(), cellVectors);
        System.out.println("founded pointTriplets: " + pointTriplets.size());
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
            HashSet<Node> allNodes = graph.getAllNodes();
            int graphSize = allNodes.size();
            System.out.println("graphSize: " + graphSize);
            BasisFinder basisFinder = new BasisFinder();
            ArrayList<Basis> bases = basisFinder.findBases(allNodes,goodPoints, cellVectors);
            System.out.println("bases: " + bases.size());
            HashMap<Node, Point> bestNodeCoordinates = new HashMap<>();
            Basis bestBasis = null;
            int maxSize = 0;
            for (Basis basis : bases) {
                HashMap<Node, Point> nodeCoordinates = graphToPoints(basis.getPointBasis(), basis.getNodeBasis());
                if (nodeCoordinates.size() == graphSize) {
                    bestNodeCoordinates = nodeCoordinates;
                    bestBasis = basis;
                    break;
                } else {
                    if (nodeCoordinates.size() > maxSize) {
                        bestNodeCoordinates = nodeCoordinates;
                        maxSize = nodeCoordinates.size();
                        bestBasis = basis;
                    }
                }
            }
            System.out.println("bestNodeCoordinates: " + bestNodeCoordinates.size());
            ArrayList<Node> basisNodes = new ArrayList<>();
            if(bestBasis != null) {
               NodeTriplet bestNodeBasis =  bestBasis.getNodeBasis();
                basisNodes.add(bestNodeBasis.getNodeA());
                basisNodes.add(bestNodeBasis.getNodeB());
                basisNodes.add(bestNodeBasis.getCenter());
            }
            for (Node node : bestNodeCoordinates.keySet()) {
                Point point = bestNodeCoordinates.get(node);
                Scalar color;
                if(basisNodes.contains(node)){
                   color = new Scalar(255, 0, 0);
                } else {
                    color = new Scalar(0, 255, 0);
                }
                Imgproc.circle(resized, point, 10, color, 2);
            }
        }


//        for(KeyPoint keyPoint : keyPointsArray){
//            Point center = keyPoint.pt;
//            Imgproc.circle(resized, center, 10, new Scalar(0, 255, 0), 2);
//        }
//        Features2d.drawKeypoints(resized, codeKeyPoints, resized);
        showFrame(resized);
    }


    public MatOfKeyPoint computeKeyPoints(Mat mat) {
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.GRID_SIMPLEBLOB);
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        featureDetector.detect(mat, keypoints);
        return keypoints;
    }


    public void threadCode() {
        while (true) {
            Mat coloredFrame = new Mat();
            camera.read(coloredFrame);
            Mat frame = new Mat();
            Imgproc.cvtColor(coloredFrame, frame, Imgproc.COLOR_RGB2GRAY);
            try {
                LinkedList<RotatedRect> codeAreas = codeFinder.findCodes(frame);
                int resizeFactor = codeFinder.getResizeFactor();
                CodeCleaner codeCleaner = new CodeCleaner();
                DataMatrixInterpreter dataMatrixInterpreter = new DataMatrixInterpreter();
                for (RotatedRect rotatedRect : codeAreas) {
                    Point center = rotatedRect.center;
                    double width = rotatedRect.size.width * resizeFactor;
                    double height = rotatedRect.size.height * resizeFactor;
                    int x = (int) (center.x * resizeFactor - width / 2);
                    if (x < 0) {
                        x = 0;
                    }
                    int y = (int) (center.y * resizeFactor - height / 2);
                    if (y < 0) {
                        y = 0;
                    }

                    Mat circleImage = frame.submat(new Rect(x, y, (int) width, (int) height));
                    Mat code = codeFinder.extractCode(circleImage);
                    if (code != null) {

                        Mat boundedCode = codeCleaner.getBoundedCode(code);
                        Mat cleanedCode = codeCleaner.cleanCode(boundedCode);
                        Core.bitwise_not(cleanedCode, cleanedCode);
                        BufferedImage bufferedImage = ImageUtils.matToBufferedImage(cleanedCode);
                        String text = dataMatrixInterpreter.decode(bufferedImage);
                        if (text != null) {
                            Point[] rect_points = new Point[4];
                            rotatedRect.points(rect_points);
                            Scalar color = new Scalar(0, 0, 255);
                            for (int j = 0; j < 4; j++) {
                                Point a = rect_points[j].clone();
                                a.x = a.x * resizeFactor;
                                a.y = a.y * resizeFactor;
                                Point b = rect_points[(j + 1) % 4].clone();
                                b.x = b.x * resizeFactor;
                                b.y = b.y * resizeFactor;
                                Imgproc.line(coloredFrame, a, b, color, 1);
                                BufferedImage image = ImageUtils.matToBufferedImage(coloredFrame);
                                final WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        canvas.getGraphicsContext2D().drawImage(fxImage, 0, 0);
                                    }
                                });
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showFrame(Mat frame) {
        BufferedImage image = ImageUtils.matToBufferedImage(frame);
        final WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                canvas.getGraphicsContext2D().drawImage(fxImage, 0, 0);
            }
        });
    }

    public void addTripletsInGraph(List<NodeTriplet> triplets) {
        if (graph == null) {
            graph = new Graph();
        }
        List<NodeTriplet> tripletsForAdd = new ArrayList<>();
        List<NodeTriplet> notAddedTriplets = new ArrayList<>(triplets);
        while (notAddedTriplets.size() != tripletsForAdd.size()) {
            tripletsForAdd = notAddedTriplets;
            notAddedTriplets = new ArrayList<>();
            for (NodeTriplet nodeTriplet : tripletsForAdd) {
                Node nodeA = nodeTriplet.getNodeA();
                Node nodeB = nodeTriplet.getNodeB();
                Node center = nodeTriplet.getCenter();
//                HashSet<Node>  allNodes = graph.getAllNodes();
//                Node equalsParentInGraph = NodeUtils.findEqualsNode(allNodes, center);
                boolean isAdded = graph.addNodes(nodeA, nodeB, center);
                if (!isAdded) {
                    notAddedTriplets.add(nodeTriplet);
                }
            }
        }
    }

    public HashMap<Node, Point> graphToPoints(PointTriplet pointBasis, NodeTriplet nodeBasis) {
        Node nodeA = nodeBasis.getNodeA();
        Node nodeB = nodeBasis.getNodeB();
        Node nodeCenter = nodeBasis.getCenter();
        HashSet<Node> allNodes = graph.getAllNodes();

        Node nodeAInGraph = NodeUtils.findEqualsNode(allNodes, nodeA);
        Node nodeBInGraph = NodeUtils.findEqualsNode(allNodes, nodeB);
        Node nodeCenterInGraph = NodeUtils.findEqualsNode(allNodes, nodeCenter);

        Graph.NodeAxe nodeAAxe = nodeCenterInGraph.getNeighborsAxe(nodeAInGraph);
        Graph.NodeAxe nodeBAxe = nodeCenterInGraph.getNeighborsAxe(nodeBInGraph);

        Point nodeAVector = PointUtils.minus(pointBasis.getCenter(), pointBasis.getPointA());
        Point nodeBVector = PointUtils.minus(pointBasis.getCenter(), pointBasis.getPointB());

        HashMap<Node, Point> result = new HashMap<>();
        result.put(nodeAInGraph, pointBasis.getPointA().clone());
        result.put(nodeBInGraph, pointBasis.getPointB().clone());
        result.put(nodeCenterInGraph, pointBasis.getCenter().clone());

        calculateCoordinateInStraightLine(result, nodeAInGraph, nodeCenterInGraph, nodeAVector);
        calculateCoordinateInStraightLine(result, nodeAInGraph, nodeCenterInGraph, PointUtils.turnOver(nodeAVector));
        calculateCoordinateInStraightLine(result, nodeBInGraph, nodeCenterInGraph, nodeBVector);
        calculateCoordinateInStraightLine(result, nodeBInGraph, nodeCenterInGraph, PointUtils.turnOver(nodeBVector));
        boolean wasAdded = true;
        while (wasAdded) {
            wasAdded = false;
            for (Node node : allNodes) {
                if (!result.keySet().contains(node)) {
                    Point point = getCoordinateForNode(node, result);
                    if (point != null) {
                        result.put(node, point);
                        wasAdded = true;
                    }
                }
            }
        }
        return result;
    }

    public Point getCoordinateForNode(Node node, HashMap<Node, Point> result) {
        ArrayList<Node> neighborsInAxeA = node.getNeighborsByAxe(Graph.NodeAxe.AXE_A);
        ArrayList<Node> neighborsInAxeB = node.getNeighborsByAxe(Graph.NodeAxe.AXE_B);
        if (neighborsInAxeA.isEmpty() || neighborsInAxeB.isEmpty()) {
            return null; // der node hat keine nachbarn
        }

        Node neighbor_1_A = neighborsInAxeA.get(0);
        Node neighbor_2_A = null;
        if (neighborsInAxeA.size() > 1) {
            neighbor_2_A = neighborsInAxeA.get(1);
        }

        Node neighbor_1_B = neighborsInAxeB.get(0);
        Node neighbor_2_B = null;
        if (neighborsInAxeB.size() > 1) {
            neighbor_2_B = neighborsInAxeB.get(1);
        }
        Point point = getCoordinateForNodeByNeighbors(node, neighbor_1_A, neighbor_1_B, result);
        if (point != null) {
            return point;
        }

        point = getCoordinateForNodeByNeighbors(node, neighbor_1_A, neighbor_2_B, result);
        if (point != null) {
            return point;
        }

        point = getCoordinateForNodeByNeighbors(node, neighbor_2_A, neighbor_1_B, result);
        if (point != null) {
            return point;
        }

        point = getCoordinateForNodeByNeighbors(node, neighbor_2_A, neighbor_2_B, result);
        if (point != null) {
            return point;
        }

        return null;
    }

    public Point getCoordinateForNodeByNeighbors(Node node, Node neighborA, Node neighborB, HashMap<Node, Point> result) {
        if (neighborA == null || neighborB == null || node == null) {
            return null;
        }
        if (result.keySet().contains(neighborA) && result.keySet().contains(neighborB)) {
            ArrayList<Node> diagonallyNeighbors_1 = NodeUtils.getJointNeighbors(neighborA, neighborB);
            if (diagonallyNeighbors_1 != null) {
                diagonallyNeighbors_1.remove(node);
                if (!diagonallyNeighbors_1.isEmpty()) {
                    Node diagonallyNeighbor = diagonallyNeighbors_1.get(0);
                    if (result.keySet().contains(diagonallyNeighbor)) {
                        Point resultPoint = calculateQuadratEdge(result.get(neighborA), result.get(diagonallyNeighbor), result.get(neighborB));
                        return resultPoint;
                    }
                }
            }
        }
        return null;
    }

    private Point calculateQuadratEdge(Point point1, Point point2, Point point3) {// point1 und point3 liegen auf der diagonal des quadrates!
        double x4 = point1.x + point3.x - point2.x;
        double y4 = point1.y + point3.y - point2.y;
        return new Point(x4, y4);
    }

    public void calculateCoordinateInStraightLine(HashMap<Node, Point> result, Node referencePoint, Node lastNeighbor, Point vector) {
        Node oppositeNeighbor = referencePoint.getOppositeNeighbor(lastNeighbor);
        Point lastPoint = vector;
        while (oppositeNeighbor != null) {
            lastPoint = PointUtils.plus(lastPoint, vector);
            result.put(oppositeNeighbor, lastPoint);
            lastNeighbor = referencePoint;
            referencePoint = oppositeNeighbor;
            oppositeNeighbor = referencePoint.getOppositeNeighbor(lastNeighbor);
        }
    }
}
