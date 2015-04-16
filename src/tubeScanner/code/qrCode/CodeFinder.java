package tubeScanner.code.qrCode;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import tubeScanner.code.utils.ImageUtils;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by Alex on 17.11.2014.
 */
public class CodeFinder {
    private int resizeFactor;
    private Point codeCenter;

    public Mat extractCode(Mat grayImage) {
        Mat code = null;
        double imageArea = grayImage.cols() * grayImage.rows();
        Mat binImage = new Mat(grayImage.rows(), grayImage.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(grayImage, binImage, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(binImage, binImage, new Size(3, 3), 3, 3);

        Imgproc.adaptiveThreshold(binImage, binImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 51, 0);
        Imgproc.dilate(binImage, binImage, new Mat(), new Point(-1, -1), 1);
        Imgproc.erode(binImage, binImage, new Mat(), new Point(-1, -1), 1);
        Mat hierarchy = new Mat();
        Mat connectedComponents = new Mat();
        int componentsSize = Imgproc.connectedComponents(binImage, connectedComponents);
        Set<Point>[] componentPoints = new Set[componentsSize];
        for (int r = 0; r < binImage.rows(); ++r) {
            for (int c = 0; c < binImage.cols(); ++c) {
                int[] label = new int[1];
                connectedComponents.get(r, c, label);
                int labelNum = label[0];
                Set<Point> set = componentPoints[labelNum];
                if (set == null) {
                    set = new HashSet<Point>();
                    componentPoints[labelNum] = set;
                }
                set.add(new Point(c, r));
            }
        }

        RotatedRect rotatedRect = null;
        for (Set<Point> points : componentPoints) {
            Point[] contourPoints = points.toArray(new Point[0]);
            RotatedRect tempRotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contourPoints));
            Size rectSize = tempRotatedRect.size;
            double minSize = Math.min(rectSize.width, rectSize.height);
            double maxSize = Math.max(rectSize.width, rectSize.height);
            double minSizeDiff = 75;//unterschied zwischen width und height in prozent
            double sizeDiff = minSize / maxSize * 100;
            double minArea = imageArea / 9;
            double maxArea = imageArea / 1.3;
            double area = rectSize.area();
            if (sizeDiff > minSizeDiff && area > minArea && area < maxArea) {
                Point[] rect_points = new Point[4];
                tempRotatedRect.points(rect_points);
                rotatedRect = tempRotatedRect;
            }
        }

        hierarchy.release();
        if (rotatedRect != null && rotatedRect.size.width > 0 && rotatedRect.size.height > 0) {
            Mat rotatedImage = ImageUtils.rotate(grayImage, rotatedRect.center, rotatedRect.angle);
            int puffer = 2;
            int x = (int) (rotatedRect.center.x - puffer - rotatedRect.size.width / 2);
            if (x < 0) {
                x = 0;
            }
            int y = (int) (rotatedRect.center.y - puffer - rotatedRect.size.height / 2);
            if (y < 0) {
                y = 0;
            }
            int width = (int) rotatedRect.size.width + puffer * 2;
            if (x + width >= rotatedImage.cols()) {
                width = rotatedImage.cols() - x - 1;
            }
            int height = (int) rotatedRect.size.height + puffer * 2;
            if (y + height >= rotatedImage.rows()) {
                height = rotatedImage.rows() - y - 1;
            }

            if (width < 24 || height < 24) {
                return null;
            }
            code = new Mat();
            Imgproc.cvtColor(rotatedImage, code, Imgproc.COLOR_RGB2GRAY);
            code = code.submat(new Rect(x, y, width, height));
            Imgproc.threshold(code, code, 120, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
            codeCenter = rotatedRect.center.clone();
        }
        return code;
    }

    public int getResizeFactor() {
        return resizeFactor;
    }

    public Point getCodeCenter() {
        return codeCenter;
    }
}
