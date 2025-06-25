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
}