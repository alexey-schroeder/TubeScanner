package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.*;
import sample.utils.frameSorce.CameraFrameSource;
import sample.utils.frameSorce.FrameSource;
import sample.utils.frameSorce.ImageFileFrameSource;

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
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                controller.stop();
            }
        });
        FrameSource frameSource = new CameraFrameSource();
//        FrameSource frameSource = new ImageFileFrameSource();
        controller.setFrameSource(frameSource);
        primaryStage.show();
        controller.start();
    }


    public static void main(String[] args) {
        launch(args);
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
