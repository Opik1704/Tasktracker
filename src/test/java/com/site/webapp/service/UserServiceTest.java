package com.site.webapp.service;

import com.site.webapp.models.User;
import com.site.webapp.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void saveRegisteredUser() {
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setPassword("password");
        user.setFirstName("Имя");
        user.setLastName("Фамилия");
        User result = userService.saveRegisteredUser(user);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(userRepository.findByEmail("test@mail.ru").isPresent());
    }

    @Test
    void findUserById() {
        User user = new User();
        user.setEmail("find@mail.com");
        user.setPassword("password");
        user.setFirstName("Имя");
        user.setLastName("Фамилия");
        User saved = userService.saveRegisteredUser(user);
        Long id = saved.getId();

        User found = userService.findUserById(id);

        assertNotNull(found);
        assertEquals("find@mail.com", found.getEmail());
    }

    @Test
    void findByEmail() {
        User user = new User();
        user.setEmail("search@test.com");
        user.setPassword("123456");
        user.setFirstName("Имя");
        user.setLastName("Фамилия");
        userService.saveRegisteredUser(user);

        User found = userService.findByEmail("search@test.com");

        assertNotNull(found);
        assertEquals("search@test.com", found.getEmail());
    }

    @Test
    void saveRegisteredUserDuplicateEmail() {
        User user1 = new User();
        user1.setEmail("duplicate@mail.com");
        user1.setPassword("123456");
        user1.setFirstName("Имя");
        user1.setLastName("Фамилия");
        userService.saveRegisteredUser(user1);

        User user2 = new User();
        user2.setEmail("duplicate@mail.com");
        user2.setPassword("123456");
        user2.setFirstName("Имя");
        user2.setLastName("Фамилия");

        assertThrows(DataIntegrityViolationException.class, () -> userService.saveRegisteredUser(user2));
    }
//    @Test
//    void deleteUser() {
//        User user = new User();
//        user.setEmail("delete@mail.com");
//        user.setPassword("123456");
//        user.setFirstName("Имя");
//        user.setLastName("Фамилия");
//        userService.saveUser(user);
//        Long id = user.getId();
//        boolean deleted = userService.deleteUser(id);
//
//        assertTrue(deleted);
//        assertNull(userService.findUserById(id));
//    }
}