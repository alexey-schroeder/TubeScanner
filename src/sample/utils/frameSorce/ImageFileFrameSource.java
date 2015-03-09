package sample.utils.frameSorce;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.Scanner;

/**
 * Created by Alex on 07.03.2015.
 */
public class ImageFileFrameSource extends FrameSource {
    private String folderName = "frames";
    private File folder;
    private String folderPath;
    private int frameCounter;
    private boolean withWaiting = true;

    public ImageFileFrameSource() {
        folder = new File(folderName);
        folderPath = folder.getAbsoluteFile().getAbsolutePath() + File.separatorChar;
    }

    @Override
    public Mat getFrame() {
        if(withWaiting){
            System.out.print("");
        }
        System.out.println(frameCounter);
        if(frameCounter == 413){
            System.out.println();
        }
        Mat result = Imgcodecs.imread(folderPath + frameCounter + ".bmp");
        frameCounter++;
        return result;
    }

    private void waitForUser() {
        Scanner keyboard = new Scanner(System.in);
        keyboard.nextInt();
    }

    @Override
    public void stop() {

    }
}
