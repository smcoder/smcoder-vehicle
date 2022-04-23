package org.smcoder.vehicle.algorithms;

import org.smcoder.vehicle.algorithms.basic.MathTools;
import org.smcoder.vehicle.algorithms.basic.Path;
import org.smcoder.vehicle.algorithms.basic.Point;

import java.util.ArrayList;
import java.util.List;

public class ClusteringAlgorithms {
    public Path clusterExtraPoints(Path path, double distanceScale) {
        List<Point> clusteringPoints = new ArrayList<>();
        int startIndex = 0;
        int endIndex;
        boolean clusterFlag = false;
        List<Point> points = path.getPoints();
        for (int index = 0; index < points.size(); index++) {
            endIndex = index;
            for (int i = startIndex; i < endIndex; i++) {
                if (MathTools.calculateDoublePointsDistance(points.get(i), points.get(endIndex)) > distanceScale) {
                    clusterFlag = true;
                }
            }
            if (clusterFlag) {
                clusteringPoints.add(clusterPoints(startIndex, endIndex - 1, points));
                clusterFlag = false;
                startIndex = endIndex;
                if (endIndex == points.size() - 1) {
                    clusteringPoints.add(points.get(endIndex));
                }
            } else if (endIndex == points.size() - 1) {
                clusteringPoints.add(clusterPoints(startIndex, endIndex, points));
            }
        }
        Path result = new Path();
        result.addPoints(clusteringPoints);
        return result;
    }

    private Point clusterPoints(int startIndex, int endIndex, List<Point> points) {
        double sumX = 0;
        double sumY = 0;
        for (int i = startIndex; i <= endIndex; i++) {
            sumX += points.get(i).getX();
            sumY += points.get(i).getY();
        }
        return new Point(sumX / (endIndex - startIndex), sumY / (endIndex - startIndex));
    }
}
