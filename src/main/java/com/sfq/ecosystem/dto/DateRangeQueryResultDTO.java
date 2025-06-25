package com.sfq.ecosystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateRangeQueryResultDTO {
    private long totalCount;
    private List<DateRangeQueryInfoDTO> records;
}