package com.example.fishingapp.service;

import com.example.fishingapp.dto.UserDto;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.exception.UsernameAlreadyExistsException;
import com.example.fishingapp.mapper.UserMapper;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.UserRepository;
import com.example.fishingapp.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void findById_returnsUser_whenExists() {
        User user = User.builder()
                .id(1L)
                .username("ImaHer")
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = UserMapper.mapUser(userService.findById(1L));

        assertThat(result.getUsername(), containsString("ImaHer"));
        assertThat(result, is(user));
        assertThat(result, notNullValue());
    }

    @Test
    void findById_returnsUser_whenNotExists(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {userService.findById(1L);});

        assertThat(exception.getMessage(), containsString("User"));
        assertThat(exception.getMessage(), containsString("id"));
        assertThat(exception.getMessage(), containsString("1"));
    }

    @Test
    void findByUsername_returnsUser_whenExists() {
        // Datos de prueba
        User user = User.builder()
                .id(1L)
                .username("ImaHer")
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();
        String username = "ImaHer";

        // Mock del repositorio
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Ejecutar el método
        UserDto result = userService.findByUsername(username);

        // Comprobaciones con AssertJ
        assertThat(result, notNullValue());
        assertThat(result.username(), containsString(username));
        assertThat(result.id(), is(1L));
    }

    @Test
    void findByUsername_throwsException_whenNotExists() {
        String username = "ImaHer";

        // Mock del repositorio
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Ejecutar y verificar excepción
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.findByUsername(username)
        );

        // Comprobación del mensaje
        assertThat(exception.getMessage(), containsString("User"));
        assertThat(exception.getMessage(), containsString("ImaHer"));
    }

    @Test
    void findAllUsers_returnAllUsers_whenExists(){
        List<User> users = List.of(
                User.builder()
                        .id(1L)
                        .username("ImaHer")
                        .fullName("Imanol Hernandez")
                        .email("imanol@prueba.com")
                        .build(),
                User.builder()
                        .id(2L)
                        .username("AnaLopez")
                        .fullName("Ana Lopez")
                        .email("ana@prueba.com")
                        .build()
        );

        when(userRepository.findAll()).thenReturn(users);

        List<User> existingUsers = userService.getAllUsers().stream().map(UserMapper::mapUser).toList();

        assertThat(existingUsers.get(0).getUsername(), equalTo("ImaHer"));
        assertThat(existingUsers.get(1).getUsername(), equalTo("AnaLopez"));
        assertThat(existingUsers.get(0).getEmail(), is("imanol@prueba.com"));
        assertThat(existingUsers.get(1).getEmail(), is("ana@prueba.com"));

        assertThat(existingUsers, hasItem(hasProperty("username", is("ImaHer"))));

    }

    @Test
    void findAllUsers_throwsException_whenNotExists() {
        // Mock del repositorio
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = userService.getAllUsers().stream().map(UserMapper::mapUser).toList();

        // Comprobación del mensaje
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }

    @Test
    void updateUserDto_updatesUser_whenValid() {
        // Datos de prueba
        UserDto inputDto = new UserDto(1L, "ImaHerUpdated", "Imanol Hernandez Updated", "imanol@prueba.com");
        User existingUser = User.builder()
                .id(1L)
                .username("ImaHer")
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();
        User updatedUser = User.builder()
                .id(1L)
                .username("ImaHerUpdated")
                .fullName("Imanol Hernandez Updated")
                .email("imanol@prueba.com")
                .build();

        // Mock del repositorio
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("ImaHerUpdated")).thenReturn(Optional.empty());
        when(userRepository.save(existingUser)).thenReturn(updatedUser);

        // Ejecutar método
        UserDto result = userService.updateUserDto(inputDto);

        // Comprobaciones
        assertThat(result, notNullValue());
        assertThat(result.id(), is(1L));
        assertThat(result.username(), is("ImaHerUpdated"));
        assertThat(result.fullName(), is("Imanol Hernandez Updated"));

        // Verificar que se llamó a save
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUserDto_throwsException_whenUserNotExists() {
        UserDto inputDto = new UserDto(1L, "ImaHerUpdated", "Imanol Hernandez Updated", "imanol@prueba.com");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.updateUserDto(inputDto)
        );

        assertThat(exception.getMessage(), equalTo("user not found with id : '1'"));


        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserDto_throwsException_whenUsernameAlreadyExists() {
        UserDto inputDto = new UserDto(1L, "ExistingUsername", "Imanol Hernandez Updated", "imanol@prueba.com");
        User existingUser = User.builder()
                .id(1L)
                .username("ImaHer")
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();
        User otherUserWithSameUsername = User.builder()
                .id(2L)
                .username("ExistingUsername")
                .fullName("Ana Lopez")
                .email("ana@prueba.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("ExistingUsername")).thenReturn(Optional.of(otherUserWithSameUsername));

        UsernameAlreadyExistsException exception = assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userService.updateUserDto(inputDto)
        );

        assertThat(exception.getMessage(), equalTo("Username Already Exists For User ExistingUsername"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_deletesUser_whenExists() {
        String username = "ImaHer";
        User existingUser = User.builder()
                .id(1L)
                .username(username)
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();

        // Mock del repositorio
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(existingUser));

        // Ejecutar método
        userService.deleteUser(username);

        // Verificar que se llamó a delete
        verify(userRepository).delete(existingUser);
    }

    @Test
    void deleteUser_throwsException_whenNotExists() {
        String username = "ImaHer";

        // Mock del repositorio: no existe
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Ejecutar y verificar excepción
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.deleteUser(username)
        );

        assertThat(exception.getMessage(), equalTo("user not found with username : 'ImaHer'"));

        // Verificar que no se llamó a delete
        verify(userRepository, never()).delete(any());
    }

    @Test
    void updateUserDto_throwsException_whenEmailAlreadyExists() {
        // DTO con nuevo email ya en uso
        UserDto inputDto = new UserDto(1L, "ImaHer", "Imanol Hernandez", "emailEnUso@prueba.com");

        User existingUser = User.builder()
                .id(1L)
                .username("ImaHer")
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();

        User userWithSameEmail = User.builder()
                .id(2L)
                .username("OtroUsuario")
                .fullName("Otro Usuario")
                .email("emailEnUso@prueba.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("emailEnUso@prueba.com")).thenReturn(Optional.of(userWithSameEmail));

        UsernameAlreadyExistsException exception = assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userService.updateUserDto(inputDto)
        );

        assertThat(exception.getMessage(), equalTo("Email Already Exists: emailEnUso@prueba.com"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserDto_doesNotChange_whenSameUsernameAndEmail() {
        User existingUser = User.builder()
                .id(1L)
                .username("ImaHer")
                .fullName("Imanol Hernandez")
                .email("imanol@prueba.com")
                .build();

        UserDto inputDto = new UserDto(
                1L,
                "ImaHer", // mismo username
                "Imanol Hernandez actualizado", // solo cambia el nombre
                "imanol@prueba.com" // mismo email
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserDto result = userService.updateUserDto(inputDto);

        assertThat(result, notNullValue());
        assertThat(result.id(), is(1L));
        assertThat(result.username(), is("ImaHer"));
        verify(userRepository, times(1)).save(existingUser);
    }

}
