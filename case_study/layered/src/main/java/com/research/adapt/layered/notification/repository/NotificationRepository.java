package com.research.adapt.layered.notification.repository;

import com.research.adapt.layered.notification.entity.Notification;
import com.research.adapt.layered.notification.entity.NotificationStatus;
import com.research.adapt.layered.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByStatus(NotificationStatus status);

    List<Notification> findByType(NotificationType type);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.status = :status")
    List<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.readAt IS NULL")
    List<Notification> findUnreadNotificationsByUserId(Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.readAt IS NULL")
    Long countUnreadNotificationsByUserId(Long userId);
}
