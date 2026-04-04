package com.vn.nhom2.controller;

import com.vn.nhom2.dto.request.MedicationRequest;
import com.vn.nhom2.dto.response.ConsumptionHistoryResponse;
import com.vn.nhom2.dto.response.MedicationResponse;
import com.vn.nhom2.service.MedicationService;
import com.vn.nhom2.util.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medications")
@RequiredArgsConstructor
@Tag(name = "Medication Management", description = "Medication management endpoints for health tracking")
@SecurityRequirement(name = "Bearer Authentication")
public class MedicationController {
    private final MedicationService medicationService;

    /**
     * Get all active medications for the current user
     * Requires authentication
     *
     * @return list of active medications
     */
    @GetMapping
    @Operation(summary = "Get all medications", description = "Retrieve all active medications for the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medications retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User not authenticated")
    })
    public ResponseEntity<StandardResponse> getAllMedications() {
        List<MedicationResponse> medications = medicationService.getAllMedications();
        return ResponseEntity.ok(new StandardResponse("200", "Thành công", medications));
    }

    /**
     * Get a specific medication by ID
     * Requires authentication
     *
     * @param medicationId the medication ID
     * @return medication details
     */
    @GetMapping("/{medicationId}")
    @Operation(summary = "Get medication by ID", description = "Retrieve a specific medication by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medication retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Medication not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User not authenticated")
    })
    public ResponseEntity<StandardResponse> getMedicationById(@PathVariable Long medicationId) {
        MedicationResponse medication = medicationService.getMedicationById(medicationId);
        return ResponseEntity.ok(new StandardResponse("200", "Thành công", medication));
    }

    /**
     * Create a new medication
     * Requires authentication
     *
     * @param request medication details
     * @return created medication
     */
    @PostMapping
    @Operation(summary = "Create new medication", description = "Add a new medication to the current user's medication list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Medication created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid medication data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StandardResponse> createMedication(@Valid @RequestBody MedicationRequest request) {
        MedicationResponse medication = medicationService.createMedication(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new StandardResponse("201", "Thuốc đã được thêm thành công", medication));
    }

    /**
     * Update an existing medication
     * Requires authentication
     *
     * @param medicationId the medication ID
     * @param request      updated medication details
     * @return updated medication
     */
    @PutMapping("/{medicationId}")
    @Operation(summary = "Update medication", description = "Update details of an existing medication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medication updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid medication data"),
            @ApiResponse(responseCode = "404", description = "Medication not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StandardResponse> updateMedication(
            @PathVariable Long medicationId,
            @Valid @RequestBody MedicationRequest request) {
        MedicationResponse medication = medicationService.updateMedication(medicationId, request);
        return ResponseEntity.ok(new StandardResponse("200", "Thuốc đã được cập nhật thành công", medication));
    }

    /**
     * Delete a medication
     * Requires authentication
     * Deletion requires confirmation from frontend
     *
     * @param medicationId the medication ID
     */
    @DeleteMapping("/{medicationId}")
    @Operation(summary = "Delete medication", description = "Remove a medication from the user's medication list (with confirmation)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medication deleted successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Medication not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StandardResponse> deleteMedication(@PathVariable Long medicationId) {
        medicationService.deleteMedication(medicationId);
        return ResponseEntity.ok(new StandardResponse("200", "Thuốc đã được xóa thành công", null));
    }

    /**
     * Consume medication (mark as taken)
     * Decrements the remaining quantity by dosageAmount and creates a consumption
     * history record
     * Can be called when user takes the medication at any of the scheduled times
     * Requires authentication
     *
     * @param medicationId the medication ID
     * @return updated medication with decremented remaining quantity and progress
     *         percentage
     */
    @PostMapping("/{medicationId}/consume")
    @Operation(summary = "Consume medication", description = "Record that medication has been consumed at one of the scheduled times. Decrements remaining quantity by dosageAmount and records timestamp. When remaining quantity reaches 0, medication is marked as completed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medication consumption recorded successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Medication not found"),
            @ApiResponse(responseCode = "400", description = "Invalid state - medication expired, not started, or insufficient remaining quantity"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StandardResponse> consumeMedication(@PathVariable Long medicationId) {
        MedicationResponse medication = medicationService.consumeMedication(medicationId);
        return ResponseEntity.ok(new StandardResponse("200", "Bạn đã uống thuốc xong", medication));
    }

    /**
     * Get consumption history for a medication
     * Retrieves all recorded dose consumptions with timestamps
     * Requires authentication
     *
     * @param medicationId the medication ID
     * @return list of consumption history records
     */
    @GetMapping("/{medicationId}/consumption-history")
    @Operation(summary = "Get consumption history", description = "Retrieve all consumption records for a specific medication, ordered by most recent first")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consumption history retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "404", description = "Medication not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User not authenticated")
    })
    public ResponseEntity<StandardResponse> getConsumptionHistory(@PathVariable Long medicationId) {
        List<ConsumptionHistoryResponse> history = medicationService.getConsumptionHistory(medicationId);
        return ResponseEntity.ok(new StandardResponse("200", "Thành công", history));
    }
}
