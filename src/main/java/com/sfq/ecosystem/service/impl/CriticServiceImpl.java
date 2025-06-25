package com.sfq.ecosystem.service.impl;

import com.sfq.ecosystem.service.CriticService;

import java.util.Arrays;

public class CriticServiceImpl implements CriticService {

    @Override
    public double[] criticMethod(double[][] data) {
        // 检查输入数据是否有效
        if (data == null || data.length < 2 || data[0].length < 2) {
            System.out.println("错误：输入数据必须是一个至少包含2个样本和2个指标的二维数组。");
            return null;
        }

        int nSamples = data.length;
        int nCriteria = data[0].length;

        // 1. 变异性计算 (标准差)
        double[] stdDev = new double[nCriteria];
        for (int j = 0; j < nCriteria; j++) {
            double mean = 0;
            for (int i = 0; i < nSamples; i++) {
                mean += data[i][j];
            }
            mean /= nSamples;

            double variance = 0;
            for (int i = 0; i < nSamples; i++) {
                variance += Math.pow(data[i][j] - mean, 2);
            }
            variance /= nSamples;
            stdDev[j] = Math.sqrt(variance);
        }

        // 检查是否有标准差为零的列（常量列）
        boolean hasZeroStdDev = false;
        for (double std : stdDev) {
            if (std == 0) {
                hasZeroStdDev = true;
                break;
            }
        }
        if (hasZeroStdDev) {
            System.out.println("警告：数据中存在标准差为零的指标（常量列）。");
        }

        // 2. 冲突性计算 (基于相关系数)
        double[][] corrMatrix = new double[nCriteria][nCriteria];
        for (int j = 0; j < nCriteria; j++) {
            for (int k = 0; k < nCriteria; k++) {
                double numerator = 0;
                double denominator1 = 0;
                double denominator2 = 0;

                double meanJ = 0;
                double meanK = 0;
                for (int i = 0; i < nSamples; i++) {
                    meanJ += data[i][j];
                    meanK += data[i][k];
                }
                meanJ /= nSamples;
                meanK /= nSamples;

                for (int i = 0; i < nSamples; i++) {
                    numerator += (data[i][j] - meanJ) * (data[i][k] - meanK);
                    denominator1 += Math.pow(data[i][j] - meanJ, 2);
                    denominator2 += Math.pow(data[i][k] - meanK, 2);
                }

                if (denominator1 == 0 || denominator2 == 0) {
                    corrMatrix[j][k] = 0;
                } else {
                    corrMatrix[j][k] = numerator / Math.sqrt(denominator1 * denominator2);
                }
            }
        }

        // 计算每个指标与其他指标的冲突性
        double[] conflict = new double[nCriteria];
        for (int j = 0; j < nCriteria; j++) {
            for (int k = 0; k < nCriteria; k++) {
                conflict[j] += 1 - corrMatrix[j][k];
            }
        }

        // 3. 信息量计算 C_j = σ_j * Σ(1 - r_jk)
        double[] infoContent = new double[nCriteria];
        for (int j = 0; j < nCriteria; j++) {
            infoContent[j] = stdDev[j] * conflict[j];
        }

        // 4. 权重计算 (归一化信息量)
        double totalInfo = 0;
        for (double info : infoContent) {
            totalInfo += info;
        }

        if (totalInfo == 0) {
            System.out.println("警告：总信息量为零，无法计算权重。可能是所有指标标准差都为零。");
            double[] equalWeights = new double[nCriteria];
            Arrays.fill(equalWeights, 1.0 / nCriteria);
            return equalWeights;
        }

        double[] weights = new double[nCriteria];
        for (int j = 0; j < nCriteria; j++) {
            weights[j] = infoContent[j] / totalInfo;
        }

        return weights;
    }
}