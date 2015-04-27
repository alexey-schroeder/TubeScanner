package tubeScanner.code.graph;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.opencv.core.Point;
import tubeScanner.Controller;
import tubeScanner.code.events.SearchCodeEvent;
import tubeScanner.code.utils.PointUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Alex on 01.03.2015.
 */
public class CanvasGraphVisualiser {
    private Canvas canvas;
    private HashMap<Node, Point> nodeCoordinates;
    private int defaultRadius = 20;
    private double currentRadius;
    private double scaleFactor = 1;
    private double graphCenterX;
    private double graphCenterY;
    private double width;
    private double height;
    private double rand = 20;
    private Point canvasCenter;
    private HashMap<Point, Node> graphNodeInCanvasMap;
    private Tooltip tooltip;
    private Point lastPointWithTooltip;
    private boolean stopShowByException = false;
    private EventHandler<SearchCodeEvent> searchCodeEventEventHandler;
    private Controller controller;

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        width = canvas.getWidth();
        height = canvas.getHeight();
        canvasCenter = new Point(width / 2, height / 2);
        graphNodeInCanvasMap = new HashMap<>();
        EventHandler<MouseEvent> mouseMovedListener = getMouseMovedListener();
        canvas.setOnMouseMoved(mouseMovedListener);

        EventHandler<MouseEvent> mouseClickedListener = getMouseClickedListener();
        canvas.setOnMouseClicked(mouseClickedListener);
    }

    protected EventHandler<MouseEvent> getMouseClickedListener() {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Point point = getNodePointByCoordinaten(event.getX(), event.getY());
                MouseButton clickedButton = event.getButton();
                switch (clickedButton) {
                    case PRIMARY:
                        primaryMouseButtonClicked(point);
                        break;
                    case SECONDARY:
                        secondaryMouseButtonClicked(point, event);
                        break;
                }
            }
        };
    }

    private void secondaryMouseButtonClicked(Point point, MouseEvent event) {
        Node node = graphNodeInCanvasMap.get(point);
        if (node != null) {
            ContextMenu cm = new ContextMenu();
            ArrayList<Tooltip> tooltips = new ArrayList<Tooltip>();
            EventHandler<MouseEvent> onEntered =  new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {

                    NodeCoordinateInTubeCalculator nodeCoordinateInTubeCalculator = new NodeCoordinateInTubeCalculator();
                    nodeCoordinateInTubeCalculator.setGraph(controller.getGraph());
                    nodeCoordinateInTubeCalculator.setZeroPoint(node);
                    nodeCoordinateInTubeCalculator.setCharAxe(Graph.NodeAxe.AXE_A);

                    for (Point tempPoint : graphNodeInCanvasMap.keySet()) {
                        Node tempNode = graphNodeInCanvasMap.get(tempPoint);
                        TubeCoordinate tubeCoordinate = nodeCoordinateInTubeCalculator.getTubeCoordinateForNode(tempNode);
                        if(tubeCoordinate != null) {
                            String text = tubeCoordinate.getCharCoordinate() + " : " + tubeCoordinate.getNumberCoordinate();
                            Tooltip tempTooltip = new Tooltip(text);
                            tooltips.add(tempTooltip);
                            Point2D  coordinate = canvas.localToScreen(tempPoint.x, tempPoint.y);
                            tempTooltip.show(canvas,coordinate.getX(), coordinate.getY());
                            System.out.println(text);
                        }

                    }
                }
            };
            Line line1 = new Line(60, 10, 150, 10);
            final Line line2 = new Line(60, 30, 150, 50);
            MenuItem cmItem1 = getMenuItemForLine("A : 1", line1, onEntered);
            MenuItem cmItem2 = getMenuItemForLine("1 : A", line2, onEntered);

//            cmItem1.setOnAction(new EventHandler<ActionEvent>() {
//                public void handle(ActionEvent e) {
//                    NodeCoordinateInTubeCalculator nodeCoordinateInTubeCalculator = new NodeCoordinateInTubeCalculator();
//                    nodeCoordinateInTubeCalculator.setGraph(controller.getGraph());
//                    nodeCoordinateInTubeCalculator.setCharAxe(Graph.NodeAxe.AXE_A);
//                    nodeCoordinateInTubeCalculator.setZeroPoint(node);
//                }
//            });

            cm.getItems().add(cmItem1);
            cm.getItems().add(cmItem2);
            cm.show(canvas, event.getScreenX(), event.getScreenY());
        }
    }

    private MenuItem getMenuItemForLine(String menuName, final Line line, EventHandler<MouseEvent> eventEventHandler) {

        Label menuLabel = new Label(menuName);
        // apply style to occupy larger space for label
        menuLabel.setStyle("-fx-padding: 5 10 5 10");
        MenuItem mi = new MenuItem();
        mi.setGraphic(menuLabel);
        line.setStroke(Color.BLUE);
        menuLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, eventEventHandler);
        menuLabel.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                line.setStroke(Color.RED);
                DropShadow ds = new DropShadow();
                line.setEffect(ds);
                System.out.println("in");
            }
        });

        menuLabel.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                line.setStroke(Color.BLUE);
                line.setEffect(null);
                System.out.println("out");
            }
        });

        return mi;
    }

    private void primaryMouseButtonClicked(Point point) {
        lastPointWithTooltip = point;
        Node node = graphNodeInCanvasMap.get(point);
        String code = null;
        if (node != null) {
            code = node.getCode();
            unMarkNode(point);
            markNode(point);
        }
        if (searchCodeEventEventHandler != null) {
            SearchCodeEvent codeEvent = new SearchCodeEvent(code);
            searchCodeEventEventHandler.handle(codeEvent);
        }
    }

    protected EventHandler<MouseEvent> getMouseMovedListener() {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Point point = getNodePointByCoordinaten(event.getX(), event.getY());

                if (point != null) {
                    lastPointWithTooltip = point;
                    unMarkNode(point);
                    Node node = graphNodeInCanvasMap.get(point);
                    markNode(point);
                    if (tooltip != null) {
                        if (!tooltip.getText().equalsIgnoreCase(node.getCode())) { // das ist ein anderes node
                            tooltip.setText(node.getCode());
                        }
                        tooltip.setAnchorX(event.getScreenX() + 20);
                        tooltip.setAnchorY(event.getScreenY());
                    } else {
                        tooltip = new Tooltip(node.getCode());
                        tooltip.show(canvas, event.getScreenX() + 20, event.getScreenY());
                    }
                } else {
                    if (lastPointWithTooltip != null) {
                        unMarkNode(lastPointWithTooltip);
                    }
                    if (tooltip != null) {
                        tooltip.hide();
                        tooltip = null; // todo auf null setzen ist eigentlich falsch, man muss den tooltip einfach wieder erscheinen lassen
                    }
                }
            }
        };
    }

    private void markNode(Point point) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.YELLOW);
        gc.strokeOval(point.x - currentRadius, point.y - currentRadius, currentRadius * 2, currentRadius * 2);
    }

    private void markNode(Point point, Color color) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setStroke(color);
                gc.strokeOval(point.x - currentRadius, point.y - currentRadius, currentRadius * 2, currentRadius * 2);
            }
        });

    }

    public void unMarkNode(Point point) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(point.x - currentRadius, point.y - currentRadius, currentRadius * 2, currentRadius * 2);
        gc.setStroke(Color.GREEN);
        gc.strokeOval(point.x - currentRadius, point.y - currentRadius, currentRadius * 2, currentRadius * 2);
    }

    private Point getNodePointByCoordinaten(double x, double y) {
        for (Point point : graphNodeInCanvasMap.keySet()) {
            if (point.x - currentRadius <= x && point.x + currentRadius >= x && point.y - currentRadius <= y && point.y + currentRadius >= y) {
                return point;
            }
        }
        return null;
    }

    public void setMarkedNodeByCode(String code) {
        setDefaultColorToAllNodes();
        for (Point point : graphNodeInCanvasMap.keySet()) {
            Node node = graphNodeInCanvasMap.get(point);
            if (node.getCode().equalsIgnoreCase(code)) {
                markNode(point);
            }
        }
    }

    public void markNodeByCode(String code) {
        for (Point point : graphNodeInCanvasMap.keySet()) {
            Node node = graphNodeInCanvasMap.get(point);
            if (node.getCode().equalsIgnoreCase(code)) {
                markNode(point, Color.RED);
            }
        }
    }

    public void unmarkNodeByCode(String code) {
        for (Point point : graphNodeInCanvasMap.keySet()) {
            Node node = graphNodeInCanvasMap.get(point);
            if (node.getCode().equalsIgnoreCase(code)) {
                markNode(point, Color.GREEN);
            }
        }
    }

    private void setDefaultColorToAllNodes() {
        for (Point point : graphNodeInCanvasMap.keySet()) {
            unMarkNode(point);
        }
    }

    public HashMap<Node, Point> getNodeCoordinates() {
        return nodeCoordinates;
    }

    public void setNodeCoordinates(HashMap<Node, Point> nodeCoordinates) {
        this.nodeCoordinates = nodeCoordinates;
    }

    // Achtung! Canvas hat Zero point in dem left-oberee Ecke. Um richtig zu zoommenmüssen wir zuesrt in system rechnen, wo der zero point in zentrum von canvas ist.
    public synchronized void drawGraph() {
        if (stopShowByException) {
            return;
        }
        calculateGraphScaleAndCenter();
        graphNodeInCanvasMap.clear();
        double shiftX = width / 2 - graphCenterX; // differenze zwischen zentrum von canvas und zentrum von graph
        double shiftY = height / 2 - graphCenterY;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(5);
        gc.clearRect(0, 0, width, height);
        gc.setStroke(Color.RED);
        gc.strokeRect(0, 0, width, height);
        gc.setStroke(Color.GREEN);
        currentRadius = defaultRadius * scaleFactor;
        for (Node node : nodeCoordinates.keySet()) {
            Point point = nodeCoordinates.get(node);
            Point shiftedPoint = new Point(point.x + shiftX, point.y + shiftY);// der graph ist jetzt zentriert
            // um richtig zu zoommen müssen wir zuerst in koordinat system rechnen, das sein zentrum in der mitte des canvas hat
            Point vector = PointUtils.minus(shiftedPoint, canvasCenter);
            Point scaledVector = PointUtils.multWithFactor(vector, scaleFactor);
            // hier wird zurück aus dem koordinat system mit zentrum in Canvas-zentrum in system mit anfang in linkem oberem ecke gerechnet
            Point scaledShiftedPoint = PointUtils.plus(canvasCenter, scaledVector);
            boolean isPointValid = checkNodeCoordinate(scaledShiftedPoint); // passt  point in canvas?

            gc.strokeOval(scaledShiftedPoint.x - currentRadius, scaledShiftedPoint.y - currentRadius, currentRadius * 2, currentRadius * 2);
            String code = node.getCode();
            String textToShow = code.substring(code.length() - 3);
            gc.fillText(textToShow, scaledShiftedPoint.x - 5, scaledShiftedPoint.y);
            if (!isPointValid) {
                stopShowByException = true;
                String exceptionString = "Error in drawGraph, point is out of the canvas(" + width + " x " + height + "). "
                        + scaledShiftedPoint + ", currentRadius = " + currentRadius + ", scaleFactor = " + scaleFactor;
                throw new RuntimeException(exceptionString);
            }
            graphNodeInCanvasMap.put(scaledShiftedPoint, node);
        }
    }

    public boolean checkNodeCoordinate(Point scaledShiftedPoint) {
        boolean isValid = scaledShiftedPoint.x - currentRadius >= 0 &&
                scaledShiftedPoint.x + currentRadius <= width &&
                scaledShiftedPoint.y - currentRadius >= 0 &&
                scaledShiftedPoint.y + currentRadius <= height;
        return isValid;
    }

    public void calculateGraphScaleAndCenter() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        for (Node node : nodeCoordinates.keySet()) {
            Point point = nodeCoordinates.get(node);
            minX = Math.min(point.x - defaultRadius, minX);
            minY = Math.min(point.y - defaultRadius, minY);
            maxX = Math.max(point.x + defaultRadius, maxX);
            maxY = Math.max(point.y + defaultRadius, maxY);
        }

        double w = maxX - minX;
        double h = maxY - minY;

        graphCenterX = minX + (w / 2);
        graphCenterY = minY + (h / 2);

        double scaleX = (width - 2 * rand) / w;
        double scaleY = (height - 2 * rand) / h;

        scaleFactor = Math.min(scaleX, scaleY);
//        System.out.println(scaleX + " / " + scaleY + " / " + scaleFactor);
    }

    public void setSearchCodeEventEventHandler(EventHandler<SearchCodeEvent> searchCodeEventEventHandler) {
        this.searchCodeEventEventHandler = searchCodeEventEventHandler;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }
}
