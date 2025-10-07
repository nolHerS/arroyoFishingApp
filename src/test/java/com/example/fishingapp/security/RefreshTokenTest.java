package com.example.fishingapp.security;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class RefreshTokenTest {

    @Test
    void prePersist_setsCreatedAtAndTokenAndRevoked_whenFieldsAreNull() {
        RefreshToken token = RefreshToken.builder()
                .authUser(new AuthUser()) // necesario por el @JoinColumn
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build(); // token y revoked son null

        token.prePersist();

        assertThat(token.getCreatedAt(), notNullValue());
        assertThat(token.getToken(), notNullValue());
        assertThat(token.getRevoked(), is(false));
    }

    @Test
    void prePersist_doesNotOverrideExistingToken() {
        String existingToken = UUID.randomUUID().toString();

        RefreshToken token = RefreshToken.builder()
                .token(existingToken)
                .authUser(new AuthUser())
                .expiryDate(LocalDateTime.now().plusDays(1))
                .revoked(true)
                .build();

        token.prePersist();

        assertThat(token.getToken(), is(existingToken)); // no lo cambia
        assertThat(token.getRevoked(), is(true)); // no lo cambia
        assertThat(token.getCreatedAt(), notNullValue());
    }

    @Test
    void prePersist_setsRevokedFalse_whenNull() {
        RefreshToken token = RefreshToken.builder()
                .authUser(new AuthUser())
                .expiryDate(LocalDateTime.now().plusDays(1))
                .token(UUID.randomUUID().toString())
                .revoked(null)
                .build();

        token.prePersist();

        assertThat(token.getRevoked(), is(false));
    }

    @Test
    void isExpired_returnsTrue_whenExpiryDateIsPast() {
        RefreshToken token = RefreshToken.builder()
                .authUser(new AuthUser())
                .expiryDate(LocalDateTime.now().minusHours(1))
                .token(UUID.randomUUID().toString())
                .revoked(false)
                .build();

        assertThat(token.isExpired(), is(true));
    }

    @Test
    void isExpired_returnsFalse_whenExpiryDateIsFuture() {
        RefreshToken token = RefreshToken.builder()
                .authUser(new AuthUser())
                .expiryDate(LocalDateTime.now().plusHours(1))
                .token(UUID.randomUUID().toString())
                .revoked(false)
                .build();

        assertThat(token.isExpired(), is(false));
    }
}
