package sample.utils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Alex on 02.12.2014.
 */
public class CodeCleaner {
    public enum StartPoint {LEFT_TOP, RIGHT_TOP, RIGHT_BOTTOM, LEFT_BOTTOM}

    ;
    static int counter = 0;

    public Mat cleanCode(Mat code) {

        int size = calculateSize(code);
        StartPoint startPoint = findStartPoint(code, size);
        Mat result = null;
        Mat checked = checkBounds(code);
        Mat recized = Mat.zeros(size * 12, size * 12, checked.type());
        Imgproc.resize(checked, recized, new Size(size * 12, size * 12), 0, 0, Imgproc.INTER_CUBIC);
        Imgproc.adaptiveThreshold(recized, recized, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 2);
//        Imgcodecs.imwrite("lines/resized_" + counter + ".bmp", recized);
//        Imgcodecs.imwrite("lines/checked_" + counter + ".bmp", checked);
//        counter++;
        switch (startPoint) {
            case LEFT_TOP:
                result = normalizeInLeftTopCase(recized, size);
                break;
            case RIGHT_TOP:
                result = normalizeInRightTopCase(recized, size);
                break;
            case RIGHT_BOTTOM:
                result = normalizeInRightBottomCase(recized, size);
                break;
            case LEFT_BOTTOM:
                result = normalizeInLeftBottomCase(recized, size);
                break;
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

    public Mat checkBounds(Mat code) {
        int size = calculateSize(code);
        StartPoint startPoint = findStartPoint(code, size);
        int leftBound = 0;
        int rightBound = code.cols() - 1;
        int topBound = 0;
        int bottomBound = code.rows() - 1;
        boolean isOk;
        switch (startPoint) {
            case LEFT_TOP:
                isOk = checkColumn(code, leftBound, 0.15);
                while (!isOk) {
                    leftBound++;
                    isOk = checkColumn(code, leftBound, 0.15);
                }
                isOk = checkRow(code, topBound, 0.15);
                while (!isOk) {
                    topBound++;
                    isOk = checkRow(code, topBound, 0.15);
                }
                break;
            case RIGHT_TOP:
                isOk = checkColumn(code, rightBound, 0.15);
                while (!isOk) {
                    rightBound--;
                    isOk = checkColumn(code, rightBound, 0.15);
                }
                isOk = checkRow(code, topBound, 0.15);
                while (!isOk) {
                    topBound++;
                    isOk = checkRow(code, topBound, 0.15);
                }
                break;
            case RIGHT_BOTTOM:
                isOk = checkColumn(code, rightBound, 0.15);
                while (!isOk) {
                    rightBound--;
                    isOk = checkColumn(code, rightBound, 0.15);
                }
                isOk = checkRow(code, bottomBound, 0.15);
                while (!isOk) {
                    bottomBound--;
                    isOk = checkRow(code, bottomBound, 0.15);
                }
                break;
            case LEFT_BOTTOM:
                isOk = checkColumn(code, leftBound, 0.15);
                while (!isOk) {
                    leftBound++;
                    isOk = checkColumn(code, leftBound, 0.15);
                }
                isOk = checkRow(code, bottomBound, 0.15);
                while (!isOk) {
                    bottomBound--;
                    isOk = checkRow(code, bottomBound, 0.15);
                }
                break;
        }
        Mat boundedCode = code.submat(new Rect(leftBound, topBound, rightBound - leftBound + 1, bottomBound - topBound + 1)).clone();
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

    public StartPoint findStartPoint(Mat mat, int size) {
        double leftTop = getProbabilityByMask(mat, size, StartPoint.LEFT_TOP);
        double rightTop = getProbabilityByMask(mat, size, StartPoint.RIGHT_TOP);
        double leftBottom = getProbabilityByMask(mat, size, StartPoint.LEFT_BOTTOM);
        double rightBottom = getProbabilityByMask(mat, size, StartPoint.RIGHT_BOTTOM);
        if (leftTop > Math.max(rightTop, Math.max(leftBottom, rightBottom))) {
            return StartPoint.LEFT_TOP;
        } else if (rightTop > Math.max(leftTop, Math.max(leftBottom, rightBottom))) {
            return StartPoint.RIGHT_TOP;
        } else if (leftBottom > Math.max(leftTop, Math.max(rightTop, rightBottom))) {
            return StartPoint.LEFT_BOTTOM;
        } else {
            return StartPoint.RIGHT_BOTTOM;
        }

    }

    public StartPoint getStartPointByMask(Mat mat, int size, StartPoint bestPoint, StartPoint lastBestPoint) {
        double lastBestPointProbability = getProbabilityByMask(mat, size, lastBestPoint);
        double bestPointProbability = getProbabilityByMask(mat, size, bestPoint);
        if (lastBestPointProbability > bestPointProbability) {
            return lastBestPoint;
        } else {
            return bestPoint;
        }
    }

    public double getProbabilityByMask(Mat mat, int size, StartPoint point) {
        double probabilitySumme = 0;
        switch (point) {
            case LEFT_TOP:
                for (int row = 0; row < size; row++) {
                    probabilitySumme = probabilitySumme + getProbabilityInRow(mat, row, 254);
                }
                for (int col = 0; col < size; col++) {
                    probabilitySumme = probabilitySumme + getProbabilityInColumn(mat, col, 254);
                }
                break;
            case RIGHT_TOP:
                for (int row = 0; row < size; row++) {
                    probabilitySumme = probabilitySumme + getProbabilityInRow(mat, row, 254);
                }
                for (int col = mat.cols() - size; col < mat.cols(); col++) {
                    probabilitySumme = probabilitySumme + getProbabilityInColumn(mat, col, 254);
                }
                break;
            case LEFT_BOTTOM:
                for (int row = mat.rows() - size; row < mat.rows(); row++) {
                    probabilitySumme = probabilitySumme + getProbabilityInRow(mat, row, 254);
                }
                for (int col = 0; col < size; col++) {
                    probabilitySumme = probabilitySumme + getProbabilityInColumn(mat, col, 254);
                }
                break;
            case RIGHT_BOTTOM:
                for (int row = mat.rows() - size; row < mat.rows(); row++) {
                    probabilitySumme = probabilitySumme + getProbabilityInRow(mat, row, 254);
                }
                for (int col = mat.cols() - size; col < mat.cols(); col++) {
                    probabilitySumme = probabilitySumme + getProbabilityInColumn(mat, col, 254);
                }
                break;
        }
        return probabilitySumme;
    }

    private boolean checkStartPoint(Mat mat, int size, StartPoint startPoint) {
        return false;
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

    public double getBlackProbability(Mat point) {
        double counter_black = 0;
        for (int row = 0; row < point.rows(); row++) {
            for (int col = 0; col < point.cols(); col++) {
                double[] data = point.get(row, col);
                if (data[0] < 254) {
                    counter_black++;
                }
            }
        }
        return counter_black / (point.cols() * point.rows());
    }

    public Mat normalizeInLeftTopCase(Mat code, int size) {
        Mat result = Mat.zeros(size * 12, size * 12, code.type());
        for (int x = 0; x < size * 12; x = x + size) {
            for (int y = 0; y < size * 12; y = y + size) {
                int xPoint = x + size / 2;
                if (xPoint > code.cols()) {
                    xPoint = code.cols() - 1;
                }

                int yPoint = y + size / 2;
                if (yPoint > code.rows()) {
                    yPoint = code.rows() - 1;
                }
                double[] data = code.get(yPoint, xPoint);
                if (data == null) {
                    System.out.println("x = " + x + ", y = " + y + ", xPoint = " + xPoint + ", yPoint = " + yPoint + ", xSize = " + code.cols() + ", ySize = " + code.rows());
                }
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(data[0]));
                calculatedPoint.copyTo(result.submat(new Rect(x, y, size, size)));
            }
        }
        return result;
    }

    public Mat normalizeInRightTopCase(Mat code, int size) {
        Mat result = Mat.zeros(size * 12, size * 12, code.type());
        int xDiff = code.cols() - size * 12;
        int yDiff = code.rows() - size * 12;
        for (int x = size * 12; x > 0; x = x - size) {
            for (int y = 0; y < size * 12; y = y + size) {
                int xPoint = x - size / 2 + xDiff;
                if (xPoint < 0) {
                    xPoint = 0;
                }

                int yPoint = y + size / 2;
                if (yPoint > code.rows()) {
                    yPoint = code.rows() - 1;
                }
                double[] data = code.get(yPoint, xPoint);
                if (data == null) {
                    System.out.println("x = " + x + ", y = " + y + ", xPoint = " + xPoint + ", yPoint = " + yPoint + ", xSize = " + code.cols() + ", ySize = " + code.rows());
                }
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(data[0]));
                calculatedPoint.copyTo(result.submat(new Rect(x - size, y, size, size)));
            }
        }
        return result;
    }

    public Mat normalizeInRightBottomCase(Mat code, int size) {
        Mat result = Mat.zeros(size * 12, size * 12, code.type());
        int xDiff = code.cols() - size * 12;
        int yDiff = code.rows() - size * 12;
        for (int x = size * 12; x > 0; x = x - size) {
            for (int y = size * 12; y > 0; y = y - size) {
                int xPoint = x - size / 2 + xDiff;
                if (xPoint < 0) {
                    xPoint = 0;
                }

                int yPoint = y - size / 2 + yDiff;
                if (yPoint < 0) {
                    yPoint = 0;
                } else if (yPoint > code.rows()) {
                    yPoint = code.rows() - 1;
                }
                double[] data = code.get(yPoint, xPoint);
                if (data == null) {
                    System.out.println("x = " + x + ", y = " + y + ", xPoint = " + xPoint + ", yPoint = " + yPoint + ", xSize = " + code.cols() + ", ySize = " + code.rows());
                }
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(data[0]));
                calculatedPoint.copyTo(result.submat(new Rect(x - size, y - size, size, size)));
            }
        }
        return result;
    }

    public Mat normalizeInLeftBottomCase(Mat code, int size) {
        Mat result = Mat.zeros(size * 12, size * 12, code.type());
        int xDiff = code.cols() - size * 12;
        int yDiff = code.rows() - size * 12;
        for (int x = 0; x < size * 12; x = x + size) {
            for (int y = size * 12; y > 0; y = y - size) {
                int xPoint = x + size / 2;
                if (xPoint >= code.cols()) {
                    xPoint = code.cols() - 1;
                }

                int yPoint = y - size / 2 + yDiff;
                if (yPoint < 0) {
                    yPoint = 0;
                }
                double[] data = code.get(yPoint, xPoint);
                if (data == null) {
                    System.out.println("x = " + x + ", y = " + y + ", xPoint = " + xPoint + ", yPoint = " + yPoint + ", xSize = " + code.cols() + ", ySize = " + code.rows());
                }
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(data[0]));
                calculatedPoint.copyTo(result.submat(new Rect(x, y - size, size, size)));
            }
        }
        return result;
    }

