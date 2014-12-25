package sample.test;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import sample.utils.clustering.Cluster;
import sample.utils.clustering.DBSCANClusterer;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import static org.testng.Assert.*;

public class CircleFinderTest {

    @BeforeClass
    public static void loadLibrary() {
        System.setProperty("java.library.path", "./lib");
        Field fieldSysPath = null;
        try {
            fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExtractCircles() throws Exception {
        String fileName = "SC-Thermo-Tubes001.png";
        File file = new File(fileName);
        Mat source = Imgcodecs.imread(file.getAbsolutePath(), CvType.CV_8UC4);
        int resizeFactor = Math.min(source.rows(), source.cols()) / 1000;
        if (resizeFactor < 1) {
            resizeFactor = 1;
        }
        Mat resized = Mat.zeros(source.rows() / resizeFactor, source.cols() / resizeFactor, source.type());
        Imgproc.resize(source, resized, new Size(source.cols() / resizeFactor, source.rows() / resizeFactor), 0, 0, Imgproc.INTER_CUBIC);
        Imgcodecs.imwrite("resizedImage.bmp", resized);
        Mat binImage = new Mat(resized.rows(), resized.cols(), resized.type());
        Imgproc.GaussianBlur(resized, binImage, new Size(51, 51), 0, 0);
        Imgcodecs.imwrite("gaussianImage.bmp", binImage);
        Imgproc.equalizeHist(binImage, binImage);
        Imgcodecs.imwrite("equalizeImage.bmp", binImage);
        int blokSize = Math.min(binImage.cols(), binImage.rows()) / 14;
        if (blokSize % 2 == 0) {
            blokSize++;
        }
        System.out.println("blockSize = " + blokSize);
        Imgproc.adaptiveThreshold(binImage, binImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, blokSize, 0);
        Imgcodecs.imwrite("binImage.bmp", binImage);
        Imgproc.erode(binImage, binImage, new Mat(), new Point(-1, -1), 3);
        Imgcodecs.imwrite("erodeImage.bmp", binImage);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(binImage, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//        Imgproc.drawContours(resized, contours, -1, new Scalar(255,255,0));
//        Imgcodecs.imwrite("contours.bmp", resized);
        List<RotatedRect> allRotatedRects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            RotatedRect tempRotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            if (isQuadrat(tempRotatedRect)) {
                allRotatedRects.add(tempRotatedRect);
//                Point[] rect_points = new Point[4];
//                tempRotatedRect.points(rect_points);
//                Scalar color = new Scalar(0, 0, 255);
//                for (int j = 0; j < 4; j++) {
//                    Imgproc.line(resized, rect_points[j], rect_points[(j + 1) % 4], color, 1);
//                }
            }
        }

        LinkedList<RotatedRect> referenceRects = getReferenceRects(allRotatedRects);
        LinkedList<RotatedRect> circleAreas = calculateCircleAreas(referenceRects, binImage);
        for (RotatedRect rotatedRect : circleAreas) {
            Point[] rect_points = new Point[4];
            rotatedRect.points(rect_points);
            Scalar color = new Scalar(0, 0, 255);
            for (int j = 0; j < 4; j++) {
                Imgproc.line(resized, rect_points[j], rect_points[(j + 1) % 4], color, 1);
            }
        }


        Imgcodecs.imwrite("rects.bmp", resized);

    }

    public LinkedList<RotatedRect> calculateCircleAreas(LinkedList<RotatedRect> referenceRects, Mat image) {
        LinkedList<RotatedRect> tempResult = new LinkedList<>();
        Point[] cellVectors = calculateCellVectors(referenceRects);
        Point vectorA = cellVectors[0];
        Point vectorB = cellVectors[1];
        RotatedRect startRect = referenceRects.getFirst();

        tempResult.add(startRect);
        double maxDistance = Math.sqrt(image.cols() * image.cols() + image.rows() * image.rows());
        double distance = 0;
        LinkedList<RotatedRect> rectsInColumn = createRotatedRectsInColumn(image, startRect, vectorB, referenceRects);
        tempResult.addAll(rectsInColumn);
        double vectorALength = getVectorLength(vectorA);
        RotatedRect referenceRect = startRect;
        while (distance < maxDistance) {
            Point nextPoint = plus(referenceRect.center, vectorA);
            RotatedRect rect = findRotatedRect(referenceRects, nextPoint);
            if (rect == null) {
                rect = new RotatedRect(nextPoint, referenceRect.size, referenceRect.angle);
            }
            tempResult.add(rect);

            referenceRect = rect;
            rectsInColumn = createRotatedRectsInColumn(image, referenceRect, vectorB, referenceRects);
            tempResult.addAll(rectsInColumn);
            distance = distance + vectorALength;
        }
        distance = 0;
        referenceRect = startRect;
        while (distance < maxDistance) {
            Point nextPoint = minus(referenceRect.center, vectorA);
            RotatedRect rect = findRotatedRect(referenceRects, nextPoint);
            if (rect == null) {
                rect = new RotatedRect(nextPoint, referenceRect.size, referenceRect.angle);
            }
            tempResult.add(rect);

            referenceRect = rect;
            rectsInColumn = createRotatedRectsInColumn(image, referenceRect, vectorB, referenceRects);
            tempResult.addAll(rectsInColumn);
            distance = distance + vectorALength;
        }

        LinkedList<RotatedRect> result = new LinkedList<>();
        for(RotatedRect rotatedRect : tempResult){
            if(isInBounds(image, rotatedRect.center)){
                result.add(rotatedRect);
            }
        }
        return result;
    }

    private LinkedList<RotatedRect> createRotatedRectsInColumn(Mat image, RotatedRect startRect, Point vector, LinkedList<RotatedRect> referenceRects) {
        LinkedList<RotatedRect> result = new LinkedList<>();
        double maxDistance = Math.sqrt(image.cols() * image.cols() + image.rows() * image.rows());
        double distance = 0;
        double vectorLength = getVectorLength(vector);
        RotatedRect referenceRect = startRect;
        while (distance < maxDistance) {
            Point nextPoint = plus(referenceRect.center, vector);
            RotatedRect rect = findRotatedRect(referenceRects, nextPoint);
            if (rect == null) {
                rect = new RotatedRect(nextPoint, startRect.size, startRect.angle);
            }
            result.add(rect);

            referenceRect = rect;
            distance = distance + vectorLength;
        }
        distance = 0;
        referenceRect = startRect;
        while (distance < maxDistance) {
            Point nextPoint = minus(referenceRect.center, vector);
            RotatedRect rect = findRotatedRect(referenceRects, nextPoint);
            if (rect == null) {
                rect = new RotatedRect(nextPoint, startRect.size, startRect.angle);
            }
            result.add(rect);
            referenceRect = rect;
            distance = distance + vectorLength;
        }
        return result;
    }

    private double getDistance(Point a, Point b) {
        double xDiff = a.x - b.x;
        double yDiff = a.y - b.y;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    private double getVectorLength(Point point) {
        return Math.sqrt(point.x * point.x + point.y * point.y);
    }

    public RotatedRect findRotatedRect(LinkedList<RotatedRect> rotatedRects, Point point) {
        for (RotatedRect rotatedRect : rotatedRects) {
            double width = rotatedRect.size.width;
            double height = rotatedRect.size.height;
            double diagonal = Math.sqrt(width * width + height * height);
            if (getDistance(rotatedRect.center, point) < diagonal / 2) {
                return rotatedRect;
            }
        }
        return null;
    }

    public Point plus(Point a, Point b) {
        return new Point(a.x + b.x, a.y + b.y);
    }

    public Point minus(Point a, Point b) {
        return new Point(a.x - b.x, a.y - b.y);
    }

    public boolean isInBounds(Mat mat, Point point) {
        return point.x < mat.cols() && point.x >= 0 && point.y >= 0 && point.y < mat.rows();
    }

    public Point[] calculateCellVectors(LinkedList<RotatedRect> referenceRects) {
        Point[] result = new Point[2];
        ArrayList<Point> points = new ArrayList<>();
        for (RotatedRect rotatedRect : referenceRects) {
            points.add(rotatedRect.center);
        }

        ArrayList<Point> differenceVectoren = calculateDifferenceVectoren(points);
        double maxDistanceInCluster = referenceRects.getFirst().size.width / 10;
        DBSCANClusterer dbscanClusterer = new DBSCANClusterer(maxDistanceInCluster, 5);
        List<Cluster<Point>> clusters = dbscanClusterer.cluster(differenceVectoren);
        Collections.sort(clusters, new Comparator<Cluster<Point>>() {
            @Override
            public int compare(Cluster<Point> o1, Cluster<Point> o2) {
                return o2.getPoints().size() - o1.getPoints().size();
            }
        });

        Point point_0 = calculateClusterCenter(clusters.get(0));
//        Point point_1 = calculateClusterCenter(clusters.get(1));
//        double angleDiff = Math.abs(90 - getAngleBetweenVectors(point_0, point_1));
//        double vectorLength = getVectorLength(point_1);
        ArrayList<Point> candidates = new ArrayList<>();
        for (int i = 1; i < clusters.size(); i++) {
            Cluster<Point> tempCluster = clusters.get(i);
            Point tempPoint = calculateClusterCenter(tempCluster);
//            double tempVectorLength = getVectorLength(tempPoint);
            double tempAngleDiff = Math.abs(90 - getAngleBetweenVectors(point_0, tempPoint));
            double maxAngleDiff = 10;
            if(tempAngleDiff < maxAngleDiff){
                candidates.add(tempPoint);
            }
        }

        Collections.sort(candidates, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return (int) (getVectorLength(o1) - getVectorLength(o2));
            }
        });
        result[0] = point_0;
        result[1] = candidates.get(0);
        return result;
    }

    //in degrees
    public double getAngleBetweenVectors(Point a, Point b) {
        double nenner = a.x * b.x + a.y * b.y;
        double zeller = getVectorLength(a) * getVectorLength(b);
        double cosA = nenner / zeller;
        double arccos = Math.acos(cosA);
        return Math.toDegrees(arccos);
    }

    public Point calculateClusterCenter(Cluster<Point> cluster) {
        List<Point> points = cluster.getPoints();
        double x = 0;
        double y = 0;
        for (Point point : points) {
            x = x + point.x;
            y = y + point.y;
        }
        return new Point(x / points.size(), y / points.size());
    }

    private ArrayList<Point> calculateDifferenceVectoren(ArrayList<Point> points) {
        ArrayList<Point> result = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            Point referencePoint = points.get(i);
            for (int j = i + 1; j < points.size(); j++) {
                Point point = points.get(j);
                Point vector = minus(referencePoint, point);
                result.add(vector);
                Point negativVector = new Point(-vector.x, -vector.y);
                result.add(negativVector);
            }
        }
        return result;
    }

