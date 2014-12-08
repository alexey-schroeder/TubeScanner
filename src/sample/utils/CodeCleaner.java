package sample.utils;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Alex on 02.12.2014.
 */
public class CodeCleaner {
    private enum StartPoint {LEFT_TOP, RIGHT_TOP, RIGHT_BOTTOM, LEFT_BOTTOM}

    ;

    public Mat cleanCode(Mat code) {

        int size = calculateSize(code);
        StartPoint startPoint = findStartPoint(code, size);
        Mat result = null;
        switch (startPoint) {
            case LEFT_TOP:
                result = calculateLeftTopCase(code, size);
                result = normalizeInLeftTopCase(result, size);
                break;
            case RIGHT_TOP:
                result = calculateRightTopCase(code, size);
                result = normalizeInRightTopCase(result, size);
                break;
            case RIGHT_BOTTOM:
                result = calculateRightBottomCase(code, size);
                result = normalizeInRightBottomCase(result, size);
                break;
            case LEFT_BOTTOM:
                result = calculateLeftBottomCase(code, size);
                result = normalizeInLeftBottomCase(result, size);
        }
        return result;
    }

    public Mat getBoundedCode(Mat code) {
        int leftBound = findLeftBound(code, 0.85);
        int rightBound = findRightBound(code, 0.85);
        int topBound = findTopBound(code, 0.85);
        int bottomBound = findBottomBound(code, 0.85);
        Mat boundedCode = code.submat(new Rect(leftBound, topBound, rightBound - leftBound, bottomBound - topBound)).clone();
        return boundedCode;
    }

    public Mat calculateLeftTopCase(Mat mat, int size) {
        int rows = size * 12;
        int cols = size * 12;
        int rowSize = size;

        Mat result = mat.clone();

        for (int row = 0; row < mat.rows(); row = row + size) {
            if (row > mat.rows()) {
                rowSize = mat.rows() - row;
                row = mat.rows();
            }
            int colSize = size;
            for (int col = 0; col < mat.cols(); col = col + size) {
                if (col > mat.cols()) {
                    colSize = mat.cols() - col;
                    col = mat.rows();
                }
                Imgproc.rectangle(result, new Point(col, row), new Point(col + colSize, row + rowSize), new Scalar(255, 0, 0), 1);
            }
        }
        return result;
    }

    public Mat calculateRightTopCase(Mat mat, int size) {
        int rows = size * 12;
        int cols = size * 12;
        int rowSize = size;

        Mat result = mat.clone();

        for (int row = 0; row < mat.rows(); row = row + size) {
            if (row > mat.rows()) {
                rowSize = mat.rows() - row;
                row = mat.rows();
            }
            int colSize = size;
            for (int col = mat.cols() - size; col >= 0; col = col - size) {
                if (col < 0) {
                    colSize = colSize + col;
                    col = 0;
                }
                Imgproc.rectangle(result, new Point(col, row), new Point(col + colSize, row + rowSize), new Scalar(255, 0, 0), 1);
            }
        }
        return result;
    }

    public Mat calculateRightBottomCase(Mat mat, int size) {
        int rows = size * 12;
        int cols = size * 12;
        int rowSize = size;

        Mat result = mat.clone();

        for (int row = mat.rows() - size; row >= 0; row = row - size) {
            if (row < 0) {
                rowSize = rowSize + row;
                row = 0;
            }
            int colSize = size;
            for (int col = mat.cols() - size; col >= 0; col = col - size) {
                if (col < 0) {
                    colSize = colSize + col;
                    col = 0;
                }
                Imgproc.rectangle(result, new Point(col, row), new Point(col + colSize, row + rowSize), new Scalar(255, 0, 0), 1);
            }
        }
        return result;
    }

    public Mat calculateLeftBottomCase(Mat mat, int size) {
        int rows = size * 12;
        int cols = size * 12;
        int rowSize = size;

        Mat result = mat.clone();
        for (int row = mat.rows() - size; row >= 0; row = row - size) {
            if (row < 0) {
                rowSize = rowSize + row;
                row = 0;
            }
            int colSize = size;
            for (int col = 0; col < mat.cols(); col = col + size) {
                if (col + size > mat.cols()) {
                    colSize = mat.cols() - col;
                }
                Imgproc.rectangle(result, new Point(col, row), new Point(col + colSize, row + rowSize), new Scalar(255, 0, 0), 1);
            }
        }
        return result;
    }

