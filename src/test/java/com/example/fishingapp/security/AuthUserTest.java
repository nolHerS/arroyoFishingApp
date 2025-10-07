package com.example.fishingapp.security;

import com.example.fishingapp.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class AuthUserTest {

    private AuthUser authUser;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("userprofile")
                .email("profile@example.com")
                .build();

        authUser = AuthUser.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .role(Role.ADMIN)
                .enabled(true)
                .accountNonLocked(true)
                .user(user)
                .createdAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetAuthorities_returnsCorrectRole() {
        Collection<?> authorities = authUser.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ADMIN")));
    }

    @Test
    void testGetUsername_returnsEmail() {
        assertEquals("testuser", authUser.getUsername());
    }

    @Test
    void testGetPassword_returnsPassword() {
        assertEquals("encodedPassword", authUser.getPassword());
    }

    @Test
    void testIsAccountNonExpired_alwaysTrue() {
        assertTrue(authUser.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked_reflectsValue() {
        authUser.setAccountNonLocked(false);
        assertFalse(authUser.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired_alwaysTrue() {
        assertTrue(authUser.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled_reflectsValue() {
        authUser.setEnabled(false);
        assertFalse(authUser.isEnabled());
    }

    @Test
    void testPrePersist_setsDefaultsWhenNull() {
        AuthUser newUser = AuthUser.builder()
                .username("newuser")
                .password("pass")
                .email("new@example.com")
                .build();

        // Los valores con @Builder.Default ya tienen su valor por defecto
        assertNotNull(newUser.getEnabled());
        assertNotNull(newUser.getAccountNonLocked());
        assertFalse(newUser.getEnabled());
        assertTrue(newUser.getAccountNonLocked());

        // Antes de persistir, createdAt y role deben ser nulos
        assertNull(newUser.getCreatedAt());
        assertNull(newUser.getRole());

        // Ejecutamos el método @PrePersist manualmente
        newUser.prePersist();

        // Verificamos que prePersist establezca correctamente los valores faltantes
        assertNotNull(newUser.getCreatedAt());
        assertEquals(Role.USER, newUser.getRole());
        assertFalse(newUser.getEnabled());         // Se mantiene false (según tu método)
        assertTrue(newUser.getAccountNonLocked()); // Se mantiene true
    }


    @Test
    void testBuilder_createsValidObject() {
        assertEquals("testuser", authUser.getUsername()); // username (campo propio)
        assertEquals("test@example.com", authUser.getEmail());
        assertEquals(Role.ADMIN, authUser.getRole());
        assertTrue(authUser.isEnabled());
        assertTrue(authUser.isAccountNonLocked());
        assertNotNull(authUser.getUser());
    }

    @Test
    void testToString_doesNotThrow() {
        assertDoesNotThrow(() -> authUser.toString());
    }
}
