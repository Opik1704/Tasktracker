package com.site.webapp.repo;

import com.site.webapp.models.Task;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAll(Sort sort);
    List<Task> findByArtistId(Long artistId, Sort sort);

    List<Task> findByTitleContainingIgnoreCaseOrCommentContainingIgnoreCase(String title, String comment);
    List<Task> findByArtistIdAndTitleContainingIgnoreCaseOrArtistIdAndCommentContainingIgnoreCase(Long artistId, String title, Long artistId2, String comment);

    List<Task> findAllByDeadlineBetween(LocalDateTime now, LocalDateTime twoHoursLater);
    List<Task> findAllByArtistIdAndDeadlineBetween(Long artistId,LocalDateTime start,LocalDateTime end);
}
