package com.vn.nhom2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationRequest {
    @NotBlank(message = "Medication name is required")
    private String name;

    @NotNull(message = "Dosage amount is required")
    @Positive(message = "Dosage amount must be greater than 0")
    private Integer dosageAmount;

    @NotBlank(message = "Dosage unit is required")
    private String dosageUnit;

    @NotBlank(message = "Frequency is required")
    private String frequency;

    /**
     * Total quantity of pills/units (immutable once created)
     * Example: 60 tablets total
     */
    @NotNull(message = "Total quantity is required")
    @Min(value = 1, message = "Total quantity must be at least 1")
    private Integer totalQuantity;

    /**
     * First scheduled time to take medication (required)
     * Example: 07:00 (7 AM)
     */
    @NotNull(message = "First medication time is required")
    @Schema(type = "string", example = "07:00", description = "Time in HH:mm format")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime medicationTime1;

    /**
     * Second scheduled time (optional)
     * Example: 14:00 (2 PM)
     */
    @Schema(type = "string", example = "14:00", description = "Time in HH:mm format")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime medicationTime2;

    /**
     * Third scheduled time (optional)
     * Example: 21:00 (9 PM)
     */
    @Schema(type = "string", example = "21:00", description = "Time in HH:mm format")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime medicationTime3;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}

