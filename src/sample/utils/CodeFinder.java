package sample.utils;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import sample.utils.clustering.Cluster;
import sample.utils.clustering.DBSCANClusterer;

import java.awt.image.BufferedImage;
import java.util.*;


/**
 * Created by Alex on 17.11.2014.
 */
public class CodeFinder {
    int resizeFactor;

    public LinkedList<RotatedRect> findCodes(Mat source) throws Exception {
        resizeFactor = Math.min(source.rows(), source.cols()) / 1000;
        if (resizeFactor < 1) {
            resizeFactor = 1;
        }
        Mat resized = Mat.zeros(source.rows() / resizeFactor, source.cols() / resizeFactor, source.type());
        Imgproc.resize(source, resized, new Size(source.cols() / resizeFactor, source.rows() / resizeFactor), 0, 0, Imgproc.INTER_CUBIC);

        Mat binImage = new Mat(resized.rows(), resized.cols(), resized.type());
        Imgproc.GaussianBlur(resized, binImage, new Size(51, 51), 0, 0);
        Imgproc.equalizeHist(binImage, binImage);
        int blokSize = Math.min(binImage.cols(), binImage.rows()) / 14;
        if (blokSize % 2 == 0) {
            blokSize++;
        }
        Imgproc.adaptiveThreshold(binImage, binImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, blokSize, 0);
        Imgcodecs.imwrite("threshold.bmp", binImage);
        Imgproc.erode(binImage, binImage, new Mat(), new Point(-1, -1), 3);
        Imgcodecs.imwrite("erode.bmp", binImage);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(binImage, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        List<RotatedRect> allRotatedRects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            RotatedRect tempRotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            if (isQuadrat(tempRotatedRect)) {
                allRotatedRects.add(tempRotatedRect);
            }
        }

        LinkedList<RotatedRect> referenceRects = getReferenceRects(allRotatedRects);

        for (RotatedRect rotatedRect : referenceRects) {
            Point[] rect_points = new Point[4];
            rotatedRect.points(rect_points);
            Scalar color = new Scalar(0, 0, 255);
            for (int j = 0; j < 4; j++) {
                Imgproc.line(resized, rect_points[j], rect_points[(j + 1) % 4], color, 1);
            }
        }
        Imgcodecs.imwrite("rects.bmp", resized);
        LinkedList<RotatedRect> circleAreas = calculateCircleAreas(referenceRects, binImage);
        CodeFinder codeFinder = new CodeFinder();
        DataMatrixInterpreter dataMatrixInterpreter = new DataMatrixInterpreter();
        CodeCleaner codeCleaner = new CodeCleaner();
        for (RotatedRect rotatedRect : circleAreas) {
            Point center = rotatedRect.center;
            double width = rotatedRect.size.width * resizeFactor;
            double height = rotatedRect.size.height * resizeFactor;
            int x = (int) (center.x * resizeFactor - width / 2);
            if (x < 0) {
                x = 0;
            }
            int y = (int) (center.y * resizeFactor - height / 2);
            if (y < 0) {
                y = 0;
            }

            Mat circleImage = source.submat(new Rect(x, y, (int) width, (int) height));
            Mat code = codeFinder.extractCode(circleImage);
            if (code != null) {

                Mat boundedCode = codeCleaner.getBoundedCode(code);
                Mat cleanedCode = codeCleaner.cleanCode(boundedCode);
                Core.bitwise_not(cleanedCode, cleanedCode);
                BufferedImage bufferedImage = ImageUtils.matToBufferedImage(cleanedCode);
                String text = dataMatrixInterpreter.decode(bufferedImage);
            }
        }
        return circleAreas;
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
        for (RotatedRect rotatedRect : tempResult) {
            if (isInBounds(image, rotatedRect.center)) {
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

    public double getDistance(Point a, Point b) {
        double xDiff = a.x - b.x;
        double yDiff = a.y - b.y;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    public double getVectorLength(Point point) {
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

    public Point[] calculateCellVectors(List<Point> points) {
        Point[] result = new Point[2];
        ArrayList<Point> differenceVectoren = calculateDifferenceVectoren(points);
        double maxDistanceInCluster = 5;
        DBSCANClusterer dbscanClusterer = new DBSCANClusterer(maxDistanceInCluster, 5);
        List<Cluster<Point>> clusters = dbscanClusterer.cluster(differenceVectoren);
        if (clusters.size() < 2) {
            return null;
        }
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
            if (tempAngleDiff < maxAngleDiff) {
                candidates.add(tempPoint);
            }
        }

        Collections.sort(candidates, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return (int) (getVectorLength(o1) - getVectorLength(o2));
            }
        });

        if (candidates.size() < 1) {
            return null;
        }
        result[0] = point_0;
        result[1] = candidates.get(0);
        return result;
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
            if (tempAngleDiff < maxAngleDiff) {
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

    private ArrayList<Point> calculateDifferenceVectoren(List<Point> points) {
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

    public Mat extractCode(Mat grayImage) {
        Mat code = null;
        double imageArea = grayImage.cols() * grayImage.rows();
        Mat binImage = new Mat(grayImage.rows(), grayImage.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(grayImage, binImage, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(binImage, binImage, new Size(3, 3), 3, 3);

        Imgproc.adaptiveThreshold(binImage, binImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 51, 0);
        Imgproc.dilate(binImage, binImage, new Mat(), new Point(-1, -1), 1);
        Imgproc.erode(binImage, binImage, new Mat(), new Point(-1, -1), 1);
        Mat hierarchy = new Mat();
        Mat connectedComponents = new Mat();
        int componentsSize = Imgproc.connectedComponents(binImage, connectedComponents);
        Set<Point>[] componentPoints = new Set[componentsSize];
        for (int r = 0; r < binImage.rows(); ++r) {
            for (int c = 0; c < binImage.cols(); ++c) {
                int[] label = new int[1];
                connectedComponents.get(r, c, label);
                int labelNum = label[0];
                Set<Point> set = componentPoints[labelNum];
                if (set == null) {
                    set = new HashSet<Point>();
                    componentPoints[labelNum] = set;
                }
                set.add(new Point(c, r));
            }
        }

        RotatedRect rotatedRect = null;
        for (Set<Point> points : componentPoints) {
            Point[] contourPoints = points.toArray(new Point[0]);
            RotatedRect tempRotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contourPoints));
            Size rectSize = tempRotatedRect.size;
            double minSize = Math.min(rectSize.width, rectSize.height);
            double maxSize = Math.max(rectSize.width, rectSize.height);
            double minSizeDiff = 75;//unterschied zwischen width und height in prozent
            double sizeDiff = minSize / maxSize * 100;
            double minArea = imageArea / 9;
            double maxArea = imageArea / 1.3;
            double area = rectSize.area();
            if (sizeDiff > minSizeDiff && area > minArea && area < maxArea) {
                Point[] rect_points = new Point[4];
                tempRotatedRect.points(rect_points);
                rotatedRect = tempRotatedRect;
            }
        }

        hierarchy.release();
        if (rotatedRect != null && rotatedRect.size.width > 0 && rotatedRect.size.height > 0) {
            Mat rotatedImage = ImageUtils.rotate(grayImage, rotatedRect.center, rotatedRect.angle);
            int puffer = 2;
            int x = (int) (rotatedRect.center.x - puffer - rotatedRect.size.width / 2);
            if (x < 0) {
                x = 0;
            }
            int y = (int) (rotatedRect.center.y - puffer - rotatedRect.size.height / 2);
            if (y < 0) {
                y = 0;
            }
            int width = (int) rotatedRect.size.width + puffer * 2;
            if (x + width >= rotatedImage.cols()) {
                width = rotatedImage.cols() - x;
            }
            int height = (int) rotatedRect.size.height + puffer * 2;
            if (y + height >= rotatedImage.rows()) {
                height = rotatedImage.rows() - y;
            }

            if (width == 0 || height == 0) {
                return null;
            }
            code = new Mat();
            Imgproc.cvtColor(rotatedImage, code, Imgproc.COLOR_RGB2GRAY);
            code = code.submat(new Rect(x, y, width, height));
            Imgproc.threshold(code, code, 120, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        }
        return code;
    }

    public int getResizeFactor() {
        return resizeFactor;
    }
}
