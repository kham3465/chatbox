package com.vn.nhom2.service;

import com.vn.nhom2.dto.request.MedicationRequest;
import com.vn.nhom2.dto.response.ConsumptionHistoryResponse;
import com.vn.nhom2.dto.response.MedicationResponse;

import java.util.List;

public interface MedicationService {
    /**
     * Get all active medications for the current user
     *
     * @return list of active medication responses
     */
    List<MedicationResponse> getAllMedications();

    /**
     * Get a specific medication by ID
     *
     * @param medicationId the medication ID
     * @return medication response
     */
    MedicationResponse getMedicationById(Long medicationId);

    /**
     * Create a new medication
     *
     * @param request the medication request
     * @return created medication response
     */
    MedicationResponse createMedication(MedicationRequest request);

    /**
     * Update an existing medication
     *
     * @param medicationId the medication ID
     * @param request the medication request
     * @return updated medication response
     */
    MedicationResponse updateMedication(Long medicationId, MedicationRequest request);

    /**
     * Delete a medication
     *
     * @param medicationId the medication ID
     */
    void deleteMedication(Long medicationId);

    /**
     * Consume medication (decrement quantity by dosageAmount)
     * Creates a ConsumptionHistory record with timestamp
     * When quantity reaches 0, the medication is marked as completed
     *
     * @param medicationId the medication ID
     * @return updated medication response
     */
    MedicationResponse consumeMedication(Long medicationId);

    /**
     * Get consumption history for a medication
     *
     * @param medicationId the medication ID
     * @return list of consumption history records
     */
    List<ConsumptionHistoryResponse> getConsumptionHistory(Long medicationId);
}
