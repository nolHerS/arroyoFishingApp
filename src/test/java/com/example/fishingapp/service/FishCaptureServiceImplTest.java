package com.example.fishingapp.service;

import com.example.fishingapp.dto.FishCaptureDto;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.mapper.UserMapper;
import com.example.fishingapp.model.FishCapture;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.FishCaptureRepository;
import com.example.fishingapp.service.impl.FishCaptureServiceImpl;
import com.example.fishingapp.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FishCaptureServiceImplTest {

    @Mock
    private FishCaptureRepository fishCaptureRepository;

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
                2.5,
                LocalDate.of(2025, 9, 25),
                "Rio Tajo",
                LocalDateTime.now()
        );

        User user = new User(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");

        // Mock del servicio de usuarios
        when(userService.findById(userId)).thenReturn(UserMapper.mapUserDto(user));

        // Mock del repositorio: devuelve el objeto guardado con id
        FishCapture savedCapture = new FishCapture(
                1L,
                user,
                "Trucha",
                2.5,
                LocalDate.of(2025, 9, 25),
                "Rio Tajo",
                LocalDateTime.now()
        );
        when(fishCaptureRepository.save(any(FishCapture.class))).thenReturn(savedCapture);

        // Ejecutar método
        FishCaptureDto result = fishCaptureService.createFishCapture(inputDto, userId);

        // Comprobaciones
        assertThat(result, notNullValue());
        assertThat(result.id(), is(1L));
        assertThat(result.userId(), is(userId));
        assertThat(result.fishType(), is("Trucha"));
        assertThat(result.weight(), is(2.5));
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
                2.5,
                LocalDate.of(2025, 9, 25),
                "Rio Tajo",
                LocalDateTime.now()
        );

        when(userService.findById(userId)).thenThrow(
                new ResourceNotFoundException("User", "id", userId.toString())
        );

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
        User user = new User(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");

        FishCapture capture = new FishCapture(
                captureId,
                user,
                "Trucha",
                2.5,
                LocalDate.of(2025, 9, 25),
                "Rio Tajo",
                LocalDateTime.now()
        );

        when(fishCaptureRepository.findById(captureId)).thenReturn(java.util.Optional.of(capture));

        FishCaptureDto result = fishCaptureService.findById(captureId);

        // Comprobaciones
        assertThat(result, notNullValue());
        assertThat(result.id(), is(captureId));
        assertThat(result.userId(), is(user.getId()));
        assertThat(result.fishType(), is("Trucha"));
        assertThat(result.weight(), is(2.5));
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
        User user = new User(1L, username, "Imanol Hernandez", "imanol@prueba.com");

        // Mock del servicio de usuarios
        when(userService.findByUsername(username)).thenReturn(UserMapper.mapUserDto(user));

        // Datos de capturas
        FishCapture capture1 = new FishCapture(1L, user, "Trucha", 2.5, LocalDate.of(2025, 9, 25), "Rio Tajo", LocalDateTime.now());
        FishCapture capture2 = new FishCapture(2L, user, "Salmón", 3.0, LocalDate.of(2025, 9, 24), "Rio Jerte", LocalDateTime.now());

        when(fishCaptureRepository.findByUser(user)).thenReturn(java.util.List.of(capture1, capture2));

        List<FishCaptureDto> result = fishCaptureService.getAllFishCapturesByUsername(username);

        // Comprobaciones
        assertThat(result, notNullValue());
        assertThat(result.size(), is(2));

        // Primer elemento
        assertThat(result.get(0).id(), is(1L));
        assertThat(result.get(0).userId(), is(user.getId()));
        assertThat(result.get(0).fishType(), is("Trucha"));
        assertThat(result.get(0).weight(), is(2.5));
        assertThat(result.get(0).location(), is("Rio Tajo"));

        // Segundo elemento
        assertThat(result.get(1).id(), is(2L));
        assertThat(result.get(1).userId(), is(user.getId()));
        assertThat(result.get(1).fishType(), is("Salmón"));
        assertThat(result.get(1).weight(), is(3.0));
        assertThat(result.get(1).location(), is("Rio Jerte"));
    }

    @Test
    void getAllFishCapturesByUsername_returnsEmptyList_whenNoCapturesExist() {
        String username = "ImaHer";
        User user = new User(1L, username, "Imanol Hernandez", "imanol@prueba.com");

        // Mock del servicio de usuarios
        when(userService.findByUsername(username)).thenReturn(UserMapper.mapUserDto(user));

        // Mock del repositorio: lista vacía
        when(fishCaptureRepository.findByUser(user)).thenReturn(java.util.List.of());

        List<FishCaptureDto> result = fishCaptureService.getAllFishCapturesByUsername(username);

        // Comprobaciones
        assertThat(result, notNullValue());
        assertThat(result.size(), is(0));
    }

    @Test
    void getAllFishCapture_returnsList_whenCapturesExist() {
        User user = new User(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");

        FishCapture capture1 = new FishCapture(1L, user, "Trucha", 2.5, LocalDate.of(2025, 9, 25), "Rio Tajo", LocalDateTime.now());
        FishCapture capture2 = new FishCapture(2L, user, "Salmón", 3.0, LocalDate.of(2025, 9, 24), "Rio Jerte", LocalDateTime.now());

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
        assertThat(result.get(0).weight(), is(2.5));
        assertThat(result.get(0).location(), is("Rio Tajo"));

        // Segundo elemento
        assertThat(result.get(1).id(), is(2L));
        assertThat(result.get(1).userId(), is(user.getId()));
        assertThat(result.get(1).fishType(), is("Salmón"));
        assertThat(result.get(1).weight(), is(3.0));
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
        User user = new User(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");

        FishCapture existingCapture = new FishCapture(
                captureId,
                user,
                "Trucha",
                2.5,
                LocalDate.of(2025, 9, 25),
                "Rio Tajo",
                LocalDateTime.of(2025, 9, 25, 10, 0)
        );

        FishCaptureDto inputDto = new FishCaptureDto(
                captureId,
                user.getId(),
                "Salmón",
                3.0,
                LocalDate.of(2025, 9, 26),
                "Rio Jerte",
                LocalDateTime.of(2025, 9, 26, 11, 0)
        );

        // Mock del repositorio
        when(fishCaptureRepository.findById(captureId)).thenReturn(java.util.Optional.of(existingCapture));
        when(fishCaptureRepository.save(any(FishCapture.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FishCaptureDto result = fishCaptureService.updateFishCaptureDto(inputDto);

        // Comprobaciones
        assertThat(result, notNullValue());
        assertThat(result.id(), is(captureId));
        assertThat(result.userId(), is(user.getId()));
        assertThat(result.fishType(), is("Salmón"));
        assertThat(result.weight(), is(3.0));
        assertThat(result.location(), is("Rio Jerte"));

        // Verificar que save se llamó
        verify(fishCaptureRepository).save(existingCapture);
    }

    @Test
    void updateFishCaptureDto_throwsException_whenNotExists() {
        Long captureId = 1L;
        FishCaptureDto inputDto = new FishCaptureDto(
                captureId,
                1L,
                "Salmón",
                3.0,
                LocalDate.of(2025, 9, 26),
                "Rio Jerte",
                LocalDateTime.of(2025, 9, 26, 11, 0)
        );

        // Mock del repositorio: no existe la captura
        when(fishCaptureRepository.findById(captureId)).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> fishCaptureService.updateFishCaptureDto(inputDto)
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

        // Ejecutar método
        fishCaptureService.deleteFishCaptureDto(captureId);

        // Verificar que deleteById se llamó una vez con el id correcto
        verify(fishCaptureRepository).deleteById(captureId);
    }

    @Test
    void deleteFishCaptureDto_throwsException_whenRepositoryFails() {
        Long captureId = 1L;

        // Mock del repositorio para lanzar excepción
        doThrow(new RuntimeException("Error al eliminar")).when(fishCaptureRepository).deleteById(captureId);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> fishCaptureService.deleteFishCaptureDto(captureId)
        );

        assertThat(exception.getMessage(), containsString("Error al eliminar"));
    }

}
