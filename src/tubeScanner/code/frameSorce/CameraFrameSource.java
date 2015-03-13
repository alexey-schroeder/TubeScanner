package tubeScanner.code.frameSorce;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;

/**
 * Created by Alex on 07.03.2015.
 */
public class CameraFrameSource extends FrameSource {
    private VideoCapture camera;
    private boolean withFrameSave = false;
    private String saveFolderName = "frames";
    private File saveFolder;
    private int frameCounter;

    public CameraFrameSource() {
        camera = new VideoCapture(0);
        camera.open(0); //Useless
        boolean wset = camera.set(Videoio.CV_CAP_PROP_FRAME_WIDTH, 1280);
        boolean hset = camera.set(Videoio.CV_CAP_PROP_FRAME_HEIGHT, 720);
        if (camera.isOpened()) {
            System.out.println("Camera Ok");
        } else {
            throw new RuntimeException("camera isn't initialized!");
        }
        if (withFrameSave) {
            saveFolder = new File(saveFolderName);
            if (!saveFolder.exists()) {
                saveFolder.mkdir();
            }
        }
    }

    @Override
    public Mat getFrame() {
        Mat frame = new Mat();
        camera.read(frame);
        if (withFrameSave) {
            String path = saveFolder.getAbsoluteFile().getAbsolutePath() + File.separatorChar;
            Imgcodecs.imwrite(path + frameCounter + ".bmp", frame);
            frameCounter++;
        }
        return frame;
    }

    @Override
    public void stop() {
        camera.release();
    }

    public String getSaveFolderName() {
        return saveFolderName;
    }

    public void setSaveFolderName(String saveFolderName) {
        this.saveFolderName = saveFolderName;
    }

    public boolean isWithFrameSave() {
        return withFrameSave;
    }

    public void setWithFrameSave(boolean withFrameSave) {
        this.withFrameSave = withFrameSave;
    }
}
