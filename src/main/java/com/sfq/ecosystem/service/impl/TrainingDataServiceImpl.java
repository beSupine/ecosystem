
package com.sfq.ecosystem.service.impl;

import com.sfq.ecosystem.dto.*;
import com.sfq.ecosystem.entity.TrainingData;
import com.sfq.ecosystem.model.EvaluationType;
import com.sfq.ecosystem.repository.TrainingDataMapper;
import com.sfq.ecosystem.service.IEvaluationService;
import com.sfq.ecosystem.service.IndicatorEvaluationService;
import com.sfq.ecosystem.service.TrainingDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TrainingDataServiceImpl implements TrainingDataService {

    @Autowired
    private TrainingDataMapper trainingDataMapper;

    @Autowired
    private IndicatorEvaluationService indicatorEvaluationService;

    @Autowired
    private IEvaluationService evaluationService;

    @Override
    public OverallEvaluationDTO getOverallEvaluation() {
        Map<String, String> result = trainingDataMapper.findOverallEvaluation();
        OverallEvaluationDTO dto = new OverallEvaluationDTO();
        if (result != null) {
            dto.setCollaborativeSystemEvaluation(result.get("collaborativeSystemEvaluation"));
            dto.setResourceSystemEvaluation(result.get("resourceSystemEvaluation"));
            dto.setServiceSystemEvaluation(result.get("serviceSystemEvaluation"));
        }
        return dto;
    }

    @Override
    public RadarChartDataDTO getRadarChartData() {
        Map<String, String> result = trainingDataMapper.findRadarChartData();
        RadarChartDataDTO dto = new RadarChartDataDTO();
        if (result != null) {
            dto.setCollaborativeSystem(createMap("活力", result.get("collaborative_vitality"), "组织力", result.get("collaborative_organization"), "稳定性", result.get("collaborative_stability"), "服务能力", result.get("collaborative_service")));
            dto.setResourceSystem(createMap("活力", result.get("resource_vitality"), "组织力", result.get("resource_organization"), "服务能力", result.get("resource_service")));
            dto.setServiceSystem(createMap("活力", result.get("service_vitality"), "组织力", result.get("service_organization"), "稳定性", result.get("service_stability"), "服务能力", result.get("service_service")));
        }
        return dto;
    }

    @Override
    public List<OverallEvaluationTrendDTO> getOverallEvaluationTrend() {
        List<Map<String, Object>> results = trainingDataMapper.findOverallEvaluationTrend();
        return results.stream().map(row -> {
            OverallEvaluationTrendDTO dto = new OverallEvaluationTrendDTO();
            dto.setEntryTime((LocalDateTime) row.get("entry_time"));
            dto.setCollaborativeSystemEvaluation((String) row.get("collaborativeSystemEvaluation"));
            dto.setResourceSystemEvaluation((String) row.get("resourceSystemEvaluation"));
            dto.setServiceSystemEvaluation((String) row.get("serviceSystemEvaluation"));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<RadarTrendDataDTO> getRadarDataTrend() {
        List<Map<String, Object>> results = trainingDataMapper.findRadarDataTrend();
        return results.stream().map(row -> {
            RadarTrendDataDTO dto = new RadarTrendDataDTO();
            dto.setEntryTime((LocalDateTime) row.get("entry_time"));
            dto.setCollaborativeSystem(createMap("活力", (String) row.get("collaborative_vitality"), "组织力", (String) row.get("collaborative_organization"), "稳定性", (String) row.get("collaborative_stability"), "服务能力", (String) row.get("collaborative_service")));
            dto.setResourceSystem(createMap("活力", (String) row.get("resource_vitality"), "组织力", (String) row.get("resource_organization"), "服务能力", (String) row.get("resource_service")));
            dto.setServiceSystem(createMap("活力", (String) row.get("service_vitality"), "组织力", (String) row.get("service_organization"), "稳定性", (String) row.get("service_stability"), "服务能力", (String) row.get("service_service")));
            return dto;
        }).collect(Collectors.toList());
    }
    // *** 新增方法实现 ***
    @Override
    public BottomIndicatorsDTO getBottomIndicators() {
        // 1. 从数据库获取最新的完整数据记录
        TrainingData latestData = trainingDataMapper.findLatest();
        // 2. 调用评估服务进行计算
        return indicatorEvaluationService.evaluateAndGetBottomIndicators(latestData);
    }
    @Override
    public List<IndicatorTrendDataPointDTO> getIndicatorTrends() {
        // 1. 从数据库获取最近五条完整记录
        List<TrainingData> latestFiveRecords = trainingDataMapper.findLatestFive();
        List<IndicatorTrendDataPointDTO> trends = new ArrayList<>();

        // 2. 遍历每条记录
        for (TrainingData record : latestFiveRecords) {
            IndicatorTrendDataPointDTO dataPoint = new IndicatorTrendDataPointDTO();
            dataPoint.setEntryTime(record.getEntryTime());

            // 3. 调用评估服务，获取该记录下所有指标的评估结果
            dataPoint.setCollaborative(indicatorEvaluationService.evaluateAllCollaborative(record));
            dataPoint.setResource(indicatorEvaluationService.evaluateAllResource(record));
            dataPoint.setService(indicatorEvaluationService.evaluateAllService(record));

            trends.add(dataPoint);
        }

        return trends;
    }
    // *** 新增CRUD服务实现 ***
    /**
     * 新增训练数据，并在持久化前进行评估
     */
    @Override
    public TrainingData createTrainingData(TrainingData trainingData) {
        // 1. 对传入的原始数据进行高层级评估
        performAndSetEvaluations(trainingData);

        // 2. 设置录入时间为当前时间
        trainingData.setEntryTime(LocalDateTime.now());

        // 3. 持久化包含原始数据和评估结果的完整对象
        trainingDataMapper.insert(trainingData);
        return trainingData;
    }

    /**
     * 更新训练数据，并在持久化前重新进行评估
     */
    @Override
    public TrainingData updateTrainingData(Integer id, TrainingData trainingData) {
        trainingData.setId(id);

        // 1. 根据更新后的原始数据重新进行高层级评估
        performAndSetEvaluations(trainingData);

        // 2. 更新数据库
        int affectedRows = trainingDataMapper.update(trainingData);
        if (affectedRows == 0) {
            // 如果没有行被更新，可能意味着该ID不存在
            return null;
        }
        return trainingData;
    }

    /**
     * 私有辅助方法，用于执行所有子系统的评估并设置回对象
     * @param data 包含原始指标数据的TrainingData对象
     */
    private void performAndSetEvaluations(TrainingData data) {
        // --- 协同子系统评估 ---
        double[] xietongRawData = {
                data.getXV11().doubleValue(), data.getXV12().doubleValue(), data.getXV13().doubleValue(), data.getXV14().doubleValue(),
                data.getXV21().doubleValue(), data.getXV22().doubleValue(), data.getXO11().doubleValue(), data.getXO21().doubleValue(),
                data.getXO31().doubleValue(), data.getXO32().doubleValue(), data.getXR11().doubleValue(), data.getXR12().doubleValue(),
                data.getXR13().doubleValue(), data.getXS11().doubleValue()
        };
        // 评估总体等级 X
        data.setX(evaluationService.evaluateTargetLayer(xietongRawData, EvaluationType.XIETONG));
        // 评估准则层等级 X_V, X_O, X_R, X_S
        Map<String, String> xietongCriterionResults = evaluationService.evaluateCriterionLayer(xietongRawData, EvaluationType.XIETONG);
        // 根据策略文件中定义的组名进行映射
        data.setXV(xietongCriterionResults.get("协同产出层"));
        data.setXO(xietongCriterionResults.get("协同主体层"));
        data.setXR(xietongCriterionResults.get("协同关系层"));
        data.setXS(xietongCriterionResults.get("协同数据层"));

        // --- 资源子系统评估 ---
        double[] ziyuanRawData = {
                data.getZV11().doubleValue(), data.getZV12().doubleValue(), data.getZV21().doubleValue(), data.getZV22().doubleValue(),
                data.getZV31().doubleValue(), data.getZV32().doubleValue(), data.getZO11().doubleValue(), data.getZO21().doubleValue(),
                data.getZO31().doubleValue(), data.getZO32().doubleValue(), data.getZS11().doubleValue()
        };
        data.setZ(evaluationService.evaluateTargetLayer(ziyuanRawData, EvaluationType.ZIYUAN));
        Map<String, String> ziyuanCriterionResults = evaluationService.evaluateCriterionLayer(ziyuanRawData, EvaluationType.ZIYUAN);
        data.setZV(ziyuanCriterionResults.get("资源产出层"));
        data.setZO(ziyuanCriterionResults.get("资源要素层"));
        data.setZS(ziyuanCriterionResults.get("资源接口层"));

        // --- 服务子系统评估 ---
        double[] fuwuRawData = {
                data.getFV11().doubleValue(), data.getFV12().doubleValue(), data.getFV21().doubleValue(), data.getFV22().doubleValue(),
                data.getFV31().doubleValue(), data.getFV32().doubleValue(), data.getFO11().doubleValue(), data.getFR11().doubleValue(),
                data.getFR12().doubleValue(), data.getFR13().doubleValue(), data.getFS11().doubleValue(), data.getFS12().doubleValue()
        };
        data.setF(evaluationService.evaluateTargetLayer(fuwuRawData, EvaluationType.FUWU));
        Map<String, String> fuwuCriterionResults = evaluationService.evaluateCriterionLayer(fuwuRawData, EvaluationType.FUWU);
        data.setFV(fuwuCriterionResults.get("服务产出层"));
        data.setFO(fuwuCriterionResults.get("服务能力层"));
        data.setFR(fuwuCriterionResults.get("服务安全层"));
        data.setFS(fuwuCriterionResults.get("服务交互层"));
    }
    @Override
    public void deleteTrainingData(Integer id) {
        int affectedRows = trainingDataMapper.delete(id);
        if (affectedRows == 0) {
            // 如果没有行被删除，处理方式同上
        }
    }

    @Override
    public DateRangeQueryResultDTO getDataByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        long totalCount = trainingDataMapper.countByDateRange(startDate, endDate);
        List<DateRangeQueryInfoDTO> records = trainingDataMapper.findByDateRange(startDate, endDate);
        return new DateRangeQueryResultDTO(totalCount, records);
    }

    // 辅助方法，用于创建Map
    private Map<String, String> createMap(String... keyValues) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
