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
}
