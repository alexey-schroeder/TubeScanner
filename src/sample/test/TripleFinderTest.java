package sample.test;

import org.opencv.core.Point;
import org.testng.Assert;
import org.testng.annotations.Test;
import sample.utils.PointTripleFinder;
import sample.utils.PointTriplet;

import java.util.ArrayList;
import java.util.List;

public class TripleFinderTest {

    @Test
    public void testFindTriplets() throws Exception {
        Point pointA = new Point(5, 5);
        Point pointB = new Point(2.5, 5);
        Point pointC = new Point(7.5, 5);
        Point pointD = new Point(7.5, 2.5);
        Point pointE = new Point(5, 2.5);
        Point pointF = new Point(2.5, 2.5);
        Point pointK = new Point(5, 0);
        Point pointL = new Point(5, 7.5);
        List<Point> points = new ArrayList<>();
        points.add(pointA);
        points.add(pointB);
        points.add(pointC);
        points.add(pointD);
        points.add(pointE);
        points.add(pointF);
        points.add(pointK);
        points.add(pointL);

        Point[] vectors = new Point[]{new Point(0, 2.5), new Point(2.5, 0)};
        PointTripleFinder tripleFinder = new PointTripleFinder();
        ArrayList<PointTriplet> triplets = tripleFinder.findTriplets(points, vectors);

        PointTriplet tripletA = new PointTriplet(pointB.clone(), pointC.clone(), pointA);
        PointTriplet tripletB = new PointTriplet(pointF.clone(), pointD.clone(), pointE);
        PointTriplet tripletC = new PointTriplet(pointA.clone(), pointK.clone(), pointE);
        PointTriplet tripletD = new PointTriplet(pointL.clone(), pointE.clone(), pointA);
        PointTriplet tripletE = new PointTriplet(pointL.clone(), pointE.clone(), pointC);

        Assert.assertTrue(triplets.size() == 4);
        Assert.assertTrue(triplets.contains(tripletA));
        Assert.assertTrue(triplets.contains(tripletB));
        Assert.assertTrue(triplets.contains(tripletC));
        Assert.assertTrue(triplets.contains(tripletD));
        Assert.assertFalse(triplets.contains(tripletE));
    }

    @Test
    public void testFilterDublicateTriplets() throws Exception {
        Point pointA = new Point(5, 5);
        Point pointB = new Point(2.5, 5);
        Point pointC = new Point(7.5, 5);
        Point pointD = new Point(7.5, 2.5);
        Point pointE = new Point(5, 2.5);
        Point pointF = new Point(2.5, 2.5);
        Point pointK = new Point(5, 0);
        Point pointL = new Point(5, 7.5);
        List<Point> points = new ArrayList<>();
        points.add(pointA);
        points.add(pointB);
        points.add(pointC);
        points.add(pointD);
        points.add(pointE);
        points.add(pointF);
        points.add(pointK);
        points.add(pointL);

        PointTriplet tripletA = new PointTriplet(pointB.clone(), pointC.clone(), pointA);
        PointTriplet tripletB = new PointTriplet(pointC.clone(), pointB.clone(), pointA.clone());
        PointTriplet tripletC = new PointTriplet(pointA.clone(), pointK.clone(), pointE);
        PointTriplet tripletD = new PointTriplet(pointL.clone(), pointE.clone(), pointA);
        PointTriplet tripletE = new PointTriplet(pointL.clone(), pointE.clone(), pointC);

        List<PointTriplet> triplets = new ArrayList<>();
        triplets.add(tripletA);
        triplets.add(tripletB);
        triplets.add(tripletC);
        triplets.add(tripletD);
        triplets.add(tripletE);

        PointTripleFinder tripleFinder = new PointTripleFinder();
        ArrayList<PointTriplet> filteredTriplets = tripleFinder.filterDublicateTriplets(triplets);

        Assert.assertTrue(filteredTriplets.size() == 4);
        Assert.assertTrue(filteredTriplets.contains(tripletC));
        Assert.assertTrue(filteredTriplets.contains(tripletD));
        Assert.assertTrue(filteredTriplets.contains(tripletE));
    }

