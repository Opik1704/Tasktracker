package com.site.webapp.repo;

import com.site.webapp.models.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    User findByEmail(String email);

    @EntityGraph(attributePaths = {"favouriteTasks"})
    Optional<User> findWithFavouriteTasksByEmail(String email);

}
