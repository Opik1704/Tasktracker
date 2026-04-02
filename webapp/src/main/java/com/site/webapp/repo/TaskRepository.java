package com.site.webapp.repo;

import com.site.webapp.models.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends CrudRepository<Task,Long> {
    List<Task> findByArtistId(Long artistId);

    List<Task> findAllByOrderByIdAsc();
    List<Task> findAllByOrderByIdDesc();
    List<Task> findAllByOrderByDeadlineDesc();
    List<Task> findAllByOrderByPriorityAsc();
    List<Task> findByTitleContainingIgnoreCaseOrCommentContainingIgnoreCase(String title, String comment);

    List<Task> findByArtistIdOrderByIdAsc(Long artistId);
    List<Task> findByArtistIdOrderByIdDesc(Long artistId);
    List<Task> findByArtistIdOrderByDeadlineAsc(Long artistId);
    List<Task> findByArtistIdOrderByPriorityAsc(Long artistId);
    List<Task> findByArtistIdAndTitleContainingIgnoreCaseOrArtistIdAndCommentContainingIgnoreCase(Long artistId, String title, Long artistId2, String comment);

    List<Task> findAllByDeadlineBetween(LocalDateTime now, LocalDateTime twoHoursLater);
    List<Task> findAllByArtistIdAndDeadlineBetween(Long artistId,LocalDateTime start,LocalDateTime end);

}