    private StartPoint findStartPoint(Mat mat, int size) {
        Mat point = mat.submat(new Rect(0, 0, size, size));
        int pointValue = calculatePointValue(point);
        if (pointValue < 254) {
            return StartPoint.RIGHT_BOTTOM;
        }

        point = mat.submat(new Rect(mat.cols() - size, 0, size, size));
        pointValue = calculatePointValue(point);
        if (pointValue < 254) {
            return StartPoint.LEFT_BOTTOM;
        }

        point = mat.submat(new Rect(mat.cols() - size, mat.rows() - size, size, size));
        pointValue = calculatePointValue(point);
        if (pointValue < 254) {
            return StartPoint.LEFT_TOP;
        }
        return StartPoint.RIGHT_TOP;
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
                    if (counter_white > maxValue) {
                        return 255;
                    }
                } else {
                    counter_black++;
                    if (counter_black > maxValue) {
                        return 0;
                    }
                }
            }
        }
        return 0;
    }

    public  Mat normalizeInLeftTopCase(Mat code, int size) {
        Mat result = Mat.zeros(size * 12, size * 12, code.type());
        for (int x = 0; x < size * 12; x = x + size) {
            for (int y = 0; y < size * 12; y = y + size) {
                double[] data = code.get(y + size / 2, x + size / 2);
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(data[0]));
                calculatedPoint.copyTo(result.submat(new Rect(x, y, size, size)));
            }
        }
        return result;
    }

    public  Mat normalizeInRightTopCase(Mat code, int size) {
        Mat result = Mat.zeros(size * 12, size * 12, code.type());
        for (int x = size * 12; x > 0; x = x - size) {
            for (int y = 0; y < size * 12; y = y + size) {
                double[] data = code.get(y + size / 2, x - size / 2);
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(data[0]));
                calculatedPoint.copyTo(result.submat(new Rect(x - size, y, size, size)));
            }
        }
        return result;
    }

    public  Mat normalizeInRightBottomCase(Mat code, int size) {
        Mat result = Mat.zeros(size * 12, size * 12, code.type());
        for (int x = size * 12; x > 0; x = x - size) {
            for (int y = size * 12; y > 0; y = y - size) {
                int xPoint = x -size / 2;
                if(xPoint < 0){
                    xPoint = 0;
                }

                int yPoint = y -size / 2;
                if(yPoint < 0){
                    yPoint = 0;
                }
                double[] data = code.get(yPoint, xPoint);
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(data[0]));
                calculatedPoint.copyTo(result.submat(new Rect(x - size, y - size, size, size)));
            }
        }
        return result;
    }

    public  Mat normalizeInLeftBottomCase(Mat code, int size) {
        Mat result = Mat.zeros(size * 12, size * 12, code.type());
        for (int x = 0; x < size * 12; x = x + size) {
            for (int y = size * 12; y > 0; y = y - size) {
                double[] data = code.get(y - size / 2, x + size / 2);
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(data[0]));
                calculatedPoint.copyTo(result.submat(new Rect(x, y - size, size, size)));
            }
        }
        return result;
    }

    private int calculateSize(Mat code) {
        int size = 1;
        int colDiff = code.cols() - size * 12;
        int rowDiff = code.rows() - size * 12;
        int lastDiff = colDiff * colDiff + rowDiff * rowDiff;
        int diff = lastDiff - 1;
        while (diff < lastDiff) {
            lastDiff = diff;
            size++;
            colDiff = code.cols() - size * 12;
            rowDiff = code.rows() - size * 12;
            diff = colDiff * colDiff + rowDiff * rowDiff;
        }
        return size - 1;
    }


    public int findLeftBound(Mat mat, double thresholdInPercent) {
        int threshold = (int) (mat.cols() * thresholdInPercent);
        for (int col = 0; col < mat.cols(); col++) {
            double counter = 0;
            boolean isEmpty = true;
            for (int row = 0; row < mat.rows(); row++) {
                double[] data = mat.get(row, col);
                if (data[0] < 254) {
                    counter++;
                }
            }
            if (counter < threshold) {
//                if ((mat.cols() - counter) / mat.cols() < 0.85) {
//                    col = col - 2;
//                    if (col < 0) {
//                        col = 0;
//                    }
//                }
                return col;
            }
        }
        return mat.cols();
    }

    public int findRightBound(Mat mat, double thresholdInPercent) {
        int threshold = (int) (mat.cols() * thresholdInPercent);
        for (int col = mat.cols() - 1; col >= 0; col--) {
            double counter = 0;
            boolean isEmpty = false;
            for (int row = 0; row < mat.rows(); row++) {
                double[] data = mat.get(row, col);
                if (data[0] < 254) {
                    counter++;
//                    isEmpty = counter > threshold;
                }
            }
            if (counter < threshold) {
//                if ((mat.cols() - counter) / mat.cols() < 0.85) {
//                    col = col + 2;
//                    if (col > mat.cols()) {
//                        col = mat.cols();
//                    }
//
//                }
                return col;
            }
        }
        return 0;
    }

    public int findTopBound(Mat mat, double thresholdInPercent) {
        int threshold = (int) (mat.rows() * thresholdInPercent);
        for (int row = 0; row < mat.rows(); row++) {
            double counter = 0;
            boolean isEmpty = false;
            for (int col = 0; col < mat.cols(); col++) {
                double[] data = mat.get(row, col);
                if (data[0] < 254) {
                    counter++;

                }
            }
            if (counter < threshold) {
//                if ((mat.rows() - counter) / mat.rows() < 0.85) {
//                    row = row - 2;
//                    if (row < 0) {
//                        row = 0;
//                    }
//                }
                return row;
            }
        }
        return mat.rows();
    }

    public int findBottomBound(Mat mat, double thresholdInPercent) {
        int threshold = (int) (mat.rows() * thresholdInPercent);
        for (int row = mat.rows() - 1; row >= 0; row--) {
            double counter = 0;
            boolean isEmpty = false;
            for (int col = 0; col < mat.cols() && !isEmpty; col++) {
                double[] data = mat.get(row, col);
                if (data[0] < 254) {
                    counter++;
//                    isEmpty = counter > threshold;
                }
            }
            if (counter < threshold) {
//                if ((mat.rows() - counter) / mat.rows() < 0.85) {
//                    row = row + 2;
//                    if (row > mat.rows()) {
//                        row = mat.rows();
//                    }
//                }
                return row;
            }
        }
        return 0;
    }
}
