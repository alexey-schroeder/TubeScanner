package sample.utils;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

/**
 * Created by Alex on 02.12.2014.
 */
public class CodeCleaner {

    public Mat cleanCode(Mat code) {
        int size = calculateSize(code);
        Mat result = new Mat(size * 12, size * 12, code.type());
        for (int row = 0; row < code.rows()- size; row = row + size) {
            for (int col = 0; col < code.cols() - size; col = col + size) {
                Mat point = code.submat(new Rect(col, row, size, size));
                int pointValue = calculatePointValue(point);
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(pointValue));
                calculatedPoint.copyTo(result.submat(new Rect(col, row, size, size)));
            }
        }
        return result;
    }

    private int calculatePointValue(Mat point) {
        int maxValue = point.cols() * point.rows() / 2;
        int counter_black = 0;
        int counter_white = 0;
        for (int row = 0; row < point.rows(); row++) {
            for (int col = 0; col < point.cols(); col++) {
                double[] data = point.get(row, col);
                if (data[0] > 254) {
                  counter_white++;
                    if(counter_white > maxValue){
                        return 255;
                    }
                } else {
                    counter_black++;
                    if(counter_black > maxValue){
                        return 0;
                    }
                }
            }
        }
        return 0;
    }

    private int calculateSize(Mat code) {
        int width = code.cols() / 12;
        int height = code.rows() / 12;
        int min = Math.min(width, height);
        return min;
    }
}
