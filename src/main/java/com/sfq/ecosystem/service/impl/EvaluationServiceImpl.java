package com.sfq.ecosystem.service.impl;

import com.sfq.ecosystem.model.EvaluationType;
import com.sfq.ecosystem.model.FermateanFuzzyNumber;
import com.sfq.ecosystem.service.IEvaluationService;
import com.sfq.ecosystem.service.strategy.EvaluationStrategyFactory;
import com.sfq.ecosystem.service.strategy.IEvaluationStrategy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 评估服务实现类 (已重构)
 * <p>
 * 通过策略模式实现了评估对象的可切换性。
 * 核心计算逻辑保持不变，但所有配置（数据、预处理）都从对应的策略类中动态获取。
 */
@Service
public class EvaluationServiceImpl implements IEvaluationService {

    private static final double EPSILON = 1e-9;
    private static final String[] LEVEL_NAMES = {"差", "较差", "中等", "良好", "优秀"};

    /**
     * 根据传入的原始数据数组评估目标层
     * @param rawData 原始指标数据数组
     * @param evaluationType 评估类型
     * @return 评估等级字符串
     */
    @Override
    public String evaluateTargetLayer(double[] rawData, EvaluationType evaluationType) {
        IEvaluationStrategy strategy = EvaluationStrategyFactory.getStrategy(evaluationType);
        double[][] boundariesCrisp = strategy.getBoundaries();
        double[] weights = strategy.getWeights();
        double thetaCrisp = strategy.getTheta();
        double[] processedSampleData = strategy.preprocessTargetLayerData(rawData);

        // ... 后续核心计算逻辑与您提供的版本一致 ...
        int numIndicators = boundariesCrisp.length;
        double[] minVals = new double[numIndicators];
        double[] maxVals = new double[numIndicators];
        for (int i = 0; i < numIndicators; i++) {
            minVals[i] = boundariesCrisp[i][0];
            maxVals[i] = boundariesCrisp[i][boundariesCrisp[i].length - 1];
        }

        List<List<FermateanFuzzyNumber>> boundariesFfn = convertToFfnBoundaries(boundariesCrisp, minVals, maxVals);
        List<FermateanFuzzyNumber> sampleFfn = convertToFfnSample(processedSampleData, minVals, maxVals);
        List<FermateanFuzzyNumber> qFfnList = new ArrayList<>();
        List<FermateanFuzzyNumber> pFfnList = new ArrayList<>();
        List<FermateanFuzzyNumber> vFfnList = new ArrayList<>();
        calculateQPVLists(numIndicators, minVals, maxVals, qFfnList, pFfnList, vFfnList);

        List<FermateanFuzzyNumber> credibilities = new ArrayList<>();
        for (int h = 0; h < 5; h++) {
            credibilities.add(calculateCredibilityFfn(sampleFfn, h, boundariesFfn, qFfnList, pFfnList, vFfnList, weights));
        }

        FermateanFuzzyNumber thetaFfn = new FermateanFuzzyNumber(new double[]{thetaCrisp, thetaCrisp}, new double[]{1.0 - thetaCrisp, 1.0 - thetaCrisp});
        String finalLevel = LEVEL_NAMES[0];
        for (int h = 4; h >= 0; h--) {
            if (credibilities.get(h).compareTo(thetaFfn) >= 0) {
                finalLevel = LEVEL_NAMES[h];
                break;
            }
        }
        return finalLevel;
    }


