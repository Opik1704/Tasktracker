package com.site.webapp.repo;

import com.site.webapp.models.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

    List<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndReadFalse(Long userId);

    List<Notification> findAllByUserIdAndReadFalse(Long userId);
    void deleteAllByTaskId(Long taskId);
    long deleteAllByCreatedAtBefore(LocalDateTime threshold);

    // === Notification Center ===

    @EntityGraph(attributePaths = "task")
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "task")
    Page<Notification> findByUserIdAndReadOrderByCreatedAtDesc(Long userId, boolean read, Pageable pageable);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
