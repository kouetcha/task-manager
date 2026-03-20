package com.kouetcha.service.tasksmanager;

import com.kouetcha.dto.tasksmanager.NotificationDto;
import com.kouetcha.model.tasksmanager.Notification;
import com.kouetcha.repository.tasksmanager.NotificationRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Repository
public class NotificationServiceImpl implements NotificationService{
    private final NotificationRepository notificationRepository;

    @Override
    public Notification create(@Valid NotificationDto dto){
        Notification notification=new Notification();

        notification
                    .setMessage(dto.getMessage())
                    .setEvent(dto.getEvent())
                    .setType(dto.getType())
                    .setParentId(dto.getParentId())
                    .setEmetteur(dto.getEmetteur())
                    .setDate(new Date())
                    .setRecepteur(dto.getReceveur());

        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getNotification(Long userId) {
         return notificationRepository.findAllByRecepteurIdOrderByDateDesc(userId);

    }
    @Override
    public Page<Notification> findNotification(Long userId, Pageable pageable) {

        return notificationRepository.findAllByRecepteurIdOrderByDateDesc(userId,pageable);
    }
    @Override
    public List<Notification> findNotificationNoSeen(Long userId) {
        List<Notification> notifications =
                notificationRepository.findAllByRecepteurIdAndSeenFalseOrderByDateDesc(userId);

        notifications.forEach(notification -> notification.setSeen(true));
        notificationRepository.saveAll(notifications);

        return notifications;
    }
    @Override
    public long countNotificationNoSeen(Long userId){
        return notificationRepository.countByRecepteurIdAndSeenFalse(userId);

    }
    @Override
    public Page<Notification> findNotificationNoSeen(Long userId, Pageable pageable) {
        Page<Notification> page =
                notificationRepository.findAllByRecepteurIdAndSeenFalseOrderByDateDesc(userId, pageable);

        List<Notification> notifications = page.getContent();

        notifications.forEach(notification -> notification.setSeen(true));
        notificationRepository.saveAll(notifications);

        return page;
    }

}
