package com.example.fishingapp.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomUserDetailsService extends UserDetailsService {

    UserDetails loadUserByUsernameOrEmail(String identifier);
}