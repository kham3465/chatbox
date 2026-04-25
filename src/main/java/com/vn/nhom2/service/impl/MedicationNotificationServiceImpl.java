package com.vn.nhom2.service.impl;

import com.vn.nhom2.entity.MedicationNotification;
import com.vn.nhom2.exception.ResourceNotFoundException;
import com.vn.nhom2.repo.MedicationNotificationRepository;
import com.vn.nhom2.service.MedicationNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationNotificationServiceImpl implements MedicationNotificationService {

    private final MedicationNotificationRepository notificationRepository;

    @Autowired
    public MedicationNotificationServiceImpl(MedicationNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<MedicationNotification> getAllNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderBySentTimeDesc(userId);
    }

    @Override
    public List<MedicationNotification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    @Override
    public MedicationNotification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    }

    @Override
    public void markAsRead(Long notificationId) {
        MedicationNotification notification = getNotificationById(notificationId);
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<MedicationNotification> notifications = getUnreadNotifications(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    public void deleteNotificationById(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public void deleteAllNotifications(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }
}
