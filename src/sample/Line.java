package sample;

import org.opencv.core.Point;

/**
 * Created by Alex on 17.11.2014.
 */
public class Line {
    private Point point_1;
    private Point point_2;

    public Line(Point point_1, Point point_2) {
        this.point_1 = point_1;
        this.point_2 = point_2;
    }

    public Point getPoint_1() {
        return point_1;
    }

    public void setPoint_1(Point point_1) {
        this.point_1 = point_1;
    }

    public Point getPoint_2() {
        return point_2;
    }

    public void setPoint_2(Point point_2) {
        this.point_2 = point_2;
    }
}
