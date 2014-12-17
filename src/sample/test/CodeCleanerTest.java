package sample.test;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import sample.utils.CodeCleaner;
import sample.utils.DataMatrixInterpreter;
import sample.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;

import static org.testng.Assert.*;

public class CodeCleanerTest {

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
    public void testCleanCode() throws Exception {
        File file = new File("./test images/code_7_1.bmp");
        Mat source = Imgcodecs.imread(file.getAbsolutePath(), CvType.CV_8UC4);
        CodeCleaner codeCleaner = new CodeCleaner();
        Mat boundedCode = codeCleaner.getBoundedCode(source);
        Mat cleanedCode = codeCleaner.cleanCode(boundedCode);
        Core.bitwise_not(cleanedCode, cleanedCode);
        Imgcodecs.imwrite("./test images/code_7_res.bmp", cleanedCode);
        BufferedImage bufferedImage = ImageUtils.matToBufferedImage(cleanedCode);
        DataMatrixInterpreter dataMatrixInterpreter = new DataMatrixInterpreter();
        String text = dataMatrixInterpreter.decode(bufferedImage);
        Assert.assertEquals(text, "0122708333");
    }

    @Test
    public void testGetBoundedCode() throws Exception {
        File file = new File("./test images/code_7_1.bmp");
        Mat source = Imgcodecs.imread(file.getAbsolutePath(), CvType.CV_8UC4);
        CodeCleaner codeCleaner = new CodeCleaner();
        Mat boundedCode = codeCleaner.getBoundedCode(source);
        Imgcodecs.imwrite("./test images/code_7_bounded.bmp", boundedCode);
    }

    @Test
    public void testCheckBounds() throws Exception {
        File file = new File("./test images/code_7_1.bmp");
        Mat source = Imgcodecs.imread(file.getAbsolutePath(), CvType.CV_8UC4);
        CodeCleaner codeCleaner = new CodeCleaner();
        Mat boundedCode = codeCleaner.getBoundedCode(source);
        Mat checked = codeCleaner.checkBounds(boundedCode);
        Imgcodecs.imwrite("./test images/code_7_checked.bmp", checked);
    }

    @Test(dataProvider = "startPoint")
    public void testFindStartPoint(String fileName, CodeCleaner.StartPoint expectedResult) throws Exception {
        File file = new File(fileName);
        Mat source = Imgcodecs.imread(file.getAbsolutePath(), CvType.CV_8UC4);
        CodeCleaner codeCleaner = new CodeCleaner();
        Mat boundedCode = codeCleaner.getBoundedCode(source);
        int size = codeCleaner.calculateSize(boundedCode);
        CodeCleaner.StartPoint startPoint = codeCleaner.findStartPoint(boundedCode, size);
        Assert.assertEquals(startPoint, expectedResult);
    }

    @DataProvider(name = "startPoint")
    public static Object[][] startPoints() {
        return new Object[][]{
                {"./test images/code_7_1.bmp", CodeCleaner.StartPoint.LEFT_TOP},
                {"./test images/code_6_1.bmp", CodeCleaner.StartPoint.LEFT_TOP},
                {"./test images/code_8_1.bmp", CodeCleaner.StartPoint.RIGHT_TOP},
                {"./test images/code_24_1.bmp", CodeCleaner.StartPoint.RIGHT_TOP},
                {"./test images/code_27_1.bmp", CodeCleaner.StartPoint.RIGHT_TOP},
                {"./test images/code_33_1.bmp", CodeCleaner.StartPoint.LEFT_TOP}
        };
    }

    @Test
    public void testCalculateLeftTopCase() throws Exception {

    }

    @Test
    public void testCalculateRightTopCase() throws Exception {

    }

    @Test
    public void testCalculateRightBottomCase() throws Exception {

    }

    @Test
    public void testCalculateLeftBottomCase() throws Exception {

    }

    @Test
    public void testGetStartPointByMask() throws Exception {

    }

    @Test
    public void testGetProbabilityByMask() throws Exception {

    }

    @Test
    public void testGetBlackProbability() throws Exception {

    }

    @Test
    public void testNormalizeInLeftTopCase() throws Exception {

    }

    @Test
    public void testNormalizeInRightTopCase() throws Exception {

    }

    @Test
    public void testNormalizeInRightBottomCase() throws Exception {

    }

    @Test
    public void testNormalizeInLeftBottomCase() throws Exception {

    }

    @Test
    public void testFindLeftBound() throws Exception {

    }

    @Test
    public void testCheckColumn() throws Exception {

    }

    @Test
    public void testGetProbabilityInColumn() throws Exception {

    }

    @Test
    public void testGetProbabilityInRow() throws Exception {

    }

    @Test
    public void testCheckRow() throws Exception {

    }

    @Test
    public void testFindRightBound() throws Exception {

    }

    @Test
    public void testFindTopBound() throws Exception {

    }

    @Test
    public void testFindBottomBound() throws Exception {

    }
}