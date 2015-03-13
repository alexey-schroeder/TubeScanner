package tubeScanner.code.qrCode;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import tubeScanner.code.utils.ImageUtils;

/**
 * Created by Alex on 02.12.2014.
 */
public class CodeCleaner {
    public enum StartPoint {LEFT_TOP, RIGHT_TOP, RIGHT_BOTTOM, LEFT_BOTTOM}

    ;
    static int counter = 0;

    public Mat cleanCode(Mat code) {

        int size = calculateSize(code);
        if (size == 0) {
            return null;
        }
        StartPoint startPoint = findStartPoint(code, size);
        Mat result = null;
        Mat checked = checkBounds(code);
        if (checked == null || checked.size().area() < 0.1) {
            return null;
        }
        Mat resized = Mat.zeros(size * 12, size * 12, checked.type());
        Imgproc.resize(checked, resized, new Size(size * 12, size * 12), 0, 0, Imgproc.INTER_CUBIC);
        Mat rotatedCode = null;

        switch (startPoint) {
            case LEFT_TOP:
                rotatedCode = rotateCode(resized, 90);
                break;
            case RIGHT_TOP:
                rotatedCode = rotateCode(resized, 180);
                break;
            case RIGHT_BOTTOM:
                rotatedCode = rotateCode(resized, 270);
                break;
            case LEFT_BOTTOM:
                rotatedCode = resized;
                break;
        }
//        Imgcodecs.imwrite("./test images/code_2_1_rotated.bmp", rotatedCode);
        int blockSize = size * 4;
        if (blockSize % 2 == 0) {
            blockSize++;
        }
        Imgproc.adaptiveThreshold(rotatedCode, rotatedCode, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, blockSize, 2);
//        Imgcodecs.imwrite("lines/resized_" + counter + ".bmp", recized);
//        Imgcodecs.imwrite("lines/checked_" + counter + ".bmp", checked);
//        counter++;


        result = normalizeInLeftBottomCase(rotatedCode, size);
        checkCodeMarker(result);
        return result;
    }

    public void checkCodeMarker(Mat code) {
        int size = code.cols() / 12;

        //untere leiste muss weiss sein
        int y = code.rows() - size / 2;
        for (int x = size / 2; x < code.cols() - size; x = x + size) {
            double[] data = code.get(y, x);
            if (data == null) {
//                System.out.println("x = " + x + ", y = " + y + ", xPoint = " + x + ", yPoint = " + y + ", xSize = " + code.cols() + ", ySize = " + code.rows());
            } else {
                if (data[0] < 255) {
                    Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(255));
                    int xStart = x - size / 2;
                    int xSize = size;
                    if (xStart < 0) {
                        xStart = 0;
                    }
                    if (xStart + size > code.cols()) {
                        xSize = code.cols() - xStart;
                    }

                    int yStart = y - size / 2;
                    int ySize = size;
                    if (yStart < 0) {
                        yStart = 0;
                    }
                    if (yStart + ySize > code.rows()) {
                        ySize = code.rows() - yStart;
                    }
                    calculatedPoint.copyTo(code.submat(new Rect(xStart, yStart, xSize, ySize)));
                }
            }
        }

        //obere leiste muss punktir sein
        y = size / 2;
        int counter = 0;
        for (int x = size / 2; x < code.cols() - size; x = x + size) {
            double[] data = code.get(y, x);
            if (data == null) {
//                System.out.println("x = " + x + ", y = " + y + ", xPoint = " + x + ", yPoint = " + y + ", xSize = " + code.cols() + ", ySize = " + code.rows());
            }

            if (counter % 2 == 0 && data[0] < 255) {
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(255));
                calculatedPoint.copyTo(code.submat(new Rect(x - size / 2, 0, size, size)));
            } else if (counter % 2 == 1 && data[0] > 0) {
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(0));
                calculatedPoint.copyTo(code.submat(new Rect(x - size / 2, 0, size, size)));
            }
            counter++;
        }