    public int calculateSize(Mat code) {
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
        int threshold = (int) (mat.rows() * thresholdInPercent);
        for (int col = 0; col < mat.cols(); col++) {
            double counter = 0;
            for (int row = 0; row < mat.rows(); row++) {
                double[] data = mat.get(row, col);
                if (data[0] < 254) {
                    counter++;
                }
            }
            if (counter < threshold) {
                return col;
            }
        }
        return mat.cols();
    }

    public boolean checkColumn(Mat mat, int col, double thresholdInPercent) {
        int threshold = (int) (mat.rows() * thresholdInPercent);
        double counter = 0;
        for (int row = 0; row < mat.rows(); row++) {
            double[] data = mat.get(row, col);
            if (data[0] < 254) {
                counter++;
            }
        }
        if (counter > threshold) {
            return false;
        }
        return true;
    }

    public double getProbabilityInColumn(Mat mat, int col, int minValue) {
        double counter = 0;
        for (int row = 0; row < mat.rows(); row++) {
            double[] data = mat.get(row, col);
            if (data[0] > minValue) {
                counter++;
            }
        }
        return counter / mat.rows();
    }

    public double getProbabilityInRow(Mat mat, int row, int minValue) {
        double counter = 0;
        for (int col = 0; col < mat.cols(); col++) {
            double[] data = mat.get(row, col);
            if (data[0] > minValue) {
                counter++;
            }
        }
        return counter / mat.cols();
    }

