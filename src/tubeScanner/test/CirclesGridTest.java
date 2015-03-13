package tubeScanner.test;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Created by Alex on 15.01.2015.
 */
public class CirclesGridTest {

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
        Imgproc.threshold(resized, resized, 25, 255, Imgproc.THRESH_BINARY);
//        Imgcodecs.imwrite("threshold.bmp", resized);
        MatOfPoint2f mCorners = new MatOfPoint2f();
        boolean founded = Calib3d.findCirclesGrid(resized, new Size(2, 2),mCorners, Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_SYMMETRIC_GRID);
        Assert.assertEquals(founded, true);
        System.err.println(founded);
    }

}