    public boolean isQuadrat(RotatedRect rect) {
        double minFactor = 0.85;
        Size size = rect.size;
        double min = Math.min(size.width, size.height);
        double max = Math.max(size.width, size.height);
        double factor = min / max;
        return factor > minFactor;
    }

    public LinkedList<RotatedRect> getReferenceRects(List<RotatedRect> allRotatedRects) {
        double maxFactor = 0.8;
        LinkedList<RotatedRect> copyRects = new LinkedList<>(allRotatedRects);
        Collections.sort(copyRects, new Comparator<RotatedRect>() {
            @Override
            public int compare(RotatedRect o1, RotatedRect o2) {
                double area1 = o1.size.area();
                double area2 = o2.size.area();
                if (area1 > area2) {
                    return 1;
                } else if (area1 < area2) {
                    return -1;
                }
                return 0;
            }
        });
        RotatedRect result = null;
        boolean found = false;
        while (copyRects.size() > 3 && !found) {
            result = copyRects.get(copyRects.size() / 2);
            RotatedRect firstRect = copyRects.getFirst();
            RotatedRect lastRect = copyRects.getLast();
            found = true;
            if (firstRect.size.area() / result.size.area() < maxFactor) {
                copyRects.removeFirst();
                found = false;
            }
            if (result.size.area() / lastRect.size.area() < maxFactor) {
                copyRects.removeLast();
                found = false;
            }
        }
        return copyRects;
    }
}