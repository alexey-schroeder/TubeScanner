package sample.utils;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import sample.Circle;
import sample.Line;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 17.11.2014.
 */
public class LineFinder {

    public static List<Line> extractLines(Mat image) {
        List<Line> result = new ArrayList<Line>();
        Mat lines = new Mat();
        Imgproc.HoughLines(image, lines, 1, Math.PI / 180, 100, 0, 0);
        for (int i = 0; i < lines.cols(); i++) {
            double[] line = lines.get(0, i);
            double rho = line[0];
            double theta = line[1];

            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;
            double pt1_x = Math.round(x0 + 1000 * (-b));
            double pt1_y = Math.round(y0 + 1000 * (a));
            double pt2_x = Math.round(x0 - 1000 * (-b));
            double pt2_y = Math.round(y0 - 1000 * (a));
            Point pt_1 = new Point(pt1_x, pt1_y);
            Point pt_2 = new Point(pt2_x, pt2_y);
            result.add(new Line(pt_1, pt_2));
        }
        return result;
    }
}
