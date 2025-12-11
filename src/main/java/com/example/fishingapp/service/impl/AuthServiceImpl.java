package com.example.fishingapp.service.impl;

import com.example.fishingapp.dto.auth.AuthResponse;
import com.example.fishingapp.dto.auth.LoginRequest;
import com.example.fishingapp.dto.auth.RefreshTokenRequest;
import com.example.fishingapp.dto.auth.RegisterRequest;
import com.example.fishingapp.exception.EmailAlreadyExistsException;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.exception.UsernameAlreadyExistsException;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.AuthUserRepository;
import com.example.fishingapp.repository.RefreshTokenRepository;
import com.example.fishingapp.repository.UserRepository;
import com.example.fishingapp.security.AuthUser;
import com.example.fishingapp.security.RefreshToken;
import com.example.fishingapp.security.Role;
import com.example.fishingapp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    public static final String USER_ID = "userId";
    public static final String ROLE = "role";
    private final AuthUserRepository authUserRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Verificar si el email ya existe
        if (authUserRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("El usuario ya está registrado");
        }

        // Verificar si el username ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("El email ya está en uso");
        }

        // Crear el User (tabla users)
        User user = User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .build();

        User savedUser = userRepository.save(user);

        // Crear el AuthUser (tabla auth_users)
        AuthUser authUser = AuthUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .user(savedUser)
                .build();

        AuthUser savedAuthUser = authUserRepository.save(authUser);

        // Generar tokens con claims personalizados
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(USER_ID, savedUser.getId());
        extraClaims.put(ROLE, savedAuthUser.getRole().name());

        String accessToken = jwtService.generateToken(extraClaims, savedAuthUser);
        String refreshToken = createRefreshToken(savedAuthUser);

        return buildAuthResponse(savedAuthUser, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getIdentifier(),
                            request.getPassword()
                    )
            );

            // Obtener el usuario autenticado
            AuthUser authUser = (AuthUser) authentication.getPrincipal();

            // Actualizar último login
            authUser.setLastLoginAt(LocalDateTime.now());
            authUserRepository.save(authUser);

            // Generar tokens con claims personalizados
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put(USER_ID, authUser.getUser().getId());
            extraClaims.put(ROLE, authUser.getRole().name());

            String accessToken = jwtService.generateToken(extraClaims, authUser);
            String refreshToken = createRefreshToken(authUser);

            return buildAuthResponse(authUser, accessToken, refreshToken);

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Email o contraseña incorrectos");
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        // Buscar el refresh token en la base de datos
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new ResourceNotFoundException("AuthService","Refresh token: "+ request.getRefreshToken(),"Refresh token no encontrado"));

        // Verificar si está revocado
        if (refreshToken.getRevoked()) {
            throw new RuntimeException("El refresh token ha sido revocado");
        }

        // Verificar si está expirado
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("El refresh token ha expirado");
        }

        // Obtener el usuario asociado
        AuthUser authUser = refreshToken.getAuthUser();

        // Generar nuevo access token con claims
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(USER_ID, authUser.getUser().getId());
        extraClaims.put(ROLE, authUser.getRole().name());

        String newAccessToken = jwtService.generateToken(extraClaims, authUser);

        return buildAuthResponse(authUser, newAccessToken, requestRefreshToken);
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("AuthService","Token: "+ token,"Refresh token no encontrado"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(String username) {
        AuthUser authUser = authUserRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("AuthService","Usuario: "+ username,"Usuario no encontrado"));

        refreshTokenRepository.deleteAllByAuthUserId(authUser.getId());
    }

    /**
     * Crea y guarda un refresh token en la base de datos
     */
    private String createRefreshToken(AuthUser authUser) {
        // Revocar tokens anteriores del usuario (opcional, por seguridad)
        refreshTokenRepository.deleteAllByAuthUserId(authUser.getId());

        // Crear nuevo refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .authUser(authUser)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .revoked(false)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        return savedToken.getToken();
    }

    /**
     * Construye la respuesta de autenticación
     */
    private AuthResponse buildAuthResponse(AuthUser authUser, String accessToken, String refreshToken) {
        User user = authUser.getUser();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(authUser.getRole().name())
                        .createdAt(authUser.getCreatedAt())
                        .build())
                .build();
    }
}