    public boolean checkRow(Mat mat, int row, double thresholdInPercent) {
        int threshold = (int) (mat.cols() * thresholdInPercent);
        double counter = 0;
        for (int col = 0; col < mat.cols(); col++) {
            double[] data = mat.get(row, col);
            if (data[0] < 254) {
                counter++;
            }
        }
        if (counter > threshold) {
            return false;
        }
        return true;
    }

    public int findRightBound(Mat mat, double thresholdInPercent) {
        int threshold = (int) (mat.rows() * thresholdInPercent);
        for (int col = mat.cols() - 1; col >= 0; col--) {
            double counter = 0;
            for (int row = 0; row < mat.rows(); row++) {
                double[] data = mat.get(row, col);
                if (data[0] < 254) {
                    counter++;
                }
            }
            if (counter < threshold) {
                return col;
            }
        }
        return 0;
    }

    public int findTopBound(Mat mat, double thresholdInPercent) {
        int threshold = (int) (mat.cols() * thresholdInPercent);
        for (int row = 0; row < mat.rows(); row++) {
            double counter = 0;
            for (int col = 0; col < mat.cols(); col++) {
                double[] data = mat.get(row, col);
                if (data[0] < 254) {
                    counter++;
                }
            }
            if (counter < threshold) {
                return row;
            }
        }
        return mat.rows();
    }

    public int findBottomBound(Mat mat, double thresholdInPercent) {
        int threshold = (int) (mat.cols() * thresholdInPercent);
        for (int row = mat.rows() - 1; row >= 0; row--) {
            double counter = 0;
            for (int col = 0; col < mat.cols(); col++) {
                double[] data = mat.get(row, col);
                if (data[0] < 254) {
                    counter++;
                }
            }
            if (counter < threshold) {
                return row;
            }
        }
        return 0;
    }
}
