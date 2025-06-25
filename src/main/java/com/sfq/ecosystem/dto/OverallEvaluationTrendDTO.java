package com.sfq.ecosystem.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OverallEvaluationTrendDTO {
    private LocalDateTime entryTime;
    private String collaborativeSystemEvaluation;
    private String resourceSystemEvaluation;
    private String serviceSystemEvaluation;
}