package com.sfq.ecosystem.repository;

import com.sfq.ecosystem.dto.DateRangeQueryInfoDTO;
import com.sfq.ecosystem.entity.TrainingData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface TrainingDataMapper {
    /**
     * 获取最新的综合评价
     */
    Map<String, String> findOverallEvaluation();

    /**
     * 获取最新的雷达图数据
     */
    Map<String, String> findRadarChartData();

    /**
     * 获取综合评价趋势 (最近5次)
     */
    List<Map<String, Object>> findOverallEvaluationTrend();

    /**
     * 获取雷达图数据趋势 (最近5次)
     */
    List<Map<String, Object>> findRadarDataTrend();

    TrainingData findLatest();

    List<TrainingData> findLatestFive();

    // *** 新增CRUD方法 ***
    void insert(TrainingData trainingData);
    int update(TrainingData trainingData);
    int delete(@Param("id") Integer id);
    long countByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    List<DateRangeQueryInfoDTO> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    /**
     * 根据ID查找训练数据
     * @param id 记录ID
     * @return 找到的TrainingData实体
     */
    TrainingData findById(@Param("id") Integer id);
}