package com.sfq.ecosystem.service.strategy;

import java.util.Map;

/**
 * 评估策略接口
 * 定义了获取特定评估对象所需的所有配置和数据的方法。
 * 每个实现类代表一种具体的评估方案（如协同、资源等）。
 */
public interface IEvaluationStrategy {

    /** 获取指标边界数据 */
    double[][] getBoundaries();

    /** 获取指标权重 */
    double[] getWeights();

    /** 获取样本数据集 */
    Map<String, double[]> getSamples();

    /** 获取准则层分组定义 */
    Map<String, int[]> getCriterionGroups();

    /** 获取判断阈值 theta */
    double getTheta();

    /**
     * 对整个样本数据进行预处理（用于目标层评估）
     * @param rawSampleData 原始样本数据
     * @return 预处理后的样本数据
     */
    double[] preprocessTargetLayerData(double[] rawSampleData);

    /**
     * 对分组后的样本数据进行预处理（用于准则层评估）
     * @param rawGroupData 某一组的原始数据
     * @param originalIndices 该组数据在完整指标体系中的原始索引
     * @return 预处理后的分组数据
     */
    double[] preprocessCriterionLayerData(double[] rawGroupData, int[] originalIndices);
}