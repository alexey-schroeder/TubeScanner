package sample.utils;

/**
 * Created by Alex on 16.02.2015.
 */
public class Basis {
    private PointTriplet pointBasis;
    private NodeTriplet nodeBasis;

    public Basis() {
    }

    public Basis(PointTriplet pointBasis, NodeTriplet nodeBasis) {
        this.pointBasis = pointBasis;
        this.nodeBasis = nodeBasis;
    }

    public PointTriplet getPointBasis() {
        return pointBasis;
    }

    public void setPointBasis(PointTriplet pointBasis) {
        this.pointBasis = pointBasis;
    }

    public NodeTriplet getNodeBasis() {
        return nodeBasis;
    }

    public void setNodeBasis(NodeTriplet nodeBasis) {
        this.nodeBasis = nodeBasis;
    }
}
