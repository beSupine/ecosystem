package com.sfq.ecosystem.service;
import com.sfq.ecosystem.dto.BottomIndicatorsDTO;
import com.sfq.ecosystem.dto.IndicatorInfoDTO;
import com.sfq.ecosystem.dto.IndicatorValueDTO;
import com.sfq.ecosystem.entity.TrainingData;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
//用于处理指标评估逻辑
@Service
public class IndicatorEvaluationService {

    private static final String[] LEVELS = {"差", "较差", "中等", "良好", "优秀"};
    private static final List<String> FILTER_LEVELS = Arrays.asList("差", "较差","中等");
    private static final int BOTTOM_N_LIMIT = 5;
    // 为等级定义优先级，用于排序 (数字越小，优先级越高)
    private static final Map<String, Integer> LEVEL_PRIORITY_MAP = new HashMap<>();
    static {
        LEVEL_PRIORITY_MAP.put("差", 1);
        LEVEL_PRIORITY_MAP.put("较差", 2);
        LEVEL_PRIORITY_MAP.put("中等", 3);
        LEVEL_PRIORITY_MAP.put("良好", 4);
        LEVEL_PRIORITY_MAP.put("优秀", 5);
    }

    /**
     * 主评估方法，获取并处理数据，返回各子系统评级最低的指标
     * @param data 最新的训练数据实体
     * @return 包含各子系统倒数指标的DTO
     */
    public BottomIndicatorsDTO evaluateAndGetBottomIndicators(TrainingData data) {
        BottomIndicatorsDTO bottomIndicators = new BottomIndicatorsDTO();
        if (data == null) {
            // 如果没有数据，返回空的列表
            bottomIndicators.setCollaborative(new ArrayList<>());
            bottomIndicators.setResource(new ArrayList<>());
            bottomIndicators.setService(new ArrayList<>());
            return bottomIndicators;
        }

        bottomIndicators.setCollaborative(getCollaborativeBottomIndicators(data));
        bottomIndicators.setResource(getResourceBottomIndicators(data));
        bottomIndicators.setService(getServiceBottomIndicators(data));

        return bottomIndicators;
    }
    // *** 新增方法：评估协同子系统所有指标的数值和等级 ***
    public List<IndicatorValueDTO> evaluateAllCollaborative(TrainingData data) {
        return evaluateAllIndicators(data, getCollaborativeIndicatorNames(), getCollaborativeBoundaries(), getCollaborativeExtractors());
    }

    // *** 新增方法：评估资源子系统所有指标的数值和等级 ***
    public List<IndicatorValueDTO> evaluateAllResource(TrainingData data) {
        return evaluateAllIndicators(data, getResourceIndicatorNames(), getResourceBoundaries(), getResourceExtractors());
    }

    // *** 新增方法：评估服务子系统所有指标的数值和等级 ***
    public List<IndicatorValueDTO> evaluateAllService(TrainingData data) {
        return evaluateAllIndicators(data, getServiceIndicatorNames(), getServiceBoundaries(), getServiceExtractors());
    }

