package com.example.fishingapp.service.impl;

import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.security.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "jwt.expiration=3600000",
        "jwt.refresh-token.expiration=604800000"
})
public class JwtServiceImplTest {

    @Autowired
    private JwtService jwtService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {

        userDetails = AuthUser.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .password("password")
                .role(Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .build();
    }

    @Test
    void generateToken_withExtraClaims_returnsValidToken() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", 1L);
        extraClaims.put("role", "USER");

        String token = jwtService.generateToken(extraClaims, userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateToken_withoutExtraClaims_returnsValidToken() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateRefreshToken_returnsValidToken() {
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals(userDetails.getUsername(), username);
    }

    @Test
    void extractClaim_returnsCorrectClaim() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", 1L);

        String token = jwtService.generateToken(extraClaims, userDetails);

        String subject = jwtService.extractClaim(token, Claims::getSubject);

        assertEquals(userDetails.getUsername(), subject);
    }
    @Test
    void isTokenValid_withValidToken_returnsTrue() {
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_withDifferentUser_returnsFalse() {
        String token = jwtService.generateToken(userDetails);

        AuthUser differentUser = AuthUser.builder()
                .id(2L)
                .email("different@example.com")
                .username("differentuser")
                .password("password")
                .role(Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        boolean isValid = jwtService.isTokenValid(token, differentUser);

        assertFalse(isValid);
    }

    @Test
    void getExpirationTime_returnsCorrectValue() {
        long expirationTime = jwtService.getExpirationTime();

        assertEquals(3600, expirationTime); // 3600000ms = 3600s
    }

    @Test
    void token_containsIssuedAtAndExpiration() {
        String token = jwtService.generateToken(userDetails);

        assertDoesNotThrow(() -> {
            jwtService.extractClaim(token, Claims::getIssuedAt);
            jwtService.extractClaim(token, Claims::getExpiration);
        });
    }
}
