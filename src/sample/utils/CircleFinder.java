package sample.utils;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import sample.Circle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 07.11.2014.
 */
public class CircleFinder {
    public static List<Circle> extractCircles(Mat image) {
        Mat binImage = new Mat(image.rows(), image.cols(), image.type());
        Imgproc.GaussianBlur(image, binImage, new Size(7, 7), 10, 10);
        Highgui.imwrite("gaussianImage.bmp", binImage);
        Imgproc.threshold(binImage, binImage, 50, 255, Imgproc.THRESH_BINARY);
        Highgui.imwrite("binImage.bmp", binImage);
        double iCannyLowerThreshold = 35;
        double iCannyUpperThreshold = 70;
        Imgproc.Canny(binImage, binImage, iCannyLowerThreshold, iCannyUpperThreshold);
        Highgui.imwrite("canny.bmp", binImage);
        int iMinRadius = 30;
        int iMaxRadius = 70;
        List<Circle> balls = new ArrayList<Circle>();

        Mat circles = new Mat();
        //    HoughCircles(Mat image, Mat circles, int method, double dp, double minDist, double param1, double param2, int minRadius, int maxRadius)
        Imgproc.HoughCircles(binImage, circles, Imgproc.CV_HOUGH_GRADIENT, 2.0, 3 * iMinRadius, 80, 40, iMinRadius, iMaxRadius);

        for (int i = 0; i < circles.cols(); i++) {

            double[] vecCircle = circles.get(0, i);
            if (vecCircle == null)
                break;
            int x = (int) vecCircle[0];
            int y = (int) vecCircle[1];
            int r = (int) vecCircle[2];
            balls.add(new Circle(x, y, r));
        }
        System.out.println("founded " + balls.size() + " circles");
        return balls;
    }
}
