package com.vn.nhom2.service.impl;

import com.vn.nhom2.dto.request.MedicationRequest;
import com.vn.nhom2.dto.response.ConsumptionHistoryResponse;
import com.vn.nhom2.dto.response.MedicationResponse;
import com.vn.nhom2.entity.ConsumptionHistory;
import com.vn.nhom2.entity.Medication;
import com.vn.nhom2.entity.User;
import com.vn.nhom2.exception.ClientErrorException;
import com.vn.nhom2.exception.ResourceNotFoundException;
import com.vn.nhom2.repo.ConsumptionHistoryRepository;
import com.vn.nhom2.repo.MedicationRepository;
import com.vn.nhom2.service.MedicationService;
import com.vn.nhom2.util.SecurityUtil;
import com.vn.nhom2.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicationServiceImpl implements MedicationService {
    private final MedicationRepository medicationRepository;
    private final ConsumptionHistoryRepository consumptionHistoryRepository;
    private final FCMService fcmService;
    private final UserService userService;

    @Override
    public List<MedicationResponse> getAllMedications() {
        User currentUser = getCurrentAuthenticatedUser();
        List<Medication> medications = medicationRepository.findByUserId(currentUser.getId());
        
        // Update medication status and filter
        return medications.stream()
                .map(this::updateMedicationStatus)
                // .filter(med -> med.getIsActive() == null || med.getIsActive()) // Show all medications regardless of active status, status is determined by dates and history
                .map(this::mapMedicationToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MedicationResponse getMedicationById(Long medicationId) {
        User currentUser = getCurrentAuthenticatedUser();
        Medication medication = medicationRepository.findByIdAndUserId(medicationId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Thuốc không tồn tại"));
        
        updateMedicationStatus(medication);
        return mapMedicationToResponse(medication);
    }

    @Override
    @Transactional
    @CacheEvict(value = "medicationReminders", allEntries = true)
    public MedicationResponse createMedication(MedicationRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        
        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ClientErrorException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        Medication medication = new Medication();
        medication.setUserId(currentUser.getId());
        medication.setName(request.getName());
        medication.setDosageAmount(request.getDosageAmount());
        medication.setDosageUnit(request.getDosageUnit());
        medication.setFrequency(request.getFrequency());
        medication.setTotalQuantity(request.getTotalQuantity());
        medication.setMedicationTime1(request.getMedicationTime1());
        medication.setMedicationTime2(request.getMedicationTime2());
        medication.setMedicationTime3(request.getMedicationTime3());
        medication.setStartDate(request.getStartDate());
        medication.setEndDate(request.getEndDate());
        medication.setIsActive(true);

        Medication savedMedication = medicationRepository.save(medication);
        log.info("Created medication with ID: {} for user: {}", savedMedication.getId(), currentUser.getId());
        return mapMedicationToResponse(savedMedication);
    }

    @Override
    @Transactional
    @CacheEvict(value = "medicationReminders", allEntries = true)
    public MedicationResponse updateMedication(Long medicationId, MedicationRequest request) {
        User currentUser = getCurrentAuthenticatedUser();
        Medication medication = medicationRepository.findByIdAndUserId(medicationId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Thuốc không tồn tại"));

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ClientErrorException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        medication.setName(request.getName());
        medication.setDosageAmount(request.getDosageAmount());
        medication.setDosageUnit(request.getDosageUnit());
        medication.setFrequency(request.getFrequency());
        // Note: totalQuantity is immutable and cannot be changed after creation
        medication.setMedicationTime1(request.getMedicationTime1());
        medication.setMedicationTime2(request.getMedicationTime2());
        medication.setMedicationTime3(request.getMedicationTime3());
        medication.setStartDate(request.getStartDate());
        medication.setEndDate(request.getEndDate());

        Medication updatedMedication = medicationRepository.save(medication);
        log.info("Updated medication with ID: {} for user: {}", updatedMedication.getId(), currentUser.getId());
        return mapMedicationToResponse(updatedMedication);
    }

    @Override
    @Transactional
    @CacheEvict(value = "medicationReminders", allEntries = true)
    public void deleteMedication(Long medicationId) {
        User currentUser = getCurrentAuthenticatedUser();
        Medication medication = medicationRepository.findByIdAndUserId(medicationId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Thuốc không tồn tại"));

        medicationRepository.delete(medication);
        log.info("Deleted medication with ID: {} for user: {}", medicationId, currentUser.getId());
    }

    @Override
    @Transactional
    public MedicationResponse consumeMedication(Long medicationId) {
        User currentUser = getCurrentAuthenticatedUser();
        Medication medication = medicationRepository.findByIdAndUserId(medicationId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Thuốc không tồn tại"));

        LocalDate today = LocalDate.now();
        
        // Validate: medication must be within start and end date
        if (today.isBefore(medication.getStartDate())) {
            throw new ClientErrorException("Chưa đến ngày bắt đầu uống thuốc");
        }
        
        if (today.isAfter(medication.getEndDate())) {
            throw new ClientErrorException("Thuốc đã hết hạn");
        }
        
        // Calculate remaining quantity from consumption history
        Integer totalConsumed = consumptionHistoryRepository.getTotalAmountConsumed(medicationId);
        Integer remainingQuantity = medication.getTotalQuantity() - totalConsumed;
        
        // Validate: remaining quantity must be greater than 0
        if (remainingQuantity <= 0) {
            throw new ClientErrorException("Thuốc đã hết");
        }
        
        // Validate: remaining quantity must be at least dosageAmount
        if (remainingQuantity < medication.getDosageAmount()) {
            throw new ClientErrorException("Số lượng thuốc còn lại không đủ cho một lần uống");
        }

        // Record consumption history - this is the only write operation
        ConsumptionHistory history = new ConsumptionHistory();
        history.setMedicationId(medicationId);
        history.setConsumedAt(LocalDateTime.now());
        history.setAmountConsumed(medication.getDosageAmount());
        consumptionHistoryRepository.save(history);
        
        // Calculate new remaining quantity after this consumption
        Integer newRemainingQuantity = remainingQuantity - medication.getDosageAmount();
        
        // Update isActive status if all medication consumed
        if (newRemainingQuantity == 0) {
            medication.setIsActive(false);
            medicationRepository.save(medication);
        }
        
        log.info("Consumed medication with ID: {}, remaining quantity: {}, dose consumed: {}", 
                 medicationId, newRemainingQuantity, medication.getDosageAmount());
        
        return mapMedicationToResponse(medication);
    }

    @Override
    public List<ConsumptionHistoryResponse> getConsumptionHistory(Long medicationId) {
        User currentUser = getCurrentAuthenticatedUser();
        Medication medication = medicationRepository.findByIdAndUserId(medicationId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Thuốc không tồn tại"));
        
        List<ConsumptionHistory> histories = consumptionHistoryRepository
                .findByMedicationIdOrderByConsumedAtDesc(medicationId);
        
        return histories.stream()
                .map(this::mapConsumptionHistoryToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update medication status based on current date and remaining quantity (calculated from history)
     */
    private Medication updateMedicationStatus(Medication medication) {
        LocalDate today = LocalDate.now();
        
        // Mark as inactive if endDate has passed
        if (today.isAfter(medication.getEndDate())) {
            medication.setIsActive(false);
        }
        
        // Mark as inactive if remaining quantity reaches 0 (calculated from consumption history)
        Integer totalConsumed = consumptionHistoryRepository.getTotalAmountConsumed(medication.getId());
        Integer remainingQuantity = medication.getTotalQuantity() - totalConsumed;
        if (remainingQuantity <= 0) {
            medication.setIsActive(false);
        }
        
        return medication;
    }

    /**
     * Calculate medication status: "active", "upcoming", or "expired"
     * Uses remaining quantity calculated from ConsumptionHistory
     */
    private String calculateMedicationStatus(Medication medication) {
        LocalDate today = LocalDate.now();
        
        if (today.isBefore(medication.getStartDate())) {
            return "upcoming";
        }
        
        if (today.isAfter(medication.getEndDate())) {
            return "expired";
        }
        
        // Calculate remaining from consumption history
        Integer totalConsumed = consumptionHistoryRepository.getTotalAmountConsumed(medication.getId());
        Integer remainingQuantity = medication.getTotalQuantity() - totalConsumed;
        
        if (remainingQuantity <= 0) {
            return "completed";
        }
        
        return "active";
    }

    private User getCurrentAuthenticatedUser() {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null) {
            throw new ClientErrorException("Người dùng không được xác thực");
        }
        return currentUser;
    }

    private MedicationResponse mapMedicationToResponse(Medication medication) {
        // Calculate remaining quantity from consumption history
        Integer totalConsumed = consumptionHistoryRepository.getTotalAmountConsumed(medication.getId());
        Integer remainingQuantity = medication.getTotalQuantity() - totalConsumed;
        
        MedicationResponse response = new MedicationResponse();
        response.setId(medication.getId());
        response.setName(medication.getName());
        response.setDosageAmount(medication.getDosageAmount());
        response.setDosageUnit(medication.getDosageUnit());
        response.setFrequency(medication.getFrequency());
        response.setTotalQuantity(medication.getTotalQuantity());
        response.setRemainingQuantity(Math.max(0, remainingQuantity)); // Ensure never negative
        
        // Calculate progress percentage
        if (medication.getTotalQuantity() != null && medication.getTotalQuantity() > 0) {
            double consumed = totalConsumed;
            double progress = (consumed / (double) medication.getTotalQuantity()) * 100;
            response.setProgressPercentage(Math.min(100.0, Math.max(0.0, progress)));
        } else {
            response.setProgressPercentage(0.0);
        }
        
        response.setMedicationTime1(medication.getMedicationTime1());
        response.setMedicationTime2(medication.getMedicationTime2());
        response.setMedicationTime3(medication.getMedicationTime3());
        response.setStartDate(medication.getStartDate());
        response.setEndDate(medication.getEndDate());
        response.setIsActive(medication.getIsActive());
        response.setStatus(calculateMedicationStatus(medication));
        response.setCreatedTime(medication.getCreatedTime());
        response.setUpdatedTime(medication.getUpdatedTime());
        return response;
    }

    private ConsumptionHistoryResponse mapConsumptionHistoryToResponse(ConsumptionHistory history) {
        return new ConsumptionHistoryResponse(
                history.getId(),
                history.getMedicationId(),
                history.getConsumedAt(),
                history.getAmountConsumed(),
                history.getCreatedTime()
        );
    }
    /**
     * Process medication reminder for a specific medication.
     * Uses cached FCM tokens for efficiency.
     */
    public void processMedicationReminder(Long medicationId) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Thuốc không tồn tại"));
        
        String fcmToken = userService.getFcmToken(medication.getUserId());

        if (fcmToken != null && !fcmToken.isEmpty()) {
            fcmService.sendNotification(
                    fcmToken,
                    "Lịch uống thuốc: " + medication.getName(),
                    "Đã đến giờ uống " + medication.getDosageAmount() + " " + medication.getDosageUnit() + ". Đừng quên nhé!"
            );
            log.info("Sent medication reminder for medication ID: {} to user ID: {}", medicationId, medication.getUserId());
        }
    }


    @Override
    @Cacheable(value = "medicationReminders", key = "#time.toString()")
    public List<Medication> getMedicationsToRemind(LocalTime time) {
        log.debug("Fetching medications to remind from DB for time: {}", time);
        return medicationRepository.findMedicationsToRemind(time);
    }
}

