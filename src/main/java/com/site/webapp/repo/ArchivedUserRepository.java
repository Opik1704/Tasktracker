package com.site.webapp.repo;

import com.site.webapp.models.ArchivedUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchivedUserRepository extends JpaRepository<ArchivedUser, Long> {
}
