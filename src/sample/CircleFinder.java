package sample;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 07.11.2014.
 */
public class CircleFinder {
    public static List<Circle> extractCircles(Mat image) {
        double iCannyUpperThreshold = 100;
        int iMinRadius = 30;
        int iMaxRadius = 70;
        double iAccumulator = 300;
        List<Circle> balls = new ArrayList<Circle>();
//        List<MatOfPoint> contours = new ArrayList<>();

        // find the contours
//        Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat circles = new Mat();
        //    HoughCircles(Mat image, Mat circles, int method, double dp, double minDist, double param1, double param2, int minRadius, int maxRadius)
        Imgproc.HoughCircles(image, circles, Imgproc.CV_HOUGH_GRADIENT, 2.0, 3 * iMinRadius, 80, 40, iMinRadius, iMaxRadius);
//        Objdetect.

        // iterate through the contours, find single balls and clusters of balls touching each other
        double minArea = 50; // minimal ball area
        double maxArea = 120; // maximal ball area

        for (int i = 0; i < circles.cols(); i++) {

            double[] vecCircle = circles.get(0, i);
            if (vecCircle == null)
                break;
            int x = (int) vecCircle[0];
            int y = (int) vecCircle[1];
            int r = (int) vecCircle[2];
            balls.add(new Circle(x, y, r));

        }
        System.out.println("founded " +  balls.size() + " circles");
        return balls;
    }
}
