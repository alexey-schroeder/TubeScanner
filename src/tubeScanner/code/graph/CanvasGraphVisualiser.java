package tubeScanner.code.graph;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import org.opencv.core.Point;
import tubeScanner.code.utils.PointUtils;

import java.util.HashMap;

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
                System.out.println("click");
            }
        };
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
        gc.setStroke(Color.RED);
        gc.strokeOval(point.x - currentRadius, point.y - currentRadius, currentRadius * 2, currentRadius * 2);
    }

    private void unMarkNode(Point point) {
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

    public void markNodeByCode(String code) {
        setDefaultColorToAllNodes();
        for (Point point : graphNodeInCanvasMap.keySet()) {
            Node node = graphNodeInCanvasMap.get(point);
            if (node.getCode().equalsIgnoreCase(code)) {
                markNode(point);
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
}
