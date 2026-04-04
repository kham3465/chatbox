package com.vn.nhom2.repo;

import com.vn.nhom2.entity.ConsumptionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsumptionHistoryRepository extends JpaRepository<ConsumptionHistory, Long> {
    List<ConsumptionHistory> findByMedicationId(Long medicationId);

    List<ConsumptionHistory> findByMedicationIdOrderByConsumedAtDesc(Long medicationId);

    List<ConsumptionHistory> findByMedicationIdAndConsumedAtBetween(
            Long medicationId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    /**
     * Calculate total amount consumed for a medication
     * Returns 0 if no consumption records exist
     *
     * @param medicationId the medication ID
     * @return sum of all amountConsumed, or 0 if none
     */
    @Query("SELECT COALESCE(SUM(ch.amountConsumed), 0) FROM ConsumptionHistory ch WHERE ch.medicationId = ?1")
    Integer getTotalAmountConsumed(Long medicationId);
}
