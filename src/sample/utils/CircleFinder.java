package sample.utils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import sample.Circle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 07.11.2014.
 */
public class CircleFinder {
    public static List<Circle> extractCircles(Mat image) {
        int resizeFactor = Math.min(image.rows(), image.cols()) / 1000;
        if(resizeFactor < 1){
            resizeFactor = 1;
        }
        Mat resized = Mat.zeros(image.rows() / resizeFactor, image.cols() / resizeFactor, image.type());
        Imgproc.resize(image, resized, new Size(image.cols() / resizeFactor, image.rows() / resizeFactor), 0, 0, Imgproc.INTER_CUBIC);
        Imgcodecs.imwrite("resizedImage.bmp", resized);
        Mat binImage = new Mat(resized.rows(), resized.cols(), resized.type());
        Imgproc.GaussianBlur(resized, binImage, new Size(51, 51), 0, 0);
        Imgcodecs.imwrite("gaussianImage.bmp", binImage);
        Imgproc.equalizeHist(binImage, binImage);
        Imgcodecs.imwrite("equalizeImage.bmp", binImage);
//        Mat clone = Mat.zeros(binImage.rows(), binImage.cols() , CvType.CV_32FC1);
//        binImage.convertTo(clone, CvType.CV_32FC1);
//        Mat dft = Mat.zeros(binImage.rows(), binImage.cols() , CvType.CV_32FC1);
//        Core.dft(clone, dft);
//        Imgcodecs.imwrite("dftImage.bmp", dft);

       // adaptiveThreshold(Mat src, Mat dst, double maxValue, int adaptiveMethod, int thresholdType, int blockSize, double C)
        int blokSize = Math.min(binImage.cols(), binImage.rows()) / 14;
        if(blokSize % 2 == 0){
            blokSize++;
        }
        System.out.println("blockSize = " + blokSize);
        Imgproc.adaptiveThreshold(binImage, binImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, blokSize, 0);
//        Imgproc.threshold(binImage, binImage, 100, 255, Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite("binImage.bmp", binImage);
        Imgproc.dilate(binImage, binImage, new Mat(), new Point(-1, -1), 3);
        Imgcodecs.imwrite("dilateImage.bmp", binImage);
        Imgproc.erode(binImage, binImage, new Mat(), new Point(-1, -1), 1);
        Imgcodecs.imwrite("erodeImage.bmp", binImage);

//        Imgproc.threshold(binImage, binImage, 50, 255, Imgproc.THRESH_BINARY);

        double iCannyLowerThreshold = 35;
        double iCannyUpperThreshold = 70;
        Imgproc.Canny(binImage, binImage, iCannyLowerThreshold, iCannyUpperThreshold);
        Imgcodecs.imwrite("canny.bmp", binImage);
        int iMinRadius = 30;
        int iMaxRadius = 70;
        List<Circle> balls = new ArrayList<Circle>();

        Mat circles = new Mat();
        //    HoughCircles(Mat image, Mat circles, int method, double dp, double minDist, double param1, double param2, int minRadius, int maxRadius)
        Imgproc.HoughCircles(binImage, circles, Imgproc.CV_HOUGH_GRADIENT, 2.0, 3 * iMinRadius, 100, 100, iMinRadius, iMaxRadius);

        for (int i = 0; i < circles.cols(); i++) {

            double[] vecCircle = circles.get(0, i);
            if (vecCircle == null)
                break;
            int x = (int) vecCircle[0] * resizeFactor;
            int y = (int) vecCircle[1] * resizeFactor;
            int r = (int) vecCircle[2] * resizeFactor;
            balls.add(new Circle(x, y, r));
        }
        System.out.println("founded " + balls.size() + " circles");
        return balls;
    }
}
