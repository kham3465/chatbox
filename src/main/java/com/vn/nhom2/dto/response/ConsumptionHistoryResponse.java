package com.vn.nhom2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionHistoryResponse {
    private Long id;
    private Long medicationId;
    private LocalDateTime consumedAt;
    private Integer amountConsumed;
    private LocalDateTime createdTime;
}
