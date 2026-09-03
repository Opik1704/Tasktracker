package com.site.webapp.repo;

import com.site.webapp.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

    List<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadFalse(Long userId);

    List<Notification> findAllByUserIdAndReadFalse(Long userId);
    void deleteAllByTaskId(Long taskId);
}
