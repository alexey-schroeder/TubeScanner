package tubeScanner.code.dataModel.doublet;

import org.opencv.core.Point;
import tubeScanner.code.utils.FindUtils;
import tubeScanner.code.utils.ImageUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Alex on 13.03.2015.
 */
public class PointDoubletFinder {

    public ArrayList<PointDoublet> findDoublets(Collection<Point> points, Point[] cellVectors) {
        ArrayList<PointDoublet> result = new ArrayList<>();
        if (cellVectors == null) {
            return result;
        }
        //todo den code muss man in utils ubertragen, da er im mehreren klassen verwendet wird
        Point vector_1 = cellVectors[0];
        Point vector_2 = cellVectors[1];
        double averageCellVectorLength = (ImageUtils.getVectorLength(vector_1) + ImageUtils.getVectorLength(vector_2)) / 2;
        double circleRadius = averageCellVectorLength / 3;
        double maxError = circleRadius / 2;
        for (Point point : points) {
            ArrayList<Point> neighbors = FindUtils.findNeighbor(point, points, averageCellVectorLength, maxError, cellVectors);
            for (Point neighbor : neighbors) {
                PointDoublet doublet = new PointDoublet(point, neighbor);
                result.add(doublet);
            }
        }
        ArrayList<PointDoublet> filteredResult = FindUtils.filterDublicate(result);
        return filteredResult;
    }
}
