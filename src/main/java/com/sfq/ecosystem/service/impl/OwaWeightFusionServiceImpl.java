package com.sfq.ecosystem.service.impl;

import com.sfq.ecosystem.service.OwaWeightFusionService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;

/**
 * OWA权重融合服务实现类
 * @author
 * @date 2025-06-22
 */
@Service // SpringBoot注解，将该类标记为服务层组件
public class OwaWeightFusionServiceImpl implements OwaWeightFusionService {

    private static final int METHOD_COUNT = 5; // Python代码中固定的方法数量 N

    @Override
    public double[] owaWeightFusion(double[][] data) {
        // 调用主方法，并传入默认参数 a=0.3, b=0.7
        return this.owaWeightFusion(data, 0.3, 0.7);
    }

    @Override
    public double[] owaWeightFusion(double[][] data, double a, double b) {
        // --- 1. 参数校验 ---
        // 校验参数 a 和 b 的范围
        if (a < 0 || b > 1 || a > b) {
            throw new IllegalArgumentException("参数a,b需满足 0 <= a <= b <= 1");
        }
        // 校验输入数据是否包含5种方法的权重
        if (data == null || data.length != METHOD_COUNT) {
            throw new IllegalArgumentException("输入数据需要包含 " + METHOD_COUNT + " 种方法的权重");
        }
        if (data[0] == null || data[0].length == 0) {
            throw new IllegalArgumentException("输入数据中的指标权重不能为空");
        }


        // --- 2. 生成位置权重 ---
        double[] positionWeights = generateOwaPositionWeights(a, b);
        System.out.println("生成的位置权重向量 w: " + Arrays.toString(positionWeights));


        // --- 3. 按指标遍历计算 ---
        // 获取指标的数量 n
        int n = data[0].length;
        // 初始化存储融合后但未归一化的权重向量
        double[] omegaPrime = new double[n];

        for (int j = 0; j < n; j++) {
            // 提取当前第 j 个指标下的五种权重值
            Double[] currentIndicatorWeights = new Double[METHOD_COUNT];
            for (int i = 0; i < METHOD_COUNT; i++) {
                currentIndicatorWeights[i] = data[i][j];
            }

            // 对当前指标的权重值进行降序排序
            Arrays.sort(currentIndicatorWeights, Collections.reverseOrder());

            // OWA加权求和：将排序后的权重与位置权重进行点积运算
            double weightedSum = 0;
            for (int k = 0; k < METHOD_COUNT; k++) {
                weightedSum += positionWeights[k] * currentIndicatorWeights[k];
            }
            omegaPrime[j] = weightedSum;
        }


        // --- 4. 归一化处理 ---
        // 计算 omegaPrime 向量所有元素的总和
        double sumOfOmegaPrime = Arrays.stream(omegaPrime).sum();

        // 如果总和为0，则无法归一化，返回原数组或抛出异常
        if (sumOfOmegaPrime == 0) {
            System.err.println("警告: 融合后的权重向量总和为0，无法进行归一化。");
            return omegaPrime; // 或者返回一个等权重的数组等其他处理方式
        }

        // 进行归一化
        double[] omega = new double[n];
        for (int i = 0; i < n; i++) {
            omega[i] = omegaPrime[i] / sumOfOmegaPrime;
        }

        return omega;
    }

    /**
     * 生成OWA位置权重向量。
     * 这是Python脚本中 generate_owa_position_weights 函数的Java实现。
     *
     * @param a 生成函数下界参数
     * @param b 生成函数上界参数
     * @return 位置权重向量 w
     */
    private double[] generateOwaPositionWeights(double a, double b) {
        double[] w = new double[METHOD_COUNT];
        for (int k = 1; k <= METHOD_COUNT; k++) {
            double s_k = (double) k / METHOD_COUNT;
            double s_km1 = (double) (k - 1) / METHOD_COUNT;

            // 计算 S(s_k)
            double s_k_value;
            if (s_k < a) {
                s_k_value = 0.0;
            } else if (s_k > b) {
                s_k_value = 1.0;
            } else {
                // 处理 a == b 的情况，防止除以零
                s_k_value = (b - a == 0) ? 1.0 : (s_k - a) / (b - a);
            }

            // 计算 S(s_km1)
            double s_km1_value;
            if (s_km1 < a) {
                s_km1_value = 0.0;
            } else if (s_km1 > b) {
                s_km1_value = 1.0;
            } else {
                // 处理 a == b 的情况，防止除以零
                s_km1_value = (b - a == 0) ? 1.0 : (s_km1 - a) / (b - a);
            }

            // 计算最终权重
            w[k - 1] = s_k_value - s_km1_value;
        }
        return w;
    }
}