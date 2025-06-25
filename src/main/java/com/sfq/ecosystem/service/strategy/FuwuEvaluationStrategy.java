package com.sfq.ecosystem.service.strategy;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * “服务”评价对象的具体策略实现
 */
public class FuwuEvaluationStrategy implements IEvaluationStrategy {
    @Override
    public double[][] getBoundaries() {
        // 服务相关的12个指标的边界数据
        return new double[][]{
                {0, 2, 4, 6, 8, 10}, {0, 1, 2, 3, 4, 5}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 73.2, 146.4, 219.6, 292.8, 366}, {0, 1, 2, 3, 4, 5}, {0, 0.06, 0.12, 0.18, 0.24, 0.3},
                {0, 0.4, 0.8, 1.2, 1.6, 2}, {0, 1.6, 3.2, 4.8, 6.4, 8}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 14, 28, 42, 56, 70}
        };
    }

    @Override
    public double[] getWeights() {
        // 服务相关的12个指标的权重
        return new double[]{
                0.0858, 0.0991, 0.0676, 0.1042, 0.0868, 0.0698,
                0.1244, 0.0808, 0.0913, 0.0433, 0.0573, 0.0897
        };
    }

    @Override
    public Map<String, double[]> getSamples() {
        // 服务相关的样本数据
        Map<String, double[]> samples = new LinkedHashMap<>();
        samples.put("2024", new double[]{7, 2, 0.85, 365, 3.27, 0.27, 1.55, 2.05, 0.95, 0.97, 0.99, 57.98});
        samples.put("2023", new double[]{6, 4, 0.81, 251, 3.11, 0.21, 1.42, 2.43, 0.94, 0.98, 0.98, 56.79});
        samples.put("2022", new double[]{5, 3, 0.78, 182, 2.67, 0.22, 1.43, 3.22, 0.92, 0.95, 0.96, 35.34});
        samples.put("2021", new double[]{5, 3, 0.52, 233, 2.1, 0.26, 1.32, 3.31, 0.89, 0.95, 0.95, 46.12});
        samples.put("2020", new double[]{4, 2, 0.56, 121, 2.16, 0.27, 1.12, 5.12, 0.91, 0.93, 0.95, 23.43});
        return samples;
    }

    @Override
    public Map<String, int[]> getCriterionGroups() {
        // 服务相关的准则层分组
        Map<String, int[]> groups = new LinkedHashMap<>();
        groups.put("服务产出层", new int[]{0, 1, 2, 3, 4, 5});
        groups.put("服务能力层", new int[]{6});
        groups.put("服务安全层", new int[]{7, 8, 9});
        groups.put("服务交互层", new int[]{10, 11});
        return groups;
    }

    @Override
    public double getTheta() {
        // 服务相关的阈值
        return 0.55;
    }

    /**
     * 根据 'fuwu_feima准则层.py' 的逻辑进行预处理
     */
    private double[] preprocess(double[] data, int[] originalIndices) {
        double[] processedData = data.clone();
        for (int i = 0; i < originalIndices.length; i++) {
            int originalIndex = originalIndices[i];
            // 指标6 (构件层级深度) 是居中型指标，最佳值为2
            if (originalIndex == 6) {
                processedData[i] = 2.0 - Math.abs(processedData[i] - 2.0);
            }
            // 指标7 (风险响应时间) 是成本型指标，值越小越好，进行逆向化处理
            if (originalIndex == 7) {
                processedData[i] = 8.0 - processedData[i];
            }
        }
        return processedData;
    }

    @Override
    public double[] preprocessTargetLayerData(double[] rawSampleData) {
        int[] allIndices = new int[rawSampleData.length];
        for(int i = 0; i < allIndices.length; i++) allIndices[i] = i;
        return preprocess(rawSampleData, allIndices);
    }

    @Override
    public double[] preprocessCriterionLayerData(double[] rawGroupData, int[] originalIndices) {
        return preprocess(rawGroupData, originalIndices);
    }
}