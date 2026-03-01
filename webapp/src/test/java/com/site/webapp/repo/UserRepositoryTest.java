package com.site.webapp.repo;

import com.site.webapp.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail() {
        User user = new User();
        user.setEmail("test@email.com");
        userRepository.save(user);

        User found = userRepository.findByEmail("test@email.com");
        assertNotNull(found);
        assertEquals("test@email.com",found.getEmail());
    }
}