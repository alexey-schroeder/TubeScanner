package sample.utils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import sample.Line;

import java.util.*;

import static org.opencv.core.CvType.CV_32FC2;

/**
 * Created by Alex on 17.11.2014.
 */
public class LineFinder {
    int counter = 0;
//    public static List<Line> extractLines(Mat image) {
//        Mat binImage = new Mat(image.rows(), image.cols(), image.type());
//        Imgproc.GaussianBlur(image, binImage, new Size(5, 5), 5, 5);
//        double iCannyLowerThreshold = 35;
//        double iCannyUpperThreshold = 70;
//        Imgproc.Canny(binImage, binImage, iCannyLowerThreshold, iCannyUpperThreshold);
//        List<Line> result = new ArrayList<Line>();
//        Mat lines = new Mat();
//        double rho = 1.0;					// distance from (0,0) top left
//        double theta = Math.PI/180.0;		// angle in radians
//        int threshold = 40;
//        //   HoughLines(Mat image, Mat lines, double rho, double theta, int threshold, double srn, double stn)
//        Imgproc.HoughLines(binImage, lines, rho, theta, threshold, 0.0, 0.0);
//        int d = 50;
//        for (int i = 0; i < lines.cols(); i++) {
//            double[] line = lines.get(0, i);
//            double r = line[0];
//            double t = line[1];
//
//            double a = Math.cos(t), b = Math.sin(t);
//            double x0 = a * r, y0 = b * r;
//            double pt1_x = Math.round(x0 + d * (-b));
//            double pt1_y = Math.round(y0 + d * (a));
//            double pt2_x = Math.round(x0 - d * (-b));
//            double pt2_y = Math.round(y0 - d * (a));
//            Point pt_1 = new Point(pt1_x, pt1_y);
//            Point pt_2 = new Point(pt2_x, pt2_y);
//            result.add(new Line(pt_1, pt_2));
//        }
//        return result;
//    }

//    public static List<Line> extractLines(Mat image) {
//        Highgui.imwrite("lines/rowImage_" + counter + ".bmp", image);
//        Mat binImage = new Mat(image.rows(), image.cols(), image.type());
//        Imgproc.GaussianBlur(image, binImage, new Size(5, 5), 5, 5);
////        Imgproc.adaptiveThreshold(binImage, binImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 5, 0);
////        Highgui.imwrite("lines/binImage_" + counter + ".bmp", binImage);
////        Imgproc.dilate(binImage, binImage, new Mat(), new Point(-1, -1), 3);
////        Highgui.imwrite("dilateImage.bmp", binImage);
////        Imgproc.erode(binImage, binImage, new Mat(), new Point(-1, -1), 1);
////        Highgui.imwrite("lines/erodeImage_" + counter + ".bmp", binImage);
////        Highgui.imwrite("erodeImage.bmp", binImage);
//        double iCannyLowerThreshold = 25;
//        double iCannyUpperThreshold = 70;
//        Imgproc.Canny(binImage, binImage, iCannyLowerThreshold, iCannyUpperThreshold);
//        Highgui.imwrite("lines/cannyImage_" + counter + ".bmp", binImage);
//
//        List<Line> result = new ArrayList<Line>();
//        Mat lines = new Mat();
//        int linesThreshold = 20;
//        int linesMinLineSize = 35;
//        int linesGap = 5;
//        Imgproc.HoughLinesP(binImage, lines, 1, Math.PI / 180, linesThreshold, linesMinLineSize, linesGap);
//
//        for (int x = 0; x < lines.cols(); x++) {
//            double[] vecHoughLines = lines.get(0, x);
//
//            if (vecHoughLines.length == 0)
//                break;
//
//            double x1 = vecHoughLines[0];
//            double y1 = vecHoughLines[1];
//            double x2 = vecHoughLines[2];
//            double y2 = vecHoughLines[3];
//            Point pt1 = new Point();
//            Point pt2 = new Point();
//
//            pt1.x = x1;
//            pt1.y = y1;
//            pt2.x = x2;
//            pt2.y = y2;
//            result.add(new Line(pt1, pt2));
//            Core.line(image, pt1, pt2, new Scalar(255, 0, 0, 255), 2);
//
//        }
//        Highgui.imwrite("lines/lines_" + counter + ".bmp", image);
//        counter++;
//        return result;
//    }

//    public static List<Line> extractLines(Mat grayImage, Mat coloredImage) {
//        double imageArea = grayImage.cols() * grayImage.rows();
//        Mat binImage = new Mat(grayImage.rows(), grayImage.cols(), grayImage.type());
//        Imgproc.GaussianBlur(grayImage, binImage, new Size(5, 5), 5, 5);
//        double iCannyLowerThreshold = 25;
//        double iCannyUpperThreshold = 70;
//        Imgproc.Canny(binImage, binImage, iCannyLowerThreshold, iCannyUpperThreshold);
//        Imgcodecs.imwrite("lines/cannyImage_" + counter + ".bmp", binImage);
//        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        MatOfPoint codeContour;
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(binImage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
//        double shift = grayImage.rows() / 10.0;
//        for (int i = 0; i < contours.size(); i++) {
//            MatOfPoint2f tempContour = new MatOfPoint2f();
//
//            contours.get(i).convertTo(tempContour, CV_32FC2);
//            Point[] oldPoints = tempContour.toArray();
//            Point[] newPoints = new Point[oldPoints.length + 4];
//            System.arraycopy(oldPoints, 0, newPoints, 0, oldPoints.length);
//            newPoints[oldPoints.length] = new Point(grayImage.cols() / 2 - shift, grayImage.rows() / 2);
//            newPoints[oldPoints.length + 1] = new Point(grayImage.cols() / 2, grayImage.rows() / 2 - shift);
//            newPoints[oldPoints.length + 2] = new Point(grayImage.cols() / 2 + shift, grayImage.rows() / 2);
//            newPoints[oldPoints.length + 3] = new Point(grayImage.cols() / 2, grayImage.rows() / 2 + shift);
//            MatOfPoint2f tempContour_2 = new MatOfPoint2f(newPoints);
//
//            RotatedRect rotatedRect = Imgproc.minAreaRect(tempContour_2);
//            Size rectSize = rotatedRect.size;
//            double minSize = Math.min(rectSize.width, rectSize.height);
//            double maxSize = Math.max(rectSize.width, rectSize.height);
//            double minSizeDiff = 66;//unterschied zwischen width und height in prozent
//            double sizeDiff = minSize / maxSize * 100;
//            double minArea = imageArea / 7;
//            double area = rectSize.area();
//            if (sizeDiff > minSizeDiff && area > minArea) {
//                Point[] rect_points = new Point[4];
//                rotatedRect.points(rect_points);
//                Scalar color = new Scalar(0, 0, 255);
//                for (int j = 0; j < 4; j++)
//                    Imgproc.line(coloredImage, rect_points[j], rect_points[(j + 1) % 4], color, 1);
//            }
//        }
//        hierarchy.release();
//        Imgcodecs.imwrite("lines/contour_" + counter + ".bmp", coloredImage);
//        List<Line> result = new ArrayList<Line>();
//        counter++;
//        return result;
//    }

//    public static List<Line> extractLines(Mat grayImage, Mat coloredImage) {
//        double imageArea = grayImage.cols() * grayImage.rows();
//        Mat binImage = new Mat(grayImage.rows(), grayImage.cols(), grayImage.type());
//        Imgproc.GaussianBlur(grayImage, binImage, new Size(5, 5), 5, 5);
//        double iCannyLowerThreshold = 25;
//        double iCannyUpperThreshold = 70;
//        Imgproc.Canny(binImage, binImage, iCannyLowerThreshold, iCannyUpperThreshold);
//        Imgcodecs.imwrite("lines/cannyImage_" + counter + ".bmp", binImage);
//        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
//        MatOfPoint codeContour;
//        Mat hierarchy = new Mat();
//        Imgproc.findContours(binImage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
//
//        for (int i = 0; i < contours.size(); i++) {
//            MatOfPoint2f tempContour = new MatOfPoint2f();
//            contours.get(i).convertTo(tempContour, CV_32FC2);
//            MatOfPoint triangle = new MatOfPoint();
//            Imgproc.minEnclosingTriangle(tempContour, triangle);
//            triangle.get(0, 0);
//            Point[] points = new Point[]{
//                    new Point(triangle.get(0, 0)),
//                    new Point(triangle.get(1, 0)),
//                    new Point(triangle.get(2, 0))};
////            Size rectSize = rotatedRect.size;
////            double minSize = Math.min(rectSize.width, rectSize.height);
////            double maxSize = Math.max(rectSize.width, rectSize.height);
////            double minSizeDiff = 50;//unterschied zwischen width und height in prozent
////            double sizeDiff = minSize / maxSize * 100;
////            double minArea = imageArea / 7;
////            double area = rectSize.area();
////            if (sizeDiff > minSizeDiff && area > minArea) {
////                Point[] rect_points = new Point[4];
////                rotatedRect.points(rect_points);
//            Scalar color = new Scalar(0, 0, 255);
//            for (int j = 0; j < 3; j++)
//                Imgproc.line(coloredImage, points[j], points[(j + 1) % 3], color, 1);
//        }
////        }
//        hierarchy.release();
//        Imgcodecs.imwrite("lines/contour_" + counter + ".bmp", coloredImage);
//        List<Line> result = new ArrayList<Line>();
//        counter++;
//        return result;
//    }


    public List<Line> extractLines(Mat grayImage, Mat coloredImage) {
        double imageArea = grayImage.cols() * grayImage.rows();
        Mat binImage = new Mat(grayImage.rows(), grayImage.cols(), grayImage.type());
        Imgproc.GaussianBlur(grayImage, binImage, new Size(3, 3), 3, 3);

//        Imgproc.equalizeHist(grayImage, binImage);
        double iCannyLowerThreshold = 15;
        double iCannyUpperThreshold = 75;
//        Imgproc.Canny(binImage, binImage, iCannyLowerThreshold, iCannyUpperThreshold);
//        Imgcodecs.imwrite("lines/cannyImage_" + counter + ".bmp", binImage);
        Imgproc.adaptiveThreshold(binImage, binImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 51, 0);
        Imgcodecs.imwrite("lines/thresholdImage_" + counter + ".bmp", binImage);
//        Imgproc.dilate(binImage, binImage, new Mat(), new Point(-1, -1), 1);
//        Imgcodecs.imwrite("lines/dilateImage_" + counter + ".bmp", binImage);
//        Imgproc.erode(binImage, binImage, new Mat(), new Point(-1, -1), 1);
//        Imgcodecs.imwrite("lines/erodeImage_" + counter + ".bmp", binImage);
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfPoint codeContour;
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

//        Imgcodecs.imwrite("lines/connectedImage_" + counter + ".bmp", connectedComponents);
//        Imgproc.findContours(binImage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        RotatedRect rotatedRect = null;
        for (Set<Point> points : componentPoints) {
//            MatOfPoint contour = contours.get(i);
            Point[] contourPoints = points.toArray(new Point[0]);
//            if (contourPoints.length > 10) {
//                Point[] maxPoints = findPointsByMaxDistance(contourPoints);
//                double distance_1 = getSqrDistance(maxPoints[0], maxPoints[1]);
//                double distance_2 = getSqrDistance(maxPoints[1], maxPoints[2]);
//                double max = Math.max(distance_1, distance_2);
//                double min = Math.min(distance_1, distance_2);
//                double diff = min / max;
//                if (diff > 0.5) {
//                    Scalar color = new Scalar(255, 0, 0);
//                    for (Point point : maxPoints) {
//                        Imgproc.circle(coloredImage, point, 2, color);
//                    }
//                        Imgproc.line(coloredImage, maxPoints[0], maxPoints[1], color, 1);
//                        Imgproc.line(coloredImage, maxPoints[1], maxPoints[2], color, 1);
//                }
//            }

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
//                Scalar color = new Scalar(0, 0, 255);
//                for (int j = 0; j < 4; j++) {
//                    Imgproc.line(coloredImage, rect_points[j], rect_points[(j + 1) % 4], color, 1);
//                }
                rotatedRect = tempRotatedRect;
            }
        }


//        MatOfPoint[] maxCounturs = new MatOfPoint[3];
//        for (int i = 0; i < contours.size(); i++) {
//            MatOfPoint contour = contours.get(i);
//            Point[] contourPoints = contour.toArray();
//            if (maxCounturs[0] == null || maxCounturs[0].toArray().length < contourPoints.length) {
//                maxCounturs[0] = contour;
//            } else if (maxCounturs[1] == null || maxCounturs[1].toArray().length < contourPoints.length) {
//                maxCounturs[1] = contour;
//            } else if (maxCounturs[2] == null || maxCounturs[2].toArray().length < contourPoints.length) {
//                maxCounturs[2] = contour;
//            }
//        }
//        for (int i = 0; i < maxCounturs.length; i++) {
//            Scalar color = new Scalar(Math.random() * 255, Math.random() * 255, Math.random() * 255);
//            Imgproc.drawContours(coloredImage, Arrays.asList(maxCounturs), i, color, 1);
//        }
        hierarchy.release();
        if (rotatedRect != null) {
            Mat rotatedImage = ImageUtils.rotate(grayImage, rotatedRect.center, rotatedRect.angle);
            int puffer = 2;
            int x = (int) (rotatedRect.center.x - puffer - rotatedRect.size.width / 2);
            int y = (int) (rotatedRect.center.y - puffer - rotatedRect.size.height / 2);
            Mat code = rotatedImage.submat(new Rect(x, y, (int) rotatedRect.size.width + puffer * 2, (int) rotatedRect.size.height + puffer * 2));
            Imgproc.adaptiveThreshold(code, code, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 0);
            Imgcodecs.imwrite("lines/contour_" + counter + ".bmp", code);
        }
        List<Line> result = new ArrayList<Line>();
        counter++;
        return result;
    }

