package com.vn.nhom2.repo;

import com.vn.nhom2.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    /**
     * Find all active medications for a specific user
     *
     * @param userId the user ID
     * @return list of active medications
     */
    List<Medication> findByUserIdAndIsActive(Long userId, Boolean isActive);

    /**
     * Find all medications for a specific user (including inactive)
     *
     * @param userId the user ID
     * @return list of medications
     */
    List<Medication> findByUserId(Long userId);

    /**
     * Find a specific medication by ID and user ID
     *
     * @param id the medication ID
     * @param userId the user ID
     * @return medication if found
     */
    Optional<Medication> findByIdAndUserId(Long id, Long userId);
}
