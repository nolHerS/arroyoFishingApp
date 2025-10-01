package com.example.fishingapp.config;

import org.springframework.context.annotation.Bean;

public class TestBeansConfig {
    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}
