package com.sfq.ecosystem;

import com.sfq.ecosystem.service.IndependenceWeightService;
import com.sfq.ecosystem.service.impl.IndependenceWeightServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class IndependenceWeightServiceTest {

    private IndependenceWeightService independenceWeightService;

    @BeforeEach
    public void setUp() {
        // 通常会通过 @Autowired 注入，但为了简单测试，直接实例化
        independenceWeightService = new IndependenceWeightServiceImpl();
    }

    @Test
    public void testCalculateFinalWeights() {
        // 与 Python 脚本中相同的数据
        double[][] standardizedData = {
                {0.40, 0.53, 0.92, 0.48, 0.75, 0.26, 0.25, 0.45, 0.68, 0.40, 0.82, 0.39, 0.41, 0.88},
                {0.70, 0.60, 0.89, 0.58, 0.80, 0.39, 0.40, 0.55, 0.78, 0.00, 0.70, 0.27, 0.36, 0.83},
                {0.45, 0.69, 0.93, 0.43, 0.72, 0.32, 0.55, 0.75, 0.79, 0.40, 0.85, 0.43, 0.33, 0.90},
                {0.65, 0.58, 0.95, 0.66, 0.85, 0.48, 0.35, 0.80, 0.69, 0.57, 0.88, 0.50, 0.56, 0.85},
                {0.60, 0.76, 0.97, 0.61, 0.88, 0.61, 0.65, 0.86, 0.70, 0.80, 0.91, 0.54, 0.60, 0.95},
        };

        // 定义分块
        List<Integer> splitCols = Arrays.asList(4, 2, 4, 3, 1);

        // Python 脚本计算出的期望结果
        // [0.06325988 0.0813812  0.071534   0.06941093 0.06571598 0.07714016
        //  0.0782414  0.07850553 0.07632644 0.07251263 0.08051676 0.04618765
        //  0.07000527 0.07142857]
        double[] expectedWeights = {
                0.06325988, 0.0813812, 0.071534, 0.06941093, 0.06571598, 0.07714016,
                0.0782414, 0.07850553, 0.07632644, 0.07251263, 0.08051676, 0.04618765,
                0.07000527, 0.07142857
        };

        // 调用 Java 方法计算
        double[] finalWeights = independenceWeightService.calculateFinalWeights(standardizedData, splitCols);

        // 打印结果以供观察
        System.out.println("Calculated Weights: " + Arrays.toString(finalWeights));
        System.out.println("Expected Weights:   " + Arrays.toString(expectedWeights));
        System.out.println("Sum of Weights: " + Arrays.stream(finalWeights).sum());


        // 使用 assertEquals 进行断言比较，delta 用于处理浮点数精度误差
        double delta = 1e-6; // 允许的误差范围
        assertArrayEquals(expectedWeights, finalWeights, delta);
    }
}