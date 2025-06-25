package com.sfq.ecosystem.service.strategy;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * “协同”评价对象的具体策略实现
 */
public class XietongEvaluationStrategy implements IEvaluationStrategy {

    @Override
    public double[][] getBoundaries() {
        return new double[][]{
                {0, 4, 8, 12, 16, 20}, {0, 1.6, 3.2, 4.8, 6.4, 8}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 0.4, 0.8, 1.2, 1.6, 2}, {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 1, 2, 3, 4, 5},
                {0, 0.04, 0.08, 0.12, 0.16, 0.2}, {0, 0.46, 0.92, 1.38, 1.84, 2.3}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 0.4, 0.8, 1.2, 1.6, 2}, {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 1.6, 3.2, 4.8, 6.4, 8},
                {0, 0.4, 0.8, 1.2, 1.6, 2}, {0, 0.2, 0.4, 0.6, 0.8, 1}
        };
    }

    @Override
    public double[] getWeights() {
        return new double[]{
                0.0589, 0.0683, 0.0697, 0.0636, 0.0587, 0.0732,
                0.0947, 0.0664, 0.0663, 0.1286, 0.0629, 0.0652,
                0.0796, 0.0441
        };
    }

    @Override
    public Map<String, double[]> getSamples() {
        Map<String, double[]> samples = new LinkedHashMap<>();
        samples.put("2024", new double[]{12, 6.05, 0.97, 1.22, 0.88, 3.07, 0.13, 1.98, 0.70, 0.21, 0.91, 4.34, 1.19, 0.95});
        samples.put("2023", new double[]{13, 4.64, 0.95, 1.31, 0.85, 2.42, 0.07, 1.83, 0.69, 0.28, 0.88, 3.96, 1.12, 0.85});
        samples.put("2022", new double[]{9, 5.51, 0.93, 0.85, 0.72, 1.61, 0.11, 1.72, 0.79, 0.33, 0.85, 3.47, 0.65, 0.90});
        samples.put("2021", new double[]{14, 4.83, 0.89, 1.15, 0.80, 1.94, 0.08, 1.26, 0.78, 0.45, 0.70, 2.17, 0.72, 0.83});
        samples.put("2020", new double[]{8, 4.21, 0.92, 0.95, 0.75, 1.32, 0.05, 1.03, 0.68, 0.33, 0.82, 3.14, 0.81, 0.88});
        return samples;
    }

    @Override
    public Map<String, int[]> getCriterionGroups() {
        Map<String, int[]> groups = new LinkedHashMap<>();
        groups.put("协同产出层", new int[]{0, 1, 2, 3, 4, 5});
        groups.put("协同主体层", new int[]{6, 7, 8, 9});
        groups.put("协同关系层", new int[]{10, 11, 12});
        groups.put("协同数据层", new int[]{13});
        return groups;
    }

    @Override
    public double getTheta() {
        return 0.8;
    }

    private double processCompetition(double x) {
        return 2.0 - Math.abs(x - 0.15);
    }

    @Override
    public double[] preprocessTargetLayerData(double[] rawSampleData) {
        double[] processedData = rawSampleData.clone();
        processedData[9] = processCompetition(processedData[9]); // 处理指标9
        return processedData;
    }

    @Override
    public double[] preprocessCriterionLayerData(double[] rawGroupData, int[] originalIndices) {
        double[] processedData = rawGroupData.clone();
        for (int i = 0; i < originalIndices.length; i++) {
            if (originalIndices[i] == 9) {
                processedData[i] = processCompetition(processedData[i]);
            }
        }
        return processedData;
    }
}