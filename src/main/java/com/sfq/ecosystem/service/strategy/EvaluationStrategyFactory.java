package com.sfq.ecosystem.service.strategy;

import com.sfq.ecosystem.model.EvaluationType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 评估策略工厂
 * <p>
 * 根据评估类型返回相应的策略实例。
 */
public class EvaluationStrategyFactory {

    private static final Map<EvaluationType, IEvaluationStrategy> strategyMap = new ConcurrentHashMap<>();

    static {
        strategyMap.put(EvaluationType.XIETONG, new XietongEvaluationStrategy());
        strategyMap.put(EvaluationType.ZIYUAN, new ZiyuanEvaluationStrategy());
        strategyMap.put(EvaluationType.FUWU, new FuwuEvaluationStrategy()); // 注册新的服务策略
    }

    public static IEvaluationStrategy getStrategy(EvaluationType type) {
        if (type == null || !strategyMap.containsKey(type)) {
            throw new IllegalArgumentException("不支持的评估类型: " + type);
        }
        return strategyMap.get(type);
    }
}