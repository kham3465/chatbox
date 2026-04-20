package com.vn.nhom2.service;

import com.vn.nhom2.entity.MedicationNotification;

import java.util.List;

public interface MedicationNotificationService {
    // Lấy tất cả thông báo của một user
    List<MedicationNotification> getAllNotifications(Long userId);

    // Lấy tất cả thông báo chưa đọc của một user
    List<MedicationNotification> getUnreadNotifications(Long userId);

    // Lấy chi tiết một thông báo theo ID
    MedicationNotification getNotificationById(Long notificationId);

    // Đánh dấu một thông báo là đã đọc
    void markAsRead(Long notificationId);

    // Đánh dấu tất cả thông báo của user là đã đọc
    void markAllAsRead(Long userId);

    // Xóa một thông báo theo ID
    void deleteNotificationById(Long notificationId);

    // Xóa tất cả thông báo của một user
    void deleteAllNotifications(Long userId);
}
