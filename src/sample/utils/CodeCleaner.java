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
                break;
            case RIGHT_TOP:
                result = calculateRightTopCase(code, size);
                break;
            case RIGHT_BOTTOM:
                result = calculateRightBottomCase(code, size);
                break;
            case LEFT_BOTTOM:
                result = calculateLeftBottomCase(code, size);
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
        Mat result = new Mat(size * 12, size * 12, mat.type());
        for (int row = 0; row < mat.rows() - size; row = row + size) {
            for (int col = 0; col < mat.cols() - size; col = col + size) {
                Mat point = mat.submat(new Rect(col, row, size, size));
                int pointValue = calculatePointValue(point);
                Mat calculatedPoint = new Mat(size, size, mat.type(), new Scalar(pointValue));
                calculatedPoint.copyTo(result.submat(new Rect(col, row, size, size)));
            }
        }
        return result;
    }

    public Mat calculateRightTopCase(Mat mat, int size) {
        Mat result = new Mat(size * 12, size * 12, mat.type());
        for (int row = 0; row < mat.rows() - size; row = row + size) {
            for (int col = mat.cols() - size; col > size; col = col - size) {
                Mat point = mat.submat(new Rect(col, row, size, size));
                int pointValue = calculatePointValue(point);
                Mat calculatedPoint = new Mat(size, size, mat.type(), new Scalar(pointValue));
                calculatedPoint.copyTo(result.submat(new Rect(col, row, size, size)));
            }
        }
        return result;
    }

    public Mat calculateRightBottomCase(Mat mat, int size) {
        Mat result = new Mat(size * 12, size * 12, mat.type());
        for (int row = mat.rows() - size; row > size; row = row - size) {
            for (int col = mat.cols() - size; col > size; col = col - size) {
                Mat point = mat.submat(new Rect(col, row, size, size));
                int pointValue = calculatePointValue(point);
                Mat calculatedPoint = new Mat(size, size, mat.type(), new Scalar(pointValue));
                calculatedPoint.copyTo(result.submat(new Rect(col, row, size, size)));
            }
        }
        return result;
    }

    public Mat calculateLeftBottomCase(Mat mat, int size) {
        int rows = size * 12;
        int cols = size * 12;
        int rowSize = size;

        Mat result = Mat.zeros(rows, cols, mat.type());
        int rowCounter = 1;

        int counter = 0;
        for (int row = mat.rows() - size; row >= 0; row = row - size) {
            System.out.println(row);
            int colCounter = 0;
            if (row < 0) {
                rowSize = rowSize + row;
                row = 0;
            }
            int resultRow = rows - rowCounter * size;
            int colSize = size;
            for (int col = 0; col < mat.cols(); col = col + size) {
                if (col + size > mat.cols()) {
                    colSize = mat.cols() - col;
                }
                Mat point = mat.submat(new Rect(col, row, colSize, rowSize));
                int pointValue = calculatePointValue(point);
                Mat calculatedPoint = new Mat(size, size, mat.type(), new Scalar(pointValue));

                int resultCol = colCounter * size;
                calculatedPoint.copyTo(result.submat(new Rect(resultCol, resultRow, size, size)));
                Imgcodecs.imwrite("lines/code_temp_" + counter + ".bmp", result);
                counter++;
                colCounter++;
            }
            rowCounter++;
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
