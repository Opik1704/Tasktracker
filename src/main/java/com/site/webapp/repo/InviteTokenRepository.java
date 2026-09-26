package com.site.webapp.repo;

import com.site.webapp.models.InviteToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InviteTokenRepository extends JpaRepository<InviteToken, Long> {

    Optional<InviteToken> findByToken(String token);

    int deleteAllByExpiresAtBeforeAndUsedAtIsNull(LocalDateTime now);

    boolean existsByEmail(String email);
}
