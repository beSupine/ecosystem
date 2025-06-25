package com.sfq.ecosystem.dto;

import lombok.Data;

@Data
public class OverallEvaluationDTO {
    private String collaborativeSystemEvaluation; // 协同子系统总体评价 (X)
    private String resourceSystemEvaluation;    // 资源子系统总体评价 (Z)
    private String serviceSystemEvaluation;     // 服务子系统总体评价 (F)
}


