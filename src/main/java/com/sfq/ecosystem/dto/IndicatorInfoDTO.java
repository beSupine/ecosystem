package com.sfq.ecosystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorInfoDTO {
    private String indicatorName; // 指标名称, e.g., "X_V11"
    private String indicatorLevel; // 指标等级, e.g., "较差"
}