    @Test
    public void testFindNeighbor() throws Exception {
        Point pointA = new Point(5, 5);
        Point pointB = new Point(2.5, 5);
        Point pointC = new Point(7.5, 5);
        Point pointD = new Point(7.5, 2.5);
        Point pointE = new Point(5, 2.5);
        Point pointF = new Point(2.5, 2.5);

        List<Point> points = new ArrayList<>();
        points.add(pointA);
        points.add(pointB);
        points.add(pointC);
        points.add(pointD);
        points.add(pointE);
        points.add(pointF);

        PointTripleFinder tripleFinder = new PointTripleFinder();
        ArrayList<Point> neighbors = tripleFinder.findNeighbor(pointA, points, 2.44, 0.07);
        Assert.assertTrue(neighbors.contains(pointB));
        Assert.assertTrue(neighbors.contains(pointC));
        Assert.assertTrue(neighbors.contains(pointE));

        Assert.assertFalse(neighbors.contains(pointF));
        Assert.assertFalse(neighbors.contains(pointD));

        neighbors = tripleFinder.findNeighbor(pointB, points, 2.44, 0.07);
        Assert.assertTrue(neighbors.contains(pointA));
        Assert.assertTrue(neighbors.contains(pointF));

        Assert.assertFalse(neighbors.contains(pointE));
        Assert.assertFalse(neighbors.contains(pointD));
        Assert.assertFalse(neighbors.contains(pointC));
    }

    @Test
    public void testFindThirdPoint() throws Exception {
        Point pointA = new Point(5, 5);
        Point pointB = new Point(2.5, 5);
        Point pointC = new Point(7.5, 5);
        Point pointD = new Point(7.5, 2.5);
        Point pointE = new Point(5, 2.5);
        Point pointF = new Point(2.5, 2.5);
        List<Point> points = new ArrayList<>();
        points.add(pointA);
        points.add(pointB);
        points.add(pointC);
        points.add(pointD);
        points.add(pointE);
        points.add(pointF);
        PointTripleFinder tripleFinder = new PointTripleFinder();
        ArrayList<Point> thirdPoints = tripleFinder.findThirdPoint(pointA, pointB, points, 2.44, 0.07);
        Assert.assertTrue(thirdPoints.size() == 1);
        Assert.assertTrue(thirdPoints.contains(pointC));

        thirdPoints = tripleFinder.findThirdPoint(pointA, pointE, points, 2.44, 0.07);
        Assert.assertTrue(thirdPoints.isEmpty());

        thirdPoints = tripleFinder.findThirdPoint(pointE, pointD, points, 2.44, 0.07);
        Assert.assertTrue(thirdPoints.size() == 1);
        Assert.assertTrue(thirdPoints.contains(pointF));

        Point pointK = new Point(5, 0);
        Point pointL = new Point(5, 7.5);
        points.add(pointK);
        points.add(pointL);

        thirdPoints = tripleFinder.findThirdPoint(pointE, pointA, points, 2.44, 0.07);
        Assert.assertTrue(thirdPoints.size() == 2);
        Assert.assertTrue(thirdPoints.contains(pointK));
        Assert.assertTrue(thirdPoints.contains(pointL));
    }

    @Test
    public void testCreateTriplet() throws Exception {
        Point pointA = new Point(5, 5);
        Point pointB = new Point(2.5, 5);
        Point pointC = new Point(7.5, 5);
        PointTripleFinder tripleFinder = new PointTripleFinder();
        PointTriplet referenceTriplet = new PointTriplet(pointB.clone(), pointC.clone(), pointA.clone());
        PointTriplet tripletA = tripleFinder.createTriplet(pointA, pointB, pointC);
        PointTriplet tripletB = tripleFinder.createTriplet(pointA.clone(), pointB.clone(), pointC.clone());

        Assert.assertTrue(referenceTriplet.equals(tripletA));
        Assert.assertTrue(referenceTriplet.equals(tripletB));
    }
}