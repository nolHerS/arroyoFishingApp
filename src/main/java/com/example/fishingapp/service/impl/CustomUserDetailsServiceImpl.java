package com.example.fishingapp.service.impl;

import com.example.fishingapp.repository.AuthUserRepository;
import com.example.fishingapp.service.CustomUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {


    private final AuthUserRepository authUserRepository;

    public CustomUserDetailsServiceImpl(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) {
        return loadUserByUsernameOrEmail(usernameOrEmail);
    }

    @Override
    public UserDetails loadUserByUsernameOrEmail(String identifier) throws UsernameNotFoundException {
        return authUserRepository.findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
