package com.sfq.ecosystem.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class RadarTrendDataDTO {
    private LocalDateTime entryTime;
    private Map<String, String> collaborativeSystem;
    private Map<String, String> resourceSystem;
    private Map<String, String> serviceSystem;
}