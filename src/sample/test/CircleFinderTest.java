package sample.test;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

import static org.testng.Assert.*;

public class CircleFinderTest {

    @BeforeClass
    public static void loadLibrary() {
        System.setProperty("java.library.path", "./lib");
        Field fieldSysPath = null;
        try {
            fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExtractCircles() throws Exception {
        String fileName = "SC-Thermo-Tubes001.png";
        File file = new File(fileName);
        Mat source = Imgcodecs.imread(file.getAbsolutePath(), CvType.CV_8UC4);
        int resizeFactor = Math.min(source.rows(), source.cols()) / 1000;
        if (resizeFactor < 1) {
            resizeFactor = 1;
        }
        Mat resized = Mat.zeros(source.rows() / resizeFactor, source.cols() / resizeFactor, source.type());
        Imgproc.resize(source, resized, new Size(source.cols() / resizeFactor, source.rows() / resizeFactor), 0, 0, Imgproc.INTER_CUBIC);
        Imgcodecs.imwrite("resizedImage.bmp", resized);
        Mat binImage = new Mat(resized.rows(), resized.cols(), resized.type());
        Imgproc.GaussianBlur(resized, binImage, new Size(51, 51), 0, 0);
        Imgcodecs.imwrite("gaussianImage.bmp", binImage);
        Imgproc.equalizeHist(binImage, binImage);
        Imgcodecs.imwrite("equalizeImage.bmp", binImage);
        int blokSize = Math.min(binImage.cols(), binImage.rows()) / 14;
        if (blokSize % 2 == 0) {
            blokSize++;
        }
        System.out.println("blockSize = " + blokSize);
        Imgproc.adaptiveThreshold(binImage, binImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, blokSize, 0);
        Imgcodecs.imwrite("binImage.bmp", binImage);
        Imgproc.erode(binImage, binImage, new Mat(), new Point(-1, -1), 3);
        Imgcodecs.imwrite("erodeImage.bmp", binImage);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(binImage, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
//        Imgproc.drawContours(resized, contours, -1, new Scalar(255,255,0));
//        Imgcodecs.imwrite("contours.bmp", resized);
        List<RotatedRect> allRotatedRects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            RotatedRect tempRotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            if (isQuadrat(tempRotatedRect)) {
                allRotatedRects.add(tempRotatedRect);
//                Point[] rect_points = new Point[4];
//                tempRotatedRect.points(rect_points);
//                Scalar color = new Scalar(0, 0, 255);
//                for (int j = 0; j < 4; j++) {
//                    Imgproc.line(resized, rect_points[j], rect_points[(j + 1) % 4], color, 1);
//                }
            }
        }

        LinkedList<RotatedRect> referenceRects = getReferenceRects(allRotatedRects);
        for(RotatedRect rotatedRect : referenceRects){
            Point[] rect_points = new Point[4];
            rotatedRect.points(rect_points);
                Scalar color = new Scalar(0, 0, 255);
                for (int j = 0; j < 4; j++) {
                    Imgproc.line(resized, rect_points[j], rect_points[(j + 1) % 4], color, 1);
                }
        }


        Imgcodecs.imwrite("rects.bmp", resized);

    }

    public boolean isQuadrat(RotatedRect rect) {
        double minFactor = 0.85;
        Size size = rect.size;
        double min = Math.min(size.width, size.height);
        double max = Math.max(size.width, size.height);
        double factor = min / max;
        return factor > minFactor;
    }

    public LinkedList<RotatedRect> getReferenceRects(List<RotatedRect> allRotatedRects) {
        double maxFactor = 0.8;
        LinkedList<RotatedRect> copyRects = new LinkedList<>(allRotatedRects);
        Collections.sort(copyRects, new Comparator<RotatedRect>() {
            @Override
            public int compare(RotatedRect o1, RotatedRect o2) {
                double area1 = o1.size.area();
                double area2 = o2.size.area();
                if (area1 > area2) {
                    return 1;
                } else if (area1 < area2) {
                    return -1;
                }
                return 0;
            }
        });
        RotatedRect result = null;
        boolean found = false;
        while (copyRects.size() > 3 && !found) {
            result = copyRects.get(copyRects.size() / 2);
            RotatedRect firstRect = copyRects.getFirst();
            RotatedRect lastRect = copyRects.getLast();
            found = true;
            if (firstRect.size.area() / result.size.area() < maxFactor) {
                copyRects.removeFirst();
                found = false;
            }
            if (result.size.area() / lastRect.size.area() < maxFactor) {
                copyRects.removeLast();
                found = false;
            }
        }
        return copyRects;
    }
}