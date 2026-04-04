package com.vn.nhom2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medications")
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "dosage_amount", nullable = false)
    private Integer dosageAmount;

    @Column(name = "dosage_unit", nullable = false)
    private String dosageUnit;

    @Column(name = "frequency", nullable = false)
    private String frequency;

    /**
     * Total initial quantity (original amount of pills/units)
     * Immutable - set once when medication created
     * Never changes - serves as baseline for calculating remaining quantity
     */
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    /**
     * First scheduled time to take medication (e.g., 07:00)
     */
    @Column(name = "medication_time_1")
    private LocalTime medicationTime1;

    /**
     * Second scheduled time to take medication (optional, e.g., 14:00)
     */
    @Column(name = "medication_time_2")
    private LocalTime medicationTime2;

    /**
     * Third scheduled time to take medication (optional, e.g., 21:00)
     */
    @Column(name = "medication_time_3")
    private LocalTime medicationTime3;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * ConsumptionHistory: Single source of truth for consumption tracking
     * Remaining quantity is calculated as: totalQuantity - SUM(amountConsumed)
     * This ensures perfect consistency and maintains audit trail
     */
    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConsumptionHistory> consumptionHistories;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
