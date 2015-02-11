package sample.test;

import junit.framework.Assert;
import org.opencv.core.Point;
import org.testng.annotations.Test;
import sample.utils.PointTriplet;

public class TripletTest {

    @Test
    public void testEquals() throws Exception {
        Point pointA = new Point(5, 5);
        Point pointB = new Point(2.5, 5);
        Point pointC = new Point(7.5, 5);
        Point pointD = new Point(7.7, 5);

        Point pointA_2 = new Point(5, 5);
        Point pointB_2 = new Point(2.5, 5);
        Point pointC_2 = new Point(7.5, 5);

        PointTriplet tripletA = new PointTriplet(pointA, pointB, pointC);
        PointTriplet tripletB = new PointTriplet(pointA_2, pointB_2, pointC_2);
        Assert.assertTrue(tripletA.equals(tripletB));
        Assert.assertTrue(tripletB.equals(tripletA));

        PointTriplet tripletC = new PointTriplet(pointA, pointB, pointD);
        Assert.assertFalse(tripletA.equals(tripletC));
        Assert.assertFalse(tripletC.equals(tripletA));

        PointTriplet tripletD = new PointTriplet(pointB, pointA, pointC);
        Assert.assertTrue(tripletD.equals(tripletA));
        Assert.assertTrue(tripletD.equals(tripletB));
    }
}