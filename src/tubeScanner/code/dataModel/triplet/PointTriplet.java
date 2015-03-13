package tubeScanner.code.dataModel.triplet;

import org.opencv.core.Point;
import tubeScanner.code.utils.ImageUtils;

/**
 * Created by Alex on 09.02.2015.
 */
public class PointTriplet {
    private Point center;
    private Point pointA;
    private Point pointB;

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
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

    public PointTriplet(Point pointA, Point pointB, Point center) {
        this.center = center;
        this.pointA = pointA;
        this.pointB = pointB;
        if (ImageUtils.arePointsEquals(center, pointA) ||
                ImageUtils.arePointsEquals(center, pointB) ||
                ImageUtils.arePointsEquals(pointA, pointB)) {
            throw new RuntimeException("the points in triplet are equals! " + toString());
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PointTriplet triplet = (PointTriplet) o;

        if (!ImageUtils.arePointsEquals(center, triplet.center)) return false;
        if (!ImageUtils.arePointsEquals(pointA, triplet.pointA) && !ImageUtils.arePointsEquals(pointA, triplet.pointB))
            return false;
        if (!ImageUtils.arePointsEquals(pointB, triplet.pointA) && !ImageUtils.arePointsEquals(pointB, triplet.pointB))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = center.hashCode();
        result = 31 * result + pointA.hashCode();
        result = 31 * result + pointB.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PointTriplet{" +
                "center=" + center +
                ", pointA=" + pointA +
                ", pointB=" + pointB +
                '}';
    }
}
