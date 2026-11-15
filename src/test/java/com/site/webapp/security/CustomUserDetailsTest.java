package com.site.webapp.security;

import com.site.webapp.models.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    @Test
    void activeUser_deletedFalse_isEnabledTrue() {
        User user = new User();
        user.setDeleted(false);

        CustomUserDetails details = new CustomUserDetails(user);

        assertTrue(details.isEnabled(), "isEnabled() should be true for active user (deleted=false)");
    }

    @Test
    void softDeletedUser_deletedFalse_isEnabledFalse() {
        User user = new User();
        user.setDeleted(true);

        CustomUserDetails details = new CustomUserDetails(user);

        assertFalse(details.isEnabled(), "isEnabled() should be false for soft-deleted user (deleted=true)");
    }

    @Test
    void getUsername_returnsEmail() {
        User user = new User();
        user.setEmail("test@example.com");

        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals("test@example.com", details.getUsername(), "getUsername() should return the user's email");
    }

    @Test
    void getPassword_returnsPassword() {
        User user = new User();
        user.setPassword("secret123");

        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals("secret123", details.getPassword(), "getPassword() should return the user's password");
    }

    @Test
    void getId_returnsId() {
        User user = new User();
        user.setId(1L);

        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals(1L, details.getId(), "getId() should return the user's id");
    }

    @Test
    void getFullName_returnsFullName_whenFirstAndLastSet() {
        User user = new User();
        user.setFirstName("Ivan");
        user.setLastName("Ivanov");

        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals("Ivan Ivanov", details.getFullName(), "getFullName() should return 'First Last' when both are set");
    }

    @Test
    void getFullName_returnsEmail_whenNoName() {
        User user = new User();

        CustomUserDetails details = new CustomUserDetails(user);

        assertEquals(user.getEmail(), details.getFullName(), "getFullName() should return email when no first/last name set");
    }

    @Test
    void isAccountNonLocked_returnsTrue() {
        User user = new User();
        CustomUserDetails details = new CustomUserDetails(user);

        assertTrue(details.isAccountNonLocked(), "isAccountNonLocked() should always return true");
    }

    @Test
    void isAccountNonExpired_returnsTrue() {
        User user = new User();
        CustomUserDetails details = new CustomUserDetails(user);

        assertTrue(details.isAccountNonExpired(), "isAccountNonExpired() should always return true");
    }

    @Test
    void isCredentialsNonExpired_returnsTrue() {
        User user = new User();
        CustomUserDetails details = new CustomUserDetails(user);

        assertTrue(details.isCredentialsNonExpired(), "isCredentialsNonExpired() should always return true");
    }
}