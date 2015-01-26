package sample.test;

import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Created by Alex on 21.01.2015.
 */
public class SimpleBlobDetectorTest {

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
    public void testBlob() throws Exception {
        String fileName = "SC-Thermo-Tubes001.png";
        File file = new File(fileName);
        Mat source = Imgcodecs.imread(file.getAbsolutePath(), CvType.CV_8UC4);
        int resizeFactor = Math.min(source.rows(), source.cols()) / 1000;
        if (resizeFactor < 1) {
            resizeFactor = 1;
        }
        int mult = 5;

        resizeFactor = resizeFactor * mult;
        Mat resized = Mat.zeros(source.rows() / resizeFactor, source.cols() / resizeFactor, source.type());
        Imgproc.resize(source, resized, new Size(source.cols() / resizeFactor, source.rows() / resizeFactor), 0, 0, Imgproc.INTER_CUBIC);
        Imgproc.resize(resized, resized, new Size(resized.cols() * mult, resized.rows() * mult), 0, 0, Imgproc.INTER_CUBIC);

//        int blokSize = Math.min(resized.cols(), resized.rows()) / 14;
//        if(blokSize % 2 == 0){
//            blokSize++;
//        }
//        Imgproc.adaptiveThreshold(resized, resized, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, blokSize, 0);
        MatOfKeyPoint codeKeypoints = computeKeyPoints(resized);
        Features2d.drawKeypoints(resized, codeKeypoints, resized);
        Imgcodecs.imwrite("points.bmp", resized);
    }

    public MatOfKeyPoint computeKeyPoints(Mat mat) {
//        File outputFile = null;
//        try {
//            outputFile = File.createTempFile("orbDetectorParams", ".YAML", new File("."));
//            writeToFile(outputFile, "%YAML:1.0\nscaleFactor: 1.2\nnLevels: 8\nfirstLevel: 0 \nedgeThreshold: 0\npatchSize: 10\nWTA_K: 2\nscoreType: 1\nnFeatures: 500\n");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.MSER);
//        featureDetector.read("prop.iml");
//        featureDetector.write("prop_1.iml");
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        featureDetector.detect(mat, keypoints);
        return keypoints;
    }
}
