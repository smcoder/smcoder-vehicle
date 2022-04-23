package org.smcoder.vehicle.algorithms.basic;

public class MathTools {
    public static double calculateDoublePointsDistance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }

    public static double calculateVariance(Double[] datas) {
        double sum = 0;
        double average = calculateSum(datas);
        for (double data : datas) {
            sum += Math.pow(data - average, 2);
        }
        return sum;
    }

    public static double calculateSum(Double[] datas) {
        double sum = 0;
        for (double data : datas) {
            sum += data;
        }
        return sum;
    }

    public static double calculateSum(Integer[] datas) {
        double sum = 0;
        for (double data : datas) {
            sum += data;
        }
        return sum;
    }

    public static double calculateAverage(Double[] datas) {
        return calculateSum(datas) / datas.length;
    }
}
