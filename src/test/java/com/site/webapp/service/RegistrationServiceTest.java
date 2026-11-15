package com.site.webapp.service;

import com.site.webapp.exception.user.UserAlreadyExistsException;
import com.site.webapp.models.User;
import com.site.webapp.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RegistrationServiceTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    private OAuth2User oauth2User(String email) {
        return new DefaultOAuth2User(
                List.of(),
                Map.of("email", email, "given_name", "Ivan", "family_name", "Ivanov"),
                "email");
    }

    @Test
    void registerOAuth2User_newUser_createsAccount() {
        User created = registrationService.registerOAuth2User(oauth2User("new-oauth@test.com"), "google");

        assertNotNull(created.getId());
        assertEquals("new-oauth@test.com", created.getEmail());
        assertTrue(userRepository.findByEmail("new-oauth@test.com").isPresent());
    }

    @Test
    void registerOAuth2User_existingActiveUser_returnsSameUser() {
        User existing = registrationService.registerOAuth2User(oauth2User("active@test.com"), "google");

        User again = registrationService.registerOAuth2User(oauth2User("active@test.com"), "google");

        assertEquals(existing.getId(), again.getId());
        assertTrue(userRepository.findByEmailIncludingDeleted("active@test.com").isPresent());
    }

    @Test
    void registerOAuth2User_softDeletedUser_throwsControlledError() {
        User created = registrationService.registerOAuth2User(oauth2User("deleted-oauth@test.com"), "google");
        created.setDeleted(true);
        userRepository.save(created);

        assertThrows(UserAlreadyExistsException.class,
                () -> registrationService.registerOAuth2User(oauth2User("deleted-oauth@test.com"), "google"));

        // Дубликат не создан: удалённый аккаунт существует ровно один (включая deleted)
        assertTrue(userRepository.findByEmailIncludingDeleted("deleted-oauth@test.com").isPresent());
        assertTrue(userRepository.findByEmail("deleted-oauth@test.com").isEmpty());
    }

    @Test
    void registerOAuth2User_activeUser_createdExactlyOnce() {
        registrationService.registerOAuth2User(oauth2User("once@test.com"), "google");

        assertEquals(1, userRepository.findByEmailIncludingDeleted("once@test.com")
                .map(u -> 1).orElse(0));
    }
}