//linke leiste muss weiss sein
        int x = size / 2;
        for (y = size / 2; y < code.rows() - size; y = y + size) {
            double[] data = code.get(y, x);
            if (data == null) {
//                System.out.println("x = " + x + ", y = " + y + ", xPoint = " + x + ", yPoint = " + y + ", xSize = " + code.cols() + ", ySize = " + code.rows());
            }
            if (data[0] < 255) {
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(255));
                calculatedPoint.copyTo(code.submat(new Rect(0, y - size / 2, size, size)));
            }
        }

        // rechte leiste muss punktir sein
        counter = 0;
        x = code.cols() - size / 2;
        for (y = code.rows() - size / 2; y < size; y = y - size) {
            double[] data = code.get(y, x);
            if (data == null) {
//                System.out.println("x = " + x + ", y = " + y + ", xPoint = " + x + ", yPoint = " + y + ", xSize = " + code.cols() + ", ySize = " + code.rows());
            }

            if (counter % 2 == 0 && data[0] < 255) {
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(255));
                calculatedPoint.copyTo(code.submat(new Rect(x - size / 2, y - size / 2, size, size)));
            } else if (counter % 2 == 1 && data[0] > 0) {
                Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(0));
                calculatedPoint.copyTo(code.submat(new Rect(x - size / 2, y - size / 2, size, size)));
            }
            counter++;
        }
    }

    public Mat rotateCode(Mat code, double angle) {
        Point center = new Point(code.cols() / 2, code.rows() / 2);
        Mat rotatedCode = ImageUtils.rotate(code, center, angle);
        return rotatedCode;
    }

    public Mat getBoundedCode(Mat code) {
        int leftBound = findLeftBound(code, 0.85);
        int rightBound = findRightBound(code, 0.85);
        int topBound = findTopBound(code, 0.85);
        int bottomBound = findBottomBound(code, 0.85);
        if (leftBound >= code.cols() || rightBound <= 0 || topBound >= code.rows() || bottomBound <= 0) {
            return null;
        }
        Mat boundedCode = code.submat(new Rect(leftBound, topBound, rightBound - leftBound, bottomBound - topBound)).clone();
        return boundedCode;
    }

    public Mat checkBounds(Mat code) {
        if (code.rows() == 0 || code.cols() == 0) {
            return null;
        }


        int size = calculateSize(code);

        if (size == 0) {
            return null;
        }
        StartPoint startPoint = findStartPoint(code, size);
        int leftBound = 0;
        int maxLeftBound = 2 * size;
        int rightBound = code.cols() - 1;
        int minRightBound = code.cols() - 2 * size;
        int topBound = 0;
        int maxTopBound = 2 * size;
        int bottomBound = code.rows() - 1;
        int minBottomBound = code.rows() - 2 * size;
        boolean isOk;
        switch (startPoint) {
            case LEFT_TOP:
                isOk = checkColumn(code, leftBound, 0.15);
                while (!isOk && leftBound < maxLeftBound) {
                    leftBound++;
                    isOk = checkColumn(code, leftBound, 0.15);
                }
                isOk = checkRow(code, topBound, 0.15);
                while (!isOk && topBound < maxTopBound) {
                    topBound++;
                    isOk = checkRow(code, topBound, 0.15);
                }
                break;
            case RIGHT_TOP:
                isOk = checkColumn(code, rightBound, 0.15);
                while (!isOk && rightBound > minRightBound) {
                    rightBound--;
                    isOk = checkColumn(code, rightBound, 0.15);
                }
                isOk = checkRow(code, topBound, 0.15);
                while (!isOk && topBound < maxTopBound) {
                    topBound++;
                    isOk = checkRow(code, topBound, 0.15);
                }
                break;
            case RIGHT_BOTTOM:
                isOk = checkColumn(code, rightBound, 0.15);
                while (!isOk && rightBound > minRightBound) {
                    rightBound--;
                    isOk = checkColumn(code, rightBound, 0.15);
                }
                isOk = checkRow(code, bottomBound, 0.15);
                while (!isOk && bottomBound > minBottomBound) {
                    bottomBound--;
                    isOk = checkRow(code, bottomBound, 0.15);
                }
                break;
            case LEFT_BOTTOM:
                isOk = checkColumn(code, leftBound, 0.15);
                while (!isOk && leftBound < maxLeftBound) {
                    leftBound++;
                    isOk = checkColumn(code, leftBound, 0.15);
                }
                isOk = checkRow(code, bottomBound, 0.15);
                while (!isOk && bottomBound > minBottomBound) {
                    bottomBound--;
                    isOk = checkRow(code, bottomBound, 0.15);
                }
                break;
        }

        if (rightBound < 0) {
            rightBound = 0;
        }

        if (bottomBound < 0) {
            bottomBound = 0;
        }
        if (leftBound == rightBound || bottomBound == topBound) {
            return code;
        }

        Mat boundedCode = code.submat(new Rect(leftBound, topBound, rightBound - leftBound + 1, bottomBound - topBound + 1)).clone();
        return boundedCode;
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
//                    System.out.println("x = " + x + ", y = " + y + ", xPoint = " + xPoint + ", yPoint = " + yPoint + ", xSize = " + code.cols() + ", ySize = " + code.rows());
                } else {
                    Mat calculatedPoint = new Mat(size, size, code.type(), new Scalar(data[0]));
                    calculatedPoint.copyTo(result.submat(new Rect(x, y - size, size, size)));
                }
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
            try {
                double[] data = mat.get(row, col);

                if (data[0] < 254) {
                    counter++;
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            if (data != null && data[0] > minValue) {
                counter++;
            }
        }
        return counter / mat.rows();
    }

    public double getProbabilityInRow(Mat mat, int row, int minValue) {
        double counter = 0;
        for (int col = 0; col < mat.cols(); col++) {
            double[] data = mat.get(row, col);
            if (data != null && data[0] > minValue) {
                counter++;
            }
        }
        return counter / mat.cols();
    }

    public boolean checkRow(Mat mat, int row, double thresholdInPercent) {
        int threshold = (int) (mat.cols() * thresholdInPercent);
        double counter = 0;
        for (int col = 0; col < mat.cols(); col++) {
            try {
                double[] data = mat.get(row, col);
                if (data != null && data[0] < 254) {
                    counter++;
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                if (data != null && data[0] < 254) {
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
                if (data != null && data[0] < 254) {
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
                if (data != null && data[0] < 254) {
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
