package com.sfq.ecosystem;

import com.sfq.ecosystem.service.CriticService;
import com.sfq.ecosystem.service.impl.CriticServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CriticServiceTest {

    @Test
    public void testCriticMethodWithValidData() {
        CriticService criticService = new CriticServiceImpl();

        double[][] sampleData = {
                {0.40, 0.53, 0.92, 0.48, 0.75, 0.26, 0.25, 0.45, 0.68, 0.40, 0.82, 0.39, 0.41, 0.88},
                {0.70, 0.60, 0.89, 0.58, 0.80, 0.39, 0.40, 0.55, 0.78, 0.00, 0.70, 0.27, 0.36, 0.83},
                {0.45, 0.69, 0.93, 0.43, 0.72, 0.32, 0.55, 0.75, 0.79, 0.40, 0.85, 0.43, 0.33, 0.90},
                {0.65, 0.58, 0.95, 0.66, 0.85, 0.48, 0.35, 0.80, 0.69, 0.57, 0.88, 0.50, 0.56, 0.85},
                {0.60, 0.76, 0.97, 0.61, 0.88, 0.61, 0.65, 0.86, 0.70, 0.80, 0.91, 0.54, 0.60, 0.95}
        };

        double[] weights = criticService.criticMethod(sampleData);
        assertNotNull(weights);
        System.out.println(Arrays.toString(weights));

        double sum = 0;
        for (double w : weights) {
            sum += w;
        }
        assertEquals(1.0, sum, 0.0001); // 验证权重和是否接近1
    }

    @Test
    public void testCriticMethodWithInvalidData() {
        CriticService criticService = new CriticServiceImpl();

        double[] invalidData = {1, 2, 3};
        double[][] invalidData2D = new double[1][invalidData.length];
        invalidData2D[0] = invalidData;

        double[] weights = criticService.criticMethod(invalidData2D);
        assertNull(weights);
    }
}