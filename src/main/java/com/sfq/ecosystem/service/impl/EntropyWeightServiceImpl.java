package com.sfq.ecosystem.service.impl;

import com.sfq.ecosystem.service.EntropyWeightService;

import java.util.Arrays;

public class EntropyWeightServiceImpl implements EntropyWeightService {

    @Override
    public double[] entropyWeightMethod(double[][] X) {
        // 0. 输入校验
        if (X == null) {
            throw new IllegalArgumentException("输入数据不能为 null。");
        }
        if (X.length < 1 || X[0].length < 1) {
            throw new IllegalArgumentException("输入数据必须是二维数组 (样本数 x 指标数)。");
        }
        for (double[] row : X) {
            for (double val : row) {
                if (val < 0) {
                    System.out.println("警告：输入数据包含负值，熵权法通常要求非负数据。结果可能无效。");
                    break;
                }
            }
        }

        int m = X.length; // 样本数
        int n = X[0].length; // 指标数

        if (m <= 1) {
            System.out.println("警告：样本数量小于等于1，无法计算熵或权重可能无意义。");
            double[] equalWeights = new double[n];
            Arrays.fill(equalWeights, 1.0 / n);
            return equalWeights;
        }

        // 1. 计算贡献度 (P_ij) - 归一化处理
        double[] colSum = new double[n];
        for (int j = 0; j < n; j++) {
            for (double[] row : X) {
                colSum[j] += row[j];
            }
        }

        double epsilon = 1e-12;
        double[][] P = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                P[i][j] = X[i][j] / (colSum[j] + epsilon);
            }
        }

        // 2. 计算信息熵 (E_j)
        double k = 1 / Math.log(m);
        double[] E = new double[n];
        for (int j = 0; j < n; j++) {
            double entropyTermSum = 0;
            for (int i = 0; i < m; i++) {
                if (P[i][j] > 0) {
                    entropyTermSum += P[i][j] * Math.log(P[i][j]);
                }
            }
            E[j] = -k * entropyTermSum;
            E[j] = Math.max(0.0, Math.min(1.0, E[j])); // 确保熵值在 [0, 1] 范围内
        }

        // 3. 计算差异性系数 (D_j)
        double[] D = new double[n];
        for (int j = 0; j < n; j++) {
            D[j] = 1 - E[j];
        }

        // 4. 计算归一化权重 (W_j)
        double sumD = 0;
        for (double val : D) {
            sumD += val;
        }

        if (sumD == 0) {
            System.out.println("警告：所有指标的差异性系数之和为0，无法计算权重。返回均等权重。");
            double[] equalWeights = new double[n];
            Arrays.fill(equalWeights, 1.0 / n);
            return equalWeights;
        } else {
            double[] W = new double[n];
            for (int j = 0; j < n; j++) {
                W[j] = D[j] / sumD;
            }
            double sumW = 0;
            for (double val : W) {
                sumW += val;
            }
            for (int j = 0; j < n; j++) {
                W[j] /= sumW;
            }
            return W;
        }
    }
}