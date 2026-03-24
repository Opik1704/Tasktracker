package com.site.webapp.repo;

import com.site.webapp.models.Tasks;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TasksRepository extends CrudRepository<Tasks,Long> {
    List<Tasks> findByArtistId(Long artistId);

    List<Tasks> findAllByOrderByIdAsc();
    List<Tasks> findAllByOrderByIdDesc();
    List<Tasks> findAllByOrderByDeadlineDesc();
    List<Tasks> findAllByOrderByPriorityAsc();
    List<Tasks> findByTitleContainingIgnoreCaseOrCommentContainingIgnoreCase(String title, String comment);

    List<Tasks> findByArtistIdOrderByIdAsc(Long artistId);
    List<Tasks> findByArtistIdOrderByIdDesc(Long artistId);
    List<Tasks> findByArtistIdOrderByDeadlineAsc(Long artistId);
    List<Tasks> findByArtistIdOrderByPriorityAsc(Long artistId);
    List<Tasks> findByArtistIdAndTitleContainingIgnoreCaseOrArtistIdAndCommentContainingIgnoreCase(Long artistId, String title, Long artistId2, String comment);
}
