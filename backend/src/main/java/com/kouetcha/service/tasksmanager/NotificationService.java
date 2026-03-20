package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.NotificationDto;
import com.kouetcha.model.tasksmanager.Notification;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    Notification create(@Valid NotificationDto dto);

    List<Notification> getNotification(Long userId);

    Page<Notification> findNotification(Long userId, Pageable pageable);

    void markAsSeen(List<Long> ids);

    void deletes(List<Long> ids);

    void markAllAsSeen();

    List<Notification> findNotificationNoSeen(Long userId);

    long countNotificationNoSeen(Long userId);

    Page<Notification> findNotificationNoSeen(Long userId, Pageable pageable);
}
