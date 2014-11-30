package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

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


        public static void main(String[] args) {
        launch(args);
    }
//    public static void main(String[] args) throws IOException {
//        loadLibrary();
//        File file = new File("code.bmp");
//        Mat source = Imgcodecs.imread(file.getAbsolutePath(), CvType.CV_8UC4);
//        Mat binImage = new Mat(source.rows(), source.cols(), source.type());
//        Imgproc.adaptiveThreshold(source, binImage, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 51, 0);
//        Imgcodecs.imwrite("codeInv.bmp", binImage);
//        String text = new Controller().decode(new File("codeInv.bmp"));
//        System.out.println(text);
//    }

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
