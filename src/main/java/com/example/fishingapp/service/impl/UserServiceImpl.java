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
    public UserDto createUser(UserDto userDto) {

        Optional<User> optionalUser = userRepository.findByUsername(userDto.username());

        if(optionalUser.isPresent()){
            throw new UsernameAlreadyExistsException("Username Already Exists For User "+userDto.username());
        }

        User user = UserMapper.mapUser(userDto);

        User savedUser = userRepository.save(user);

        return UserMapper.mapUserDto(savedUser);
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
    public UserDto updateUserDto(UserDto UserDto) {

        User existingUser = userRepository.findById(UserDto.id()).orElseThrow(() -> new ResourceNotFoundException(
                "user","id",UserDto.id().toString()
        ));

        Optional<User> existingUsername = userRepository.findByUsername(UserDto.username());

        if(existingUsername.isPresent()){
            throw new UsernameAlreadyExistsException("Username Already Exists For User "+UserDto.username());
        }

        existingUser.setFullName(UserDto.fullName());
        existingUser.setEmail(UserDto.email());
        existingUser.setUsername(UserDto.username());

        User updateUser = userRepository.save(existingUser);

        return UserMapper.mapUserDto(updateUser);
    }

    @Override
    public void deleteUser(String username) {

        Optional<User> existingUsername = userRepository.findByUsername(username);

        userRepository.delete(existingUsername.orElseThrow(() -> new ResourceNotFoundException(
                "user","id",username
        )));


    }
}
