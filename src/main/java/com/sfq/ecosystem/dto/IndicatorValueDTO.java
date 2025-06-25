package com.sfq.ecosystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorValueDTO {
    private String indicatorName;   // 指标名, 例如 "X_V11"
    private String indicatorLevel;  // 指标评定等级, 例如 "良好"
    private BigDecimal value;       // 指标原始数值
}