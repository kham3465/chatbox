package com.vn.nhom2.repo;

import com.vn.nhom2.entity.MedicationNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationNotificationRepository extends JpaRepository<MedicationNotification, Long> {
    List<MedicationNotification> findByUserIdOrderBySentTimeDesc(Long userId);
    List<MedicationNotification> findByUserIdAndIsReadFalse(Long userId);
    void deleteByUserId(Long userId);
}
