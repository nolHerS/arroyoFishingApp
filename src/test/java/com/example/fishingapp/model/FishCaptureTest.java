package com.example.fishingapp.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class FishCaptureTest {

    @Test
    void testBuilderAndGetters() {
        User user = User.builder().id(1L).username("juanp").fullName("Juan Pérez").email("juan@example.com").build();

        FishCapture capture = FishCapture.builder()
                .id(100L)
                .captureDate(LocalDate.of(2025, 9, 29))
                .createdAt(LocalDateTime.of(2025, 9, 29, 10, 0))
                .fishType("Trucha")
                .location("Río Tajo")
                .weight(2.5f)
                .user(user)
                .build();

        assertThat(capture.getId(), is(100L));
        assertThat(capture.getFishType(), is("Trucha"));
        assertThat(capture.getUser(), is(user));
    }

    @Test
    void testEqualsAndHashCode() {
        User user = User.builder().id(1L).username("juanp").fullName("Juan Pérez").email("juan@example.com").build();

        FishCapture capture1 = FishCapture.builder()
                .id(100L).captureDate(LocalDate.now()).fishType("Trucha").weight(2.5f).user(user).build();
        FishCapture capture2 = FishCapture.builder()
                .id(100L).captureDate(LocalDate.now()).fishType("Trucha").weight(2.5f).user(user).build();

        assertThat(capture1, is(capture2));
        assertThat(capture1.hashCode(), is(capture2.hashCode()));
    }

    @Test
    void testToString() {
        User user = User.builder().id(1L).username("juanp").fullName("Juan Pérez").email("juan@example.com").build();
        FishCapture capture = FishCapture.builder()
                .id(100L).captureDate(LocalDate.now()).fishType("Trucha").weight(2.5f).user(user).build();

        String result = capture.toString();

        assertThat(result, containsString("Trucha"));
        assertThat(result, containsString("2.5"));
        assertThat(result, containsString("juanp"));
    }
}
