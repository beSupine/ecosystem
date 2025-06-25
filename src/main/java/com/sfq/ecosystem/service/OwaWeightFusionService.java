package com.sfq.ecosystem.service;

/**
 * OWA权重融合服务接口
 * @author s
 * @date 2025-06-22
 */
public interface OwaWeightFusionService {

    /**
     * OWA权重融合主函数。
     * 该方法接收一个包含多种评价方法权重的二维数组，并使用OWA算子进行融合，
     * 最终得出一个综合的权重向量。
     *
     * @param data 各方法的权重矩阵，预期是一个 5xN 的二维数组 (5种方法, N个指标)。
     * @param a    生成函数下界参数，取值范围 [0, 1]，且 a <= b。
     * @param b    生成函数上界参数，取值范围 [0, 1]，且 a <= b。
     * @return 融合后的综合权重向量 (一维数组)。
     */
    double[] owaWeightFusion(double[][] data, double a, double b);

    /**
     * OWA权重融合主函数的重载方法，使用默认的 a 和 b 参数。
     * 默认 a=0.3, b=0.7。
     *
     * @param data 各方法的权重矩阵，预期是一个 5xN 的二维数组 (5种方法, N个指标)。
     * @return 融合后的综合权重向量 (一维数组)。
     */
    double[] owaWeightFusion(double[][] data);
}