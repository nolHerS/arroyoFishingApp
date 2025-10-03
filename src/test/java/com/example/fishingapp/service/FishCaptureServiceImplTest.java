package com.example.fishingapp.service;

import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.mapper.UserMapper;
import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.repository.UserRepository;
import com.example.fishingapp.service.impl.FishCaptureServiceImpl;
import com.example.fishingapp.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FishCaptureServiceImplTest {

    @Mock
    private FishCaptureRepository fishCaptureRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FishCaptureServiceImpl fishCaptureService;

    @Mock
    private UserServiceImpl userService;

    @Test
    void createFishCapture_savesCapture_whenUserExists() {
        // Datos de prueba
        Long userId = 1L;
        FishCaptureDto inputDto = new FishCaptureDto(
                null,
                userId,
                "Trucha",
                2.5F,
                LocalDate.of(2025, 9, 25),
                "Rio Tajo",
                LocalDateTime.now()
        );

        User user = User.builder()
                .id(1L)
                .username("ImaHer")
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();


        // Mock del servicio de usuarios
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Mock del repositorio: devuelve el objeto guardado con id
        FishCapture savedCapture = new FishCapture(
                1L,
                LocalDate.of(2025, 9, 25),
                LocalDateTime.now(),
                "Trucha",
                "Rio Tajo",
                2.5F,
                user
        );
        when(fishCaptureRepository.save(any(FishCapture.class))).thenReturn(savedCapture);

        // Ejecutar método
        FishCaptureDto result = fishCaptureService.createFishCapture(inputDto, userId);

        // Comprobaciones
        assertThat(result, notNullValue());
        assertThat(result.id(), is(1L));
        assertThat(result.userId(), is(userId));
        assertThat(result.fishType(), is("Trucha"));
        assertThat(result.weight(), is(2.5F));
        assertThat(result.location(), is("Rio Tajo"));

        // Verificar que save se llamó una vez
        verify(fishCaptureRepository).save(any(FishCapture.class));
    }

    @Test
    void createFishCapture_throwsException_whenUserNotExists() {
        Long userId = 1L;
        FishCaptureDto inputDto = new FishCaptureDto(
                null,
                userId,
                "Trucha",
                2.5F,
                LocalDate.of(2025, 9, 25),
                "Rio Tajo",
                LocalDateTime.now()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> fishCaptureService.createFishCapture(inputDto, userId)
        );

        assertThat(exception.getMessage(), containsString("User"));
        assertThat(exception.getMessage(), containsString("id"));
        assertThat(exception.getMessage(), containsString(userId.toString()));

        verify(fishCaptureRepository, never()).save(any(FishCapture.class));
    }

    @Test
    void findById_returnsFishCapture_whenExists() {
        Long captureId = 1L;
        User user = User.builder()
                .id(1L)
                .username("ImaHer")
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();

        FishCapture capture = new FishCapture(
                1L,
                LocalDate.of(2025, 9, 25),
                LocalDateTime.now(),
                "Trucha",
                "Rio Tajo",
                2.5F,
                user
        );

        when(fishCaptureRepository.findById(captureId)).thenReturn(java.util.Optional.of(capture));

        FishCaptureDto result = fishCaptureService.findById(captureId);

        // Comprobaciones
        assertThat(result, notNullValue());
        assertThat(result.id(), is(captureId));
        assertThat(result.userId(), is(user.getId()));
        assertThat(result.fishType(), is("Trucha"));
        assertThat(result.weight(), is(2.5F));
        assertThat(result.location(), is("Rio Tajo"));
    }

    @Test
    void findById_throwsException_whenNotExists() {
        Long captureId = 1L;

        when(fishCaptureRepository.findById(captureId)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> fishCaptureService.findById(captureId)
        );

        assertThat(exception.getMessage(), containsString("FishCapture"));
        assertThat(exception.getMessage(), containsString("id"));
        assertThat(exception.getMessage(), containsString(captureId.toString()));
    }

    @Test
    void getAllFishCapturesByUsername_returnsList_whenCapturesExist() {
        String username = "ImaHer";
        User user = User.builder()
                .id(1L)
                .username(username)
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();

        // Mock del servicio de usuarios
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Datos de capturas
        FishCapture capture1 = new FishCapture();
        capture1.setId(1L);
        capture1.setCaptureDate(LocalDate.of(2025, 9, 25));
        capture1.setCreatedAt(LocalDateTime.now());
        capture1.setFishType("Trucha");
        capture1.setLocation("Rio Tajo");
        capture1.setWeight(2.5f);
        capture1.setUser(user);

        FishCapture capture2 = new FishCapture();
        capture2.setId(2L);
        capture2.setCaptureDate(LocalDate.of(2025, 9, 24));
        capture2.setCreatedAt(LocalDateTime.now());
        capture2.setFishType("Salmón");
        capture2.setLocation("Rio Jerte");
        capture2.setWeight(3.0f);
        capture2.setUser(user);


        when(fishCaptureRepository.findByUser(user)).thenReturn(java.util.List.of(capture1, capture2));

        List<FishCaptureDto> result = fishCaptureService.getAllFishCapturesByUsername(username);

        // Comprobaciones
        assertThat(result, notNullValue());
        assertThat(result.size(), is(2));

        // Primer elemento
        assertThat(result.get(0).id(), is(1L));
        assertThat(result.get(0).userId(), is(user.getId()));
        assertThat(result.get(0).fishType(), is("Trucha"));
        assertThat(result.get(0).weight(), is(2.5F));
        assertThat(result.get(0).location(), is("Rio Tajo"));

        // Segundo elemento
        assertThat(result.get(1).id(), is(2L));
        assertThat(result.get(1).userId(), is(user.getId()));
        assertThat(result.get(1).fishType(), is("Salmón"));
        assertThat(result.get(1).weight(), is(3.0F));
        assertThat(result.get(1).location(), is("Rio Jerte"));
    }

    @Test
    void getAllFishCapturesByUsername_returnsEmptyList_whenNoCapturesExist() {
        String username = "ImaHer";
        User user = User.builder()
                .id(1L)
                .username(username)
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();


        // Mock del servicio de usuarios
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Mock del repositorio: lista vacía
        when(fishCaptureRepository.findByUser(user)).thenReturn(java.util.List.of());

        List<FishCaptureDto> result = fishCaptureService.getAllFishCapturesByUsername(username);

        // Comprobaciones
        assertThat(result, notNullValue());
        assertThat(result.size(), is(0));
    }

    @Test
    void getAllFishCapture_returnsList_whenCapturesExist() {
        User user = User.builder()
                .id(1L)
                .username("ImaHer")
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();

        FishCapture capture1 = new FishCapture();
        capture1.setId(1L);
        capture1.setCaptureDate(LocalDate.of(2025, 9, 25));
        capture1.setCreatedAt(LocalDateTime.now());
        capture1.setFishType("Trucha");
        capture1.setLocation("Rio Tajo");
        capture1.setWeight(2.5f);
        capture1.setUser(user);

        FishCapture capture2 = new FishCapture();
        capture2.setId(2L);
        capture2.setCaptureDate(LocalDate.of(2025, 9, 24));
        capture2.setCreatedAt(LocalDateTime.now());
        capture2.setFishType("Salmón");
        capture2.setLocation("Rio Jerte");
        capture2.setWeight(3.0f);
        capture2.setUser(user);


        // Mock del repositorio
        when(fishCaptureRepository.findAll()).thenReturn(java.util.List.of(capture1, capture2));

        List<FishCaptureDto> result = fishCaptureService.getAllFishCapture();

        // Comprobaciones
        assertThat(result, notNullValue());
        assertThat(result.size(), is(2));

        // Primer elemento
        assertThat(result.get(0).id(), is(1L));
        assertThat(result.get(0).userId(), is(user.getId()));
        assertThat(result.get(0).fishType(), is("Trucha"));
        assertThat(result.get(0).weight(), is(2.5F));
        assertThat(result.get(0).location(), is("Rio Tajo"));

        // Segundo elemento
        assertThat(result.get(1).id(), is(2L));
        assertThat(result.get(1).userId(), is(user.getId()));
        assertThat(result.get(1).fishType(), is("Salmón"));
        assertThat(result.get(1).weight(), is(3.0F));
        assertThat(result.get(1).location(), is("Rio Jerte"));
    }

    @Test
    void getAllFishCapture_returnsEmptyList_whenNoCapturesExist() {
        // Mock del repositorio: lista vacía
        when(fishCaptureRepository.findAll()).thenReturn(java.util.List.of());

        List<FishCaptureDto> result = fishCaptureService.getAllFishCapture();

        // Comprobaciones
        assertThat(result, notNullValue());
        assertThat(result.size(), is(0));
    }

    @Test
    void updateFishCaptureDto_updatesCapture_whenExists() {
        Long captureId = 1L;
        Long userId = 1L;
        User user = User.builder()
                .id(1L)
                .username("ImaHer")
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();

        FishCapture existingCapture = new FishCapture();
        existingCapture.setId(captureId);
        existingCapture.setCaptureDate(LocalDate.of(2025, 9, 25));
        existingCapture.setCreatedAt(LocalDateTime.of(2025, 9, 25, 10, 0));
        existingCapture.setFishType("Trucha");
        existingCapture.setLocation("Rio Tajo");
        existingCapture.setWeight(2.5f);
        existingCapture.setUser(user);

        FishCaptureDto inputDto = new FishCaptureDto(
                captureId,
                user.getId(),
                "Salmón",
                3.0F,
                LocalDate.of(2025, 9, 26),
                "Rio Jerte",
                LocalDateTime.of(2025, 9, 26, 11, 0)
        );

        // Mock del repositorio
        when(fishCaptureRepository.findById(captureId)).thenReturn(java.util.Optional.of(existingCapture));
        when(fishCaptureRepository.save(any(FishCapture.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FishCaptureDto result = fishCaptureService.updateFishCaptureDto(inputDto,userId);

        // Comprobaciones
        assertThat(result, notNullValue());
        assertThat(result.id(), is(captureId));
        assertThat(result.userId(), is(user.getId()));
        assertThat(result.fishType(), is("Salmón"));
        assertThat(result.weight(), is(3.0F));
        assertThat(result.location(), is("Rio Jerte"));

        // Verificar que save se llamó
        verify(fishCaptureRepository).save(existingCapture);
    }

    @Test
    void updateFishCaptureDto_throwsException_whenNotExists() {
        Long captureId = 1L;
        Long userId = 1L;
        FishCaptureDto inputDto = new FishCaptureDto(
                captureId,
                1L,
                "Salmón",
                3.0F,
                LocalDate.of(2025, 9, 26),
                "Rio Jerte",
                LocalDateTime.of(2025, 9, 26, 11, 0)
        );

        // Mock del repositorio: no existe la captura
        when(fishCaptureRepository.findById(captureId)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> fishCaptureService.updateFishCaptureDto(inputDto,userId)
        );

        assertThat(exception.getMessage(), containsString("FishCapture"));
        assertThat(exception.getMessage(), containsString("id"));
        assertThat(exception.getMessage(), containsString(captureId.toString()));

        // Verificar que save **no se llamó**
        verify(fishCaptureRepository, never()).save(any(FishCapture.class));
    }

    @Test
    void deleteFishCaptureDto_callsRepository_whenExists() {
        Long captureId = 1L;
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .username("ImaHer")
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();

        FishCapture existingCapture = new FishCapture();
        existingCapture.setId(captureId);
        existingCapture.setUser(user);

        // Mock: la captura existe
        when(fishCaptureRepository.findById(captureId)).thenReturn(Optional.of(existingCapture));

        // Ejecutar método
        fishCaptureService.deleteFishCaptureDto(captureId, userId);

        // Verificar que deleteById se llamó una vez con el id correcto
        verify(fishCaptureRepository).deleteById(captureId);
    }

    @Test
    void deleteFishCaptureDto_throwsException_whenRepositoryFails() {
        Long captureId = 1L;
        Long userId = 1L;

        User user = User.builder()
                .id(userId)
                .username("ImaHer")
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();

        FishCapture existingCapture = new FishCapture();
        existingCapture.setId(captureId);
        existingCapture.setUser(user);

        // Mock: la captura existe
        when(fishCaptureRepository.findById(captureId)).thenReturn(Optional.of(existingCapture));

        // Mock del repositorio para lanzar excepción al eliminar
        doThrow(new RuntimeException("Error al eliminar")).when(fishCaptureRepository).deleteById(captureId);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> fishCaptureService.deleteFishCaptureDto(captureId, userId)
        );

        assertThat(exception.getMessage(), containsString("Error al eliminar"));
    }

}
