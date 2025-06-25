package com.sfq.ecosystem;
import com.sfq.ecosystem.service.impl.OwaWeightFusionServiceImpl;
import org.junit.jupiter.api.Test; // 使用 JUnit 5
import java.util.Arrays;

/**
 * OwaWeightFusionServiceImpl 的测试类
 * @author Gemini AI
 * @date 2025-06-22
 */
class OwaWeightFusionServiceImplTest {

    @Test // 标记这是一个测试方法
    void testOwaWeightFusion() {
        // --- 1. 准备测试数据 ---
        // 实例化服务类，这里不通过Spring注入，而是直接创建对象进行单元测试
        OwaWeightFusionServiceImpl owaService = new OwaWeightFusionServiceImpl();

        // 示例数据：5种方法对14个指标的赋权结果
        // 这与Python脚本中的示例数据完全相同
        double[][] data = {
                {0.0706, 0.0733, 0.0715, 0.0704, 0.0714, 0.0714, 0.0723, 0.0750, 0.0698, 0.0686, 0.0700, 0.0699, 0.0743, 0.0714},
                {0.0441, 0.0168, 0.0009, 0.0241, 0.0056, 0.0885, 0.1066, 0.0547, 0.0042, 0.5360, 0.0078, 0.0515, 0.0570, 0.0022},
                {0.0608, 0.0756, 0.0724, 0.0659, 0.0714, 0.0776, 0.0741, 0.0659, 0.0635, 0.0763, 0.0766, 0.0735, 0.0774, 0.0691},
                {0.1237, 0.0554, 0.0151, 0.0740, 0.0397, 0.0638, 0.0998, 0.0821, 0.0786, 0.1625, 0.0498, 0.0565, 0.0666, 0.0324},
                {0.0763, 0.0481, 0.0107, 0.0566, 0.0275, 0.1104, 0.1198, 0.0844, 0.0239, 0.2228, 0.0322, 0.0814, 0.0885, 0.0174}
        };

        // --- 2. 调用被测试的方法 ---
        // 使用默认参数(a=0.3, b=0.7)进行融合
        System.out.println("开始执行 OWA 权重融合测试...");
        double[] fusedWeights = owaService.owaWeightFusion(data);

        // --- 3. 验证与输出结果 ---
        // 打印融合后的综合权重，并进行格式化，方便查看
        System.out.println("融合后的综合权重 omega: " + Arrays.toString(fusedWeights));

        // 验证总和是否为1 (由于浮点数精度问题，使用一个小的容差)
        double sum = Arrays.stream(fusedWeights).sum();
        System.out.println("权重总和: " + sum);
        assert Math.abs(1.0 - sum) < 1e-9;

        System.out.println("OWA 权重融合测试执行完毕。");
    }
}