package com.site.webapp.repo;

import com.site.webapp.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }
    @Test
    void findByEmail() {
        User user = new User();
        user.setEmail("test2@email.com");
        user.setPassword("123456");
        user.setFirstName("Тест");
        user.setLastName("Тестов");

        userRepository.save(user);

        User found = userRepository.findByEmail("test2@email.com");

        assertNotNull(found);
        assertEquals("test2@email.com",found.getEmail());
    }
}