package sample.utils;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import sample.Line;

import java.util.ArrayList;
import java.util.List;

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

    public static List<Line> extractLines(Mat image) {

        Mat binImage = new Mat(image.rows(), image.cols(), image.type());
        Imgproc.GaussianBlur(image, binImage, new Size(5, 5), 5, 5);
        double iCannyLowerThreshold = 35;
        double iCannyUpperThreshold = 70;
        Imgproc.Canny(binImage, binImage, iCannyLowerThreshold, iCannyUpperThreshold);
        Highgui.imwrite("lines/binImage_" + counter + ".bmp", binImage);

        List<Line> result = new ArrayList<Line>();
        Mat lines = new Mat();
        int iHoughLinesThreshold = 3;
        int iHoughLinesMinLineSize = 25;
        int iHoughLinesGap = 2;
        Imgproc.HoughLinesP(binImage, lines, 1, Math.PI / 180, iHoughLinesThreshold, iHoughLinesMinLineSize, iHoughLinesGap);

        for (int x = 0; x < Math.min(lines.cols(), 40); x++) {
            double[] vecHoughLines = lines.get(0, x);

            if (vecHoughLines.length == 0)
                break;

            double x1 = vecHoughLines[0];
            double y1 = vecHoughLines[1];
            double x2 = vecHoughLines[2];
            double y2 = vecHoughLines[3];
            Point pt1 = new Point();
            Point pt2 = new Point();

            pt1.x = x1;
            pt1.y = y1;
            pt2.x = x2;
            pt2.y = y2;
            result.add(new Line(pt1, pt2));
            Core.line(image, pt1, pt2, new Scalar(255, 0, 0, 255), 2);

        }
        Highgui.imwrite("lines/lines_" + counter + ".bmp", image);
        counter++;
        return result;
    }
}
