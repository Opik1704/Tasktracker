package com.site.webapp.repo;

import com.site.webapp.models.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment,Long> {
    List<TaskAttachment> findByTaskId(Long taskId);
}
