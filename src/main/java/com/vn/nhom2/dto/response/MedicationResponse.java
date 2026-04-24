package com.vn.nhom2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationResponse {
    private Long id;
    private String name;
    private Integer dosageAmount;
    private String dosageUnit;
    private String frequency;
    
    /**
     * Total quantity (immutable - original amount)
     */
    private Integer totalQuantity;
    
    /**
     * CALCULATED: Remaining quantity based on consumption history
     * Formula: totalQuantity - SUM(amountConsumed from ConsumptionHistory)
     * NOT stored in database - derived on-demand
     */
    private Integer remainingQuantity;
    
    /**
     * CALCULATED: Progress percentage showing how many pills consumed
     * Formula: (totalQuantity - remainingQuantity) / totalQuantity * 100
     * NOT stored in database - derived on-demand
     */
    private Double progressPercentage;
    
    /**
     * First scheduled time to take medication
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime medicationTime1;
    
    /**
     * Second scheduled time (optional)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime medicationTime2;
    
    /**
     * Third scheduled time (optional)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime medicationTime3;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private String status; // "active", "upcoming", "expired"
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
