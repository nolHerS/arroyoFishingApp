package com.example.fishingapp.config;

import com.example.fishingapp.repository.AuthUserRepository;
import com.example.fishingapp.repository.CaptureImageRepository;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.repository.RefreshTokenRepository;
import com.example.fishingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected FishCaptureRepository fishCaptureRepository;

    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;

    @Autowired
    protected AuthUserRepository authUserRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected CaptureImageRepository captureImageRepository;

    @BeforeEach
    void cleanDatabase() {
        // Limpiar TODO en orden correcto: de hojas a raíz
        captureImageRepository.deleteAll();       // Primero las imágenes
        fishCaptureRepository.deleteAll();        // Luego las capturas
        refreshTokenRepository.deleteAll();       // Tokens de refresco
        authUserRepository.deleteAll();           // Usuarios de autenticación
        userRepository.deleteAll();               // Finalmente los usuarios
    }
}