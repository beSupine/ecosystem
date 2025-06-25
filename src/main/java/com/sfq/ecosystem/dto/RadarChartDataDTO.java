package com.sfq.ecosystem.dto;

import lombok.Data;
import java.util.Map;

@Data
public class RadarChartDataDTO {
    private Map<String, String> collaborativeSystem; // 协同子系统雷达图数据
    private Map<String, String> resourceSystem;      // 资源子系统雷达图数据
    private Map<String, String> serviceSystem;       // 服务子系统雷达图数据
}