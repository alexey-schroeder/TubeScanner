package sample.test;

import org.opencv.core.Point;
import org.testng.Assert;
import org.testng.annotations.Test;
import sample.utils.PointUtils;

import static org.testng.Assert.*;

public class PointUtilsTest {

    @Test
    public void testPlus() throws Exception {

    }

    @Test
    public void testMinus() throws Exception {

    }

    @Test
    public void testGetAngleBetweenVectors() throws Exception {

    }

    @Test
    public void testGetVectorLength() throws Exception {

    }

    @Test
    public void testGetDistance() throws Exception {

    }

    @Test
    public void testMultWithFactor() throws Exception {

    }

    @Test
    public void testFlipByY() throws Exception {

    }

    @Test
    public void testTurnOver() throws Exception {

    }

    @Test
    public void testGetPerpendicularVector() throws Exception {

    }

    @Test
    public void testRotateVector() throws Exception {

    }

    @Test
    public void testCalculateQuadratEdge() throws Exception {
        Point point1 = new Point(1, 1);
        Point point2 = new Point(2, 1);
        Point point3 = new Point(2, 2);
        Point point4 = new Point(1, 2);

        Point result = PointUtils.calculateQuadratEdge(point1, point2, point3);

        Assert.assertEquals(point4, result);

        result = PointUtils.calculateQuadratEdge(point4, point1, point2);
        Assert.assertEquals(point3, result);

        result = PointUtils.calculateQuadratEdge(point4, point3, point2);
        Assert.assertEquals(point1, result);
    }
}