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

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsersServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void findById_returnsUser_whenExists() {
        User user = new User(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");
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
    void createUser_savesUser_whenUsernameDoesNotExist() {
        UserDto inputDto = new UserDto(null, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");
        User mappedUser = new User(null, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");
        User savedUser = new User(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");

        // Mock del repositorio
        when(userRepository.findByUsername("ImaHer")).thenReturn(Optional.empty());
        when(userRepository.save(mappedUser)).thenReturn(savedUser);

        // Ejecutar el método
        UserDto result = userService.createUser(inputDto);

        // Verificaciones con AssertJ
        assertThat(result, notNullValue());
        assertThat(result.username(), containsString("ImaHer"));
        assertThat(result.id(), is(1L));

        // Verificar que se llamó al save
        verify(userRepository).save(mappedUser);
    }

    @Test
    void createUser_throwsException_whenUsernameAlreadyExists() {
        UserDto inputDto = new UserDto(null, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");
        User existingUser = new User(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");

        // Mock del repositorio: el username ya existe
        when(userRepository.findByUsername("ImaHer")).thenReturn(Optional.of(existingUser));

        // Ejecutar y verificar excepción
        UsernameAlreadyExistsException exception = assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userService.createUser(inputDto)
        );

        assertThat(exception.getMessage(), containsString("Username Already Exists For User"));
        assertThat(exception.getMessage(), containsString("ImaHer"));

        // Verificar que no se llamó a save
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByUsername_returnsUser_whenExists() {
        // Datos de prueba
        User user = new User(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");
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
                new User(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com"),
                new User(2L, "AnaLopez", "Ana Lopez", "ana@prueba.com")
        );

        when(userRepository.findAll()).thenReturn(users);

        List<User> existingUsers = userService.getAllUsers().stream().map(UserMapper::mapUser).toList();

        assertThat(existingUsers.get(0).getUsername(), equalTo("ImaHer"));
        assertThat(existingUsers.get(1).getUsername(), equalTo("AnaLopez"));
        assertThat(existingUsers.get(0).getEmail(), is("imanol@prueba.com"));
        assertThat(existingUsers.get(1).getEmail(), is("ana@prueba.com"));

        assertThat(existingUsers, hasItem(new User(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com")));

    }

    @Test
    void findAllUsers_throwsException_whenNotExists() {
        // Mock del repositorio
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = userService.getAllUsers().stream().map(UserMapper::mapUser).toList();;

        // Comprobación del mensaje
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }

    @Test
    void updateUserDto_updatesUser_whenValid() {
        // Datos de prueba
        UserDto inputDto = new UserDto(1L, "ImaHerUpdated", "Imanol Hernandez Updated", "imanol@prueba.com");
        User existingUser = new User(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");
        User updatedUser = new User(1L, "ImaHerUpdated", "Imanol Hernandez Updated", "imanol@prueba.com");

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
        User existingUser = new User(1L, "ImaHer", "Imanol Hernandez", "imanol@prueba.com");
        User otherUserWithSameUsername = new User(2L, "ExistingUsername", "Ana Lopez", "ana@prueba.com");

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
        User existingUser = new User(1L, username, "Imanol Hernandez", "imanol@prueba.com");

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

        assertThat(exception.getMessage(), equalTo("user not found with id : 'ImaHer'"));

        // Verificar que no se llamó a delete
        verify(userRepository, never()).delete(any());
    }


}
