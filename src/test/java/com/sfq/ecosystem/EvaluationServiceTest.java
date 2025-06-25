package com.sfq.ecosystem;

import com.sfq.ecosystem.model.EvaluationType;
import com.sfq.ecosystem.service.IEvaluationService;
import com.sfq.ecosystem.service.impl.EvaluationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * 评估服务测试类 (已更新)
 * <p>
 * 演示如何调用服务来评估不同类型（协同、资源、服务）的对象。
 */
public class EvaluationServiceTest {

    private IEvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        this.evaluationService = new EvaluationServiceImpl();
    }

    @Test
    @DisplayName("测试 '协同' 评价对象 - 目标层评估")
    void testXietongTargetLayer() {
        evaluationService.evaluateAllTargetSamples(EvaluationType.XIETONG);
    }

    @Test
    @DisplayName("测试 '协同' 评价对象 - 准则层评估")
    void testXietongCriterionLayer() {
        Map<String, Map<String, String>> allResults = evaluationService.evaluateAllCriterionSamples(EvaluationType.XIETONG);
        printCriterionResults(allResults, EvaluationType.XIETONG);
    }

    @Test
    @DisplayName("测试 '资源' 评价对象 - 目标层评估")
    void testZiyuanTargetLayer() {
        evaluationService.evaluateAllTargetSamples(EvaluationType.ZIYUAN);
    }

    @Test
    @DisplayName("测试 '资源' 评价对象 - 准则层评估")
    void testZiyuanCriterionLayer() {
        Map<String, Map<String, String>> allResults = evaluationService.evaluateAllCriterionSamples(EvaluationType.ZIYUAN);
        printCriterionResults(allResults, EvaluationType.ZIYUAN);
    }

    @Test
    @DisplayName("测试 '服务' 评价对象 - 目标层评估")
    void testFuwuTargetLayer() {
        evaluationService.evaluateAllTargetSamples(EvaluationType.FUWU);
    }

    @Test
    @DisplayName("测试 '服务' 评价对象 - 准则层评估")
    void testFuwuCriterionLayer() {
        Map<String, Map<String, String>> allResults = evaluationService.evaluateAllCriterionSamples(EvaluationType.FUWU);
        printCriterionResults(allResults, EvaluationType.FUWU);
    }

    /**
     * 辅助方法，用于统一打印准则层评估结果
     * @param allResults 评估结果
     * @param type 评估类型
     */
    private void printCriterionResults(Map<String, Map<String, String>> allResults, EvaluationType type) {
        allResults.forEach((sampleName, results) -> {
            System.out.printf("\n样本 [%s] 的 '%s' 准则层评估汇总:\n", sampleName, type.getDescription());
            results.forEach((group, level) -> System.out.printf("  - %s: %s\n", group, level));
            System.out.println("--------------------------------------");
        });
    }
}