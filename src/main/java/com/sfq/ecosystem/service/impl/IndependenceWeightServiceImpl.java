package com.sfq.ecosystem.service.impl;

import com.sfq.ecosystem.service.IndependenceWeightService;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class IndependenceWeightServiceImpl implements IndependenceWeightService {

    private static final Logger logger = LoggerFactory.getLogger(IndependenceWeightServiceImpl.class);
    private static final double EPSILON = 1e-10;

    @Override
    public double[] independenceWeight(double[][] data) {
        if (data == null || data.length == 0 || data[0].length == 0) {
            throw new IllegalArgumentException("输入数据不能为空。");
        }

        int nSamples = data.length;
        int nIndicators = data[0].length;

        if (nIndicators < 2) {
            return new double[]{1.0};
        }

        if (nSamples <= nIndicators) {
            logger.warn("样本数量 ({}) 小于或等于指标数量 ({}). " +
                    "复相关系数可能被人为抬高，影响权重可靠性。", nSamples, nIndicators);
        }

        List<Double> invRValues = new ArrayList<>();

        for (int j = 0; j < nIndicators; j++) {
            double[] y = getColumn(data, j);
            double[][] X = removeColumn(data, j);

            OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
            double r2;

            try {
                regression.newSampleData(y, X);
                r2 = regression.calculateRSquared();
                r2 = Math.max(0, r2); // 确保 R^2 不为负
            } catch (Exception e) {
                logger.warn("指标 {} 的线性回归计算失败 ({}). 将 R^2 设为 1 (最低独立性)。", j, e.getMessage());
                r2 = 1.0;
            }

            double rj = Math.sqrt(r2);
            double invRj = 1.0 / (rj + EPSILON);
            invRValues.add(invRj);
        }

        double[] invRArray = invRValues.stream().mapToDouble(Double::doubleValue).toArray();
        double totalInvR = Arrays.stream(invRArray).sum();
        double[] weights;

        if (totalInvR <= EPSILON * nIndicators) {
            logger.warn("所有指标似乎都高度相关 (Rj 接近 1). 将分配相等的权重。");
            weights = new double[nIndicators];
            Arrays.fill(weights, 1.0 / nIndicators);
        } else if (Double.isInfinite(totalInvR) || Double.isNaN(totalInvR)) {
            logger.warn("计算出的权重总和为无穷大或NaN，可能由于某个指标完全独立(Rj=0)。将为 Rj=0 的指标分配权重，其他为0。");
            weights = new double[nIndicators];
            long zeroRIndicesCount = invRValues.stream().filter(d -> Double.isInfinite(d)).count();
            if (zeroRIndicesCount > 0) {
                double weightVal = 1.0 / zeroRIndicesCount;
                for (int i = 0; i < nIndicators; i++) {
                    if (Double.isInfinite(invRValues.get(i))) {
                        weights[i] = weightVal;
                    } else {
                        weights[i] = 0;
                    }
                }
            } else { // 如果是 NaN 但没有 inf，则均分权重作为后备
                Arrays.fill(weights, 1.0 / nIndicators);
            }
        } else {
            weights = Arrays.stream(invRArray).map(d -> d / totalInvR).toArray();
        }

        // 最终归一化以处理浮点精度问题
        double finalSum = Arrays.stream(weights).sum();
        return Arrays.stream(weights).map(w -> w / finalSum).toArray();
    }

    @Override
    public double[] calculateFinalWeights(double[][] standardizedData, List<Integer> splitCols) {
        int totalCols = splitCols.stream().mapToInt(Integer::intValue).sum();
        if (totalCols != standardizedData[0].length) {
            throw new IllegalArgumentException("分块列数之和与数据总列数不匹配。");
        }

        List<Double> combinedWeights = new ArrayList<>();
        RealMatrix dataMatrix = new Array2DRowRealMatrix(standardizedData);
        int currentCol = 0;

        for (int cols : splitCols) {
            double[] weights;
            if (cols == 1) {
                weights = new double[]{1.0};
            } else {
                double[][] part = dataMatrix.getSubMatrix(0, dataMatrix.getRowDimension() - 1, currentCol, currentCol + cols - 1).getData();
                weights = independenceWeight(part);
            }

            for (double weight : weights) {
                combinedWeights.add(weight * cols / totalCols);
            }
            currentCol += cols;
        }

        return combinedWeights.stream().mapToDouble(Double::doubleValue).toArray();
    }

    // 辅助方法：获取矩阵的某一列
    private double[] getColumn(double[][] matrix, int columnIndex) {
        return Arrays.stream(matrix).mapToDouble(row -> row[columnIndex]).toArray();
    }

    // 辅助方法：删除矩阵的某一列
    private double[][] removeColumn(double[][] matrix, int columnIndex) {
        int nRows = matrix.length;
        int nCols = matrix[0].length;
        double[][] result = new double[nRows][nCols - 1];
        for (int i = 0; i < nRows; i++) {
            int newColIndex = 0;
            for (int j = 0; j < nCols; j++) {
                if (j != columnIndex) {
                    result[i][newColIndex++] = matrix[i][j];
                }
            }
        }
        return result;
    }
}