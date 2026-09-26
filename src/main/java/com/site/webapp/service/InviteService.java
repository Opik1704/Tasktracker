package com.site.webapp.service;

import com.site.webapp.dto.SendInviteDto;
import com.site.webapp.exception.EntityAlreadyExistsException;
import com.site.webapp.exception.RoleNotFoundException;
import com.site.webapp.models.InviteToken;
import com.site.webapp.models.Role;
import com.site.webapp.repo.InviteTokenRepository;
import com.site.webapp.repo.RoleRepository;
import com.site.webapp.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InviteService {
    private static final Logger log = LoggerFactory.getLogger(InviteService.class);

    private final InviteTokenRepository inviteTokenRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public InviteService(InviteTokenRepository inviteTokenRepository,
                         UserRepository userRepository,
                         RoleRepository roleRepository) {
        this.inviteTokenRepository = inviteTokenRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public InviteToken createInvite(SendInviteDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EntityAlreadyExistsException("Пользователь с email " + dto.getEmail() + " уже зарегистрирован");
        }

        Role role = roleRepository.findByName(dto.getRoleName()).orElseThrow(() -> new RoleNotFoundException("Роль " + dto.getRoleName() + " не найдена"));

        InviteToken inviteToken = InviteToken.create(dto.getEmail(), role);

        return inviteTokenRepository.save(inviteToken);
    }

    @Transactional
    public int deleteExpiredInvites(){
        int countDeletedInvites = inviteTokenRepository.deleteAllByExpiresAtBeforeAndUsedAtIsNull(LocalDateTime.now());
        log.info("Очистка инвайтов: удалено {} просроченных токенов", countDeletedInvites);
        return countDeletedInvites;
    }
}
