package com.sfq.ecosystem.service;

import com.sfq.ecosystem.model.EvaluationType;
import java.util.Map;

/**
 * 协同生态系统评估服务接口 (已更新)
 */
public interface IEvaluationService {
    // *** 新增方法: 直接根据传入的原始数据数组进行目标层评估 ***
    String evaluateTargetLayer(double[] rawData, EvaluationType evaluationType);

    // *** 新增方法: 直接根据传入的原始数据数组进行准则层评估 ***
    Map<String, String> evaluateCriterionLayer(double[] rawData, EvaluationType evaluationType);

}