package com.kouetcha.repository.tasksmanager;

import com.kouetcha.model.tasksmanager.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findAllByRecepteurIdAndSeenFalseOrderByDateDesc(Long userId);

    Page<Notification> findAllByRecepteurIdAndSeenFalseOrderByDateDesc(Long userId, Pageable pageable);

    List<Notification> findAllByRecepteurIdOrderByDateDesc(Long userId);
    Page<Notification> findAllByRecepteurIdOrderByDateDesc(Long userId, Pageable pageable);

    long countByRecepteurIdAndSeenFalse(Long userId);
}
