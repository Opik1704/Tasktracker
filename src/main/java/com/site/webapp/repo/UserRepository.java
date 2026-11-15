package com.site.webapp.repo;

import com.site.webapp.models.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"favouriteTasks"})

    Optional<User> findWithFavouriteTasksByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Поиск пользователя по email без учёта soft delete.
     * Обходит Hibernate {@code @SQLRestriction("deleted = false")}, чтобы
     * отличать удалённые/архивированные аккаунты от отсутствующих.
     * Используется нативный запрос, т.к. JPQL наследует {@code @SQLRestriction}.
     */
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailIncludingDeleted(@Param("email") String email);
}
