package sample;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import sample.utils.CodeCleaner;
import sample.utils.DataMatrixInterpreter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application {
    private static Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        loadLibrary();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = (Parent) fxmlLoader.load();
        controller = fxmlLoader.getController();
        primaryStage.setTitle("Calibrator");
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
        controller.initGrabber();
        controller.startGrabber();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                controller.stopGrabber();
            }
        });
    }


    //        public static void main(String[] args) {
//        launch(args);
//    }
    public static void main(String[] args) throws IOException {
        loadLibrary();
        File file = new File("lines/code_1_2.bmp");
        Mat source = Imgcodecs.imread(file.getAbsolutePath(), CvType.CV_8UC4);
//        CodeCleaner codeCleaner = new CodeCleaner();
//        Mat rec =codeCleaner.calculateLeftBottomCase(source, 4);

        Mat rec = calculateLeftBottomCase(source, 4);
        Imgcodecs.imwrite("lines/code_temp.bmp", rec);
        Imgproc.dilate(rec, rec, new Mat(), new Point(-1, -1), 1);
        Imgcodecs.imwrite("lines/code_dilate.bmp", rec);
        Imgproc.erode(rec, rec, new Mat(), new Point(-1, -1), 2);
        Imgcodecs.imwrite("lines/code_erode.bmp", rec);
        Core.bitwise_not(rec, rec);
        Imgcodecs.imwrite("lines/code_negative.bmp", rec);
        DataMatrixInterpreter dataMatrixInterpreter = new DataMatrixInterpreter();
        String text = dataMatrixInterpreter.decode(new File("lines/code_negative.bmp"));
        System.out.println(text);
    }


    private static Mat calculateLeftBottomCase(Mat mat, int size) {
        int rows = size * 12;
        int cols = size * 12;
        int rowSize = size;

        Mat result = Mat.zeros(rows, cols, mat.type());
        int rowCounter = 1;

        int counter = 0;
        for (int row = mat.rows() - size; row >= 0; row = row - size) {
            int colCounter = 0;
            if (row < 0) {
                rowSize = rowSize + row;
                row = 0;
            }
            int resultRow = rows - rowCounter * size;
            int colSize = size;
//            System.out.println("row: " + row);
            for (int col = 0; col < mat.cols(); col = col + size) {
                if (col + size > mat.cols()) {
                    colSize = mat.cols() - col;
                }
                Imgproc.rectangle(mat, new Point(col, row), new Point(col + colSize, row + rowSize), new Scalar(255, 0, 0), 1);
//                Mat point = mat.submat(new Rect(col, row, colSize, rowSize));
//                int pointValue = calculatePointValue(point);
//                Mat calculatedPoint = new Mat(size, size, mat.type(), new Scalar(pointValue));
//
//                int resultCol = colCounter * size;
//                calculatedPoint.copyTo(result.submat(new Rect(resultCol, resultRow, size, size)));
//                Imgcodecs.imwrite("lines/code_temp_" + counter + ".bmp", result);
                counter++;
                colCounter++;
            }
            rowCounter++;
        }
        return mat;
    }

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
}
