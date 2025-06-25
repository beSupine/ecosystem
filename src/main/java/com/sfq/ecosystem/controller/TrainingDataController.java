package com.sfq.ecosystem.controller;

import com.sfq.ecosystem.dto.*;
import com.sfq.ecosystem.entity.TrainingData;
import com.sfq.ecosystem.service.TrainingDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/evaluation")
@CrossOrigin(origins = "http://localhost:8080")
public class TrainingDataController {

    @Autowired
    private TrainingDataService trainingDataService;

    @GetMapping("/overall")
    public ResponseEntity<OverallEvaluationDTO> getOverallEvaluation() {
        OverallEvaluationDTO dto = trainingDataService.getOverallEvaluation();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/radar")
    public ResponseEntity<RadarChartDataDTO> getRadarChartData() {
        RadarChartDataDTO dto = trainingDataService.getRadarChartData();
        return ResponseEntity.ok(dto);
    }

    /**
     * 接口三: 获取综合评价历年趋势 (最近5次)
     */
    @GetMapping("/overall-trend")
    public ResponseEntity<List<OverallEvaluationTrendDTO>> getOverallEvaluationTrend() {
        List<OverallEvaluationTrendDTO> trendData = trainingDataService.getOverallEvaluationTrend();
        return ResponseEntity.ok(trendData);
    }

    /**
     * 接口四: 获取雷达图数据历年趋势 (最近5次)
     */
    @GetMapping("/radar-trend")
    public ResponseEntity<List<RadarTrendDataDTO>> getRadarDataTrend() {
        List<RadarTrendDataDTO> trendData = trainingDataService.getRadarDataTrend();
        return ResponseEntity.ok(trendData);
    }
    /**
     * 接口五: 获取各子系统评级倒数三级的五个指标
     * @return 包含各子系统倒数指标列表的DTO
     */
    @GetMapping("/bottom-indicators")
    public ResponseEntity<BottomIndicatorsDTO> getBottomIndicators() {
        return ResponseEntity.ok(trainingDataService.getBottomIndicators());
    }

    @GetMapping("/indicator-trends")
    public ResponseEntity<List<IndicatorTrendDataPointDTO>> getIndicatorTrends() {
        return ResponseEntity.ok(trainingDataService.getIndicatorTrends());
    }

    // --- 新增、编辑、删除接口 ---

    /**
     * 新增一条训练数据记录
     * @param trainingData 前端传入的只包含最终指标层数据的对象
     * @return 创建成功后的完整数据记录 (包含ID和时间)
     */
    @PostMapping
    public ResponseEntity<TrainingData> createTrainingData(@RequestBody TrainingData trainingData) {
        TrainingData createdData = trainingDataService.createTrainingData(trainingData);
        return ResponseEntity.ok(createdData);
    }

    /**
     * 编辑指定ID的训练数据记录，传入json格式的TrainingData对象，不包括ID和创建时间
     * @param id 要编辑的记录ID
     * @param trainingData 前端传入的更新数据
     * @return 更新成功后的数据记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<TrainingData> updateTrainingData(@PathVariable Integer id, @RequestBody TrainingData trainingData) {
        TrainingData updatedData = trainingDataService.updateTrainingData(id, trainingData);
        if (updatedData == null) {
            return ResponseEntity.notFound().build(); // 如果找不到对应ID的记录，返回404
        }
        return ResponseEntity.ok(updatedData);
    }

    /**
     * 删除指定ID的训练数据记录
     * @param id 要删除的记录ID
     * @return 成功则返回204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainingData(@PathVariable Integer id) {
        trainingDataService.deleteTrainingData(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据日期范围查询数据
     * @param startDate 开始日期, 格式 YYYY-MM-DD'T'HH:mm:ss
     * @param endDate 结束日期, 格式 YYYY-MM-DD'T'HH:mm:ss
     * @return 包含总数和记录列表的结果
     */
    @GetMapping("/by-date-range")
    public ResponseEntity<DateRangeQueryResultDTO> getDataByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        DateRangeQueryResultDTO result = trainingDataService.getDataByDateRange(startDate, endDate);
        return ResponseEntity.ok(result);
    }
    /**
     * 根据ID获取单条训练数据记录
     * @param id 记录的ID
     * @return 完整的TrainingData对象
     */
    @GetMapping("/{id}")
    public ResponseEntity<TrainingData> getTrainingDataById(@PathVariable Integer id) {
        TrainingData data = trainingDataService.findById(id);
        if (data != null) {
            return ResponseEntity.ok(data);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}