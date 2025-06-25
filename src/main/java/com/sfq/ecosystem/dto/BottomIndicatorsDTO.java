package com.sfq.ecosystem.dto;

import lombok.Data;
import java.util.List;

@Data
public class BottomIndicatorsDTO {
    private List<IndicatorInfoDTO> collaborative; // 协同子系统倒数指标
    private List<IndicatorInfoDTO> resource;      // 资源子系统倒数指标
    private List<IndicatorInfoDTO> service;       // 服务子系统倒数指标
}