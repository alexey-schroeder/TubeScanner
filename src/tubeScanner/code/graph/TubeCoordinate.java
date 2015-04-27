package tubeScanner.code.graph;

/**
 * Created by Alex on 21.04.2015.
 */
public class TubeCoordinate {
    private int numberCoordinate;
    private String charCoordinate;

    public int getNumberCoordinate() {
        return numberCoordinate;
    }

    public void setNumberCoordinate(int numberCoordinate) {
        this.numberCoordinate = numberCoordinate;
    }

    public String getCharCoordinate() {
        return charCoordinate;
    }

    public void setCharCoordinate(String charCoordinate) {
        this.charCoordinate = charCoordinate;
    }

    public TubeCoordinate(String charCoordinate, int numberCoordinate) {
        this.charCoordinate = charCoordinate;
        this.numberCoordinate = numberCoordinate;
    }
}