    /**
     * 根据传入的原始数据数组评估准则层
     * @param rawData 原始指标数据数组
     * @param evaluationType 评估类型
     * @return 一个Map，键为准则层名称，值为评估等级
     */
    @Override
    public Map<String, String> evaluateCriterionLayer(double[] rawData, EvaluationType evaluationType) {
        IEvaluationStrategy strategy = EvaluationStrategyFactory.getStrategy(evaluationType);
        Map<String, String> groupResults = new LinkedHashMap<>();
        Map<String, int[]> groups = strategy.getCriterionGroups();
        double[][] boundariesCrisp = strategy.getBoundaries();
        double[] weights = strategy.getWeights();
        double thetaCrisp = strategy.getTheta();

        for (Map.Entry<String, int[]> entry : groups.entrySet()) {
            String groupName = entry.getKey();
            int[] groupIndices = entry.getValue();

            double[] groupSampleRaw = new double[groupIndices.length];
            double[][] groupBoundaries = new double[groupIndices.length][];
            double[] groupWeights = new double[groupIndices.length];
            for (int i = 0; i < groupIndices.length; i++) {
                int originalIndex = groupIndices[i];
                groupSampleRaw[i] = rawData[originalIndex];
                groupBoundaries[i] = boundariesCrisp[originalIndex];
                groupWeights[i] = weights[originalIndex];
            }

            double[] groupSampleProcessed = strategy.preprocessCriterionLayerData(groupSampleRaw, groupIndices);

            // ... 后续核心计算逻辑与您提供的版本一致 ...
            double weightSum = 0;
            for(double w : groupWeights) weightSum += w;
            double[] normalizedWeights = new double[groupWeights.length];
            if (weightSum > 0) {
                for (int i = 0; i < groupWeights.length; i++) normalizedWeights[i] = groupWeights[i] / weightSum;
            }

            double[] minVals = new double[groupIndices.length];
            double[] maxVals = new double[groupIndices.length];
            for (int i = 0; i < groupIndices.length; i++) {
                minVals[i] = groupBoundaries[i][0];
                maxVals[i] = groupBoundaries[i][groupBoundaries[i].length - 1];
            }

            List<List<FermateanFuzzyNumber>> boundariesFfn = convertToFfnBoundaries(groupBoundaries, minVals, maxVals);
            List<FermateanFuzzyNumber> sampleFfn = convertToFfnSample(groupSampleProcessed, minVals, maxVals);
            List<FermateanFuzzyNumber> qFfnList = new ArrayList<>();
            List<FermateanFuzzyNumber> pFfnList = new ArrayList<>();
            List<FermateanFuzzyNumber> vFfnList = new ArrayList<>();
            calculateQPVLists(groupIndices.length, minVals, maxVals, qFfnList, pFfnList, vFfnList);

            List<FermateanFuzzyNumber> credibilities = new ArrayList<>();
            for (int h = 0; h < 5; h++) {
                credibilities.add(calculateCredibilityFfn(sampleFfn, h, boundariesFfn, qFfnList, pFfnList, vFfnList, normalizedWeights));
            }

            FermateanFuzzyNumber thetaFfn = new FermateanFuzzyNumber(new double[]{thetaCrisp, thetaCrisp}, new double[]{1.0 - thetaCrisp, 1.0 - thetaCrisp});
            String finalLevel = LEVEL_NAMES[0];
            for (int h = 4; h >= 0; h--) {
                if (credibilities.get(h).compareTo(thetaFfn) >= 0) {
                    finalLevel = LEVEL_NAMES[h];
                    break;
                }
            }
            groupResults.put(groupName, finalLevel);
        }
        return groupResults;
    }

    private FermateanFuzzyNumber normalizeAndConvertFfn(double value, double minVal, double maxVal) {
        double s;
        if (maxVal - minVal < EPSILON) {
            s = (value == minVal) ? 0.5 : (value > minVal ? 1.0 : 0.0);
        } else {
            s = (value - minVal) / (maxVal - minVal);
        }
        s = Math.max(0.0, Math.min(s, 1.0));
        return new FermateanFuzzyNumber(new double[]{s, s}, new double[]{1.0 - s, 1.0 - s});
    }

    private List<List<FermateanFuzzyNumber>> convertToFfnBoundaries(double[][] boundariesCrisp, double[] minVals, double[] maxVals) {
        List<List<FermateanFuzzyNumber>> boundariesFfn = new ArrayList<>();
        for (int i = 0; i < boundariesCrisp.length; i++) {
            List<FermateanFuzzyNumber> row = new ArrayList<>();
            for (int j = 0; j < boundariesCrisp[i].length; j++) {
                row.add(normalizeAndConvertFfn(boundariesCrisp[i][j], minVals[i], maxVals[i]));
            }
            boundariesFfn.add(row);
        }
        return boundariesFfn;
    }

    private List<FermateanFuzzyNumber> convertToFfnSample(double[] sampleData, double[] minVals, double[] maxVals) {
        List<FermateanFuzzyNumber> sampleFfn = new ArrayList<>();
        for (int i = 0; i < sampleData.length; i++) {
            sampleFfn.add(normalizeAndConvertFfn(sampleData[i], minVals[i], maxVals[i]));
        }
        return sampleFfn;
    }

    private void calculateQPVLists(int numItems, double[] minVals, double[] maxVals, List<FermateanFuzzyNumber> qList, List<FermateanFuzzyNumber> pList, List<FermateanFuzzyNumber> vList) {
        for (int i = 0; i < numItems; i++) {
            double totalRange = maxVals[i] - minVals[i];
            if (totalRange < EPSILON) {
                qList.add(FermateanFuzzyNumber.FFN_ZERO);
                pList.add(FermateanFuzzyNumber.FFN_ZERO);
                vList.add(FermateanFuzzyNumber.FFN_ZERO);
            } else {
                double step = totalRange / 5.0;
                qList.add(normalizeAndConvertFfn(0.25 * step, 0, totalRange));
                pList.add(normalizeAndConvertFfn(0.5 * step, 0, totalRange));
                vList.add(normalizeAndConvertFfn(0.75 * step, 0, totalRange));
            }
        }
    }

