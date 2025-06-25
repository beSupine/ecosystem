package com.sfq.ecosystem.service;

/**
 * 主成分分析（PCA）赋权服务接口
 */
public interface PcaService {

    /**
     * 使用主成分分析法计算各指标的权重。
     *
     * @param data 一个二维数组，其中行代表样本，列代表指标。
     * @return 一个 double 数组，代表计算出的每个指标的权重。
     */
    double[] principalComponentWeighting(double[][] data);
}