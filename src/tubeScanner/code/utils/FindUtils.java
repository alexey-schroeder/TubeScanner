package tubeScanner.code.utils;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 13.03.2015.
 */
public class FindUtils {

    public static ArrayList<Point> findNeighbor(Point point, Collection<Point> points, double referenceDistance, double maxError, Point[] cellVectors) {
        ArrayList<Point> neighbors = new ArrayList<>();
        double maxAngleDiff = 10;
        for (Point tempPoint : points) {
            double distanceDiff = Math.abs(referenceDistance - ImageUtils.getDistance(point, tempPoint));
            if (distanceDiff <= maxError) {
                Point vector = PointUtils.minus(point, tempPoint);
                double angle_1 = PointUtils.getAngleBetweenVectors(cellVectors[0], vector);
                double angle_2 = PointUtils.getAngleBetweenVectors(cellVectors[1], vector);
                boolean angle_1IsCorrect = Math.abs(angle_1 - 90) < maxAngleDiff;
                boolean angle_2IsCorrect = Math.abs(angle_2 - 90) < maxAngleDiff;
                if (angle_1IsCorrect || angle_2IsCorrect) {
                    neighbors.add(tempPoint);
                }
            }
        }
        return neighbors;
    }

    public static <T> ArrayList<T> filterDublicate(Collection<T> triplets) {
        ArrayList<T> result = new ArrayList<>();
        for (T triplet : triplets) {
            boolean contains = false;
            for (T filteredTriplet : result) {
                if (triplet.equals(filteredTriplet)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                result.add(triplet);
            }
        }
        return result;
    }

    public static <K, V> K findKeyForValueInMap(Map<K, V> map, V value) {
        for (K key : map.keySet()) {
            if (map.get(key).equals(value)) {
                return key;
            }
        }
        return null;
    }

    //map must be a bijection in order for this to work properly
    public static <K,V> HashMap<V,K> reverse(Map<K,V> map) {
        HashMap<V,K> rev = new HashMap<V, K>();
        for(Map.Entry<K,V> entry : map.entrySet())
            rev.put(entry.getValue(), entry.getKey());
        return rev;
    }
}
