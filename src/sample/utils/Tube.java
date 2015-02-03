package sample.utils;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Alex on 27.01.2015.
 */
public class Tube {

    private class Coordinate {
        int x;
        int y;

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        private Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
            mainCodesMap = new HashMap<>();
            allMaps = new HashSet<>();
        }
    }

    private HashMap<String, Coordinate> mainCodesMap; // hier sind nur die codes, die mit einander verbunden sind
    private HashSet<HashMap<String, Coordinate>> allMaps;

    public void addCode(String code) {
        if (mainCodesMap.isEmpty()) {
            Coordinate coordinate = new Coordinate(6, 6);
            mainCodesMap.put(code, coordinate);
        } else {
            if(mainCodesMap.containsKey(code)){
                return;
            }
            boolean available = false;
            for (HashMap<String, Coordinate> map : allMaps) {
                if (map.containsKey(code)) {
                    available = true;
                    break;
                }
            }
            if (!available) {
                HashMap<String, Coordinate> newMap = new HashMap<>();
                Coordinate coordinate = new Coordinate(6, 6);
                newMap.put(code, coordinate);
                allMaps.add(newMap);
            }

        }
    }

    public void addCode(String referenceCode, String code, int xDiff, int yDiff) {
        if(mainCodesMap.isEmpty()){

        }
        Coordinate referenceCoordinate = mainCodesMap.get(referenceCode);
        if (referenceCoordinate == null) {
            Coordinate codeCoordinate = mainCodesMap.get(code);
            if (codeCoordinate == null) { // beide mainCodesMap sind in mainMap nicht vorhanden

            }
        }
    }
}
