

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tubeScanner.code.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencv.core.Point;

/**
 * DBSCAN (density-based spatial clustering of applications with noise) algorithm.
 * <p>
 * The DBSCAN algorithm forms clusters based on the idea of density connectivity, i.e.
 * a point p is density connected to another point q, if there exists a chain of
 * points p<sub>i</sub>, with i = 1 .. n and p<sub>1</sub> = p and p<sub>n</sub> = q,
 * such that each pair &lt;p<sub>i</sub>, p<sub>i+1</sub>&gt; is directly density-reachable.
 * A point q is directly density-reachable from point p if it is in the &epsilon;-neighborhood
 * of this point.
 * <p>
 * Any point that is not density-reachable from a formed cluster is treated as noise, and
 * will thus not be present in the result.
 * <p>
 * The algorithm requires two parameters:
 * <ul>
 * <li>eps: the distance that defines the &epsilon;-neighborhood of a point
 * <li>minPoints: the minimum number of density-connected points required to form a cluster
 * </ul>
 * <p>
 * <b>Note:</b> as DBSCAN is not a centroid-based clustering algorithm, the resulting
 * {@link Cluster} objects will have no defined center, i.e. {@link Cluster#getCenter()} will
 * return {@code null}.
 *
 * @see <a href="http://en.wikipedia.org/wiki/DBSCAN">DBSCAN (wikipedia)</a>
 * @see <a href="http://www.dbs.ifi.lmu.de/Publikationen/Papers/KDD-96.final.frame.pdf">
 * A Density-Based Algorithm for Discovering Clusters in Large Spatial Databases with Noise</a>
 * @since 3.1
 */
public class DBSCANClusterer {

    /**
     * Maximum radius of the neighborhood to be considered.
     */
    private final double eps;

    /**
     * Minimum number of points needed for a cluster.
     */
    private final int minPts;

    /**
     * Status of a point during the clustering process.
     */
    private enum PointStatus {
        /**
         * The point has is considered to be noise.
         */
        NOISE,
        /**
         * The point is already part of a cluster.
         */
        PART_OF_CLUSTER
    }

    /**
     * Creates a new instance of a DBSCANClusterer.
     *
     * @param eps    maximum radius of the neighborhood to be considered
     * @param minPts minimum number of points needed for a cluster
     */
    public DBSCANClusterer(final double eps, final int minPts) {

        this.eps = eps;
        this.minPts = minPts;
    }

    /**
     * Returns the maximum radius of the neighborhood to be considered.
     *
     * @return maximum radius of the neighborhood
     */
    public double getEps() {
        return eps;
    }

    /**
     * Returns the minimum number of points needed for a cluster.
     *
     * @return minimum number of points needed for a cluster
     */
    public int getMinPts() {
        return minPts;
    }

    /**
     * Performs DBSCAN cluster analysis.
     * <p>
     * <b>Note:</b> as DBSCAN is not a centroid-based clustering algorithm, the resulting
     * {@link Cluster} objects will have no defined center, i.e. {@link Cluster#getCenter()} will
     * return {@code null}.
     *
     * @param points the points to cluster
     * @return the list of clusters
     */
    public ArrayList<Cluster<Point>> cluster(final Collection<Point> points) {


        final ArrayList<Cluster<Point>> clusters = new ArrayList<Cluster<Point>>();
        final Map<Point, PointStatus> visited = new HashMap<Point, PointStatus>();

        for (final Point point : points) {
            if (visited.get(point) != null) {
                continue;
            }
            final List<Point> neighbors = getNeighbors(point, points);
            if (neighbors.size() >= minPts) {
                // DBSCAN does not care about center points
                final Cluster<Point> cluster = new Cluster<Point>(null);
                clusters.add(expandCluster(cluster, point, neighbors, points, visited));
            } else {
                visited.put(point, PointStatus.NOISE);
            }
        }

        return clusters;
    }

    /**
     * Expands the cluster to include density-reachable items.
     *
     * @param cluster   Cluster to expand
     * @param point     Point to add to cluster
     * @param neighbors List of neighbors
     * @param points    the data set
     * @param visited   the set of already visited points
     * @return the expanded cluster
     */
    private Cluster<Point> expandCluster(final Cluster<Point> cluster,
                                         final Point point,
                                         final List<Point> neighbors,
                                         final Collection<Point> points,
                                         final Map<Point, PointStatus> visited) {
        cluster.addPoint(point);
        visited.put(point, PointStatus.PART_OF_CLUSTER);

        List<Point> seeds = new ArrayList<Point>(neighbors);
        int index = 0;
        while (index < seeds.size()) {
            final Point current = seeds.get(index);
            PointStatus pStatus = visited.get(current);
            // only check non-visited points
            if (pStatus == null) {
                final List<Point> currentNeighbors = getNeighbors(current, points);
                if (currentNeighbors.size() >= minPts) {
                    seeds = merge(seeds, currentNeighbors);
                }
            }

            if (pStatus != PointStatus.PART_OF_CLUSTER) {
                visited.put(current, PointStatus.PART_OF_CLUSTER);
                cluster.addPoint(current);
            }

            index++;
        }
        return cluster;
    }

    /**
     * Returns a list of density-reachable neighbors of a {@code point}.
     *
     * @param point  the point to look for
     * @param points possible neighbors
     * @return the List of neighbors
     */
    private List<Point> getNeighbors(final Point point, final Collection<Point> points) {
        final List<Point> neighbors = new ArrayList<Point>();
        for (final Point neighbor : points) {
            if (point != neighbor && getDistance(neighbor, point) <= eps){
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    private double getDistance(Point a, Point b) {
        double xDiff = a.x - b.x;
        double yDiff = a.y - b.y;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    /**
     * Merges two lists together.
     *
     * @param one first list
     * @param two second list
     * @return merged lists
     */
    private List<Point> merge(final List<Point> one, final List<Point> two) {
        final Set<Point> oneSet = new HashSet<Point>(one);
        for (Point item : two) {
            if (!oneSet.contains(item)) {
                one.add(item);
            }
        }
        return one;
    }
}

