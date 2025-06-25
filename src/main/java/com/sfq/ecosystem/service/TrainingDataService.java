package com.sfq.ecosystem.service;

import com.sfq.ecosystem.dto.*;
import com.sfq.ecosystem.entity.TrainingData;

import java.time.LocalDateTime;
import java.util.List;

public interface TrainingDataService {
    OverallEvaluationDTO getOverallEvaluation();
    RadarChartDataDTO getRadarChartData();
    List<OverallEvaluationTrendDTO> getOverallEvaluationTrend();
    List<RadarTrendDataDTO> getRadarDataTrend();
    //查倒数指标接口
    BottomIndicatorsDTO getBottomIndicators();

    List<IndicatorTrendDataPointDTO> getIndicatorTrends();
    // *** 新增CRUD服务接口 ***
    TrainingData createTrainingData(TrainingData trainingData);
    TrainingData updateTrainingData(Integer id, TrainingData trainingData);
    void deleteTrainingData(Integer id);
    DateRangeQueryResultDTO getDataByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    /**
     * 根据ID查找训练数据
     * @param id 记录ID
     * @return 找到的TrainingData实体，否则返回null
     */
    TrainingData findById(Integer id);
}