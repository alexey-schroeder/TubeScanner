package sample.utils.frameSorce;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;

/**
 * Created by Alex on 07.03.2015.
 */
public class ImageFileFrameSource extends FrameSource {
    private String folderName = "frames";
    private File folder;
    private String folderPath;
    private int frameCounter;

    public ImageFileFrameSource() {
        folder = new File(folderName);
        folderPath = folder.getAbsoluteFile().getAbsolutePath() + File.separatorChar;
    }

    @Override
    public Mat getFrame() {
        System.out.println(frameCounter);
        if(frameCounter == 413){
            System.out.println();
        }
        Mat result = Imgcodecs.imread(folderPath + frameCounter + ".bmp");
        frameCounter++;
        return result;
    }

    @Override
    public void stop() {

    }
}
