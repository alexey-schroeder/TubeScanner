package tubeScanner.code.frameSorce;

import org.opencv.core.Mat;

/**
 * Created by Alex on 07.03.2015.
 */
public abstract class FrameSource {
    public abstract Mat getFrame();
    public abstract void stop();
}
