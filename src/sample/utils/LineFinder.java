package sample.utils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import sample.Line;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_32FC2;

/**
 * Created by Alex on 17.11.2014.
 */
public class LineFinder {
    static int counter = 0;
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

    public static List<Line> extractLines(Mat grayImage, Mat coloredImage) {
        double imageArea = grayImage.cols() * grayImage.rows();
        Mat binImage = new Mat(grayImage.rows(), grayImage.cols(), grayImage.type());
        Imgproc.GaussianBlur(grayImage, binImage, new Size(5, 5), 5, 5);
        double iCannyLowerThreshold = 25;
        double iCannyUpperThreshold = 70;
        Imgproc.Canny(binImage, binImage, iCannyLowerThreshold, iCannyUpperThreshold);
        Imgcodecs.imwrite("lines/cannyImage_" + counter + ".bmp", binImage);
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        MatOfPoint codeContour;
        Mat hierarchy = new Mat();
        Imgproc.findContours(binImage, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        double shift = grayImage.rows() / 10.0;
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint2f tempContour = new MatOfPoint2f();

            contours.get(i).convertTo(tempContour, CV_32FC2);
            Point[] oldPoints = tempContour.toArray();
            Point[] newPoints = new Point[oldPoints.length + 4];
            System.arraycopy(oldPoints, 0, newPoints, 0, oldPoints.length);
            newPoints[oldPoints.length] = new Point(grayImage.cols() / 2 - shift, grayImage.rows() / 2);
            newPoints[oldPoints.length + 1] = new Point(grayImage.cols() / 2, grayImage.rows() / 2 - shift);
            newPoints[oldPoints.length + 2] = new Point(grayImage.cols() / 2 + shift, grayImage.rows() / 2);
            newPoints[oldPoints.length + 3] = new Point(grayImage.cols() / 2, grayImage.rows() / 2 + shift);
            MatOfPoint2f tempContour_2 = new MatOfPoint2f(newPoints);

            RotatedRect rotatedRect = Imgproc.minAreaRect(tempContour_2);
            Size rectSize = rotatedRect.size;
            double minSize = Math.min(rectSize.width, rectSize.height);
            double maxSize = Math.max(rectSize.width, rectSize.height);
            double minSizeDiff = 66;//unterschied zwischen width und height in prozent
            double sizeDiff = minSize / maxSize * 100;
            double minArea = imageArea / 7;
            double area = rectSize.area();
            if (sizeDiff > minSizeDiff && area > minArea) {
                Point[] rect_points = new Point[4];
                rotatedRect.points(rect_points);
                Scalar color = new Scalar(0, 0, 255);
                for (int j = 0; j < 4; j++)
                    Imgproc.line(coloredImage, rect_points[j], rect_points[(j + 1) % 4], color, 1);
            }
        }
        hierarchy.release();
        Imgcodecs.imwrite("lines/contour_" + counter + ".bmp", coloredImage);
        List<Line> result = new ArrayList<Line>();
        counter++;
        return result;
    }

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
//            MatOfPoint contour = contours.get(i);
//            Point[] contourPoints = contour.toArray();
//            Point[] maxPoints = findPointsByMaxDistance(contourPoints);
//            for (Point point : maxPoints) {
//                Imgproc.circle(coloredImage, point, 2, new Scalar(255, 0, 0));
//            }
//        }
//        hierarchy.release();
//        Imgcodecs.imwrite("lines/contour_" + counter + ".bmp", coloredImage);
//        List<Line> result = new ArrayList<Line>();
//        counter++;
//        return result;
//    }

    private static Point[] findPointsByMaxDistance(Point[] contourPoints) {
        return new Point[0];
    }
}
