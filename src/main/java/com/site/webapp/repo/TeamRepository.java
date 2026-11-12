package com.site.webapp.repo;

import com.site.webapp.models.Team;
import com.site.webapp.models.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findByOwnerId(Long ownerId);

    List<Team> findByMembersContaining(User user);

    Optional<Team> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByName(String name);

    @EntityGraph(attributePaths = "members")
    Optional<Team> findWithMembersById(Long id);
}
