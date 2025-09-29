package com.example.fishingapp.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class UserTest {

    @Test
    void testBuilderAndGetters() {
        User user = User.builder()
                .id(1L)
                .username("juanp")
                .fullName("Juan Pérez")
                .email("juan@example.com")
                .build();

        assertThat(user.getId(), is(1L));
        assertThat(user.getUsername(), is("juanp"));
        assertThat(user.getFullName(), is("Juan Pérez"));
        assertThat(user.getEmail(), is("juan@example.com"));
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = User.builder().id(1L).username("juanp").fullName("Juan Pérez").email("juan@example.com").build();
        User user2 = User.builder().id(1L).username("juanp").fullName("Juan Pérez").email("juan@example.com").build();

        assertThat(user1, is(user2));
        assertThat(user1.hashCode(), is(user2.hashCode()));
    }

    @Test
    void testToString() {
        User user = User.builder().id(1L).username("juanp").fullName("Juan Pérez").email("juan@example.com").build();
        String result = user.toString();

        assertThat(result, containsString("juanp"));
        assertThat(result, containsString("Juan Pérez"));
        assertThat(result, containsString("juan@example.com"));
    }
}
