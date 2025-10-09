package com.example.fishingapp.service.impl;

import com.example.fishingapp.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String usuario) throws UsernameNotFoundException {
        return authUserRepository.findByUsername(usuario)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con usuario: " + usuario
                ));
    }
}