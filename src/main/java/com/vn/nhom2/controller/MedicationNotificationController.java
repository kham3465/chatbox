package com.vn.nhom2.controller;

import com.vn.nhom2.entity.MedicationNotification;
import com.vn.nhom2.service.MedicationNotificationService;
import com.vn.nhom2.service.UserService;
import com.vn.nhom2.util.StandardResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Medication Notification API")
@SecurityRequirement(name = "Bearer Authentication")
public class MedicationNotificationController {

    private final MedicationNotificationService notificationService;
    private final UserService userService;

    @GetMapping("/all")
    public ResponseEntity<Object> getAllNotifications() {
        Long userId = userService.getCurrentUserProfile().getId();
        List<MedicationNotification> notifications = notificationService.getAllNotifications(userId);
        return new ResponseEntity<>(new StandardResponse("200", "Done", notifications), HttpStatus.OK);
    }

    @GetMapping("/unread")
    public ResponseEntity<Object> getUnreadNotifications() {
        Long userId = userService.getCurrentUserProfile().getId();
        List<MedicationNotification> notifications = notificationService.getUnreadNotifications(userId);
        return new ResponseEntity<>(new StandardResponse("200", "Done", notifications), HttpStatus.OK);
    }

    @PutMapping("/mark_read/{id}")
    public ResponseEntity<Object> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return new ResponseEntity<>(new StandardResponse("200", "Marked as read", null), HttpStatus.OK);
    }

    @PutMapping("/mark_all_read")
    public ResponseEntity<Object> markAllAsRead() {
        Long userId = userService.getCurrentUserProfile().getId();
        notificationService.markAllAsRead(userId);
        return new ResponseEntity<>(new StandardResponse("200", "All marked as read", null), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotificationById(id);
        return new ResponseEntity<>(new StandardResponse("200", "Deleted", null), HttpStatus.OK);
    }

    @DeleteMapping("/delete_all")
    public ResponseEntity<Object> deleteAllNotifications() {
        Long userId = userService.getCurrentUserProfile().getId();
        notificationService.deleteAllNotifications(userId);
        return new ResponseEntity<>(new StandardResponse("200", "All deleted", null), HttpStatus.OK);
    }
}
