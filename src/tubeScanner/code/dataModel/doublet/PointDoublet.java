package tubeScanner.code.dataModel.doublet;

import org.opencv.core.Point;

/**
 * Created by Alex on 13.03.2015.
 */
public class PointDoublet {
    private Point pointA;
    private Point pointB;

    public PointDoublet(Point pointA, Point pointB) {
        this.pointA = pointA;
        this.pointB = pointB;
    }

    public Point getPointA() {
        return pointA;
    }

    public void setPointA(Point pointA) {
        this.pointA = pointA;
    }

    public Point getPointB() {
        return pointB;
    }

    public void setPointB(Point pointB) {
        this.pointB = pointB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PointDoublet that = (PointDoublet) o;

        if (!pointA.equals(that.pointA) && !pointA.equals(that.pointB)) return false;
        if (!pointB.equals(that.pointB) && !pointB.equals(that.pointA)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pointA.hashCode() + pointB.hashCode();
        return result;
    }
}
