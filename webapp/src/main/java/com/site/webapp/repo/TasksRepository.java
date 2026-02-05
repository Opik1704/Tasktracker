package com.site.webapp.repo;

import com.site.webapp.models.Tasks;
import org.springframework.data.repository.CrudRepository;

public interface TasksRepository extends CrudRepository<Tasks,Long> {
}