    /**
     * 评估协同子系统的所有最终指标
     */
    private List<IndicatorInfoDTO> getCollaborativeBottomIndicators(TrainingData data) {
        // 按照数据库和原始顺序定义指标名称
        List<String> indicatorNames = Arrays.asList(
                "X_V11", "X_V12", "X_V13", "X_V14", "X_V21", "X_V22", "X_O11", "X_O21", "X_O31", "X_O32", "X_R11", "X_R12", "X_R13", "X_S11"
        );
        // 从 XietongEvaluationStrategy.java 文件中提取的边界值
        double[][] boundaries = {
                {0, 4, 8, 12, 16, 20}, {0, 1.6, 3.2, 4.8, 6.4, 8}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 0.4, 0.8, 1.2, 1.6, 2}, {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 1, 2, 3, 4, 5},
                {0, 0.04, 0.08, 0.12, 0.16, 0.2}, {0, 0.46, 0.92, 1.38, 1.84, 2.3}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 0.4, 0.8, 1.2, 1.6, 2}, {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 1.6, 3.2, 4.8, 6.4, 8},
                {0, 0.4, 0.8, 1.2, 1.6, 2}, {0, 0.2, 0.4, 0.6, 0.8, 1}
        };
        // 定义从TrainingData对象中提取每个指标值的方法
        List<Function<TrainingData, BigDecimal>> extractors = Arrays.asList(
                TrainingData::getXV11, TrainingData::getXV12, TrainingData::getXV13, TrainingData::getXV14, TrainingData::getXV21, TrainingData::getXV22,
                TrainingData::getXO11, TrainingData::getXO21, TrainingData::getXO31, TrainingData::getXO32, TrainingData::getXR11, TrainingData::getXR12,
                TrainingData::getXR13, TrainingData::getXS11
        );

        List<IndicatorInfoDTO> allIndicators = new ArrayList<>();
        for (int i = 0; i < indicatorNames.size(); i++) {
            BigDecimal rawValue = extractors.get(i).apply(data);
            if (rawValue == null) continue; // 如果数据库中值为NULL则跳过

            double value = rawValue.doubleValue();
            // 根据 XietongEvaluationStrategy.java 对居中型指标 X_O32 (协同主体竞争程度) 进行一致性处理
            if (i == 9) { // X_O32 是第10个指标，索引为9
                value = 2.0 - Math.abs(value - 0.15);
            }
            String level = getLevel(value, boundaries[i]);
            allIndicators.add(new IndicatorInfoDTO(indicatorNames.get(i), level));
        }

        // 筛选出等级为"差"或"较差"的指标，并按原始顺序返回最多5个
        return filterAndLimit(allIndicators);
    }

    /**
     * 评估资源子系统的所有最终指标
     */
    private List<IndicatorInfoDTO> getResourceBottomIndicators(TrainingData data) {
        List<String> indicatorNames = Arrays.asList(
                "Z_V11", "Z_V12", "Z_V21", "Z_V22", "Z_V31", "Z_V32", "Z_O11", "Z_O21", "Z_O31", "Z_O32", "Z_S11"
        );

        double[][] boundaries = {
                {0, 18, 36, 54, 72, 90}, {0, 10, 20, 30, 40, 50}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 1, 2, 3, 4, 5},  {0, 0.06, 0.12, 0.18, 0.24, 0.3},
                {0, 0.8, 1.6, 2.4, 3.2, 4}, {0, 0.46, 0.92, 1.38, 1.84, 2.3}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 1600, 3200, 4800, 6400, 8000},{0, 400, 800, 1200, 1600, 2000}
        };
        List<Function<TrainingData, BigDecimal>> extractors = Arrays.asList(
                TrainingData::getZV11, TrainingData::getZV12, TrainingData::getZV21, TrainingData::getZV22, TrainingData::getZV31,
                TrainingData::getZV32, TrainingData::getZO11, TrainingData::getZO21, TrainingData::getZO31, TrainingData::getZO32,
                TrainingData::getZS11
        );

        List<IndicatorInfoDTO> allIndicators = new ArrayList<>();
        for (int i = 0; i < indicatorNames.size(); i++) {
            BigDecimal rawValue = extractors.get(i).apply(data);
            if (rawValue == null) continue;

            double value = rawValue.doubleValue();
            switch (i) {
                case 6: // Z_O11, 对应策略文件中的索引6, 居中型
                    value = 4.0 - Math.abs(value - 5.0);
                    break;
                case 9: // Z_O32, 对应策略文件中的索引9, 居中型
                    value = 8000.0 - Math.abs(value - 2000.0);
                    break;
                case 10: // Z_S11, 对应策略文件中的索引10, 成本型
                    value = 2000.0 - value;
                    break;
            }

            String level = getLevel(value, boundaries[i]);
            allIndicators.add(new IndicatorInfoDTO(indicatorNames.get(i), level));
        }

        return filterAndLimit(allIndicators);
    }

    /**
     * 评估服务子系统的所有最终指标
     */
    private List<IndicatorInfoDTO> getServiceBottomIndicators(TrainingData data) {
        List<String> indicatorNames = Arrays.asList(
                "F_V11", "F_V12", "F_V21", "F_V22", "F_V31", "F_V32", "F_O11", "F_R11", "F_R12", "F_R13", "F_S11", "F_S12"
        );
        // 从 FuwuEvaluationStrategy.java 文件中提取的边界值
        double[][] boundaries = {
                {0, 2, 4, 6, 8, 10}, {0, 1, 2, 3, 4, 5}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 73.2, 146.4, 219.6, 292.8, 366}, {0, 1, 2, 3, 4, 5}, {0, 0.06, 0.12, 0.18, 0.24, 0.3},
                {0, 0.4, 0.8, 1.2, 1.6, 2}, {0, 1.6, 3.2, 4.8, 6.4, 8}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 14, 28, 42, 56, 70}
        };
        List<Function<TrainingData, BigDecimal>> extractors = Arrays.asList(
                TrainingData::getFV11, TrainingData::getFV12, TrainingData::getFV21, TrainingData::getFV22, TrainingData::getFV31, TrainingData::getFV32,
                TrainingData::getFO11, TrainingData::getFR11, TrainingData::getFR12, TrainingData::getFR13, TrainingData::getFS11, TrainingData::getFS12
        );

        List<IndicatorInfoDTO> allIndicators = new ArrayList<>();
        for (int i = 0; i < indicatorNames.size(); i++) {
            BigDecimal rawValue = extractors.get(i).apply(data);
            if (rawValue == null) continue;

            double value = rawValue.doubleValue();
            // 根据 FuwuEvaluationStrategy.java 进行预处理
            // 索引6 (第7个指标 F_O11): 构件层级深度数，居中型指标，最佳值为2
            if (i == 6) {
                value = 2.0 - Math.abs(value - 2.0);
            }
            // 索引7 (第8个指标 F_R11): 风险响应时间，成本型指标，值越小越好，逆向化
            if (i == 7) {
                value = 8.0 - value;
            }
            String level = getLevel(value, boundaries[i]);
            allIndicators.add(new IndicatorInfoDTO(indicatorNames.get(i), level));
        }

        return filterAndLimit(allIndicators);
    }
    // *** 新增通用评估方法 ***
    private List<IndicatorValueDTO> evaluateAllIndicators(TrainingData data, List<String> indicatorNames, double[][] boundaries, List<Function<TrainingData, BigDecimal>> extractors) {
        List<IndicatorValueDTO> allIndicators = new ArrayList<>();
        for (int i = 0; i < indicatorNames.size(); i++) {
            BigDecimal rawValue = extractors.get(i).apply(data);
            if (rawValue == null) continue;

            double processedValue = rawValue.doubleValue();
            String indicatorName = indicatorNames.get(i);

            // 根据指标名称应用一致性处理
            switch (indicatorName) {
                case "X_O32":
                    processedValue = 2.0 - Math.abs(processedValue - 0.15);
                    break;
                case "Z_O11":
                    processedValue = 4.0 - Math.abs(processedValue - 5.0);
                    break;
                case "Z_O32":
                    processedValue = 8000.0 - Math.abs(processedValue - 2000.0);
                    break;
                case "Z_S11":
                    processedValue = 2000.0 - processedValue;
                    break;
                case "F_O11":
                    processedValue = 2.0 - Math.abs(processedValue - 2.0);
                    break;
                case "F_R11":
                    processedValue = 8.0 - processedValue;
                    break;
            }

            String level = getLevel(processedValue, boundaries[i]);
            allIndicators.add(new IndicatorValueDTO(indicatorName, level, rawValue));
        }
        return allIndicators;
    }

    /**
     * 根据一致性处理后的值和边界，确定指标等级
     * @param value 处理后的值
     * @param bounds 包含5个等级的6个边界点
     * @return 等级字符串 ("差", "较差", "中等", "良好", "优秀")
     */
    private String getLevel(double value, double[] bounds) {
        if (value > bounds[4]) return LEVELS[4]; // 优秀
        if (value > bounds[3]) return LEVELS[3]; // 良好
        if (value > bounds[2]) return LEVELS[2]; // 中等
        if (value > bounds[1]) return LEVELS[1]; // 较差
        return LEVELS[0]; // 差
    }

    /**
     * 从所有指标中筛选出等级为"差"或"较差"的，并返回最多N个
     */
    /**
     * 从所有指标中筛选出需要关注的等级，并按照 "差" > "较差" > "中等" 的优先级排序后，返回最多N个
     */
    private List<IndicatorInfoDTO> filterAndLimit(List<IndicatorInfoDTO> allIndicators) {
        return allIndicators.stream()
                // 1. 筛选出需要关注的等级 ("差", "较差", "中等")
                .filter(indicator -> FILTER_LEVELS.contains(indicator.getIndicatorLevel()))
                // 2. 按照等级优先级排序 (差 -> 较差 -> 中等)。
                //    Comparator.comparing 会保持同等级指标的原始相对顺序。
                .sorted(Comparator.comparing(indicator -> LEVEL_PRIORITY_MAP.get(indicator.getIndicatorLevel())))
                // 3. 取排序后的前N个
                .limit(BOTTOM_N_LIMIT)
                .collect(Collectors.toList());
    }

    // --- 辅助方法，提供各子系统元数据 ---
    private List<String> getCollaborativeIndicatorNames() {
        return Arrays.asList("X_V11", "X_V12", "X_V13", "X_V14", "X_V21", "X_V22", "X_O11", "X_O21", "X_O31", "X_O32", "X_R11", "X_R12", "X_R13", "X_S11");
    }

    private double[][] getCollaborativeBoundaries() {
        return new double[][]{
                {0, 4, 8, 12, 16, 20}, {0, 1.6, 3.2, 4.8, 6.4, 8}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 0.4, 0.8, 1.2, 1.6, 2}, {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 1, 2, 3, 4, 5},
                {0, 0.04, 0.08, 0.12, 0.16, 0.2}, {0, 0.46, 0.92, 1.38, 1.84, 2.3}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 0.4, 0.8, 1.2, 1.6, 2}, {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 1.6, 3.2, 4.8, 6.4, 8},
                {0, 0.4, 0.8, 1.2, 1.6, 2}, {0, 0.2, 0.4, 0.6, 0.8, 1}
        };
    }

    private List<Function<TrainingData, BigDecimal>> getCollaborativeExtractors() {
        return Arrays.asList(
                TrainingData::getXV11, TrainingData::getXV12, TrainingData::getXV13, TrainingData::getXV14, TrainingData::getXV21, TrainingData::getXV22,
                TrainingData::getXO11, TrainingData::getXO21, TrainingData::getXO31, TrainingData::getXO32, TrainingData::getXR11, TrainingData::getXR12,
                TrainingData::getXR13, TrainingData::getXS11
        );
    }

    private List<String> getResourceIndicatorNames() {
        return Arrays.asList("Z_V11", "Z_V12", "Z_V21", "Z_V22", "Z_V31", "Z_V32", "Z_O11", "Z_O21", "Z_O31", "Z_O32", "Z_S11");
    }

    private double[][] getResourceBoundaries() {
        return new double[][]{
                {0, 18, 36, 54, 72, 90}, {0, 10, 20, 30, 40, 50}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 1, 2, 3, 4, 5}, {0, 400, 800, 1200, 1600, 2000},
                {0, 0.06, 0.12, 0.18, 0.24, 0.3}, {0, 0.8, 1.6, 2.4, 3.2, 4}, {0, 0.46, 0.92, 1.38, 1.84, 2.3},
                {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 1600, 3200, 4800, 6400, 8000}
        };
    }

    private List<Function<TrainingData, BigDecimal>> getResourceExtractors() {
        return Arrays.asList(
                TrainingData::getZV11, TrainingData::getZV12, TrainingData::getZV21, TrainingData::getZV22, TrainingData::getZV31,
                TrainingData::getZV32, TrainingData::getZO11, TrainingData::getZO21, TrainingData::getZO31, TrainingData::getZO32, TrainingData::getZS11
        );
    }

    private List<String> getServiceIndicatorNames() {
        return Arrays.asList("F_V11", "F_V12", "F_V21", "F_V22", "F_V31", "F_V32", "F_O11", "F_R11", "F_R12", "F_R13", "F_S11", "F_S12");
    }

    private double[][] getServiceBoundaries() {
        return new double[][]{
                {0, 2, 4, 6, 8, 10}, {0, 1, 2, 3, 4, 5}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 73.2, 146.4, 219.6, 292.8, 366}, {0, 1, 2, 3, 4, 5}, {0, 0.06, 0.12, 0.18, 0.24, 0.3},
                {0, 0.4, 0.8, 1.2, 1.6, 2}, {0, 1.6, 3.2, 4.8, 6.4, 8}, {0, 0.2, 0.4, 0.6, 0.8, 1},
                {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 0.2, 0.4, 0.6, 0.8, 1}, {0, 14, 28, 42, 56, 70}
        };
    }

    private List<Function<TrainingData, BigDecimal>> getServiceExtractors() {
        return Arrays.asList(
                TrainingData::getFV11, TrainingData::getFV12, TrainingData::getFV21, TrainingData::getFV22, TrainingData::getFV31, TrainingData::getFV32,
                TrainingData::getFO11, TrainingData::getFR11, TrainingData::getFR12, TrainingData::getFR13, TrainingData::getFS11, TrainingData::getFS12
        );
    }
}
