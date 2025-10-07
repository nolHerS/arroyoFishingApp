package com.example.fishingapp.security;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class VerificationTokenTest {

    @Test
    void prePersist_setsCreatedAtAndTokenAndUsed_whenFieldsAreNull() {
        VerificationToken token = VerificationToken.builder()
                .authUser(new AuthUser())
                .tokenType(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .build(); // token y used son null

        token.prePersist();

        assertThat(token.getCreatedAt(), notNullValue());
        assertThat(token.getToken(), notNullValue());
        assertThat(token.getUsed(), is(false));
    }

    @Test
    void prePersist_doesNotOverrideExistingTokenOrUsed() {
        String existingToken = UUID.randomUUID().toString();

        VerificationToken token = VerificationToken.builder()
                .token(existingToken)
                .authUser(new AuthUser())
                .tokenType(VerificationToken.TokenType.PASSWORD_RESET)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .used(true)
                .build();

        token.prePersist();

        assertThat(token.getToken(), is(existingToken)); // no cambia el token
        assertThat(token.getUsed(), is(true)); // no cambia el flag
        assertThat(token.getCreatedAt(), notNullValue());
    }

    @Test
    void prePersist_setsUsedFalse_whenNull() {
        VerificationToken token = VerificationToken.builder()
                .authUser(new AuthUser())
                .tokenType(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .token(UUID.randomUUID().toString())
                .used(null)
                .build();

        token.prePersist();

        assertThat(token.getUsed(), is(false));
    }

    @Test
    void isExpired_returnsTrue_whenExpiryDateIsPast() {
        VerificationToken token = VerificationToken.builder()
                .authUser(new AuthUser())
                .tokenType(VerificationToken.TokenType.PASSWORD_RESET)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .token(UUID.randomUUID().toString())
                .used(false)
                .build();

        assertThat(token.isExpired(), is(true));
    }

    @Test
    void isExpired_returnsFalse_whenExpiryDateIsFuture() {
        VerificationToken token = VerificationToken.builder()
                .authUser(new AuthUser())
                .tokenType(VerificationToken.TokenType.EMAIL_VERIFICATION)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .token(UUID.randomUUID().toString())
                .used(false)
                .build();

        assertThat(token.isExpired(), is(false));
    }
}
