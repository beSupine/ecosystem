package com.sfq.ecosystem;
import com.sfq.ecosystem.service.PcaService;
import com.sfq.ecosystem.service.impl.PcaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;


class PcaServiceImplTest {


    private PcaService pcaService;
    @BeforeEach
    public void setUp() {
        // 通常会通过 @Autowired 注入，但为了简单测试，直接实例化
        pcaService = new PcaServiceImpl();
    }

    @Test
    void testPrincipalComponentWeighting() {
        // 来自Python脚本的样本数据
        double[][] data = {
                {0.40, 0.53, 0.92, 0.48, 0.75, 0.26, 0.25, 0.45, 0.68, 0.40, 0.82, 0.39, 0.41, 0.88},
                {0.70, 0.60, 0.89, 0.58, 0.80, 0.39, 0.40, 0.55, 0.78, 0.00, 0.70, 0.27, 0.36, 0.83},
                {0.45, 0.69, 0.93, 0.43, 0.72, 0.32, 0.55, 0.75, 0.79, 0.40, 0.85, 0.43, 0.33, 0.90},
                {0.65, 0.58, 0.95, 0.66, 0.85, 0.48, 0.35, 0.80, 0.69, 0.57, 0.88, 0.50, 0.56, 0.85},
                {0.60, 0.76, 0.97, 0.61, 0.88, 0.61, 0.65, 0.86, 0.70, 0.80, 0.91, 0.54, 0.60, 0.95}
        };

        // 调用主成分赋权方法
        double[] weights = pcaService.principalComponentWeighting(data);

        // 断言结果不为null
        assertNotNull(weights);

        // 打印结果以供验证
        System.out.println("计算出的权重为:");
        String formattedWeights = Arrays.stream(weights)
                .mapToObj(w -> String.format("%.8f", w))
                .collect(Collectors.joining(", "));
        System.out.println("[" + formattedWeights + "]");

        // 作为对比，原始Python脚本的输出结果约为：
        // [0.0664684  0.06941198 0.071738   0.07297838 0.07683935 0.07542944
        //  0.07068153 0.07721867 0.06173491 0.06316279 0.07621453 0.07436034
        //  0.07085773 0.07290396]
    }
}