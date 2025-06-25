package com.sfq.ecosystem.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class IndicatorTrendDataPointDTO {
    private LocalDateTime entryTime;
    private List<IndicatorValueDTO> collaborative;
    private List<IndicatorValueDTO> resource;
    private List<IndicatorValueDTO> service;
}