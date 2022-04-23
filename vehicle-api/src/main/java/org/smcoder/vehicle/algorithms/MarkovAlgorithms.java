package org.smcoder.vehicle.algorithms;

import org.smcoder.vehicle.algorithms.basic.MathTools;
import org.smcoder.vehicle.algorithms.basic.Matrix;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MarkovAlgorithms {
    private List<Integer> exampleStatusData;
    private int statusCounts;
    private double[][][] probablitiesMatrix;
    private double[] selfCorrelationCoefficient;
    private double[] stageWeights;
    private int[][] frequencyMatrix;
    private int markovStage;
    private double[][] predictValueMatrix;
    private boolean hasMarkovFeature;

    public MarkovAlgorithms(List<Integer> exampleStatusData, int statusCounts, int markovStage) {
        this.markovStage = markovStage;
        this.statusCounts = statusCounts;
        this.exampleStatusData = exampleStatusData;
        frequencyMatrix = new int[statusCounts][statusCounts];
        probablitiesMatrix = new double[this.markovStage][statusCounts][statusCounts];
        calculateTransformationProbablitiesMatrix(this.exampleStatusData, statusCounts);
        Matrix matrix = new Matrix();
        if (validateMarkov()) {
            for (int i = 1; i < this.markovStage; i++) {
                matrix.setArray(probablitiesMatrix[i - 1]);
                probablitiesMatrix[i] = matrix.multiply(probablitiesMatrix[0]);
            }
            calculateCorrelationCoefficient();
            calculateStageWeight();
        }
    }

    public double[][] predictProbablities(List<Integer> data) {
        List<Integer> dataTest = data;
        if (!hasMarkovFeature || dataTest.size() <= markovStage) {
            return null;
        }
        int[] basicData = new int[markovStage];
        predictValueMatrix = new double[dataTest.size() - markovStage][statusCounts];
        int index = 0;
        int predictFrom = 0;
        while (dataTest.size() > predictFrom + markovStage) {
            for (int i = 0; i < markovStage; i++) {
                basicData[i] = dataTest.get(i + predictFrom);
            }
            for (int i = 0; i < statusCounts; i++) {
                for (int j = 0; j < markovStage; j++) {
                    int state = basicData[basicData.length - 1 - j];
                    if (state >= 0) {
                        predictValueMatrix[index][i] += stageWeights[j] * probablitiesMatrix[j][state][i];
                    }
                }
            }
            predictFrom ++;
            index++;
        }
        return predictValueMatrix;
    }

    private boolean validateMarkov() {
        int totalFij = exampleStatusData.size() - 1;
        double[] cp = new double[statusCounts];
        for (int i = 0; i < statusCounts; i++) {
            for (int j = 0; j < statusCounts; j++) {
                cp[i] += (double) frequencyMatrix[j][i] / totalFij;
            }
        }
        double gm = 0;
        for (int i = 0; i < statusCounts; i++) {
            for (int j = 0; j < statusCounts; j++) {
                if (frequencyMatrix[i][j] > 0 && cp[j] > 0) {
                    gm += 2 * frequencyMatrix[i][j] * Math.abs(Math.log(probablitiesMatrix[0][i][j] / cp[j]));
                }
            }
        }
        double[] table = new double[]{3.84145882069413, 9.48772903678115,
                16.9189776046204, 26.2962276048642, 37.6524841334828,
                50.9984601657106, 66.3386488629688, 83.6752607427210,
                103.009508712226, 124.342113404004, 147.673529763818,
                173.004059094245, 200.333908832898, 229.663226447109,
                260.992119636005, 294.320668884306, 329.648935544535,
                366.976967201223, 406.304801326655, 447.632467830808,
                490.959990876927, 536.287390198110, 583.614682067880,
                632.941880026341, 684.268995430845, 737.596037878713,
                792.923015535393, 850.249935391850, 909.576803468370,
                970.903624977351, 1034.23040445441, 1099.55714586474,
                1166.88385269006, 1236.21052800010, 1307.53717451179,
                1380.86379463852, 1456.19039053135, 1533.51696411377,
                1612.84351711092, 1694.17005107462,};
        Set<Integer> set = new HashSet<Integer>();
        for (Integer value : exampleStatusData) {
            set.add(value);
        }
        hasMarkovFeature = (gm >= table[set.size() - 1]);
        return hasMarkovFeature;
    }

    /**
     * 计算相关系数
     */
    private void calculateCorrelationCoefficient() {
        Integer[] exampleDataArray = exampleStatusData.toArray(new Integer[exampleStatusData.size()]);
        double average = MathTools.calculateSum(exampleDataArray) / (double) exampleStatusData.size();
        double variance = MathTools.calculateSum(exampleDataArray);
        selfCorrelationCoefficient = new double[markovStage];
        for (int stage = 0; stage < markovStage; stage++) {
            double sum = 0;
            for (int l = 0; l < exampleStatusData.size() - markovStage; l++) {
                sum += (exampleStatusData.get(l) - average) * (exampleStatusData.get(l + stage) - average);
            }
            selfCorrelationCoefficient[stage] = Math.abs(sum / variance);
        }
    }

    /**
     * 计算不同阶的权重
     */
    private void calculateStageWeight() {
        stageWeights = new double[markovStage];
        double sum = 0;
        for (int stage = 0; stage < markovStage; stage++) {
            sum += selfCorrelationCoefficient[stage];
        }
        for (int stage = 0; stage < markovStage; stage++) {
            stageWeights[stage] = selfCorrelationCoefficient[stage] / sum;
        }
    }

    private void calculateTransformationProbablitiesMatrix(List<Integer> datas, int statusCount) {
        if (datas.size() < 2)
            return;
        int[] number = new int[statusCount];
        for (int i = 0; i < datas.size() - 1; i++) {
            frequencyMatrix[datas.get(i)][datas.get(i + 1)]++;
            number[datas.get(i)]++;
        }

        for (int i = 0; i < statusCount; i++) {
            for (int j = 0; j < statusCount; j++) {
                if (frequencyMatrix[i][j] > 0) {
                    probablitiesMatrix[0][i][j] = (double) frequencyMatrix[i][j] / number[i];
                }
            }
        }
    }
}
