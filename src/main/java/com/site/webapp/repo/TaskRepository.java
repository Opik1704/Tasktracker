package com.site.webapp.repo;

import com.site.webapp.models.Tag;
import com.site.webapp.models.Task;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAll(Sort sort);
    List<Task> findByArtistId(Long artistId, Sort sort);

    List<Task> findByTitleContainingIgnoreCaseOrCommentContainingIgnoreCase(String title, String comment);
    List<Task> findByArtistIdAndTitleContainingIgnoreCaseOrArtistIdAndCommentContainingIgnoreCase(Long artistId, String title, Long artistId2, String comment);

    List<Task> findAllByDeadlineBetween(LocalDateTime now, LocalDateTime twoHoursLater);
    List<Task> findAllByArtistIdAndDeadlineBetween(Long artistId,LocalDateTime start,LocalDateTime end);

    List<Task> findAllByOwnerIdAndDeletedFalse(Long ownerId);
    List<Task> findAllByOwnerIdAndDeletedTrue(Long ownerId);

    Optional<Task> findByIdAndOwnerIdAndDeletedTrue(Long id, Long ownerId);

    Optional<Task> findByIdAndDeletedTrue(Long id);

    List<Task> findAllByDeletedTrueAndDeletedAtBefore(LocalDateTime thresholdDate);

    void deleteByIdAndDeletedTrue(Long id);

    long countByArtistIdAndStatusNot(Long artistId, Task.TaskStatus status);

    // === Stage 2: Statistics repository methods ===

    long countByOwnerIdAndStatusAndDeletedFalse(Long ownerId, Task.TaskStatus status);

    List<Task> findByOwnerIdAndStatusAndDeletedFalseAndCompletedAtIsNotNullOrderByCompletedAtAsc(Long ownerId, Task.TaskStatus status);

    @Query(nativeQuery = true, value = "SELECT * FROM tasks WHERE owner_id = :ownerId AND status = :status AND deleted = false AND completed_at IS NOT NULL ORDER BY (completed_at - created_at) ASC LIMIT 1")
    Task findFastestCompletedTaskByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") String status);

    default Task findFastestCompletedTaskByOwnerIdAndStatus(Long ownerId, Task.TaskStatus status) {
        return findFastestCompletedTaskByOwnerIdAndStatus(ownerId, status.name());
    }

    @Query(nativeQuery = true, value = "SELECT * FROM tasks WHERE owner_id = :ownerId AND status = :status AND deleted = false AND completed_at IS NOT NULL AND completed_at >= :start AND completed_at < :end")
    List<Task> findCompletedByOwnerIdAndStatusAndPeriod(@Param("ownerId") Long ownerId, @Param("status") String status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    default List<Task> findCompletedByOwnerIdAndStatusAndPeriod(Long ownerId, Task.TaskStatus status, LocalDateTime start, LocalDateTime end) {
        return findCompletedByOwnerIdAndStatusAndPeriod(ownerId, status.name(), start, end);
    }

    long countByOwnerIdAndStatusAndDeletedFalseAndCompletedAtIsNull(Long ownerId, Task.TaskStatus status);

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM tasks WHERE owner_id = :ownerId AND status = :status AND deleted = false AND completed_at IS NOT NULL AND deadline IS NOT NULL AND completed_at <= deadline")
    long countOnTimeByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") String status);

    default long countOnTimeByOwnerIdAndStatus(Long ownerId, Task.TaskStatus status) {
        return countOnTimeByOwnerIdAndStatus(ownerId, status.name());
    }

    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM tasks WHERE owner_id = :ownerId AND status = :status AND deleted = false AND completed_at IS NOT NULL AND deadline IS NOT NULL")
    long countCompletedWithDeadlineByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") String status);

    default long countCompletedWithDeadlineByOwnerIdAndStatus(Long ownerId, Task.TaskStatus status) {
        return countCompletedWithDeadlineByOwnerIdAndStatus(ownerId, status.name());
    }

    @Query(nativeQuery = true, value = "SELECT COALESCE(AVG(EXTRACT(EPOCH FROM (completed_at - created_at)) / 3600), 0) FROM tasks WHERE owner_id = :ownerId AND status = :status AND deleted = false AND completed_at IS NOT NULL")
    Double averageCompletionHoursByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") String status);

    default Double averageCompletionHoursByOwnerIdAndStatus(Long ownerId, Task.TaskStatus status) {
        return averageCompletionHoursByOwnerIdAndStatus(ownerId, status.name());
    }

    // === Tag filtering methods ===

    @Query("SELECT DISTINCT t FROM Task t JOIN t.tags tag WHERE tag IN :tags")
    List<Task> findByTagsIn(Collection<Tag> tags);

    @Query("SELECT DISTINCT t FROM Task t JOIN t.tags tag WHERE tag.id IN :tagIds GROUP BY t.id HAVING COUNT(DISTINCT tag.id) = :tagCount")
    List<Task> findByTagsContainsAll(@Param("tagIds") List<Long> tagIds, @Param("tagCount") long tagCount);
}