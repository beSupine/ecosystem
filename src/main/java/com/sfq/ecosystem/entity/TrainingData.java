package com.sfq.ecosystem.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 使用Lombok简化代码，你也可以手动添加getter/setter
import lombok.Data;

@Data
public class TrainingData {
    private Integer id;
    private Integer userId;
    private LocalDateTime entryTime;

    // 协同子系统评价
    private String x; // ENUM('差', '较差', '中等', '良好', '优秀')
    private String xV;
    private String xO;
    private String xR;
    private String xS;
    private BigDecimal xV1;
    private BigDecimal xV2;
    private BigDecimal xO1;
    private BigDecimal xO2;
    private BigDecimal xO3;
    private BigDecimal xR1;
    private BigDecimal xS1;
    private BigDecimal xV11;
    private BigDecimal xV12;
    private BigDecimal xV13;
    private BigDecimal xV14;
    private BigDecimal xV21;
    private BigDecimal xV22;
    private BigDecimal xO11;
    private BigDecimal xO21;
    private BigDecimal xO31;
    private BigDecimal xO32;
    private BigDecimal xR11;
    private BigDecimal xR12;
    private BigDecimal xR13;
    private BigDecimal xS11;

    // 资源子系统评价
    private String z; // ENUM('差', '较差', '中等', '良好', '优秀')
    private String zV;
    private String zO;
    private String zS;
    private BigDecimal zV1;
    private BigDecimal zV2;
    private BigDecimal zV3;
    private BigDecimal zO1;
    private BigDecimal zO2;
    private BigDecimal zO3;
    private BigDecimal zS1;
    private BigDecimal zV11;
    private BigDecimal zV12;
    private BigDecimal zV21;
    private BigDecimal zV22;
    private BigDecimal zV31;
    private BigDecimal zV32;
    private BigDecimal zO11;
    private BigDecimal zO21;
    private BigDecimal zO31;
    private BigDecimal zO32;
    private BigDecimal zS11;

    // 服务子系统评价
    private String f; // ENUM('差', '较差', '中等', '良好', '优秀')
    private String fV;
    private String fO;
    private String fR;
    private String fS;
    private BigDecimal fV1;
    private BigDecimal fV2;
    private BigDecimal fV3;
    private BigDecimal fO1;
    private BigDecimal fR1;
    private BigDecimal fS1;
    private BigDecimal fV11;
    private BigDecimal fV12;
    private BigDecimal fV21;
    private BigDecimal fV22;
    private BigDecimal fV31;
    private BigDecimal fV32;
    private BigDecimal fO11;
    private BigDecimal fR11;
    private BigDecimal fR12;
    private BigDecimal fR13;
    private BigDecimal fS11;
    private BigDecimal fS12;
}
