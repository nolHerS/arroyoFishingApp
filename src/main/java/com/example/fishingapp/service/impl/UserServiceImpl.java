package com.example.fishingapp.service.impl;

import com.example.fishingapp.dto.UserDto;
import com.example.fishingapp.exception.ResourceNotFoundException;
import com.example.fishingapp.exception.UsernameAlreadyExistsException;
import com.example.fishingapp.mapper.UserMapper;
import com.example.fishingapp.model.User;
import com.example.fishingapp.repository.UserRepository;
import com.example.fishingapp.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDto findByUsername(String userName) {

        return userRepository.findByUsername(userName).map(UserMapper::mapUserDto).orElseThrow(
                () -> new ResourceNotFoundException("User", "username", userName)
        );
    }

    @Override
    public UserDto findById(Long id) {

        return userRepository.findById(id).map(UserMapper::mapUserDto).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", id.toString())
        );
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(UserMapper::mapUserDto).toList();
    }

    @Override
    public UserDto updateUserDto(UserDto userDto) {

        User existingUser = userRepository.findById(userDto.id()).orElseThrow(() -> new ResourceNotFoundException(
                "user", "id", userDto.id().toString()
        ));

        // Verificar si el nuevo username ya existe (y no es el mismo usuario)
        if (!existingUser.getUsername().equals(userDto.username())) {
            Optional<User> existingUsername = userRepository.findByUsername(userDto.username());
            if (existingUsername.isPresent()) {
                throw new UsernameAlreadyExistsException("Username Already Exists For User " + userDto.username());
            }
        }

        // Verificar si el nuevo email ya existe (y no es el mismo usuario)
        if (!existingUser.getEmail().equals(userDto.email())) {
            Optional<User> existingEmail = userRepository.findByEmail(userDto.email());
            if (existingEmail.isPresent()) {
                throw new UsernameAlreadyExistsException("Email Already Exists: " + userDto.email());
            }
        }

        existingUser.setFullName(userDto.fullName());
        existingUser.setEmail(userDto.email());
        existingUser.setUsername(userDto.username());

        User updatedUser = userRepository.save(existingUser);
        return UserMapper.mapUserDto(updatedUser);
    }

    @Override
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("user", "username", username));

        userRepository.delete(user); // Elimina User y AuthUser automáticamente
    }
}
