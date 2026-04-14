package com.vn.nhom2.service.impl;

import com.vn.nhom2.entity.Medication;
import com.vn.nhom2.repo.MedicationRepository;
import com.vn.nhom2.service.MedicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderTask {

    private final MedicationRepository medicationRepository;
    private final MedicationService medicationService;

    /**
     * Task runs every minute to check for medications that need a reminder.
     * Uses cron "0 * * * * *" to trigger at the start of every minute.
     */
    @Scheduled(cron = "0 * * * * *")
    public void sendMedicationReminders() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        
        // This call is now cached in MedicationService
        List<Medication> medications = medicationService.getMedicationsToRemind(now);

        if (!medications.isEmpty()) {
            log.info("Found {} medications to remind at {}", medications.size(), now);
            for (Medication medication : medications) {
                try {
                    medicationService.processMedicationReminder(medication.getId());
                } catch (Exception e) {
                    log.error("Error processing reminder for medication ID {}: {}", medication.getId(), e.getMessage());
                }
            }
        }
    }
}
