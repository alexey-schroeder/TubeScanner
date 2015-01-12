package sample.test;

import com.googlecode.javacv.cpp.opencv_features2d;
import com.googlecode.javacv.cpp.opencv_stitching;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 05.01.2015.
 */
public class SurfTest {

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
    public void testSurf() throws Exception {

        Mat source = loadImage("code.bmp");
        Mat negativ = new Mat(source.rows(), source.cols(), source.type());
        Core.bitwise_not(source, negativ);
        Imgcodecs.imwrite("negativeCode.bmp", negativ);
        Mat circle = loadImage("circle_7.bmp");

        Mat codeDescriptors = computeDescriptors(negativ);
        System.out.println(codeDescriptors.rows());
        Mat circleDescriptors = computeDescriptors(circle);
        System.out.println(circleDescriptors.rows());

        DescriptorMatcher flannBasedMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        MatOfDMatch dMatch = new MatOfDMatch();
        flannBasedMatcher.match(codeDescriptors, circleDescriptors, dMatch);
        DMatch[] matches = dMatch.toArray();

        double max_dist = 0;
        double min_dist = Double.MAX_VALUE;
        //-- Quick calculation of max and min distances between keypoints
        for (int i = 0; i < codeDescriptors.rows(); i++) {
            double dist = matches[i].distance;
            if (dist < min_dist) min_dist = dist;
            if (dist > max_dist) max_dist = dist;
        }
        System.out.println(max_dist);
        System.out.println(min_dist);
    }

    public Mat loadImage(String fileName) {
        File file = new File(fileName);
        Mat mat = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//        mat.convertTo(mat, Imgproc.COLOR_RGB2GRAY);
        return mat;
    }

    public Mat computeDescriptors(Mat mat) {
        File outputFile = null;
        try {
            outputFile = File.createTempFile("orbDetectorParams", ".YAML", new File("."));
            writeToFile(outputFile, "%YAML:1.0\nscaleFactor: 1.2\nnLevels: 8\nfirstLevel: 0 \nedgeThreshold: 31\npatchSize: 31\nWTA_K: 2\nscoreType: 1\nnFeatures: 500\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        featureDetector.read(outputFile.getPath());
//        featureDetector.write("harris.yml");

        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        featureDetector.detect(mat, keypoints);

        Mat descriptors = new Mat();
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        extractor.compute(mat, keypoints, descriptors);
        return descriptors;
    }

    private void writeToFile(File file, String data) throws Exception {
        FileOutputStream stream = new FileOutputStream(file);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(stream);
        outputStreamWriter.write(data);
        outputStreamWriter.close();
        stream.close();
    }
}
