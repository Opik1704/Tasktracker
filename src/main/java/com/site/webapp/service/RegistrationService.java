package com.site.webapp.service;

import com.site.webapp.dto.AcceptInviteDto;
import com.site.webapp.dto.RegistrationDto;
import com.site.webapp.exception.InvalidInviteTokenException;
import com.site.webapp.exception.RoleNotFoundException;
import com.site.webapp.exception.UserAlreadyExistsException;
import com.site.webapp.models.InviteToken;
import com.site.webapp.models.Role;
import com.site.webapp.models.User;
import com.site.webapp.repo.InviteTokenRepository;
import com.site.webapp.repo.RoleRepository;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.security.SecurityRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);


    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InviteTokenRepository inviteTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserService userService,
                               UserRepository userRepository,
                               RoleRepository roleRepository,
                               InviteTokenRepository inviteTokenRepository,
                               PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.inviteTokenRepository = inviteTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public User registerStandartUser(RegistrationDto registrationDto){

        if(registrationDto == null){
            throw new IllegalArgumentException("RegistrationDto cannot be null");
        }

        log.debug("Попытка стандартной регистрации пользователя с email: {}", registrationDto.getEmail());

        if(userRepository.existsByEmail(registrationDto.getEmail())){
            throw new UserAlreadyExistsException("User with email " + registrationDto.getEmail() + " already exists");
        }

        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());

        Role employeeRole = roleRepository.findByName(SecurityRoles.EMPLOYEE)
                .orElseThrow(() -> new RoleNotFoundException("EMPLOYEE role not found"));
        user.setRoles(Set.of(employeeRole));

        User savedUser = userService.saveRegisteredUser(user);
        log.info("Успешно зарегистрирован новый стандартный пользователь ID {} ({})", savedUser.getId(), savedUser.getEmail());

        return savedUser;
    }


    @Transactional
    public User registerOAuth2User(OAuth2User oAuth2User, String provider){
        String email = oAuth2User.getAttribute("email");

        if (email == null || email.isBlank()) {
            log.error("Ошибка авторизации через OAuth2 ({}): провайдер не предоставил email", provider);
            throw new IllegalArgumentException("OAuth2 provider did not return an email");
        }

        log.debug("Обработка OAuth2 входа/регистрации через {} для email: {}", provider, email);

        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            log.info("Пользователь ID {} ({}) успешно авторизован через OAuth2 ({})", user.getId(), email, provider);
            return user;
        }

        User user = new User();
        user.setEmail(email);
        user.setFirstName(oAuth2User.getAttribute("given_name"));
        user.setLastName(oAuth2User.getAttribute("family_name"));

        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        Role employeeRole = roleRepository.findByName(SecurityRoles.EMPLOYEE)
                .orElseThrow(() -> new RoleNotFoundException("EMPLOYEE role not found"));
        user.setRoles(Set.of(employeeRole));

        User savedUser = userRepository.save(user);
        log.info("Успешно создан новый аккаунт через OAuth2 ({}) ID {} ({})", provider, savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }


    @Transactional
    public User registerByInviteToken(String tokenValue, AcceptInviteDto registrationDto) {

        InviteToken invite = inviteTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> {
                    log.warn("Попытка регистрации по несуществующему токену: {}", tokenValue);
                    return new InvalidInviteTokenException("Недействительный или истекший токен");
                });

        if (invite.isUsed()) {
            log.warn("Попытка повторного использования токена {} для email {}", tokenValue, invite.getEmail());
            throw new InvalidInviteTokenException("Токен приглашения уже был использован");
        }

        if (invite.isExpired()) {
            log.warn("Попытка использования просроченного токена {} (истек {})", tokenValue, invite.getExpiresAt());
            throw new InvalidInviteTokenException("Срок действия токена приглашения истек");
        }

        if (userRepository.existsByEmail(invite.getEmail())) {
            log.warn("Попытка регистрации по инвайту на уже существующий email: {}", invite.getEmail());
            throw new UserAlreadyExistsException("Пользователь с email " + invite.getEmail() + " уже существует");
        }

        User user = new User();
        user.setEmail(invite.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());

        Role defaultRole = roleRepository.findByName(SecurityRoles.EMPLOYEE)
                .orElseThrow(() -> new RoleNotFoundException("EMPLOYEE role not found"));

        Role roleToAssign = invite.getRole() != null ? invite.getRole() : defaultRole;

        user.setRoles(Set.of(roleToAssign));

        User savedUser = userService.saveRegisteredUser(user);

        invite.setUsedAt(LocalDateTime.now());
        log.info("Пользователь ID {} ({}) успешно зарегистрирован по токену инвайта", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }
}
