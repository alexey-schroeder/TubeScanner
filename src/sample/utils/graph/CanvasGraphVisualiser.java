package sample.utils.graph;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.opencv.core.Point;
import sample.utils.PointUtils;

import java.util.HashMap;

/**
 * Created by Alex on 01.03.2015.
 */
public class CanvasGraphVisualiser {
    private Canvas canvas;
    private HashMap<Node, Point> nodeCoordinates;
    private int defaultRadius = 20;
    private double scaleFactor = 1;
    private double graphCenterX;
    private double graphCenterY;
    private double width;
    private double height;
    private double rand = 20;
    private Point canvasCenter;

    public Canvas getCanvas() {
        return canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        width = canvas.getWidth();
        height = canvas.getHeight();
        canvasCenter = new Point(width / 2, height / 2);
    }

    public HashMap<Node, Point> getNodeCoordinates() {
        return nodeCoordinates;
    }

    public void setNodeCoordinates(HashMap<Node, Point> nodeCoordinates) {
        this.nodeCoordinates = nodeCoordinates;
    }

    // Achtung! Canvas hat Zero point in dem left-oberee Ecke. Um richtig zu zoommenmüssen wir zuesrt in system rechnen, wo der zero point in zentrum von canvas ist.
    public void drawGraph() {
        calculateGraphScaleAndCenter();
        double shiftX = width / 2 - graphCenterX; // differenze zwischen zentrum von canvas und zentrum von graph
        double shiftY = height / 2 - graphCenterY;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        gc.setStroke(Color.RED);
        gc.strokeRect(0, 0, width, height);
        gc.setStroke(Color.GREEN);
        double currentRadius = defaultRadius * scaleFactor;
        for (Node node : nodeCoordinates.keySet()) {
            Point point = nodeCoordinates.get(node);
            Point shiftedPoint = new Point(point.x + shiftX, point.y + shiftY);// der graph ist jetzt zentriert
            // um richtig zu zoommen müssen wir zuerst in koordinat system rechnen, das sein zentrum in der mitte des canvas hat
            Point vector = PointUtils.minus(shiftedPoint, canvasCenter);
            Point scaledVector = PointUtils.multWithFactor(vector, scaleFactor);
            // hier wird zurück aus dem koordinat system mit zentrum in Canvas-zentrum in system mit anfang in linkem oberem ecke gerechnet
            Point scaledShiftedPoint = PointUtils.plus(canvasCenter, scaledVector);
            gc.strokeOval(scaledShiftedPoint.x - currentRadius, scaledShiftedPoint.y - currentRadius, currentRadius * 2, currentRadius * 2);
        }
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

        if (scaleX > 1 && scaleY > 1) {
            scaleFactor = Math.min(scaleX, scaleY);
        } else if (scaleX < 1 && scaleY < 1) {
            scaleFactor = Math.max(scaleX, scaleY);
        } else {
            scaleFactor = Math.min(scaleX, scaleY);
        }
        System.out.println(scaleX + " / " + scaleY + " / " + scaleFactor);
    }
}