    private FermateanFuzzyNumber calculateCredibilityFfn(
            List<FermateanFuzzyNumber> sampleFfn, int h, List<List<FermateanFuzzyNumber>> boundariesFfn,
            List<FermateanFuzzyNumber> qFfnList, List<FermateanFuzzyNumber> pFfnList,
            List<FermateanFuzzyNumber> vFfnList, double[] weights) {
        int N = sampleFfn.size();
        List<FermateanFuzzyNumber> phis_n = new ArrayList<>(N);
        List<FermateanFuzzyNumber> sigmas_n = new ArrayList<>(N);

        FermateanFuzzyNumber totalPhiWeighted = new FermateanFuzzyNumber(new double[]{0.0, 0.0}, new double[]{0.0, 0.0});

        for (int n = 0; n < N; n++) {
            FermateanFuzzyNumber x_m_ffn = sampleFfn.get(n);
            FermateanFuzzyNumber b_h_ffn = boundariesFfn.get(n).get(h);
            FermateanFuzzyNumber q_ffn = qFfnList.get(n);
            FermateanFuzzyNumber p_ffn = pFfnList.get(n);
            FermateanFuzzyNumber v_ffn = vFfnList.get(n);

            FermateanFuzzyNumber x_plus_p = x_m_ffn.add(p_ffn);
            FermateanFuzzyNumber x_plus_q = x_m_ffn.add(q_ffn);

            FermateanFuzzyNumber phi_n;
            if (x_plus_p.compareTo(b_h_ffn) < 0) {
                phi_n = FermateanFuzzyNumber.FFN_ZERO;
            } else if (x_plus_q.compareTo(b_h_ffn) >= 0) {
                phi_n = FermateanFuzzyNumber.FFN_ONE;
            } else {
                FermateanFuzzyNumber num_phi = x_plus_p.subtract(b_h_ffn);
                FermateanFuzzyNumber den_phi = p_ffn.subtract(q_ffn);
                if (den_phi.equals(FermateanFuzzyNumber.FFN_ZERO)) {
                    phi_n = FermateanFuzzyNumber.FFN_ZERO;
                } else {
                    phi_n = num_phi.divide(den_phi).clamp();
                }
            }
            phis_n.add(phi_n);

            FermateanFuzzyNumber sigma_n;
            FermateanFuzzyNumber x_plus_v = x_m_ffn.add(v_ffn);
            if (x_plus_p.compareTo(b_h_ffn) >= 0) {
                sigma_n = FermateanFuzzyNumber.FFN_ZERO;
            } else if (x_plus_v.compareTo(b_h_ffn) < 0) {
                sigma_n = FermateanFuzzyNumber.FFN_ONE;
            } else {
                FermateanFuzzyNumber num_sig = b_h_ffn.subtract(x_plus_p);
                FermateanFuzzyNumber den_sig = v_ffn.subtract(p_ffn);
                if (den_sig.equals(FermateanFuzzyNumber.FFN_ZERO)) {
                    sigma_n = FermateanFuzzyNumber.FFN_ZERO;
                } else {
                    sigma_n = num_sig.divide(den_sig).clamp();
                }
            }
            sigmas_n.add(sigma_n);

            if (x_m_ffn.compareTo(b_h_ffn) >= 0) {
                double newAlphaL = totalPhiWeighted.getAlphaL() + phis_n.get(n).getAlphaL() * weights[n];
                double newAlphaU = totalPhiWeighted.getAlphaU() + phis_n.get(n).getAlphaU() * weights[n];
                double newBetaL = totalPhiWeighted.getBetaL() + phis_n.get(n).getBetaL() * weights[n];
                double newBetaU = totalPhiWeighted.getBetaU() + phis_n.get(n).getBetaU() * weights[n];
                totalPhiWeighted = new FermateanFuzzyNumber(new double[]{newAlphaL, newAlphaU}, new double[]{newBetaL, newBetaU}).clamp();
            }
        }

        FermateanFuzzyNumber phi_global = totalPhiWeighted;
        FermateanFuzzyNumber product_term = FermateanFuzzyNumber.FFN_ONE;
        for (int n = 0; n < N; n++) {
            FermateanFuzzyNumber sigma_n = sigmas_n.get(n);
            if (sigma_n.compareTo(phi_global) > 0) {
                FermateanFuzzyNumber num_nu = FermateanFuzzyNumber.FFN_ONE.subtract(sigma_n);
                FermateanFuzzyNumber den_nu = FermateanFuzzyNumber.FFN_ONE.subtract(phi_global);
                if (den_nu.equals(FermateanFuzzyNumber.FFN_ZERO) || (den_nu.getAlphaL() < EPSILON && den_nu.getAlphaU() < EPSILON)) {
                    continue;
                }
                FermateanFuzzyNumber fraction = num_nu.divide(den_nu);
                product_term = product_term.multiply(fraction);
            }
        }
        return phi_global.multiply(product_term).clamp();
    }
}