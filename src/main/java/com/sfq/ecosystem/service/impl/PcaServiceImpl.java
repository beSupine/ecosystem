package com.sfq.ecosystem.service.impl;

import com.sfq.ecosystem.service.PcaService;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.DoubleStream;

@Service
public class PcaServiceImpl implements PcaService {

    // 累计贡献率阈值，用于确定主成分个数
    private static final double CUMULATIVE_CONTRIBUTION_THRESHOLD = 0.85;

    /**
     * 执行PCA赋权的主方法，功能上对应于Python脚本中的 principal_component_weighting 函数。
     *
     * @param data 输入的数据矩阵 (样本数 x 指标数).
     * @return 每个指标的最终权重。
     */
    @Override
    public double[] principalComponentWeighting(double[][] data) {
        // Python脚本的最终版本中没有对数据进行标准化，所以我们直接进入计算相关系数矩阵的步骤。
        RealMatrix dataMatrix = MatrixUtils.createRealMatrix(data);

        // 第二步: 计算相关系数矩阵。
        // PearsonsCorrelation 类计算的是输入矩阵各列之间的相关性。
        RealMatrix correlationMatrix = new PearsonsCorrelation().computeCorrelationMatrix(dataMatrix);

        // 第三步和第四步: 计算主成分及其贡献率。
        // 对相关系数矩阵进行特征值分解。
        EigenDecomposition ed = new EigenDecomposition(correlationMatrix);
        double[] eigenvalues = ed.getRealEigenvalues();

        // 创建一个包含特征值和对应特征向量的配对列表，以便能将它们一起排序。
        List<EigenPair> eigenPairs = new ArrayList<>();
        for (int i = 0; i < eigenvalues.length; i++) {
            eigenPairs.add(new EigenPair(eigenvalues[i], ed.getEigenvector(i)));
        }

        // 根据特征值从大到小对配对列表进行排序。
        eigenPairs.sort(Comparator.comparingDouble(EigenPair::getValue).reversed());

        // 重新计算排序后的特征值和贡献率。
        double[] sortedEigenvalues = eigenPairs.stream().mapToDouble(EigenPair::getValue).toArray();
        double totalEigenvalueSum = DoubleStream.of(sortedEigenvalues).sum();

        double[] contributionRates = new double[sortedEigenvalues.length];
        for (int i = 0; i < sortedEigenvalues.length; i++) {
            contributionRates[i] = sortedEigenvalues[i] / totalEigenvalueSum;
        }

        // 计算累计贡献率。
        double[] cumulativeRates = new double[contributionRates.length];
        cumulativeRates[0] = contributionRates[0];
        for (int i = 1; i < contributionRates.length; i++) {
            cumulativeRates[i] = cumulativeRates[i - 1] + contributionRates[i];
        }

        // 确定主成分的个数 'p'。
        int p = 0;
        for (int i = 0; i < cumulativeRates.length; i++) {
            if (cumulativeRates[i] >= CUMULATIVE_CONTRIBUTION_THRESHOLD) {
                p = i + 1;
                break;
            }
        }
        if (p == 0) { // 如果所有累计贡献率都小于阈值，则选取所有成分
            p = cumulativeRates.length;
        }

        // 第五步: 计算最终权重。
        int indicatorCount = dataMatrix.getColumnDimension(); // 指标数量
        double[] omega = new double[indicatorCount];

        for (int j = 0; j < indicatorCount; j++) {
            double componentSum = 0;
            for (int k = 0; k < p; k++) {
                // 获取第k个主成分的特征向量
                RealVector eigenvector = eigenPairs.get(k).getVector();
                // 使用特征向量元素的绝对值进行计算
                componentSum += contributionRates[k] * Math.abs(eigenvector.getEntry(j));
            }
            omega[j] = componentSum;
        }

        // 对权重进行归一化处理。
        double omegaSum = DoubleStream.of(omega).sum();
        double[] finalWeights = new double[indicatorCount];
        for (int i = 0; i < indicatorCount; i++) {
            finalWeights[i] = omega[i] / omegaSum;
        }

        return finalWeights;
    }

    /**
     * 用于存储特征值和特征向量对的辅助内部类。
     */
    private static class EigenPair {
        private final double value;      // 特征值
        private final RealVector vector; // 特征向量

        public EigenPair(double value, RealVector vector) {
            this.value = value;
            this.vector = vector;
        }

        public double getValue() {
            return value;
        }

        public RealVector getVector() {
            return vector;
        }
    }
}