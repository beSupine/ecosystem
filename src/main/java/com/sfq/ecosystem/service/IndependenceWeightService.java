package com.sfq.ecosystem.service;

import java.util.List;

/**
 * 指标独立性权重计算服务接口
 */
public interface IndependenceWeightService {

    /**
     * 计算一组指标的独立性权重。
     * <p>
     * 该方法基于多元线性回归，计算每个指标相对于其他指标的复相关系数 Rj。
     * 权重与 1/Rj 成正比，表示越独立的指标（Rj 越小）权重越大。
     *
     * @param data 输入数据数组，假定行代表样本，列代表指标。数据应已完成标准化和一致化处理。
     * @return 包含每个指标权重的数组，总和为 1。
     */
    double[] independenceWeight(double[][] data);

    /**
     * 根据分块定义，计算所有指标的最终权重。
     *
     * @param standardizedData 标准化后的完整数据矩阵。
     * @param splitCols        定义每个分块列数的列表，例如 [4, 2, 4, 3, 1]。
     * @return 归一化后的最终权重数组，其长度等于总列数。
     */
    double[] calculateFinalWeights(double[][] standardizedData, List<Integer> splitCols);
}