    private Point[] findPointsByMaxDistance(Point[] contourPoints) {
        Point point_1 = null;
        Point point_2 = null;
        Point point_3 = null;
        double maxDistance = 0;
        for (int i = 0; i < contourPoints.length - 1; i++) {
            Point tempPoint = contourPoints[i];
            for (int j = i; j < contourPoints.length; j++) {
                Point tempPoint_2 = contourPoints[j];

                double distance = getSqrDistance(tempPoint, tempPoint_2);
                if (maxDistance < distance) {
                    point_1 = tempPoint;
                    point_2 = tempPoint_2;
                    maxDistance = distance;
                }
            }
        }
        maxDistance = 0;
        for (int i = 0; i < contourPoints.length; i++) {
            Point tempPoint = contourPoints[i];
            double distance_1 = getSqrDistance(point_1, tempPoint);
            double distance_2 = getSqrDistance(point_2, tempPoint);
            if (distance_1 + distance_2 > maxDistance) {
                point_3 = tempPoint;
                maxDistance = distance_1 + distance_2;
            }
        }
        return new Point[]{point_1, point_3, point_2};
    }

    public double getSqrDistance(Point point_1, Point point_2) {
        double xDiff = point_1.x - point_2.x;
        double yDiff = point_1.y - point_2.y;
        double distance = xDiff * xDiff + yDiff * yDiff;
        return distance;
    }
}
