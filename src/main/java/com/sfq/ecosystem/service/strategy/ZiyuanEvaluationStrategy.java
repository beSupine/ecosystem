package com.sfq.ecosystem.service.strategy;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * “资源”评价对象的具体策略实现
 */
public class ZiyuanEvaluationStrategy implements IEvaluationStrategy {

    @Override
    public double[][] getBoundaries() {
        return new double[][]{
                {0, 18, 36, 54, 72, 90}, {0, 10, 20, 30, 40, 50}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 1, 2, 3, 4, 5}, {0, 0.06, 0.12, 0.18, 0.24, 0.3},
                {0, 0.8, 1.6, 2.4, 3.2, 4}, {0, 0.46, 0.92, 1.38, 1.84, 2.3}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 1600, 3200, 4800, 6400, 8000}, {0, 400, 800, 1200, 1600, 2000}
        };
    }

    @Override
    public double[] getWeights() {
        return new double[]{
                0.1274, 0.1349, 0.0628, 0.0522, 0.0756,
                0.1584, 0.0798, 0.0675, 0.0628, 0.1069, 0.0718
        };
    }

    @Override
    public Map<String, double[]> getSamples() {
        Map<String, double[]> samples = new LinkedHashMap<>();
        samples.put("2024", new double[]{17, 6, 0.86, 0.88, 3.33, 0.05, 4, 2, 0.7, 1968.39, 1022});
        samples.put("2023", new double[]{21, 10, 0.85, 0.87, 3.085, 0.06, 4, 2, 0.69, 1927.1, 943});
        samples.put("2022", new double[]{86, 46, 0.83, 0.83, 2.61, 0.24, 4, 1.78, 0.79, 1891.35, 1190});
        samples.put("2021", new double[]{51, 35, 0.76, 0.72, 2.17, 0.14, 3, 1.57, 0.78, 1603.84, 1234});
        samples.put("2020", new double[]{11, 11, 0.71, 0.67, 2.055, 0.02, 2, 1.14, 0.68, 1422.68, 1343});
        return samples;
    }

    @Override
    public Map<String, int[]> getCriterionGroups() {
        Map<String, int[]> groups = new LinkedHashMap<>();
        groups.put("资源产出层", new int[]{0, 1, 2, 3, 4, 5});
        groups.put("资源要素层", new int[]{6, 7, 8, 9});
        groups.put("资源接口层", new int[]{10});
        return groups;
    }

    @Override
    public double getTheta() {
        return 0.6;
    }

    @Override
    public double[] preprocessTargetLayerData(double[] rawSampleData) {
        double[] processedData = rawSampleData.clone();
        processedData[6] = 4.0 - Math.abs(processedData[6] - 5.0);        // 处理指标6
        processedData[9] = 8000.0 - Math.abs(processedData[9] - 2000.0); // 处理指标9
        processedData[10] = 2000.0 - processedData[10];                   // 处理指标10
        return processedData;
    }

    @Override
    public double[] preprocessCriterionLayerData(double[] rawGroupData, int[] originalIndices) {
        double[] processedData = rawGroupData.clone();
        for (int i = 0; i < originalIndices.length; i++) {
            int originalIndex = originalIndices[i];
            if (originalIndex == 6) {
                processedData[i] = 4.0 - Math.abs(processedData[i] - 5.0);
            } else if (originalIndex == 9) {
                processedData[i] = 8000.0 - Math.abs(processedData[i] - 2000.0);
            } else if (originalIndex == 10) {
                processedData[i] = 2000.0 - processedData[i];
            }
        }
        return processedData;
    }
}